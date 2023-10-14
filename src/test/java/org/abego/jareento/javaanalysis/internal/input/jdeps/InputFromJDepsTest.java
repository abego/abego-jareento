package org.abego.jareento.javaanalysis.internal.input.jdeps;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InputFromJDepsTest {

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
