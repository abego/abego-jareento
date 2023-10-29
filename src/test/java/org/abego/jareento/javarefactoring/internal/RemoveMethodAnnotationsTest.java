package org.abego.jareento.javarefactoring.internal;

import org.abego.commons.io.FileUtil;
import org.abego.jareento.javarefactoring.JavaRefactoringAPI;
import org.abego.jareento.javarefactoring.JavaRefactoringProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.abego.commons.lang.StringUtil.sortedUnixLines;
import static org.abego.commons.lang.StringUtil.unixString;
import static org.abego.commons.util.ServiceLoaderUtil.loadService;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RemoveMethodAnnotationsTest {
    private final JavaRefactoringAPI javaRefactoring = loadService(JavaRefactoringAPI.class);

    @Test
    void smokeTest(@TempDir File tempDir) {

        TestData.copySampleProjectTo(tempDir);

        JavaRefactoringProject project = javaRefactoring.newJavaRefactoringProject(tempDir);
        StringBuilder listenerLog = new StringBuilder();

        // remove all @Override annotations, 
        javaRefactoring.removeMethodAnnotations(
                project,
                mad -> mad.getAnnotationText().equals("@Override"),
                f -> true, mad -> listenerLog.append(
                        String.format("removing %s of %s.%s\n",
                                mad.getAnnotationText(),
                                mad.getTypeDeclaringMethod(),
                                mad.getMethodSignature())),
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
                        removing @Override of com.example.Sub1.Sub1_InnerClass.innerMethodBase1(java.lang.String)
                        removing @Override of com.example.Sub1.methodBase1(java.lang.String)
                        removing @Override of com.example.Sub2.methodInterfaceA()""",
                sortedUnixLines(listenerLog.toString()));
    }

}
