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
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
                        com.example.inheritance.Base$Base_InnerClass."<init>"() - invokespecial-com.example.inheritance.Sub1$Sub1_InnerClass#com.example.inheritance.Sub1$Sub1_InnerClass():@1
                        com.example.inheritance.Base$Base_InnerClass.innerMethodBase1(java.lang.String) - invokespecial-com.example.inheritance.Sub1$Sub1_InnerClass#innerMethodBase1(java.lang.String):void@2
                        com.example.inheritance.Base."<init>"() - invokespecial-com.example.inheritance.Sub1#com.example.inheritance.Sub1():@1
                        com.example.inheritance.Base.methodBase1(java.lang.String) - invokespecial-com.example.inheritance.Sub1#methodBase1(java.lang.String):void@2
                        com.example.inheritance.Base.methodBase1(java.lang.String) - invokevirtual-com.example.inheritance.Main#entry(com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2):void@3
                        com.example.inheritance.Base.methodBase2(java.lang.String) - invokevirtual-com.example.inheritance.Main#entry(com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2):void@9
                        com.example.inheritance.InterfaceA.methodInterfaceA() - invokeinterface-com.example.inheritance.Main#entry(com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2):void@13
                        com.example.inheritance.Sub1.methodBase1(java.lang.String) - invokevirtual-com.example.inheritance.Main#entry(com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2):void@21
                        com.example.inheritance.Sub1.methodBase2(java.lang.String) - invokevirtual-com.example.inheritance.Main#entry(com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2):void@27
                        com.example.inheritance.Sub1.methodSub1() - invokevirtual-com.example.inheritance.Main#entry(com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2):void@31
                        com.example.inheritance.Sub2.methodInterfaceA() - invokevirtual-com.example.inheritance.Main#entry(com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2):void@39
                        com.example.inheritance.Sub2.methodSub2() - invokevirtual-com.example.inheritance.Main#entry(com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2):void@35
                        com.example.sample2.SynBase."<init>"() - invokespecial-com.example.sample2.SynSub#com.example.sample2.SynSub():@1
                        java.lang.Integer.valueOf(int) - invokestatic-com.example.sample2.SynSub#value():java.lang.Integer@1
                        java.lang.Object."<init>"() - invokespecial-com.example.inheritance.Base#com.example.inheritance.Base():@1
                        java.lang.Object."<init>"() - invokespecial-com.example.inheritance.Base$Base_InnerClass#com.example.inheritance.Base$Base_InnerClass():@1
                        java.lang.Object."<init>"() - invokespecial-com.example.inheritance.Main#com.example.inheritance.Main():@1
                        java.lang.Object."<init>"() - invokespecial-com.example.inheritance.Sub2#com.example.inheritance.Sub2():@1
                        java.lang.Object."<init>"() - invokespecial-com.example.sample2.SynBase#com.example.sample2.SynBase():@1""",
                sortedUnixLines(actual));
    }

    @Test
    void methodCallsToNonOverridingClass(@TempDir File tempDir) {
        String actual = methodCallsTestHelper(tempDir, "javap-CallsSample.txt");

        assertEquals("""
                        java.lang.Object."<init>"() - invokespecial-org.abego.javaanalysis.sample.calls.CallsSample#org.abego.javaanalysis.sample.calls.CallsSample():@1
                        java.lang.Object."<init>"() - invokespecial-org.abego.javaanalysis.sample.calls.CallsSample$Main#org.abego.javaanalysis.sample.calls.CallsSample$Main():@1
                        java.lang.Object."<init>"() - invokespecial-org.abego.javaanalysis.sample.calls.CallsSample$Root#org.abego.javaanalysis.sample.calls.CallsSample$Root():@1
                        java.util.function.Consumer.accept(java.lang.Object) - invokeinterface-org.abego.javaanalysis.sample.calls.CallsSample$Root#meth1(java.util.function.Consumer):void@3
                        java.util.function.Consumer.accept(java.lang.Object) - invokeinterface-org.abego.javaanalysis.sample.calls.CallsSample$Root#meth2(java.util.function.Consumer):void@3
                        java.util.function.Consumer.accept(java.lang.Object) - invokeinterface-org.abego.javaanalysis.sample.calls.CallsSample$SubA#meth1(java.util.function.Consumer):void@3
                        org.abego.javaanalysis.sample.calls.CallsSample$Root."<init>"() - invokespecial-org.abego.javaanalysis.sample.calls.CallsSample$SubA#org.abego.javaanalysis.sample.calls.CallsSample$SubA():@1
                        org.abego.javaanalysis.sample.calls.CallsSample$Root.meth1(java.util.function.Consumer) - invokevirtual-org.abego.javaanalysis.sample.calls.CallsSample$Main#meth4(org.abego.javaanalysis.sample.calls.CallsSample$Root, java.util.function.Consumer):void@2
                        org.abego.javaanalysis.sample.calls.CallsSample$Root.meth1(java.util.function.Consumer) - invokevirtual-org.abego.javaanalysis.sample.calls.CallsSample$SubA#meth4(org.abego.javaanalysis.sample.calls.CallsSample$Root, java.util.function.Consumer):void@2
                        org.abego.javaanalysis.sample.calls.CallsSample$Root.meth2(java.util.function.Consumer) - invokevirtual-org.abego.javaanalysis.sample.calls.CallsSample$Main#meth4(org.abego.javaanalysis.sample.calls.CallsSample$Root, java.util.function.Consumer):void@7
                        org.abego.javaanalysis.sample.calls.CallsSample$Root.meth2(java.util.function.Consumer) - invokevirtual-org.abego.javaanalysis.sample.calls.CallsSample$SubA#meth3(org.abego.javaanalysis.sample.calls.CallsSample$SubA, java.util.function.Consumer):void@7
                        org.abego.javaanalysis.sample.calls.CallsSample$Root.meth2(java.util.function.Consumer) - invokevirtual-org.abego.javaanalysis.sample.calls.CallsSample$SubA#meth4(org.abego.javaanalysis.sample.calls.CallsSample$Root, java.util.function.Consumer):void@7
                        org.abego.javaanalysis.sample.calls.CallsSample$SubA.meth1(java.util.function.Consumer) - invokevirtual-org.abego.javaanalysis.sample.calls.CallsSample$Main#meth3(org.abego.javaanalysis.sample.calls.CallsSample$SubA, java.util.function.Consumer):void@2
                        org.abego.javaanalysis.sample.calls.CallsSample$SubA.meth1(java.util.function.Consumer) - invokevirtual-org.abego.javaanalysis.sample.calls.CallsSample$SubA#meth3(org.abego.javaanalysis.sample.calls.CallsSample$SubA, java.util.function.Consumer):void@2
                        org.abego.javaanalysis.sample.calls.CallsSample$SubA.meth2(java.util.function.Consumer) - invokevirtual-org.abego.javaanalysis.sample.calls.CallsSample$Main#meth3(org.abego.javaanalysis.sample.calls.CallsSample$SubA, java.util.function.Consumer):void@7
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
                org.abego.javaanalysis.sample.calls.CallsSample$Root#meth1(java.util.function.Consumer):void:
                  - invokevirtual-org.abego.javaanalysis.sample.calls.CallsSample$Main#meth4(org.abego.javaanalysis.sample.calls.CallsSample$Root, java.util.function.Consumer):void@2
                  - invokevirtual-org.abego.javaanalysis.sample.calls.CallsSample$SubA#meth4(org.abego.javaanalysis.sample.calls.CallsSample$Root, java.util.function.Consumer):void@2
                org.abego.javaanalysis.sample.calls.CallsSample$Root#meth2(java.util.function.Consumer):void:
                  - invokevirtual-org.abego.javaanalysis.sample.calls.CallsSample$Main#meth3(org.abego.javaanalysis.sample.calls.CallsSample$SubA, java.util.function.Consumer):void@7
                  - invokevirtual-org.abego.javaanalysis.sample.calls.CallsSample$Main#meth4(org.abego.javaanalysis.sample.calls.CallsSample$Root, java.util.function.Consumer):void@7
                  - invokevirtual-org.abego.javaanalysis.sample.calls.CallsSample$SubA#meth3(org.abego.javaanalysis.sample.calls.CallsSample$SubA, java.util.function.Consumer):void@7
                  - invokevirtual-org.abego.javaanalysis.sample.calls.CallsSample$SubA#meth4(org.abego.javaanalysis.sample.calls.CallsSample$Root, java.util.function.Consumer):void@7
                org.abego.javaanalysis.sample.calls.CallsSample$Root#org.abego.javaanalysis.sample.calls.CallsSample$Root()::
                """, rootCalls);

        String subACalls = reportMethodCalls(project, "org.abego.javaanalysis.sample.calls.CallsSample$SubA", m -> true);
        assertEquals("""
                org.abego.javaanalysis.sample.calls.CallsSample$SubA#meth1(java.util.function.Consumer):void:
                  - invokevirtual-org.abego.javaanalysis.sample.calls.CallsSample$Main#meth3(org.abego.javaanalysis.sample.calls.CallsSample$SubA, java.util.function.Consumer):void@2
                  - invokevirtual-org.abego.javaanalysis.sample.calls.CallsSample$SubA#meth3(org.abego.javaanalysis.sample.calls.CallsSample$SubA, java.util.function.Consumer):void@2
                org.abego.javaanalysis.sample.calls.CallsSample$SubA#meth3(org.abego.javaanalysis.sample.calls.CallsSample$SubA, java.util.function.Consumer):void:
                org.abego.javaanalysis.sample.calls.CallsSample$SubA#meth4(org.abego.javaanalysis.sample.calls.CallsSample$Root, java.util.function.Consumer):void:
                org.abego.javaanalysis.sample.calls.CallsSample$SubA#org.abego.javaanalysis.sample.calls.CallsSample$SubA()::
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
                "<init>"()
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

    private static void dumpMethodsWithMultipleReturnTypes(
            JavaAnalysisProject project, String fileName) throws FileNotFoundException {
        PrintStream out = new PrintStream(fileName);
        Function<String, String> keyProvider = id -> project.classOfMethod(id) + "." + project.signatureOfMethod(id);
        Map<String, List<String>> result = project.methods().idStream()
                .collect(Collectors.groupingBy(keyProvider));
        for (Map.Entry<String, List<String>> e : result.entrySet()) {
            if (e.getValue().size() > 1) {
                out.println(e.getKey() + ":");
                for (String s : e.getValue()) {
                    out.println("  - " + s);
                }
            }
        }
        out.close();
    }

}
