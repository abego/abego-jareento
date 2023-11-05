package org.abego.jareento.javaanalysis;

import com.github.javaparser.ast.CompilationUnit;
import org.abego.jareento.javaanalysis.internal.LogFromConsumer;
import org.abego.jareento.javaanalysis.internal.JavaAnalysisFiles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.Properties;
import java.util.function.Consumer;

import static org.abego.commons.util.ListUtil.toList;
import static org.abego.commons.util.ServiceLoaderUtil.loadService;
import static org.abego.jareento.javaanalysis.internal.ProblemTest.newProblemLogFromConsumer;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProblemCheckerTest {
    private static final JavaAnalysisAPI javaAnalysisAPI = loadService(JavaAnalysisAPI.class);

    private static class ProblemCheckerSample implements ProblemChecker {

        @Override
        public ProblemType getProblemType() {
            return new ProblemTypeTest.ProblemTypeSample();
        }

        @Override
        public void checkForProblems(CompilationUnit cu, Consumer<Problem> problemConsumer) {
            Problem p = javaAnalysisAPI.newProblem(
                    getProblemType(),
                    cu.getStorage().orElseThrow().getPath().toFile(), 1,
                    new Properties());
            problemConsumer.accept(p);
        }
    }

    public static ProblemChecker getProblemCheckerSample() {
        return new ProblemCheckerSample();
    }

    public static ProblemCheckers getProblemCheckersSample() {
        return javaAnalysisAPI.newProblemCheckers(toList(getProblemCheckerSample()));
    }

    @Test
    void smokeTest(@TempDir File tempDir) {
        File javaFile = TestUtil.writeMiniJavaFile(tempDir);
        JavaAnalysisFiles files = javaAnalysisAPI.newJavaAnalysisFiles(tempDir);

        LogFromConsumer<Problem> problemLog = newProblemLogFromConsumer();
        javaAnalysisAPI.checkForProblems(
                files,
                toList(getProblemCheckerSample()),
                problemLog);

        assertEquals("ProblemTypeSample\t%s:1\n".formatted(javaFile.getAbsolutePath()),
                problemLog.getText());
    }

}
