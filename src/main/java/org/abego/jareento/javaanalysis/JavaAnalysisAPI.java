package org.abego.jareento.javaanalysis;

import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.net.URI;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * The main entry to the API of the JavaAnalysis module.
 */
public interface JavaAnalysisAPI {
    //region Storage

    /**
     * Returns the {@link JavaAnalysisProjectStorage} that stores
     * {@link JavaAnalysisProject}s in the given storageURI.
     * <p>
     * Note: Each {@link JavaAnalysisProject} is associated with a unique
     * {@link URI} that is different from the URI of the
     * JavaAnalysisProjectStorage containing the project.
     */
    JavaAnalysisProjectStorage getJavaAnalysisProjectStorage(URI storageURI);
    //endregion

    //region Configuration

    /**
     * Returns a new {@link JavaAnalysisProjectConfiguration}, referring to an
     * existing Maven project.
     * <p>
     *
     * @param name                  The name of the project configured.
     * @param mavenProjectDirectory the Maven project directory, i.e. the directory
     *                              containing the {@code pom.xml} file
     * @param sourceRoots           The directories that are the roots for the Java
     *                              source code to be included in the JavaAnalysisProject.
     * @param projectJars           when not null is the Jar files of the project, to be
     *                              included in the analysis. When null the Jar files in
     *                              the Maven project's target directory are used.
     * @param dependencies          when not null the dependencies of the project, i.e.
     *                              the Jar files to be used instead of the classpath
     *                              defined by the Maven project.
     */
    JavaAnalysisProjectConfiguration newJavaAnalysisProjectConfiguration(
            String name,
            File mavenProjectDirectory,
            File[] sourceRoots,
            File @Nullable [] projectJars,
            File @Nullable [] dependencies);

    /**
     * Returns a new {@link JavaAnalysisProjectConfiguration}, referring to an
     * existing Maven project.
     * <p>
     * The Jar files in the Maven project's target directory are included in the
     * analysis, also using the classpath define by the Maven project to resolve
     * dependencies.
     * <p>
     *
     * @param name                  The name of the project configured.
     * @param mavenProjectDirectory the Maven project directory, i.e. the directory
     *                              containing the {@code pom.xml} file
     * @param sourceRoots           The directories that are the roots for the Java
     *                              source code to be included in the JavaAnalysisProject.
     */
    default JavaAnalysisProjectConfiguration newJavaAnalysisProjectConfiguration(
            String name,
            File mavenProjectDirectory,
            File[] sourceRoots) {
        return newJavaAnalysisProjectConfiguration(
                name, mavenProjectDirectory, sourceRoots, null, null);
    }
    //endregion

    //region Problem

    //region Problem(s) factories
    Problem newProblem(
            ProblemType problemType,
            @Nullable File file,
            int lineNumber,
            Properties properties,
            @Nullable Object details);

    default Problem newProblem(
            ProblemType problemType,
            @Nullable File file,
            int lineNumber,
            Properties properties) {
        return newProblem(problemType, file, lineNumber, properties, null);
    }

    default Problem newProblem(
            ProblemType problemType,
            @Nullable File file,
            int lineNumber) {
        return newProblem(problemType, file, lineNumber, new Properties());
    }

    Problems newProblems(Iterable<Problem> problems);
    //endregion

    //region Problem Checking & Reporting

    ProblemCheckers newProblemCheckers(Iterable<ProblemChecker> items);

    ProblemReporters newProblemReporters(Iterable<ProblemReporter> items);

    /**
     * Returns all {@link ProblemChecker}s currently available.
     */
    ProblemCheckers getAllProblemCheckers();

    /**
     * Returns all {@link ProblemReporter}s currently available.
     */
    ProblemReporters getAllProblemReporters();

    /**
     * Checks for problems in the Java files under the source roots given in
     * {@code sourceRootsAndDependencies}, using the provided
     * {@code problemCheckers} and passes any {@link Problem} to the
     * {@code problemConsumer}, finally returning the detected {@link Problems}.
     * <p>
     *
     * @param sourceRootsAndDependencies each file in this array is either a
     *                                   `source root` directory of Java files
     *                                   (like 'src/main/java' in a Maven
     *                                   project), or a `jar` file to be
     *                                   included as a dependency. The source
     *                                   code inside the `source root`
     *                                   directories is checked for problems.
     * @param problemCheckers            The {@link ProblemChecker}s used to check for problems.
     * @param problemConsumer            receives any detected {@link Problem}
     *                                   (Default: {@code p -> {}})
     * @param aboutToCheckFile           Called before checking a Java file. Only when
     *                                   it returns {@code true} the file is actually
     *                                   checked. Beside selecting/filtering files for
     *                                   the problem check {@code aboutToCheckFile} may
     *                                   also be used for progress reporting.
     *                                   (Default: {@code f -> true})
     */
    Problems checkForProblems(
            File[] sourceRootsAndDependencies,
            ProblemCheckers problemCheckers,
            Consumer<Problem> problemConsumer,
            Predicate<File> aboutToCheckFile);

    /**
     * See {@link #checkForProblems(File[], ProblemCheckers, Consumer, Predicate)}.
     */
    default Problems checkForProblems(
            File[] sourceRoots,
            ProblemCheckers problemCheckers,
            Consumer<Problem> problemConsumer) {
        return checkForProblems(
                sourceRoots, problemCheckers, problemConsumer, f -> true);
    }

    /**
     * See {@link #checkForProblems(File[], ProblemCheckers, Consumer, Predicate)}.
     */
    default Problems checkForProblems(
            File[] sourceRoots, ProblemCheckers problemCheckers) {
        return checkForProblems(
                sourceRoots, problemCheckers, p -> {}, f -> true);
    }

    void reportProblems(
            Problems problems,
            ProblemReporters problemReporters,
            Consumer<String> progress);

    /**
     * See {@link #checkForProblems(File[], ProblemCheckers, Consumer, Predicate)} and
     * {@link #reportProblems(Problems, ProblemReporters, Consumer)}; returns the
     * detected {@link Problems}.
     *
     * @param processedFileToProgress when {@code true} {@code progress}
     *                                receives a message holding the name of
     *                                the Java source file that is about to
     *                                be processed.
     */
    Problems checkForProblemsAndWriteReports(
            File[] sourceRootsAndDependencies,
            ProblemCheckers problemCheckers,
            ProblemReporters problemReporters,
            boolean processedFileToProgress,
            Consumer<String> progress);

    //endregion
    //endregion
}
