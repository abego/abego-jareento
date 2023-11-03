package org.abego.jareento.cli;

import org.abego.jareento.javaanalysis.JavaAnalysisAPI;
import org.abego.jareento.javaanalysis.ProblemCheckers;
import org.abego.jareento.javaanalysis.ProblemReporters;
import org.abego.jareento.javaanalysis.ProblemType;
import org.abego.jareento.javaanalysis.Problems;
import org.abego.jareento.javaanalysis.internal.JavaAnalysisFiles;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static org.abego.commons.io.FileUtil.parseFiles;
import static org.abego.commons.util.ServiceLoaderUtil.loadService;

public class CheckForProblemsApp {
    private static final int EXIT_CODE_FOUND_PROBLEMS = 1;
    private final JavaAnalysisAPI javaAnalysisAPI = loadService(JavaAnalysisAPI.class);

    /**
     * The arguments used when running the app.
     *
     * @param printUsage  Show the print usage, don't check for any problems
     * @param silent      Silent mode, less progress output
     * @param superSilent Super Silent mode, nearly no progress output
     * @param checkerIds  The IDs of the problem checkers to use in the run.
     *                    Must not be empty, unless printUsage == true
     * @param files       A {@link org.abego.jareento.javaanalysis.internal.JavaAnalysisFiles}
     *                    instance created with these files is used in the run.
     *                    Must not be empty, unless printUsage == true
     */
    private record RunParameters(
            boolean printUsage,
            boolean silent,
            boolean superSilent,
            File outputDirectory,
            Set<String> checkerIds,
            List<String> files) {
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

    private static RunParameters parseArgs(String... args) {
        boolean silent = false;
        boolean superSilent = false;
        File outputDirectory = new File(".");
        Set<String> checkerIds = new HashSet<>();
        List<String> files = new ArrayList<>();
        int i = 0;
        while (i < args.length) {
            var arg = args[i++];
            if (arg.startsWith("-")) {
                if (!files.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Unexpected option '%s' after files.".formatted(arg));
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
                    case "-o" -> {
                        if (i < args.length) {
                            outputDirectory = new File(args[i++]);
                        } else {
                            throw new IllegalArgumentException(
                                    "Missing value for option '%s'.".formatted(arg));
                        }
                    }
                    default -> throw new IllegalArgumentException(
                            "Unexpected option '%s'".formatted(arg));
                }
            } else {
                files.add(arg);
            }
        }
        boolean printUsage = args.length == 0;

        if (!printUsage && checkerIds.isEmpty()) {
            throw new IllegalArgumentException("No Problem Checkers specified.");
        }
        if (!printUsage && files.isEmpty()) {
            throw new IllegalArgumentException("No source root specified.");
        }

        return new RunParameters(
                printUsage, silent, superSilent, outputDirectory, checkerIds, files);
    }
    
    private Problems runWith(RunParameters args) {
        if (args.printUsage()) {
            printUsage(System.out);
            printAvailableProblemCheckersAndReporters(System.out);
            return javaAnalysisAPI.newProblems(emptyList());
        }

        JavaAnalysisFiles files = javaAnalysisAPI.newJavaAnalysisFiles(
                parseFiles(args.files, ";"));
        var problemCheckers =
                javaAnalysisAPI.getProblemCheckersWithIds(args.checkerIds);
        if (problemCheckers.isEmpty()) {
            throw new IllegalArgumentException("No problem checkers specified.");
        }
        var problemReporters = javaAnalysisAPI.getAllProblemReporters();
        if (problemReporters.isEmpty()) {
            throw new IllegalArgumentException("No problem reporters found.");
        }
        Consumer<String> progress =
                args.superSilent() ? s -> {} : System.out::println;
        boolean progressOnProcessedFile = !args.silent;
        File outputDirectory = args.outputDirectory();
        
        return javaAnalysisAPI.checkForProblemsAndWriteReports(
                files,
                problemCheckers,
                problemReporters,
                progressOnProcessedFile,
                progress,
                () -> outputDirectory);
    }

    private void printAvailableProblemCheckersAndReporters(PrintStream out) {
        printAvailableProblemCheckers(out, javaAnalysisAPI.getAllProblemCheckers());
        out.println();
        printAvailableProblemReporters(out, javaAnalysisAPI.getAllProblemReporters());
    }

    private static void printUsage(PrintStream out) {
        out.println("Usage:");
        out.println("    <command> [-s] [-S] [-o dir] [-c problemCheckerId]+ [file]+");
        out.println();
        out.println("Options:");
        out.println();
        out.println("    -c value  Activate the problem checker with the ID given in the value.");
        out.println("              This option may be occur multiple times, to check for ");
        out.println("              different kinds of problems in one run.");
        out.println();
        out.println("    -o dir    Write the output to the directory dir.");
        out.println("              When missing output is written to the working directory.");
        out.println();
        out.println("    -s        Silent mode, less progress output");
        out.println();
        out.println("    -S        Super Silent mode, nearly no progress output");
        out.println();
        //TODO: update the description (include pom.xml,...). 
        out.println("    file      Either a `source root` directory of Java source files (like");
        out.println("              'src/main/java' in a Maven project), or a `jar` file to be");
        out.println("              included as a dependency. The source code inside the `source");
        out.println("              root` directories is checked for problems.");
        out.println("              Multiple directories and `jar` files may be specified.");
        out.println();
    }

    private void printAvailableProblemCheckers(
            PrintStream out, ProblemCheckers problemCheckers) {
        if (problemCheckers.isEmpty()) {
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
            PrintStream out, ProblemReporters problemReporters) {
        if (problemReporters.isEmpty()) {
            out.println("No Problem Reporters available.");
            return;
        }

        out.println("Available Problem Reporters:");
        out.println("\t[ID]\t[Title]");
        for (var pr : problemReporters) {
            out.printf("\t%s\t%s%n", pr.getID(), pr.getTitle());
        }
    }
}
