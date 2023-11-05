package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaMethodCalls;
import org.abego.jareento.javaanalysis.JavaMethodSignatures;
import org.abego.jareento.javaanalysis.JavaType;
import org.abego.jareento.javaanalysis.JavaTypes;
import org.abego.jareento.javaanalysis.SampleProjectUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.stream.Collectors;

import static org.abego.jareento.javaanalysis.JavaMethodCallTest.callingMethodsDeclaratorTexts;
import static org.abego.jareento.javaanalysis.internal.JavaMethodSignatureTest.javaMethodSignaturesText;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class JavaTypeTest {
    public static String javaTypesText(JavaTypes types) {
        return types.stream().map(JavaType::getName).sorted()
                .collect(Collectors.joining("\n"));
    }

    @Test
    void smokeTest(@TempDir File tempDir) {
        JavaAnalysisProject project = SampleProjectUtil.setupSampleProject("calls", tempDir);

        JavaType classRoot = project.getTypeWithName("calls.CallsSample$Root");
        JavaType classSubA = project.getTypeWithName("calls.CallsSample$SubA");

        assertFalse(classSubA.isInterface());
        assertEquals("calls.CallsSample$SubA", classSubA.getName());
        assertEquals("CallsSample$SubA", classSubA.getSimpleName());

        assertEquals(classRoot, classSubA.getSuperType());
        JavaTypes classRootSubTypes = classRoot.getSubTypes();
        assertEquals(1, classRootSubTypes.getSize());
        assertEquals(classSubA, classRootSubTypes.iterator().next());
        JavaTypes classRootSubTypesAndType = classRoot.getSubTypesAndType();
        assertEquals(2, classRootSubTypesAndType.getSize());
        JavaTypes classRootAllSubTypes = classRoot.getAllSubTypes();
        assertEquals(1, classRootAllSubTypes.getSize());
        JavaTypes classRootAllSubTypesAndType = classRoot.getAllSubTypesAndType();
        assertEquals(2, classRootAllSubTypesAndType.getSize());

        JavaTypes classSubASubTypes = classSubA.getSubTypes();
        assertEquals(0, classSubASubTypes.getSize());

        JavaTypes classSubAImplementedInterfaces = classSubA.getImplementedInterfaces();
        assertEquals(0, classSubAImplementedInterfaces.getSize());

        JavaTypes classSubAExtendedTypes = classSubA.getExtendedTypes();
        assertEquals(1, classSubAExtendedTypes.getSize());
        assertEquals(classRoot, classSubAExtendedTypes.iterator().next());

        JavaTypes classSubAReferencingTypes = classSubA.getReferencingTypes();
        assertEquals(0, classSubAReferencingTypes.getSize());

        JavaMethodSignatures classSubAMethodSignatures = classSubA.getMethodSignatures();
        assertEquals("""
                        CallsSample$SubA()
                        meth1(java.util.function.Consumer)
                        meth3(calls.CallsSample$SubA, java.util.function.Consumer)
                        meth4(calls.CallsSample$Root, java.util.function.Consumer)""",
                javaMethodSignaturesText(classSubAMethodSignatures));
        JavaMethodSignatures classSubAInheritedMethodSignatures = classSubA.getInheritedMethodSignatures();
        assertEquals("""
                        CallsSample$Root()
                        clone()
                        equals(java.lang.Object)
                        finalize()
                        getClass()
                        hashCode()
                        meth1(java.util.function.Consumer)
                        meth2(java.util.function.Consumer)
                        notify()
                        notifyAll()
                        toString()
                        wait()
                        wait(long)
                        wait(long, int)
                        wait(long,int)""",
                javaMethodSignaturesText(classSubAInheritedMethodSignatures));

        JavaMethodCalls callsToSubAMeth1 = classSubA.getMethodCallsToTypeWithSignature(
                "meth1(java.util.function.Consumer)");
        assertEquals("""
                        calls.CallsSample$Main#meth3(calls.CallsSample$SubA, java.util.function.Consumer):void
                        calls.CallsSample$SubA#meth3(calls.CallsSample$SubA, java.util.function.Consumer):void""",
                callingMethodsDeclaratorTexts(callsToSubAMeth1));
    }

    @Test
    void getMethodSignatures(@TempDir File tempDir) {
        JavaAnalysisProject project = SampleProjectUtil.setupSampleProject("calls", tempDir);
        JavaType classRoot = project.getTypeWithName("calls.CallsSample$Root");
        JavaTypes subTypes = classRoot.getSubTypes();

        JavaMethodSignatures signatures = subTypes.getMethodSignatures();

        assertEquals("""
                        CallsSample$SubA()
                        meth1(java.util.function.Consumer)
                        meth3(calls.CallsSample$SubA, java.util.function.Consumer)
                        meth4(calls.CallsSample$Root, java.util.function.Consumer)""",
                javaMethodSignaturesText(signatures));
    }

    @Test
    void intersectedWith(@TempDir File tempDir) {
        JavaAnalysisProject project = SampleProjectUtil.setupSampleProject("calls", tempDir);
        JavaType classRoot = project.getTypeWithName("calls.CallsSample$Root");
        JavaTypes subTypes = classRoot.getSubTypes();

        JavaTypes types = subTypes.intersectedWith(classRoot.getSubTypesAndType());

        assertEquals("calls.CallsSample$SubA",
                javaTypesText(types));
    }
    
    @Test
    void equalsTest(@TempDir File tempDir) {
        JavaAnalysisProject project = SampleProjectUtil.setupSampleProject("calls", tempDir);
        JavaType classRoot = project.getTypeWithName("calls.CallsSample$Root");
        JavaTypes subTypes = classRoot.getSubTypes();
        JavaTypes subTypes2 = classRoot.getSubTypes();

        assertEquals(subTypes, subTypes);
        assertEquals(subTypes, subTypes2);
        assertNotEquals(subTypes, null);

        assertEquals(subTypes.hashCode(), subTypes2.hashCode());
    }
}
