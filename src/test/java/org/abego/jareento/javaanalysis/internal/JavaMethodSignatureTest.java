package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaMethodSignature;
import org.abego.jareento.javaanalysis.JavaMethodSignatures;
import org.abego.jareento.javaanalysis.JavaType;
import org.abego.jareento.javaanalysis.SampleProjectUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaMethodSignatureTest {

    /**
     * Returns the texts of the given {@code signatures}.
     * sorted and separated by newlines.
     */
    public static String javaMethodSignaturesText(JavaMethodSignatures signatures) {
        return signatures.stream()
                .map(JavaMethodSignature::getText)
                .sorted()
                .collect(Collectors.joining("\n"));
    }
    
    @Test
    void intersectedWith(@TempDir File tempDir) {
        JavaAnalysisProject project = SampleProjectUtil.setupSampleProject("calls", tempDir);

        JavaType classRoot = project.getTypeWithName("calls.CallsSample$Root");
        JavaType classSubA = project.getTypeWithName("calls.CallsSample$SubA");

        JavaMethodSignatures classRootMethodSignatures = classRoot.getMethodSignatures();
        JavaMethodSignatures classSubAMethodSignatures = classSubA.getMethodSignatures();

        JavaMethodSignatures signatures = classRootMethodSignatures.intersectedWith(classSubAMethodSignatures);
        assertEquals(
                "meth1(java.util.function.Consumer)",
                javaMethodSignaturesText(signatures));
    }
}
