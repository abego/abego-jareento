package org.abego.jareento.cli;

import org.abego.commons.io.FileUtil;
import org.abego.commons.lang.StringUtil;
import org.abego.commons.test.JUnit5Util;
import org.abego.jareento.javaanalysis.JavaAnalysisAPI;
import org.abego.jareento.javaanalysis.ProblemCheckerTest;
import org.abego.jareento.javaanalysis.ProblemCheckers;
import org.abego.jareento.javaanalysis.internal.JavaAnalysisAPIImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static java.util.Collections.emptyList;
import static org.abego.commons.test.SystemTesting.runAndReturnSystemOut;
import static org.abego.commons.util.ServiceLoaderUtil.loadService;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CheckForProblemsAppTest {
    private final JavaAnalysisAPI javaAnalysisAPI = loadService(JavaAnalysisAPI.class);

    @Test
    void runWithoutArgs() {
        String actual = runAndReturnSystemOut(CheckForProblemsApp::main);

        assertEquals("""
                Usage:
                    <command> [-s] [-S] [-o dir] [-c problemCheckerId]+ [file]+

                Options:

                    -c value  Activate the problem checker with the ID given in the value.
                              This option may be occur multiple times, to check for\s
                              different kinds of problems in one run.

                    -o dir    Write the output to the directory dir.
                              When missing output is written to the working directory.

                    -s        Silent mode, less progress output

                    -S        Super Silent mode, nearly no progress output

                    file      Either a `source root` directory of Java source files (like
                              'src/main/java' in a Maven project), or a `jar` file to be
                              included as a dependency. The source code inside the `source
                              root` directories is checked for problems.
                              Multiple directories and `jar` files may be specified.

                No Problem Checkers available.

                Available Problem Reporters:
                \t[ID]\t[Title]
                \tStandard\tWrites problems as tab-separated lines to a file 'problems.txt'.
                """, StringUtil.unixString(actual));
    }

    @Test
    void runWithoutArgsnoProblemReporters() {

        try {
            JavaAnalysisAPIImpl.setAllProblemReporters(
                    javaAnalysisAPI.newProblemReporters(emptyList()));

            String actual = runAndReturnSystemOut(CheckForProblemsApp::main);

            assertEquals("""
                    Usage:
                        <command> [-s] [-S] [-o dir] [-c problemCheckerId]+ [file]+

                    Options:

                        -c value  Activate the problem checker with the ID given in the value.
                                  This option may be occur multiple times, to check for\s
                                  different kinds of problems in one run.

                        -o dir    Write the output to the directory dir.
                                  When missing output is written to the working directory.

                        -s        Silent mode, less progress output

                        -S        Super Silent mode, nearly no progress output

                        file      Either a `source root` directory of Java source files (like
                                  'src/main/java' in a Maven project), or a `jar` file to be
                                  included as a dependency. The source code inside the `source
                                  root` directories is checked for problems.
                                  Multiple directories and `jar` files may be specified.

                    No Problem Checkers available.

                    No Problem Reporters available.
                    """, StringUtil.unixString(actual));
        } finally {
            JavaAnalysisAPIImpl.resetAllProblemReporters();
        }
    }

    @Test
    void onlySilent() {
        JUnit5Util.assertThrowsWithMessage(
                IllegalArgumentException.class,
                "No Problem Checkers specified.",
                () -> CheckForProblemsApp.main("-s"));
        JUnit5Util.assertThrowsWithMessage(
                IllegalArgumentException.class,
                "No Problem Checkers specified.",
                () -> CheckForProblemsApp.main("-S"));
    }

    @Test
    void undefinedOption() {
        JUnit5Util.assertThrowsWithMessage(
                IllegalArgumentException.class,
                "Unexpected option '-foo'",
                () -> CheckForProblemsApp.main("-foo"));
    }

    @Test
    void optionAfterFiles() {
        JUnit5Util.assertThrowsWithMessage(
                IllegalArgumentException.class,
                "Unexpected option '-foo' after files.",
                () -> CheckForProblemsApp.main("file", "-foo"));
    }

    @Test
    void missingOptionValue() {
        JUnit5Util.assertThrowsWithMessage(
                IllegalArgumentException.class,
                "Missing value for option '-c'.",
                () -> CheckForProblemsApp.main("-c"));
        JUnit5Util.assertThrowsWithMessage(
                IllegalArgumentException.class,
                "Missing value for option '-o'.",
                () -> CheckForProblemsApp.main("-o"));
    }

    @Test
    void missingSourceRoot() {
        JUnit5Util.assertThrowsWithMessage(
                IllegalArgumentException.class,
                "No source root specified.",
                () -> CheckForProblemsApp.main("-c", "SomeID"));
    }

    @Test
    void run$NoProblems(@TempDir File tempDir) {
        ProblemCheckers problemCheckers = ProblemCheckerTest.getProblemCheckersSample();
        try {
            JavaAnalysisAPIImpl.setAllProblemCheckers(problemCheckers);

            CheckForProblemsApp.main(
                    "-c", "ProblemTypeSample",
                    "-o", tempDir.getAbsolutePath(),
                    tempDir.getAbsolutePath());

            String problems = FileUtil.textOf(new File(tempDir,"problems.txt"));
            assertEquals("0 problems.\n", problems);
        } finally {
            JavaAnalysisAPIImpl.resetAllProblemCheckers();
        }
    }


}
