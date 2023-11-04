package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.Problem;
import org.abego.jareento.javaanalysis.ProblemChecker;
import org.abego.jareento.javaanalysis.ProblemReporter;
import org.abego.jareento.javaanalysis.Problems;
import org.abego.jareento.shared.commons.javaparser.JavaParserUtil;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.abego.commons.util.ListUtil.toList;
import static org.abego.jareento.javaanalysis.ProblemReporter.ReportParameter;
import static org.abego.jareento.javaanalysis.internal.ProblemsImpl.newProblemsImpl;
import static org.abego.jareento.shared.commons.javaparser.JavaParserUtil.fileOf;

class ProblemUtil {
    /**
     * Returns a {@link Comparator} that compares {@link Problem}s by their
     * absolute file path, their line number and their description text.
     */
    public static Comparator<Problem> getProblemByFileComparator() {
        Comparator<Problem> fileComparator =
                Comparator.comparing(p -> p.getFile().getAbsolutePath());
        Comparator<Problem> lineComparator =
                Comparator.comparingInt(Problem::getLineNumber);
        Comparator<Problem> descriptionComparator =
                Comparator.comparing(Problem::getDescription);

        return fileComparator
                .thenComparing(lineComparator)
                .thenComparing(descriptionComparator);
    }

    /**
     * Returns a {@link Comparator} that compares {@link Problem}s by their
     * description text, their absolute file path and their line number.
     */
    public static Comparator<Problem> getProblemByDescriptionComparator() {
        return Comparator
                .comparing(Problem::getDescription)
                .thenComparing(p -> p.getFile().getAbsolutePath())
                .thenComparing(Problem::getLineNumber);
    }

    public static Problems checkForProblems(
            JavaAnalysisFiles javaAnalysisFiles,
            Iterable<ProblemChecker> problemCheckers,
            Consumer<Problem> problemConsumer,
            Predicate<File> aboutToCheckFile) {

        // We collect any detected problem for the result before passing
        // it to the provided problemConsumer.
        List<Problem> result = new ArrayList<>();
        Consumer<Problem> innerProblemConsumer = p -> {
            result.add(p);
            problemConsumer.accept(p);
        };

        problemCheckers.forEach(ProblemChecker::beginCheck);

        try {
            JavaParserUtil.forEachJavaFileDo(
                    javaAnalysisFiles.getSourceRoots(),
                    javaAnalysisFiles.getDependencies(),
                    cu -> {
                        File file = fileOf(cu);
                        if (aboutToCheckFile.test(file)) {
                            problemCheckers.forEach(
                                    checker -> checker.checkForProblems(cu, innerProblemConsumer));
                        }
                    });
        } finally {
            problemCheckers.forEach(
                    checker -> checker.endCheck(innerProblemConsumer));
        }
        return sortedUniqueProblems(result);
    }

    public static void reportProblems(
            Problems problems,
            Iterable<ProblemReporter> problemReporters,
            Consumer<String> progress,
            ReportParameter reportParameter) {
        for (var reporter : problemReporters) {
            reporter.report(problems, progress, reportParameter);
        }
    }

    public static Problems checkForProblemsAndWriteReports(
            JavaAnalysisFiles javaAnalysisFiles,
            Iterable<ProblemChecker> problemCheckers,
            Iterable<ProblemReporter> problemReporters,
            boolean processedFileToProgress,
            Consumer<String> progress,
            ProblemReporter.ReportParameter reportParameter) {

        printProblemsToCheck(problemCheckers, progress);
        printFiles("Checking source root(s):", javaAnalysisFiles.getSourceRoots(), progress);
        printFiles("using Dependencies:", javaAnalysisFiles.getDependencies(), progress);

        long startTime = System.currentTimeMillis();
        var problems = checkForProblems(
                javaAnalysisFiles,
                problemCheckers,
                p -> {},
                f -> {
                    if (processedFileToProgress)
                        progress.accept(
                                "Processing %s...".formatted(f.getAbsolutePath()));
                    return true;
                });
        long endTime = System.currentTimeMillis();

        progress.accept("%d problems found in %.3f s%n".formatted(problems.getSize(),
                (endTime - startTime) / 1000.0));

        problems = problems.sortedByDescription();

        reportProblems(problems, problemReporters, progress, reportParameter);

        return problems;
    }

    public static Problems sortedUniqueProblems(Collection<Problem> problems) {
        Comparator<Problem> comparator = Comparator
                .comparing(Problem::getDescription)
                .thenComparing(p -> p.getFile().getAbsolutePath())
                .thenComparing(Problem::getLineNumber);
        return sortedUniqueProblems(problems, comparator);
    }

    public static Problems sortedUniqueProblems(Collection<Problem> problems,
                                                Comparator<Problem> comparator) {
        List<Problem> sorted = problems.stream().sorted(comparator).toList();

        List<Problem> uniqueProblems = new ArrayList<>();
        // To make the result only contain unique problems we need to remove the duplicates.
        // Ss the problems are already sorted duplicates immediately follow the "original"
        // problem. So we only add items to the result that are different from the "previous"
        // item.
        @Nullable
        Problem previousProblem = null;
        for (Problem p : sorted) {
            if (!p.equals(previousProblem)) {
                previousProblem = p;
                uniqueProblems.add(p);
            }
        }
        return newProblemsImpl(uniqueProblems);
    }

    private static void printFiles(
            String title, File[] sourceRoots, Consumer<String> output) {
        output.accept(title);
        toList(sourceRoots).stream()
                .sorted(Comparator.comparing(File::getAbsolutePath))
                .forEach(f ->
                        output.accept("\t%s".formatted(f.getAbsolutePath())));
    }

    private static void printProblemsToCheck(
            Iterable<ProblemChecker> problemCheckers, Consumer<String> progress) {
        progress.accept("Checking for problem(s): %s".formatted(
                toList(problemCheckers).stream()
                        .map(pc -> pc.getProblemType().getID())
                        .collect(Collectors.joining(" "))));
    }

}
