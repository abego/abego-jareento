package org.abego.jareento.javaanalysis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaMethodCallTest {

    @Test
    void smokeTest(@TempDir File tempDir) {
        JavaAnalysisProject project = SampleProjectUtil.setupSampleProject("calls", tempDir);

        JavaMethod meth1 = project.getMethodWithMethodDeclarator(
                "calls.CallsSample$SubA#meth1(java.util.function.Consumer):void");

        JavaMethodCalls calls = meth1.getMethodCallsFromMe();

        assertEquals(1, calls.getSize());

        JavaMethodCall call = calls.iterator().next();

        assertEquals("accept(java.lang.Object)", call.getMethodSignature().getText());
        assertEquals("java.util.function.Consumer", call.getScope());
        assertEquals("java.util.function.Consumer", call.getBaseScope());
        assertEquals(meth1, call.getCallingMethod());
        assertEquals("calls.CallsSample$SubA", call.getCallingTypeName());
        JavaType classSubA = project.getTypeWithName("calls.CallsSample$SubA");
        assertEquals(classSubA, call.getCallingType());
    }


}
