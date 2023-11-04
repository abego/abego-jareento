package org.abego.jareento.javaanalysis.internal;

import org.abego.commons.io.FileUtil;
import org.abego.commons.test.JUnit5Util;
import org.abego.jareento.javaanalysis.JavaAnalysisAPI;
import org.abego.jareento.javaanalysis.Problem;
import org.abego.jareento.javaanalysis.ProblemType;
import org.abego.jareento.javaanalysis.ProblemTypeTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.abego.commons.util.ServiceLoaderUtil.loadService;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JavaAnalysisAPITest {
    private final JavaAnalysisAPI javaAnalysisAPI = loadService(JavaAnalysisAPI.class);

    @Test
    void newJavaAnalysisFiles$Files(@TempDir File tempDir) {
        File sources = FileUtil.mkdirs(tempDir, "sources");
        File jarFile = new File(tempDir, "other.jar");
        FileUtil.writeText(jarFile, "");
        File depProject = FileUtil.mkdirs(tempDir, "otherproject");
        File pomFile = new File(depProject, "pom.xml");
        FileUtil.writeText(pomFile, "<project><modelVersion>4.0.0</modelVersion></project>");

        JavaAnalysisFiles files = javaAnalysisAPI.newJavaAnalysisFiles(
                sources, jarFile, pomFile);

        assertEquals(sources.getAbsolutePath(),
                absolutePathes(files.getSourceRoots()));
        assertEquals("""
                        %s/other.jar
                        %s/otherproject/src/main/java"""
                        .formatted(tempDir.getAbsolutePath(), tempDir.getAbsolutePath()),
                absolutePathes(files.getDependencies()));

    }

    @Test
    void newJavaAnalysisFiles$SourceRootsAndFiles(@TempDir File tempDir) {
        File depProject = FileUtil.mkdirs(tempDir, "otherproject");

        JavaAnalysisFiles files = javaAnalysisAPI.newJavaAnalysisFiles(new File[]{tempDir}, depProject);

        assertEquals(tempDir.getAbsolutePath(),
                absolutePathes(files.getSourceRoots()));
        assertEquals(depProject.getAbsolutePath(),
                absolutePathes(files.getDependencies()));
    }

    @Test
    void newJavaAnalysisFiles$Empty() {
        JUnit5Util.assertThrowsWithMessage(
                IllegalArgumentException.class,
                "sourcesToAnalyse must not be empty",
                () -> javaAnalysisAPI.newJavaAnalysisFiles(new File[0], new File("")));
    }

    @Test
    void newProblem() {
        ProblemType problemType = new ProblemTypeTest.ProblemTypeSample();
        File file = new File("Foo.java");

        Problem problem = javaAnalysisAPI.newProblem(
                problemType, file, 123);

        assertEquals(problemType, problem.getProblemType());
        assertEquals(file.getAbsolutePath(), problem.getFile()
                .getAbsolutePath());
        assertEquals(123, problem.getLineNumber());
    }

    /**
     * Returns a newline separated  sorted list of the absolutePaths of
     * the given {@code files}.
     */
    private static String absolutePathes(File[] files) {
        return Arrays.stream(files)
                .map(File::getAbsolutePath)
                .sorted()
                .collect(Collectors.joining("\n"));
    }
}
