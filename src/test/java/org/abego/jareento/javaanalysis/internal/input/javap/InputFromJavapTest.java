package org.abego.jareento.javaanalysis.internal.input.javap;

import org.abego.commons.io.FileUtil;
import org.abego.commons.io.PrintStreamToBuffer;
import org.abego.jareento.javaanalysis.JavaAnalysisAPI;
import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaAnalysisProjectUtil;
import org.abego.jareento.javaanalysis.JavaMethodCalls;
import org.abego.jareento.javaanalysis.internal.JavaAnalysisInternalFactories;
import org.abego.jareento.javaanalysis.internal.JavaAnalysisProjectStateBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;
import static org.abego.commons.lang.StringUtil.sortedUnixLines;
import static org.abego.commons.lang.StringUtil.unixString;
import static org.abego.commons.util.LoggerUtil.logStringsAsWarnings;
import static org.abego.commons.util.ServiceLoaderUtil.loadService;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InputFromJavapTest {
    private static final Logger LOGGER = getLogger(InputFromJavapTest.class.getName());
    private final JavaAnalysisAPI javaAnalysisAPI = loadService(JavaAnalysisAPI.class);

    @Test
    void methodCalls(@TempDir File tempDir) {
        String actual = methodCallsTestHelper(tempDir, "javap-sample1.txt");

        assertEquals("""
                        com.example.inheritance.Base$Base_InnerClass.Base$Base_InnerClass() - invokespecial-com.example.inheritance.Sub1$Sub1_InnerClass#Sub1$Sub1_InnerClass():void@1
                        com.example.inheritance.Base$Base_InnerClass.innerMethodBase1(java.lang.String) - invokespecial-com.example.inheritance.Sub1$Sub1_InnerClass#innerMethodBase1(java.lang.String):void@2
                        com.example.inheritance.Base.Base() - invokespecial-com.example.inheritance.Sub1#Sub1():void@1
                        com.example.inheritance.Base.methodBase1(java.lang.String) - invokespecial-com.example.inheritance.Sub1#methodBase1(java.lang.String):void@2
                        com.example.inheritance.Base.methodBase1(java.lang.String) - invokevirtual-com.example.inheritance.Main#entry(com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2):void@3
                        com.example.inheritance.Base.methodBase2(java.lang.String) - invokevirtual-com.example.inheritance.Main#entry(com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2):void@9
                        com.example.inheritance.InterfaceA.methodInterfaceA() - invokeinterface-com.example.inheritance.Main#entry(com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2):void@13
                        com.example.inheritance.Sub1.methodBase1(java.lang.String) - invokevirtual-com.example.inheritance.Main#entry(com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2):void@21
                        com.example.inheritance.Sub1.methodBase2(java.lang.String) - invokevirtual-com.example.inheritance.Main#entry(com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2):void@27
                        com.example.inheritance.Sub1.methodSub1() - invokevirtual-com.example.inheritance.Main#entry(com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2):void@31
                        com.example.inheritance.Sub2.methodInterfaceA() - invokevirtual-com.example.inheritance.Main#entry(com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2):void@39
                        com.example.inheritance.Sub2.methodSub2() - invokevirtual-com.example.inheritance.Main#entry(com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2):void@35
                        com.example.sample2.SynBase.SynBase() - invokespecial-com.example.sample2.SynSub#SynSub():void@1
                        java.lang.Integer.valueOf(int) - invokestatic-com.example.sample2.SynSub#value():java.lang.Integer@1
                        java.lang.Object.Object() - invokespecial-com.example.inheritance.Base#Base():void@1
                        java.lang.Object.Object() - invokespecial-com.example.inheritance.Base$Base_InnerClass#Base$Base_InnerClass():void@1
                        java.lang.Object.Object() - invokespecial-com.example.inheritance.Main#Main():void@1
                        java.lang.Object.Object() - invokespecial-com.example.inheritance.Sub2#Sub2():void@1
                        java.lang.Object.Object() - invokespecial-com.example.sample2.SynBase#SynBase():void@1""",
                sortedUnixLines(actual));
    }

    @Test
    void methodCallsToNonOverridingClass(@TempDir File tempDir) {
        String actual = methodCallsTestHelper(tempDir, "javap-CallsSample.txt");

        assertEquals("""
                        calls.CallsSample$Root.CallsSample$Root() - invokespecial-calls.CallsSample$SubA#CallsSample$SubA():void@1
                        calls.CallsSample$Root.meth1(java.util.function.Consumer) - invokevirtual-calls.CallsSample$Main#meth4(calls.CallsSample$Root, java.util.function.Consumer):void@2
                        calls.CallsSample$Root.meth1(java.util.function.Consumer) - invokevirtual-calls.CallsSample$SubA#meth4(calls.CallsSample$Root, java.util.function.Consumer):void@2
                        calls.CallsSample$Root.meth2(java.util.function.Consumer) - invokevirtual-calls.CallsSample$Main#meth4(calls.CallsSample$Root, java.util.function.Consumer):void@7
                        calls.CallsSample$Root.meth2(java.util.function.Consumer) - invokevirtual-calls.CallsSample$SubA#meth4(calls.CallsSample$Root, java.util.function.Consumer):void@7
                        calls.CallsSample$SubA.meth1(java.util.function.Consumer) - invokevirtual-calls.CallsSample$Main#meth3(calls.CallsSample$SubA, java.util.function.Consumer):void@2
                        calls.CallsSample$SubA.meth1(java.util.function.Consumer) - invokevirtual-calls.CallsSample$SubA#meth3(calls.CallsSample$SubA, java.util.function.Consumer):void@2
                        calls.CallsSample$SubA.meth2(java.util.function.Consumer) - invokevirtual-calls.CallsSample$Main#meth3(calls.CallsSample$SubA, java.util.function.Consumer):void@7
                        calls.CallsSample$SubA.meth2(java.util.function.Consumer) - invokevirtual-calls.CallsSample$SubA#meth3(calls.CallsSample$SubA, java.util.function.Consumer):void@7
                        java.lang.Object.Object() - invokespecial-calls.CallsSample#CallsSample():void@1
                        java.lang.Object.Object() - invokespecial-calls.CallsSample$Main#CallsSample$Main():void@1
                        java.lang.Object.Object() - invokespecial-calls.CallsSample$Root#CallsSample$Root():void@1
                        java.util.function.Consumer.accept(java.lang.Object) - invokeinterface-calls.CallsSample$Root#meth1(java.util.function.Consumer):void@3
                        java.util.function.Consumer.accept(java.lang.Object) - invokeinterface-calls.CallsSample$Root#meth2(java.util.function.Consumer):void@3
                        java.util.function.Consumer.accept(java.lang.Object) - invokeinterface-calls.CallsSample$SubA#meth1(java.util.function.Consumer):void@3
                        """,
                unixString(actual));
    }

    private String methodCallsTestHelper(File tempDir, String resourceName) {
        JavaAnalysisProject project = createProjectUsingJavapData(tempDir, resourceName);

        PrintStreamToBuffer out = PrintStreamToBuffer.newPrintStreamToBuffer();
        project.methodCalls().idStream()
                .sorted(Comparator.comparing(id -> project.scopeOfMethodCall(id) + "." + project.signatureOfMethodCall(id) + " - " + id))
                .forEach(id -> out.println(project.scopeOfMethodCall(id) + "." + project.signatureOfMethodCall(id) + " - " + id));
        out.close();

        return out.text();
    }

    private JavaAnalysisProject createProjectUsingJavapData(File tempDir, String resourceName) {
        File file = new File(tempDir, "sample.txt");
        FileUtil.copyResourceToFile(getClass(), resourceName, file);

        JavaAnalysisProjectStateBuilder builder =
                JavaAnalysisInternalFactories.newJavaAnalysisProjectBuilder(file.toURI());

        InputFromJavap.newInputFromJavap(file, new File[0], s -> {}, javaAnalysisAPI)
                .feed(builder, logStringsAsWarnings(LOGGER));

        return JavaAnalysisInternalFactories.newJavaAnalysisProject(builder.build());
    }

    @Test
    void withMethodCallsToMethodsOfClassDoTest(@TempDir File tempDir) {
        JavaAnalysisProject project = createProjectUsingJavapData(tempDir, "javap-CallsSample.txt");

        String rootCalls = reportMethodCalls(project, "calls.CallsSample$Root", m -> true);
        assertEquals("""
                calls.CallsSample$Root#CallsSample$Root():void:
                  - invokespecial-calls.CallsSample$SubA#CallsSample$SubA():void@1
                calls.CallsSample$Root#meth1(java.util.function.Consumer):void:
                  - invokevirtual-calls.CallsSample$Main#meth4(calls.CallsSample$Root, java.util.function.Consumer):void@2
                  - invokevirtual-calls.CallsSample$SubA#meth4(calls.CallsSample$Root, java.util.function.Consumer):void@2
                calls.CallsSample$Root#meth2(java.util.function.Consumer):void:
                  - invokevirtual-calls.CallsSample$Main#meth3(calls.CallsSample$SubA, java.util.function.Consumer):void@7
                  - invokevirtual-calls.CallsSample$Main#meth4(calls.CallsSample$Root, java.util.function.Consumer):void@7
                  - invokevirtual-calls.CallsSample$SubA#meth3(calls.CallsSample$SubA, java.util.function.Consumer):void@7
                  - invokevirtual-calls.CallsSample$SubA#meth4(calls.CallsSample$Root, java.util.function.Consumer):void@7
                """, rootCalls);

        String subACalls = reportMethodCalls(project, "calls.CallsSample$SubA", m -> true);
        assertEquals("""
                calls.CallsSample$SubA#CallsSample$SubA():void:
                calls.CallsSample$SubA#meth1(java.util.function.Consumer):void:
                  - invokevirtual-calls.CallsSample$Main#meth3(calls.CallsSample$SubA, java.util.function.Consumer):void@2
                  - invokevirtual-calls.CallsSample$SubA#meth3(calls.CallsSample$SubA, java.util.function.Consumer):void@2
                calls.CallsSample$SubA#meth3(calls.CallsSample$SubA, java.util.function.Consumer):void:
                calls.CallsSample$SubA#meth4(calls.CallsSample$Root, java.util.function.Consumer):void:
                """, subACalls);
    }

    @Test
    void calledMethods(@TempDir File tempDir) {
        JavaAnalysisProject project = createProjectUsingJavapData(tempDir, "javap-CallsSample.txt");

        StringBuilder result = new StringBuilder();
        project.methodsOfClass("calls.CallsSample$Main")
                .idStream()
                .forEach(methodId -> {
                    String calledMethods = JavaAnalysisProjectUtil.
                            calledMethodsSummary(project, methodId);
                    result.append(calledMethods);
                    result.append("\n");
                });
        assertEquals("""
                meth1(java.util.function.Consumer);meth2(java.util.function.Consumer)
                meth1(java.util.function.Consumer);meth2(java.util.function.Consumer)
                Object()
                """, result.toString());
    }

    private static String reportMethodCalls(JavaAnalysisProject project, String className, Predicate<JavaMethodCalls> reportPredicate) {
        StringBuilder result = new StringBuilder();
        project.withMethodCallsToMethodsOfClassDo(className, (calledMethodId, methodCalls) -> {
            if (reportPredicate.test(methodCalls)) {
                result.append(calledMethodId);
                result.append(":\n");
                methodCalls.idStream().sorted()
                        .forEach(m -> result
                                .append("  - ")
                                .append(m)
                                .append("\n"));
            }
        });
        return result.toString();
    }

}
