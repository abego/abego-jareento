package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaAnalysisAPI;
import org.abego.jareento.javaanalysis.JavaAnalysisProjectConfiguration;
import org.abego.jareento.javaanalysis.JavaAnalysisProjectStorage;
import org.abego.jareento.javaanalysis.Problem;
import org.abego.jareento.javaanalysis.ProblemChecker;
import org.abego.jareento.javaanalysis.ProblemCheckers;
import org.abego.jareento.javaanalysis.ProblemReporter;
import org.abego.jareento.javaanalysis.ProblemReporters;
import org.abego.jareento.javaanalysis.ProblemType;
import org.abego.jareento.javaanalysis.Problems;
import org.abego.jareento.shared.commons.maven.MavenUtil;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.abego.jareento.javaanalysis.internal.JavaAnalysisFilesImpl.newJavaAnalysisFilesImpl;
import static org.abego.jareento.javaanalysis.internal.ProblemsImpl.newProblemsImpl;

public class JavaAnalysisAPIImpl implements JavaAnalysisAPI {

    @Override
    public JavaAnalysisProjectStorage getJavaAnalysisProjectStorage(URI storageURI) {
        return new JavaAnalysisProjectStorageUsingStringGraph(this, storageURI);
    }

    @Override
    public JavaAnalysisProjectConfiguration newJavaAnalysisProjectConfiguration(
            String name, File mavenProjectDirectory, File[] sourceRoots, File @Nullable [] projectJars, File @Nullable [] dependencies) {
        return JavaAnalysisProjectConfigurationImpl.newJavaAnalysisProjectConfiguration(name, mavenProjectDirectory, sourceRoots, projectJars, dependencies);
    }

    @Override
    public JavaAnalysisFiles newJavaAnalysisFiles(File... files) {
        return parseFilesForJavaAnalysisFiles(null, files);
    }

    @Override
    public JavaAnalysisFiles newJavaAnalysisFiles(File[] sourcesToAnalyse, File... files) {
        if (sourcesToAnalyse.length == 0) {
            throw new IllegalArgumentException(
                    "sourcesToAnalyse must not be empty");
        }
        return parseFilesForJavaAnalysisFiles(sourcesToAnalyse, files);
    }

    private JavaAnalysisFiles parseFilesForJavaAnalysisFiles(
            File @Nullable [] sourcesToAnalyse, File[] files) {
        Set<File> sourceRoots = new HashSet<>();
        Set<File> dependencies = new HashSet<>();
        Set<File> mavenProjectDirectories = new HashSet<>();

        if (sourcesToAnalyse != null) {
            Collections.addAll(sourceRoots, sourcesToAnalyse);
        }
        for (File f : files) {
            if (f.getName().equals("pom.xml")) {
                File mavenProjectDirectory = f.getParentFile();
                mavenProjectDirectories.add(mavenProjectDirectory);
                Collections.addAll(dependencies,
                        MavenUtil.classpathJarsFromMavenProject(mavenProjectDirectory));
                dependencies.add(MavenUtil.sourceDirectoryFromMavenProject(
                        mavenProjectDirectory));

            } else if (f.isDirectory()) {
                // when no sourcesToAnalyse are explicitly defined the
                // directories in `files` define the sourceRoots.
                // Otherwise, they are additional dependencies, used
                // to resolve types.
                if (sourcesToAnalyse == null) {
                    sourceRoots.add(f);
                } else {
                    dependencies.add(f);
                }

            } else if (f.getName().endsWith(".jar")) {
                dependencies.add(f);

            } else {
                throw new IllegalArgumentException(
                        "Unexpected file, expected directory, jar-file, or 'pom.xml'. Got %s"
                                .formatted(f.getAbsolutePath()));
            }
        }

        // When no source roots are defined yet, but we have maven 
        // project directories use the source directories as sourceRoots
        if (sourceRoots.isEmpty() && !mavenProjectDirectories.isEmpty()) {
            for (File f : mavenProjectDirectories) {
                sourceRoots.add(MavenUtil.sourceDirectoryFromMavenProject(f));
            }
        }

        return newJavaAnalysisFilesImpl(
                sourceRoots.toArray(new File[0]),
                dependencies.toArray(new File[0]));
    }

    @Override
    public Problem newProblem(ProblemType problemType, @Nullable File file, int lineNumber, Properties properties, @Nullable Object details) {
        return ProblemImpl.newProblemImpl(problemType, file, lineNumber, properties, details);
    }

    @Override
    public Problems newProblems(Iterable<Problem> problems) {
        return newProblemsImpl(problems);
    }

    @Override
    public ProblemCheckers newProblemCheckers(Iterable<ProblemChecker> items) {
        return ProblemCheckersImpl.newProblemCheckersImpl(items);
    }

    @Override
    public ProblemReporters newProblemReporters(Iterable<ProblemReporter> items) {
        return ProblemReportersImpl.newProblemReportersImpl(items);
    }

    @Override
    public ProblemCheckers getAllProblemCheckers() {
        return ProblemUtil.getAllProblemCheckers();
    }

    @Override
    public ProblemCheckers getProblemCheckersWithIds(Iterable<String> checkerIds) {

        Map<String, ProblemChecker> map = new HashMap<>();
        for (var pc : getAllProblemCheckers()) {
            map.put(pc.getProblemType().getID(), pc);
        }
        List<ProblemChecker> result = new ArrayList<>();
        List<String> missingCheckers = new ArrayList<>();
        for (var id : checkerIds) {
            var pc = map.get(id);
            if (pc == null) {
                missingCheckers.add(id);
            } else {
                result.add(pc);
            }
        }
        if (!missingCheckers.isEmpty()) {
            throw new IllegalArgumentException(
                    "ProblemChecker not found: %s".formatted(
                            String.join(", ", missingCheckers)));
        }
        return newProblemCheckers(result);
    }

    @Override
    public ProblemReporters getAllProblemReporters() {
        return ProblemUtil.getAllProblemReporters();
    }

    @Override
    public Problems checkForProblems(
            JavaAnalysisFiles javaAnalysisFiles,
            Iterable<ProblemChecker> problemCheckers,
            Consumer<Problem> problemConsumer,
            Predicate<File> aboutToCheckFile) {

        return ProblemUtil.checkForProblems(
                javaAnalysisFiles, problemCheckers, problemConsumer, aboutToCheckFile);
    }

    @Override
    public void reportProblems(
            Problems problems,
            Iterable<ProblemReporter> problemReporters,
            Consumer<String> progress,
            ProblemReporter.ReportParameter reportParameter) {
        ProblemUtil.reportProblems(problems, problemReporters, progress, reportParameter);
    }

    @Override
    public Problems checkForProblemsAndWriteReports(
            JavaAnalysisFiles javaAnalysisFiles,
            ProblemCheckers problemCheckers,
            ProblemReporters problemReporters,
            boolean progressOnProcessedFile,
            Consumer<String> progress,
            ProblemReporter.ReportParameter reportParameter) {
        return ProblemUtil.checkForProblemsAndWriteReports(
                javaAnalysisFiles,
                problemCheckers,
                problemReporters,
                progressOnProcessedFile,
                progress,
                reportParameter);
    }

}
