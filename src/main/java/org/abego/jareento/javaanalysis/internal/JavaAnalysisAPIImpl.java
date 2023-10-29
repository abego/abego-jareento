package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaAnalysisAPI;
import org.abego.jareento.javaanalysis.JavaAnalysisProjectConfiguration;
import org.abego.jareento.javaanalysis.JavaAnalysisProjectStorage;
import org.abego.jareento.javaanalysis.Problem;
import org.abego.jareento.javaanalysis.ProblemChecker;
import org.abego.jareento.javaanalysis.ProblemCheckers;
import org.abego.jareento.javaanalysis.ProblemType;
import org.abego.jareento.javaanalysis.Problems;
import org.abego.jareento.javaanalysis.ProblemReporter;
import org.abego.jareento.javaanalysis.ProblemReporters;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.net.URI;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
    public ProblemReporters getAllProblemReporters() {
        return ProblemUtil.getAllProblemReporters();
    }

    @Override
    public Problems checkForProblems(
            File[] sourceRootsAndDependencies, ProblemCheckers problemCheckers,
            Consumer<Problem> problemConsumer,
            Predicate<File> aboutToCheckFile) {

        return ProblemUtil.checkForProblems(
                sourceRootsAndDependencies, problemCheckers, problemConsumer, aboutToCheckFile);
    }

    @Override
    public void reportProblems(
            Problems problems,
            ProblemReporters problemReporters,
            Consumer<String> progress) {
        ProblemUtil.reportProblems(problems, problemReporters, progress);
    }

    @Override
    public Problems checkForProblemsAndWriteReports(
            File[] sourceRootsAndDependencies,
            ProblemCheckers problemCheckers,
            ProblemReporters problemReporters,
            boolean processedFileToProgress,
            Consumer<String> progress) {
        return ProblemUtil.checkForProblemsAndWriteReports(
                sourceRootsAndDependencies,
                problemCheckers,
                problemReporters,
                processedFileToProgress,
                progress);
    }

}
