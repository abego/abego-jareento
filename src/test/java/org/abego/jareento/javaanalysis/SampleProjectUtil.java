package org.abego.jareento.javaanalysis;

import org.abego.commons.io.FileUtil;
import org.abego.jareento.base.JareentoException;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.abego.commons.util.ServiceLoaderUtil.loadService;

public class SampleProjectUtil {

    public static final String SAMPLE_PROJECTS_ROOT_PATH = "/org/abego/jareento/sample-projects/";

    public static String sampleProjectDirectoryResourcePath(String projectName) {
        return SAMPLE_PROJECTS_ROOT_PATH + projectName+"/";
    }

    /**
     * Copies the sample project with the given {@code projectName} from the
     * resources to the {@code directory}, builds and packages the project and
     * returns a {@link JavaAnalysisProject} for that sample project.
     * <p>
     * A sample project p has its files stored in a resource directory named
     * {@code "p"} under SAMPLE_PROJECTS_ROOT_PATH, with its Java source file
     * inside a {@code "src"} directory.
     * <p>
     * The sample project must be self-contained, i.e. not have any dependencies.
     */
    public static JavaAnalysisProject setupSampleProject(
            String projectName, File directory) {
        JavaAnalysisAPI javaAnalysisAPI = loadService(JavaAnalysisAPI.class);

        File srcDir = new File(directory, "src");
        File srcMainDir = new File(srcDir, "main");
        File srcMainJavaDir = new File(srcMainDir, "java");
        File targetDir = new File(directory, "target");
        File classesDir = new File(targetDir, "classes");
        File jarFile = new File(targetDir, projectName + ".jar");

        FileUtil.copyResourcesInLocationDeep(SampleProjectUtil.class,
                sampleProjectDirectoryResourcePath(projectName) + "src", srcMainJavaDir);

        List<File> sourceFiles = allJavaFilesInDirectoryAndDeeper(srcMainJavaDir);
        var diagnostics = compileJavaFiles(sourceFiles, classesDir);

        if (!diagnostics.isEmpty()) {
            StringBuilder message = new StringBuilder();
            message.append("Error when compiling sample project '")
                    .append(projectName)
                    .append("':\n");
            for (Diagnostic<? extends JavaFileObject> d : diagnostics) {
                message.append("Line: %d, %s in %s%n".formatted(
                        d.getLineNumber(),
                        d.getMessage(null),
                        d.getSource().getName()));
            }
            throw new JareentoException(message.toString());
        }

        writeJarFile(jarFile, classesDir);

        JavaAnalysisProjectConfiguration projectConfiguration =
                javaAnalysisAPI.newJavaAnalysisProjectConfiguration(
                        projectName,
                        directory,
                        new File[]{srcMainJavaDir},
                        new File[]{jarFile},
                        new File[]{});

        File storageDir = new File(directory, "storage");
        JavaAnalysisProjectStorage storage =
                javaAnalysisAPI.getJavaAnalysisProjectStorage(storageDir.toURI());
        return storage.createAndLoadJavaAnalysisProject(
                projectConfiguration, s -> {});
    }

    private static List<Diagnostic<? extends JavaFileObject>> compileJavaFiles(
            List<File> sourceList, File classesDir) {
        DiagnosticCollector<JavaFileObject> dc = new DiagnosticCollector<>();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        try (StandardJavaFileManager mgr =
                     compiler.getStandardFileManager(dc, null, null)) {
            Iterable<? extends JavaFileObject> sources =
                    mgr.getJavaFileObjectsFromFiles(sourceList);
            Iterable<String> options = Arrays.asList(
                    "-d", classesDir.getAbsolutePath());
            JavaCompiler.CompilationTask task =
                    compiler.getTask(null, mgr, dc, options, null, sources);
            task.call();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return dc.getDiagnostics();
    }

    private static void writeJarFile(File jarFile, File contentRoot) {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes()
                .put(Attributes.Name.MANIFEST_VERSION, "1.0");

        try (JarOutputStream jarStream = new JarOutputStream(
                new FileOutputStream(jarFile), manifest)) {

            FileUtil.withFilesInDirectoryAndDeeperDo(contentRoot, f -> {
                String entryName =
                        contentRoot.toPath().relativize(f.toPath()).toString()
                                .replaceAll("\\\\", "/");
                addNewJarEntry(jarStream, entryName, f);
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void addNewJarEntry(JarOutputStream jarOutputStream, String entryName, File file) {
        try {
            JarEntry entry = new JarEntry(entryName);
            entry.setTime(file.lastModified());
            jarOutputStream.putNextEntry(entry);

            try (BufferedInputStream in =
                         new BufferedInputStream(new FileInputStream(file))) {
                byte[] buffer = new byte[1024];
                while (true) {
                    int count = in.read(buffer);
                    if (count == -1) {
                        break;
                    }
                    jarOutputStream.write(buffer, 0, count);
                }
                jarOutputStream.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<File> allJavaFilesInDirectoryAndDeeper(File srcDir) {
        return allFilesInDirectoryAndDeeper(srcDir, f -> f.getName()
                .endsWith(".java"));
    }

    private static List<File> allFilesInDirectoryAndDeeper(File srcDir, Predicate<File> selector) {
        List<File> sourceList = new ArrayList<>();
        FileUtil.withFilesInDirectoryAndDeeperDo(srcDir, selector, sourceList::add);
        return sourceList;
    }
}
