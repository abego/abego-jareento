package org.abego.jareento.javarefactoring.internal;

import org.abego.commons.io.FileUtil;
import org.abego.commons.lang.StringUtil;
import org.abego.jareento.javaanalysis.JavaAnalysisAPI;
import org.abego.jareento.javaanalysis.internal.JavaAnalysisFiles;
import org.abego.jareento.javarefactoring.JavaRefactoringAPI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.abego.commons.lang.StringUtil.sortedUnixLines;
import static org.abego.commons.lang.StringUtil.unixString;
import static org.abego.commons.util.ServiceLoaderUtil.loadService;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RemoveMethodAnnotationsTest {
    private final JavaAnalysisAPI javaAnalysisAPI = loadService(JavaAnalysisAPI.class);
    private final JavaRefactoringAPI javaRefactoring = loadService(JavaRefactoringAPI.class);

    @Test
    void smokeTest(@TempDir File tempDir) {

        TestData.copySampleProjectTo(tempDir);

        JavaAnalysisFiles javaAnalysisFiles = 
                javaAnalysisAPI.newJavaAnalysisFiles(tempDir);

        StringBuilder listenerLog = new StringBuilder();

        // remove all @Override annotations, 
        javaRefactoring.removeMethodAnnotations(
                javaAnalysisFiles,
                md -> md.getAnnotationText().equals("@Override"),
                f -> true, md -> listenerLog.append(
                        String.format("removing %s of %s.%s - %s.%s - %s(%s)- %s\n",
                                md.getAnnotationText(),
                                md.getTypeDeclaringMethod(),
                                md.getMethodSignature(),
                                md.getMethodPackageName(),
                                md.getMethodName(),
                                md.getQualifiedMethodName(),
                                StringUtil.join(", ", (Object[]) md.getMethodParameterTypeNames()),
                                md)),
                s -> {});

        assertEquals("""
                package com.example;

                public class Base {
                    public static class Base_InnerClass {
                        void innerMethodBase1(String a) {
                        }

                        void innerMethodBase2(String a) {
                        }
                    }

                    void methodBase1(String a) {
                    }

                    void methodBase2(String a) {
                    }
                }
                """, unixString(FileUtil.textOf(new File(tempDir, "com/example/Base.java"))));
        assertEquals("""
                package com.example;

                public interface InterfaceA {
                    void methodInterfaceA();
                }
                """, unixString(FileUtil.textOf(new File(tempDir, "com/example/InterfaceA.java"))));
        assertEquals("""
                package com.example;

                public class Main {
                    public static void entry(Base b, InterfaceA ia, Sub1 s1, Sub2 s2) {
                        b.methodBase1("");
                        b.methodBase2("");

                        ia.methodInterfaceA();

                        s1.methodBase1("");
                        s1.methodBase2("");
                        s1.methodSub1();

                        s2.methodSub2();
                        s2.methodInterfaceA();
                    }
                }
                """, unixString(FileUtil.textOf(new File(tempDir, "com/example/Main.java"))));
        assertEquals("""
                package com.example;

                public class Sub1 extends Base {
                    public static class Sub1_InnerClass extends Base_InnerClass {
                                void innerMethodBase1(String a) {
                            super.innerMethodBase1(a);
                        }
                    }

                    void methodSub1() {
                    }

                        void methodBase1(String a) {
                        super.methodBase1(a);
                    }
                }
                """, unixString(FileUtil.textOf(new File(tempDir, "com/example/Sub1.java"))));
        assertEquals("""
                package com.example;

                public class Sub2 implements InterfaceA {
                    @SuppressWarnings("all")
                    void methodSub2() {
                    }

                        public void methodInterfaceA() {
                    }
                }
                """, unixString(FileUtil.textOf(new File(tempDir, "com/example/Sub2.java"))));

        assertEquals("""
                        removing @Override of com.example.Sub1.Sub1_InnerClass.innerMethodBase1(java.lang.String) - com.example.innerMethodBase1 - com.example.Sub1.Sub1_InnerClass.innerMethodBase1(java.lang.String)- MyMethodAnnotationDescriptor{@Override com.example.Sub1.Sub1_InnerClass.innerMethodBase1(java.lang.String)}
                        removing @Override of com.example.Sub1.methodBase1(java.lang.String) - com.example.methodBase1 - com.example.Sub1.methodBase1(java.lang.String)- MyMethodAnnotationDescriptor{@Override com.example.Sub1.methodBase1(java.lang.String)}
                        removing @Override of com.example.Sub2.methodInterfaceA() - com.example.methodInterfaceA - com.example.Sub2.methodInterfaceA()- MyMethodAnnotationDescriptor{@Override com.example.Sub2.methodInterfaceA()}""",
                sortedUnixLines(listenerLog.toString()));
    }
}
