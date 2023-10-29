package org.abego.jareento.cli;

import org.abego.commons.lang.StringUtil;
import org.abego.commons.test.JUnit5Util;
import org.junit.jupiter.api.Test;

import static org.abego.commons.test.SystemTesting.runAndReturnSystemOut;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CheckForProblemsAppTest {

    @Test
    void runWithoutArgs() {
        String actual = runAndReturnSystemOut(CheckForProblemsApp::main);

        assertEquals("""
                Usage:
                    <command> [-s] [-S] [-c problemCheckerId]+ [file]+

                Options:

                    -c value  Activate the problem checker with the ID given in the value.
                              This option may be occur multiple times, to check for\s
                              different kinds of problems in one run.

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
    void onlySilent() {
        JUnit5Util.assertThrowsWithMessage(
                IllegalArgumentException.class,
                "No Problem Checkers specified.",
                () -> CheckForProblemsApp.main("-s"));
    }

    @Test
    void undefinedOption() {
        JUnit5Util.assertThrowsWithMessage(
                IllegalArgumentException.class,
                "Unexpected option '-foo'",
                () -> CheckForProblemsApp.main("-foo"));
    }
}
