package org.abego.jareento.javarefactoring.internal;

import org.abego.commons.io.FileUtil;
import org.abego.commons.lang.StringUtil;
import org.abego.jareento.javaanalysis.JavaAnalysisAPI;
import org.abego.jareento.javaanalysis.internal.JavaAnalysisFiles;
import org.abego.jareento.javarefactoring.JavaRefactoringAPI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.abego.commons.lang.StringUtil.unixString;
import static org.abego.commons.util.ServiceLoaderUtil.loadService;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RemoveMethodTest {
    private final JavaAnalysisAPI javaAnalysisAPI = loadService(JavaAnalysisAPI.class);
    private final JavaRefactoringAPI javaRefactoring = loadService(JavaRefactoringAPI.class);

    @Test
    void smokeTest(@TempDir File tempDir) {

        TestData.copySampleProjectTo(tempDir);

        JavaAnalysisFiles javaAnalysisFiles = javaAnalysisAPI.newJavaAnalysisFiles(tempDir);
        StringBuilder listenerLog = new StringBuilder();

        // remove all methods containing "Base" in their name 
        javaRefactoring.removeMethods(
                javaAnalysisFiles,
                md -> md.getMethodName().contains("Base"),
                f -> true,
                md -> listenerLog.append(
                        String.format("removing %s.%s - %s - %s - (%s)\n",
                                md.getTypeDeclaringMethod(),
                                md.getMethodSignature(),
                                md.getQualifiedMethodName(),
                                md.getMethodPackageName(),
                                StringUtil.join(", ", (Object[])md.getMethodParameterTypeNames()))),
                s -> {});

        assertEquals("""
                package com.example;

                public class Base {
                    public static class Base_InnerClass {
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
                    }

                    void methodSub1() {
                    }
                }
                """, unixString(FileUtil.textOf(new File(tempDir, "com/example/Sub1.java"))));
        assertEquals("""
                package com.example;

                public class Sub2 implements InterfaceA {
                    @SuppressWarnings("all")
                    void methodSub2() {
                    }

                    @Override
                    public void methodInterfaceA() {
                    }
                }
                """, unixString(FileUtil.textOf(new File(tempDir, "com/example/Sub2.java"))));

        assertEquals("""
                removing com.example.Base.Base_InnerClass.innerMethodBase1(java.lang.String) - com.example.Base.Base_InnerClass.innerMethodBase1 - com.example - (java.lang.String)
                removing com.example.Base.Base_InnerClass.innerMethodBase2(java.lang.String) - com.example.Base.Base_InnerClass.innerMethodBase2 - com.example - (java.lang.String)
                removing com.example.Base.methodBase1(java.lang.String) - com.example.Base.methodBase1 - com.example - (java.lang.String)
                removing com.example.Base.methodBase2(java.lang.String) - com.example.Base.methodBase2 - com.example - (java.lang.String)
                removing com.example.Sub1.Sub1_InnerClass.innerMethodBase1(java.lang.String) - com.example.Sub1.Sub1_InnerClass.innerMethodBase1 - com.example - (java.lang.String)
                removing com.example.Sub1.methodBase1(java.lang.String) - com.example.Sub1.methodBase1 - com.example - (java.lang.String)
                """, listenerLog.toString());
    }

}
