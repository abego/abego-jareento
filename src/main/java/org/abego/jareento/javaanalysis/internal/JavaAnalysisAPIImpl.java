package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaAnalysisAPI;
import org.abego.jareento.javaanalysis.JavaAnalysisProjectConfiguration;
import org.abego.jareento.javaanalysis.JavaAnalysisProjectStorage;
import org.abego.jareento.javaanalysis.Problem;
import org.abego.jareento.javaanalysis.ProblemChecker;
import org.abego.jareento.javaanalysis.ProblemType;
import org.abego.jareento.javaanalysis.Problems;
import org.abego.jareento.javaanalysis.ProblemsReporter;
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
    public Problem newProblem(ProblemType problemType, long locationInFileId, Properties properties, @Nullable Object details) {
        return ProblemImpl.newProblemImpl(problemType, locationInFileId, properties, details);
    }

    @Override
    public Problems newProblems(Iterable<Problem> problems) {
        return newProblemsImpl(problems);
    }

    @Override
    public Iterable<ProblemChecker> getAllProblemCheckers() {
        return ProblemUtil.getAllProblemCheckers();
    }

    @Override
    public Iterable<ProblemsReporter> getAllProblemsReporters() {
        return ProblemUtil.getAllProblemsReporters();
    }

    @Override
    public Problems checkForProblems(
            File[] sourceRootsAndDependencies, Iterable<ProblemChecker> problemCheckers,
            Consumer<Problem> problemConsumer,
            Predicate<File> aboutToCheckFile) {

        return ProblemUtil.checkForProblems(
                sourceRootsAndDependencies, problemCheckers, problemConsumer, aboutToCheckFile);
    }

    @Override
    public void reportProblems(
            Problems problems,
            Iterable<ProblemsReporter> problemsReporters,
            Consumer<String> progress) {
        ProblemUtil.reportProblems(problems, problemsReporters, progress);
    }

    @Override
    public Problems checkForProblemsAndWriteReports(
            File[] sourceRootsAndDependencies,
            Iterable<ProblemChecker> problemCheckers,
            Iterable<ProblemsReporter> problemsReporters,
            boolean processedFileToProgress,
            Consumer<String> progress) {
        return ProblemUtil.checkForProblemsAndWriteReports(
                sourceRootsAndDependencies,
                problemCheckers,
                problemsReporters,
                processedFileToProgress,
                progress);
    }

}
