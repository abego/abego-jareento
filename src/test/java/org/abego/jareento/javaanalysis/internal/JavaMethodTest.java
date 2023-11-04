package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaMethod;
import org.abego.jareento.javaanalysis.JavaMethodCalls;
import org.abego.jareento.javaanalysis.JavaType;
import org.abego.jareento.javaanalysis.SampleProjectUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.abego.jareento.javaanalysis.JavaMethodCallTest.calledScopesAndMethodSignatureTexts;
import static org.abego.jareento.javaanalysis.JavaMethodCallTest.callingMethodsDeclaratorTexts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaMethodTest {
    @Test
    void smokeTest(@TempDir File tempDir) {
        JavaAnalysisProject project = SampleProjectUtil.setupSampleProject("calls", tempDir);

        JavaMethod meth1 = project.getMethodWithMethodDeclarator(
                "calls.CallsSample$SubA#meth1(java.util.function.Consumer):void");
        JavaType classSubA = project.getTypeWithName("calls.CallsSample$SubA");
        assertEquals("meth1", meth1.getName());
        assertEquals("meth1(java.util.function.Consumer)", meth1.getMethodSignatureText());
        assertEquals("meth1(java.util.function.Consumer)", meth1.getMethodSignature().getText());
        assertEquals("void", meth1.getReturnTypeName());
        assertEquals("calls.CallsSample$SubA", meth1.getTypeName());
        assertEquals(classSubA, meth1.getJavaType());
        assertEquals("calls", meth1.getPackage());
        assertEquals("calls.CallsSample$SubA#meth1(java.util.function.Consumer):void", meth1.getMethodDeclaratorText());
        assertFalse(meth1.isConstructor());
        assertFalse(meth1.isSynthetic());
        assertFalse(meth1.isClassInitializationMethod());
        assertFalse(meth1.isObjectInitializationMethod());
        assertFalse(meth1.isAnnotatedWithOverride());
        assertTrue(meth1.getMethodsDirectlyOverridingMe().isEmpty());
        JavaMethodCalls callsToMe = meth1.getMethodCallsToMe();
        assertEquals("""
                        calls.CallsSample$Main#meth3(calls.CallsSample$SubA, java.util.function.Consumer):void
                        calls.CallsSample$SubA#meth3(calls.CallsSample$SubA, java.util.function.Consumer):void""",
                callingMethodsDeclaratorTexts(callsToMe));
        JavaMethodCalls callsFromMe = meth1.getMethodCallsFromMe();
        assertEquals("calls.CallsSample$SubA#meth1(java.util.function.Consumer):void",
                callingMethodsDeclaratorTexts(callsFromMe));
        assertEquals("java.util.function.Consumer#accept(java.lang.Object)",
                calledScopesAndMethodSignatureTexts(callsFromMe));
    }
}
