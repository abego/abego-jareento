package org.abego.jareento.javaanalysis;

import com.github.javaparser.ast.CompilationUnit;
import org.abego.commons.io.FileUtil;
import org.abego.jareento.javaanalysis.internal.JavaAnalysisFiles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.Properties;
import java.util.function.Consumer;

import static org.abego.commons.util.ListUtil.toList;
import static org.abego.commons.util.ServiceLoaderUtil.loadService;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ProblemCheckerTest {
    private static final JavaAnalysisAPI javaAnalysisAPI = loadService(JavaAnalysisAPI.class);

    public static class ProblemCheckerSample implements ProblemChecker {

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

    @Test
    void smokeTest(@TempDir File tempDir) {
        File javaFile = new File(tempDir, "Main.java");
        FileUtil.writeText(javaFile, "public class Main {}");
        JavaAnalysisFiles files = javaAnalysisAPI.newJavaAnalysisFiles(tempDir);

        StringBuilder log = new StringBuilder();
        ProblemChecker problemChecker = new ProblemCheckerSample();
        Consumer<Problem> problemConsumer = p -> {
            log.append(p.getProblemType().getID());
            log.append('\t');
            log.append(p.getFile().getAbsolutePath());
            log.append(':');
            log.append(p.getLineNumber());
            log.append('\n');
        };

        javaAnalysisAPI.checkForProblems(files, toList(problemChecker), problemConsumer);

        assertEquals("myId\t%s:1\n".formatted(javaFile.getAbsolutePath()), log.toString());
    }
}
