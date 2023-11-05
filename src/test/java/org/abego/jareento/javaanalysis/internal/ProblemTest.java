package org.abego.jareento.javaanalysis.internal;

import org.abego.commons.test.JUnit5Util;
import org.abego.jareento.javaanalysis.Problem;
import org.abego.jareento.javaanalysis.ProblemType;
import org.abego.jareento.javaanalysis.ProblemTypeTest;
import org.abego.jareento.javaanalysis.Problems;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ProblemTest {
    public static LogFromConsumer<Problem> newProblemLogFromConsumer() {
        return new LogFromConsumer<>(ProblemTest::shortProblemText);
    }

    public static String shortProblemText(Problem problem) {
        return "%s\t%s\t%s:%d".formatted(
                problem.getProblemType().getID(),
                problem.getDescription(),
                problem.getFile().getAbsolutePath(),
                problem.getLineNumber());
    }

    public static String shortProblemsText(Problems problems) {
        return problems.stream()
                .map(ProblemTest::shortProblemText)
                .collect(Collectors.joining("\n"));
    }

    @Test
    void smokeTest() {
        ProblemType problemType = new ProblemTypeTest.ProblemTypeSample2();
        File file = new File("Sample.java");
        int lineNumber = 123;
        Properties properties = new Properties();
        properties.setProperty("foo", "bar");
        String details = "someDetails";

        Problem p = ProblemImpl.newProblemImpl(
                problemType,
                file,
                lineNumber,
                properties,
                details);
        Problem p2 = ProblemImpl.newProblemImpl(
                problemType,
                file,
                lineNumber,
                properties,
                details);

        assertEquals(problemType, p.getProblemType());
        assertEquals(file.getAbsolutePath(), p.getFile().getAbsolutePath());
        assertEquals(lineNumber, p.getLineNumber());
        assertEquals("bar", p.getProperties().getProperty("foo"));
        assertEquals(details, p.getDetails());
        assertEquals("pt2Description-bar", p.getDescription());

        assertEquals(p, p);
        assertEquals(p, p2);
        assertNotEquals(p, null);
        assertNotEquals(p, "problem");
        assertEquals(p.hashCode(), p2.hashCode());
    }

    @Test
    void noFileForLineNumber() {
        ProblemType problemType = new ProblemTypeTest.ProblemTypeSample2();

        JUnit5Util.assertThrowsWithMessage(
                IllegalArgumentException.class,
                "lineNumber defined (123), but no file specified.",
                () -> ProblemImpl.newProblemImpl(
                        problemType,
                        null,
                        123,
                        new Properties(),
                        null));
    }
}
