package org.abego.jareento.javaanalysis.internal.input.jdeps;

import org.abego.commons.io.FileUtil;
import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaTypes;
import org.abego.jareento.javaanalysis.internal.JavaAnalysisProjectImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InputFromJDepsTest {
    @Test
    void smokeTest(@TempDir File tempDir) {
        File dotFile = new File(tempDir, "jdeps.dot");
        File storage = new File(tempDir, "storage");
        FileUtil.copyResourceToFile(getClass(),
                "/org/abego/jareento/javaanalysis/internal/input/jdeps/jdeps-sample.dot",
                dotFile);

        Consumer<String> problemConsumer = p -> {};
        InputFromJDeps input = InputFromJDeps.newInputFromJDeps(dotFile);
        JavaAnalysisProject project =
                JavaAnalysisProjectImpl.newJavaAnalysisProjectFromInput(
                        storage.toURI(),
                        input,
                        new File[0],
                        new File[0],
                        problemConsumer);
        JavaTypes types = project.getTypes();

        assertEquals("""
                        java.lang.Class
                        java.lang.Object
                        java.lang.invoke.MethodHandles$Lookup
                        java.util.NoSuchElementException
                        org.abego.commons.annotation.SPI
                        org.abego.commons.blackboard.Blackboard
                        org.abego.commons.blackboard.BlackboardDefault
                        org.abego.commons.diff.FileDiffUtil$DirectoryDifferencesOptions
                        org.abego.commons.diff.internal.DiffImpl$1
                        org.abego.commons.diff.internal.DiffImpl$1$1
                        org.abego.commons.seq.AbstractSeq
                        org.abego.commons.seq.Seq""",
                types.idStream().sorted().collect(Collectors.joining("\n")));
    }

    @ParameterizedTest
    @CsvSource({
            "ClassName,                             ClassName,                  ClassName,      '',          false",
            "com.example.ClassName,                 com.example.ClassName,      ClassName,      com.example, false",
            "com.example.Class$Name,                com.example.Class$Name,     Class$Name,     com.example, false",
            "com.example.package-info,              com.example.package-info,   package-info,   com.example, true",
            "com.example.ClassName (example.jar),   com.example.ClassName,      ClassName,      com.example, false",
    })
    void parsedNode(String text, String name, String simpleName,
                    String packagePath, boolean isPackageInfo) {
        InputFromJDeps.ParsedNodeInfo parsedNode = InputFromJDeps.ParsedNodeInfo.parseNode(text);

        assertEquals(name, parsedNode.getName());
        assertEquals(simpleName, parsedNode.getSimpleName());
        assertEquals(packagePath, parsedNode.getPackagePath());
        assertEquals(isPackageInfo, parsedNode.isPackageInfo());
    }
}
