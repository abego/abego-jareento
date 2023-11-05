package org.abego.jareento.javaanalysis.internal;

import org.abego.commons.io.FileUtil;
import org.abego.jareento.javaanalysis.JavaAnalysisAPI;
import org.abego.jareento.javaanalysis.Problem;
import org.abego.jareento.javaanalysis.ProblemTypeTest;
import org.abego.jareento.javaanalysis.Problems;
import org.abego.jareento.javaanalysis.StandardProblemReporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.Properties;

import static org.abego.commons.util.ListUtil.toList;
import static org.abego.commons.util.ServiceLoaderUtil.loadService;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ProblemUtilTest {
    private static final JavaAnalysisAPI javaAnalysisAPI = loadService(JavaAnalysisAPI.class);

    public static Problem getProblemSample() {
        File file = new File("/somePath/Sample.java");
        return javaAnalysisAPI.newProblem(
                new ProblemTypeTest.ProblemTypeSample(),
                file, 1,
                new Properties());
    }

    public static Problems getProblemsSample() {
        return javaAnalysisAPI.newProblems(toList(getProblemSample()));
    }

    @Test
    void reportProblems(@TempDir File tempDir) {
        File problemsTxtFile = new File(tempDir, "problems.txt");
        StandardProblemReporter reporters = new StandardProblemReporter();
        LogFromConsumer<String> progress = new LogFromConsumer<>(s -> s);
        ProblemUtil.reportProblems(
                getProblemsSample(),
                toList(reporters),
                progress,
                () -> tempDir);

        assertEquals("""
                1 problem.
                /somePath/Sample.java\t1\tProblemTypeSample\tProblemType introduced for tests
                """, FileUtil.textOf(problemsTxtFile));
    }
}
