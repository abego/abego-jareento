package org.abego.jareento.javaanalysis.internal;

import org.abego.commons.io.FileUtil;
import org.abego.commons.lang.ArrayUtil;
import org.abego.commons.lang.StringUtil;
import org.abego.commons.util.CollectionUtil;
import org.abego.commons.util.ListUtil;
import org.abego.jareento.base.JareentoException;
import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaAnalysisProjectConfiguration;
import org.abego.jareento.javaanalysis.JavaAnalysisProjectStorage;
import org.abego.jareento.javaanalysis.internal.input.javap.InputFromJavap;
import org.abego.jareento.shared.commons.progress.ProgressUtil;
import org.abego.stringgraph.core.StringGraphs;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;
import static org.abego.commons.io.FileUtil.writeFileIfOutdated;
import static org.abego.commons.lang.ArrayUtil.concatenate;
import static org.abego.commons.lang.ProcessUtil.runCommand;
import static org.abego.commons.lang.StringUtil.indent;
import static org.abego.commons.util.LoggerUtil.logStringsAsWarnings;

class JavaAnalysisProjectStorageUsingStringGraph implements JavaAnalysisProjectStorage {
    private static final Logger LOGGER = getLogger(JavaAnalysisProjectStorageUsingStringGraph.class.getName());
    private static final int DISASSEMBLE_BATCH_SIZE = 100;
    private final JavaAnalysisAPIImpl javaAnalysisAPI;
    private final URI storageURI;

    public JavaAnalysisProjectStorageUsingStringGraph(JavaAnalysisAPIImpl javaAnalysisAPI, URI storageURI) {
        this.javaAnalysisAPI = javaAnalysisAPI;
        this.storageURI = storageURI;
    }

    @Override
    public JavaAnalysisProject loadJavaAnalysisProject(URI uri, Consumer<String> progress) {
        long start = System.currentTimeMillis();
        progress.accept(String.format("Loading java analysis project... (from %s)", new File(uri).getAbsolutePath()));

        JavaAnalysisProject project = readJavaAnalysisProject(uri);

        long afterLoad = System.currentTimeMillis();
        progress.accept(String.format("Loaded. [%d ms]", afterLoad - start));
        return project;
    }

    @Override
    public URI createJavaAnalysisProject(
            JavaAnalysisProjectConfiguration projectConfiguration, Consumer<String> progress) {
        return createJavaAnalysisProject(
                new File(storageURI), projectConfiguration, progress);
    }

    public URI createJavaAnalysisProject(
            File storageDirectory,
            JavaAnalysisProjectConfiguration projectConfiguration,
            Consumer<String> progress) {

        File projectDirectory =
                new File(storageDirectory, projectConfiguration.getName());
        FileUtil.ensureDirectoryExists(projectDirectory);

        File disassemblyFile = new File(projectDirectory, "disassembly.txt");
        File projectFile = new File(projectDirectory, "main-project.jas");
        URI projectURI = projectFile.toURI();
        File[] sourceRoots = projectConfiguration.getSourceRoots();

        // Write disassembly (if missing or out-dated)
        File[] jarFiles = projectConfiguration.getProjectJars();
        if (jarFiles.length == 0) {
            @Nullable File dir = projectConfiguration.getMavenProjectDirectory();
            String message = dir != null 
                    ? "No jar files found for project %s".formatted(dir.getAbsolutePath())
                    : "No project jar files specified";
            throw new JareentoException(message);
        }
        writeFileIfOutdated(disassemblyFile, jarFiles,
                f -> writeDisassemblyFile(f, jarFiles, progress));

        // Create a (new) JavaAnalysisProject and save it (if missing or out-dated)
        File[] dependencies = projectConfiguration.getDependencies();
        File[] projectJarsAndDependencies = ArrayUtil.concatenate(jarFiles, dependencies);
        writeFileIfOutdated(projectFile, concatenate(disassemblyFile, projectJarsAndDependencies),
                f -> newJavaAnalysisProjectFromDisassemblyFile(
                        projectURI, disassemblyFile, sourceRoots, projectJarsAndDependencies, progress),
                () -> progress.accept("Found up-to-date java analysis project, using it: " + projectURI));

        return projectURI;
    }

    private void newJavaAnalysisProjectFromDisassemblyFile(
            URI projectURI, 
            File disassemblyFile, 
            File[] sourceRoots, 
            File[] dependencies, 
            Consumer<String> progress) {
        
        progress.accept("Creating java analysis project from disassembled classes...");
        Consumer<String> innerProgress = indent(progress);
        Consumer<String> innerInnerProgress = indent(innerProgress);

        ProgressUtil.withProgressAndDurationDo(
                () -> String.format("Adding disassembled classes... (from %s)", disassemblyFile.getAbsolutePath()),
                durationMillis -> String.format("Classes added into project. [%d ms]", durationMillis),
                innerProgress,
                () -> newJavaAnalysisProjectFromInput(
                        projectURI,
                        InputFromJavap.newInputFromJavap(
                                disassemblyFile, dependencies, innerInnerProgress, javaAnalysisAPI),
                        sourceRoots,
                        dependencies,
                        logStringsAsWarnings(LOGGER)));
        progress.accept(String.format("Created java analysis project '%s'.", projectURI));
    }

    private JavaAnalysisProject newJavaAnalysisProjectFromInput(
            URI uri,
            JavaAnalysisProjectInput input,
            File[] sourceRoots,
            File[] dependencies,
            Consumer<String> problemConsumer) {
        JavaAnalysisProjectStateBuilder builder =
                JavaAnalysisInternalFactories.newJavaAnalysisProjectBuilder(uri);
        builder.setSourceRoots(sourceRoots);
        builder.setDependencies(dependencies);
        input.feed(builder, problemConsumer);
        JavaAnalysisProjectStateWithSave state = builder.build();
        state.save();
        return JavaAnalysisProjectImpl.newJavaAnalysisProject(state);
    }


    private void writeDisassemblyFile(File disassemblyFile, File[] jarFiles, Consumer<String> progress) {
        ProgressUtil.withProgressAndDurationDo(
                () -> "Disassembling classes ...",
                durationMillis -> String.format("Disassembled classes to '%s' [%d ms]", disassemblyFile.getAbsolutePath(), durationMillis),
                progress,
                () -> {
                    PrintStream printStream = new PrintStream(disassemblyFile);
                    for (File f : jarFiles) {
                        List<String> classes = classesInJarFile(f);
                        int n = 0;
                        progress.accept(String.format("\tDisassembling classes from '%s'...", f.getAbsolutePath()));
                        // We process the classes in batches,
                        // a) to avoid running into the "too many arguments" problem of Process.exec
                        // b) support some "progress feedback".
                        for (List<String> someClasses : ListUtil.splitInBatches(classes, DISASSEMBLE_BATCH_SIZE)) {
                            writeJavaPOutput(printStream, f, someClasses);
                            n += someClasses.size();
                            progress.accept(String.format("\t\t(%d of %d done)", n, classes.size()));
                        }
                    }
                    printStream.close();
                });
    }

    private static void writeJavaPOutput(PrintStream printStream, File f, List<String> classes) {
        //javap -c -p -sysinfo -classpath $JAR\
        List<String> cmdParts = new ArrayList<>();
        cmdParts.add("javap");
        cmdParts.add("-c"); // Print disassembled code
        cmdParts.add("-p"); // Show all classes and members.
        cmdParts.add("-s"); // Prints internal type signatures.
        cmdParts.add("-sysinfo"); // Show system information (path, size, date, MD5 hash) of the class being processed.
        cmdParts.add("-verbose"); // Prints stack size, number of locals and arguments for methods.
        cmdParts.add("-classpath"); // path the javap command uses to look up classes.
        cmdParts.add(f.getAbsolutePath());
        CollectionUtil.addAll(cmdParts, classes);

        runCommand(cmdParts.toArray(new String[0]), printStream::println);
    }

    private static JavaAnalysisProject readJavaAnalysisProject(URI uri) {
        File javaAnalysisProjectFile = new File(uri);
        if (!javaAnalysisProjectFile.isFile()) {
            throw new JareentoException(
                    String.format("Cannot find JavaAnalysis project at %s", uri));
        }
        JavaAnalysisProjectStateUsingStringGraph state =
                new JavaAnalysisProjectStateUsingStringGraph(
                        uri,
                        StringGraphs.getInstance().readStringGraph(uri));

        return JavaAnalysisProjectImpl.newJavaAnalysisProject(state);
    }

    private static List<String> classesInJarFile(File jarFile) {

        List<String> classes = new ArrayList<>();
        //jar -tf $JAR
        runCommand(new String[]{"jar", "-tf", jarFile.getAbsolutePath()},
                s -> {
                    if (s.endsWith(".class")) {
                        classes.add(StringUtil.prefix(s, -6));
                    }
                });
        return classes;
    }

}
