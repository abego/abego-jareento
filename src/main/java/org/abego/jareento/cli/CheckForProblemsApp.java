package org.abego.jareento.cli;

import org.abego.commons.lang.IterableUtil;
import org.abego.jareento.javaanalysis.JavaAnalysisAPI;
import org.abego.jareento.javaanalysis.ProblemChecker;
import org.abego.jareento.javaanalysis.ProblemCheckers;
import org.abego.jareento.javaanalysis.ProblemType;
import org.abego.jareento.javaanalysis.Problems;
import org.abego.jareento.javaanalysis.ProblemReporter;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.abego.commons.io.FileUtil.parseFiles;
import static org.abego.commons.util.ListUtil.toList;
import static org.abego.commons.util.ServiceLoaderUtil.loadService;

public class CheckForProblemsApp {
    private static final int EXIT_CODE_FOUND_PROBLEMS = 1;
    private final JavaAnalysisAPI javaAnalysisAPI = loadService(JavaAnalysisAPI.class);

    private record ParsedArgs(
            boolean printUsage,
            boolean silent,
            boolean superSilent,
            Set<String> checkerIds,
            List<String> sourceRootsAndDependencies) {
    }

    /**
     * Check for problems as specified by the arguments.
     * <p>
     * Calling {@code main} without arguments will prompt a "usage"
     * description to {@link System#out}, including a list of 
     * available "problem checkers" and additional information.
     * <p>
     * Terminates with exit code != 0 when problems are found.
     * Details on the problems are provided in additional files generated
     * by the application.
     */
    public static void main(String... args) {
        var app = new CheckForProblemsApp();

        Problems problems = app.runWith(parseArgs(args));
        
        if (!problems.isEmpty()) {
            System.exit(EXIT_CODE_FOUND_PROBLEMS);
        }
    }

    private static ParsedArgs parseArgs(String[] args) {
        boolean silent = false;
        boolean superSilent = false;
        Set<String> checkerIds = new HashSet<>();
        List<String> sourceRootsAndDependencies = new ArrayList<>();
        int i = 0;
        while (i < args.length) {
            var arg = args[i++];
            if (arg.startsWith("-")) {
                if (!sourceRootsAndDependencies.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Unexpected option '%s' after source roots.".formatted(arg));
                }
                switch (arg) {
                    case "-s" -> silent = true;
                    case "-S" -> {
                        superSilent = true;
                        silent = true;
                    }
                    case "-c" -> {
                        if (i < args.length) {
                            checkerIds.add(args[i++]);
                        } else {
                            throw new IllegalArgumentException(
                                    "Missing value for option '%s'.".formatted(arg));
                        }
                    }
                    default -> throw new IllegalArgumentException(
                            "Unexpected option '%s'".formatted(arg));
                }
            } else {
                sourceRootsAndDependencies.add(arg);
            }
        }

        return new ParsedArgs(
                args.length == 0, silent, superSilent, checkerIds, sourceRootsAndDependencies);
    }

    private Problems runWith(ParsedArgs args) {
        if (args.printUsage()) {
            printUsage(System.out);
            printAvailableProblemCheckersAndReporters(System.out);
            return javaAnalysisAPI.newProblems(emptyList());
        }

        checkArgs(args);

        var problemCheckers = problemCheckersWithIds(args.checkerIds);
        if (IterableUtil.isEmpty(problemCheckers)) {
            throw new IllegalArgumentException("No problem checkers specified.");
        }
        var problemReporters = javaAnalysisAPI.getAllProblemReporters();
        if (IterableUtil.isEmpty(problemReporters)) {
            throw new IllegalArgumentException("No problem reporters found.");
        }
        File[] sourceRootsAndDependencies = parseFiles(
                args.sourceRootsAndDependencies, ";");

        Consumer<String> progress = args.superSilent() ? s -> {} : System.out::println;
        boolean processedFileToProgress = !args.silent;

        printProblemsToCheck(problemCheckers, progress);
        printSourceRootsToCheck(sourceRootsAndDependencies, progress);

        return javaAnalysisAPI.checkForProblemsAndWriteReports(
                sourceRootsAndDependencies,
                problemCheckers,
                problemReporters,
                processedFileToProgress,
                progress);
    }

    private static void checkArgs(ParsedArgs args) {
        if (args.checkerIds.isEmpty()) {
            throw new IllegalArgumentException("No Problem Checkers specified.");
        }
        if (args.sourceRootsAndDependencies.isEmpty()) {
            throw new IllegalArgumentException("No source root specified.");
        }
    }

    private static void printSourceRootsToCheck(File[] sourceRootsAndDependencies, Consumer<String> progress) {
        progress.accept("Checking source root(s):");
        toList(sourceRootsAndDependencies).stream()
                .filter(File::isDirectory)
                .forEach(f ->
                        progress.accept("\t%s".formatted(f.getAbsolutePath())));
    }

    private static void printProblemsToCheck(Iterable<ProblemChecker> problemCheckers, Consumer<String> progress) {
        progress.accept("Checking for problem(s): %s".formatted(
                toList(problemCheckers).stream()
                        .map(pc -> pc.getProblemType().getID())
                        .collect(Collectors.joining(" "))));
    }

    private void printAvailableProblemCheckersAndReporters(PrintStream out) {
        printAvailableProblemCheckers(out, javaAnalysisAPI.getAllProblemCheckers());
        out.println();
        printAvailableProblemReporters(out, javaAnalysisAPI.getAllProblemReporters());
    }

    private static void printUsage(PrintStream out) {
        out.println("Usage:");
        out.println("    <command> [-s] [-S] [-c problemCheckerId]+ [file]+");
        out.println();
        out.println("Options:");
        out.println();
        out.println("    -c value  Activate the problem checker with the ID given in the value.");
        out.println("              This option may be occur multiple times, to check for ");
        out.println("              different kinds of problems in one run.");
        out.println();
        out.println("    -s        Silent mode, less progress output");
        out.println();
        out.println("    -S        Super Silent mode, nearly no progress output");
        out.println();
        out.println("    file      Either a `source root` directory of Java source files (like");
        out.println("              'src/main/java' in a Maven project), or a `jar` file to be");
        out.println("              included as a dependency. The source code inside the `source");
        out.println("              root` directories is checked for problems.");
        out.println("              Multiple directories and `jar` files may be specified.");
        out.println();
    }

    private void printAvailableProblemCheckers(
            PrintStream out, Iterable<ProblemChecker> problemCheckers) {
        if (IterableUtil.isEmpty(problemCheckers)) {
            out.println("No Problem Checkers available.");
            return;
        }

        out.println("Available Problem Checker:");
        out.println("\t[ID]\t[Title]");
        for (var pc : problemCheckers) {
            ProblemType problemType = pc.getProblemType();
            out.printf("\t%s\t%s%n", problemType.getID(), problemType.getTitle());
        }
    }

    private void printAvailableProblemReporters(
            PrintStream out, Iterable<ProblemReporter> problemReporters) {
        if (IterableUtil.isEmpty(problemReporters)) {
            out.println("No Problem Reporters available.");
            return;
        }

        out.println("Available Problem Reporters:");
        out.println("\t[ID]\t[Title]");
        for (var pr : problemReporters) {
            out.printf("\t%s\t%s%n", pr.getID(), pr.getTitle());
        }
    }

    private ProblemCheckers problemCheckersWithIds(
            Set<String> checkerIds) {

        Map<String, ProblemChecker> map = new HashMap<>();
        for (var pc : javaAnalysisAPI.getAllProblemCheckers()) {
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
        return javaAnalysisAPI.newProblemCheckers(result);
    }

}
