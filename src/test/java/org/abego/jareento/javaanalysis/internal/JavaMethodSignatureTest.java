package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaMethod;
import org.abego.jareento.javaanalysis.JavaMethodCallTest;
import org.abego.jareento.javaanalysis.JavaMethodCalls;
import org.abego.jareento.javaanalysis.JavaMethodSignature;
import org.abego.jareento.javaanalysis.JavaMethodSignatures;
import org.abego.jareento.javaanalysis.JavaType;
import org.abego.jareento.javaanalysis.JavaTypes;
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

    @Test
    void getTypesWithMethod(@TempDir File tempDir) {
        JavaAnalysisProject project = SampleProjectUtil.setupSampleProject("calls", tempDir);

        JavaMethod meth1Method = project.getMethodWithMethodDeclarator(
                "calls.CallsSample$SubA#meth1(java.util.function.Consumer):void");
        JavaMethodSignature meth1Signature = meth1Method.getMethodSignature();

        JavaTypes typesWithMeth1 = meth1Signature.getTypesWithMethod();

        assertEquals("""
                        calls.CallsSample$Root
                        calls.CallsSample$SubA""",
                JavaTypeTest.javaTypesText(typesWithMeth1));
    }

    @Test
    void getMethodCalls(@TempDir File tempDir) {
        JavaAnalysisProject project = SampleProjectUtil.setupSampleProject("calls", tempDir);

        JavaMethod meth1Method = project.getMethodWithMethodDeclarator(
                "calls.CallsSample$SubA#meth1(java.util.function.Consumer):void");
        JavaMethodSignature meth1Signature = meth1Method.getMethodSignature();

        JavaMethodCalls callsToMeth1 = meth1Signature.getMethodCalls();

        assertEquals("""
                        calls.CallsSample$Main#meth3(calls.CallsSample$SubA, java.util.function.Consumer):void
                        calls.CallsSample$Main#meth4(calls.CallsSample$Root, java.util.function.Consumer):void
                        calls.CallsSample$SubA#meth3(calls.CallsSample$SubA, java.util.function.Consumer):void
                        calls.CallsSample$SubA#meth4(calls.CallsSample$Root, java.util.function.Consumer):void""",
                JavaMethodCallTest.callingMethodsDeclaratorTexts(callsToMeth1));
    }

    @Test
    void getMethodCallsToType(@TempDir File tempDir) {
        JavaAnalysisProject project = SampleProjectUtil.setupSampleProject("calls", tempDir);

        JavaMethod meth1Method = project.getMethodWithMethodDeclarator(
                "calls.CallsSample$SubA#meth1(java.util.function.Consumer):void");
        JavaMethodSignature meth1Signature = meth1Method.getMethodSignature();

        JavaMethodCalls callsToMeth1 = meth1Signature.getMethodCallsToType("calls.CallsSample$SubA");

        assertEquals("""
                        calls.CallsSample$Main#meth3(calls.CallsSample$SubA, java.util.function.Consumer):void
                        calls.CallsSample$SubA#meth3(calls.CallsSample$SubA, java.util.function.Consumer):void""",
                JavaMethodCallTest.callingMethodsDeclaratorTexts(callsToMeth1));
    }
}
