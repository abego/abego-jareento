package org.abego.jareento.javaanalysis.internal.input.javap;

import org.abego.commons.io.FileUtil;
import org.abego.jareento.javarefactoring.internal.TestData;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;

import static org.abego.jareento.javaanalysis.internal.input.javap.JavapParser.newJavapParser;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JavapParserTest {
    private static class JavapEventsLogger implements JavapParser.EventHandler {
        private final StringBuilder result = new StringBuilder();
        private int lineStartOffset = 0;

        String getText() {
            return result.toString();
        }

        @Override
        public void onClassfile(String classfile) {
            append("classfile", classfile);
            endLine();
        }

        @Override
        public void onClassfileEnd(String classfile) {
            append("classfile-end", classfile);
            endLine();
        }

        @Override
        public void onClassfileBytecodeSize(String classfile, int size) {
            append("bytecodeSize", size, classfile);
            endLine();
        }

        @Override
        public void onClassfileMD5(String classfile, String md5) {
            append("md5", md5, classfile);
            endLine();
        }

        @Override
        public  void onClassfileSHA256(String classfile, String sha256) {
            append("sha256", sha256, classfile);
            endLine();
        }

        @Override
        public void onJavaFilename(String classfile, String javafilename) {
            append("javafilename", javafilename, classfile);
            endLine();
        }

        @Override
        public void onClass(String classname, String access, String modifier, String type, String[] extendedTypes, String[] implementedTypes) {
            append("class", classname);
            append("access", access);
            append("modifier", modifier);
            append("type", type);
            append("extends", String.join(", ", extendedTypes));
            append("implements", String.join(", ", implementedTypes));
            endLine();
        }

        @Override
        public void onClassEnd(String classname) {
            append("class-end", null, classname);
            endLine();
        }

        @Override
        public void onMethod(String classname, String methodName, String access, String modifier, String returnType, String parameter, String exceptions, String typeParametersOfMethod) {
            append("method", methodName);
            append("class", classname);
            append("access", access);
            append("modifier", modifier);
            append("returnType", returnType);
            append("parameter", parameter);
            append("exceptions", exceptions);
            append("typeParametersOfMethod", typeParametersOfMethod);
            endLine();
        }

        @Override
        public void onField(String className, String fieldName, String access, String modifier, String type) {
            append("field", fieldName);
            append("class", className);
            append("access", access);
            append("modifier", modifier);
            append("type", type);
            endLine();
        }

        @Override
        public void onStaticInitialization(String className) {
            append("staticInitialization", null, className);
            endLine();
        }

        @Override
        public void onInstruction(String className, String methodName, String parameters, String returnType, int offset, String mnemonic, String arguments, String comment) {
            append("instruction", offset);
            result.append(": ");
            result.append(mnemonic);
            append("arguments", arguments);
            append("className", className);
            append("methodName", methodName);
            append("parameters", parameters);
            append("returnType", returnType);
            append("comment", comment);
            endLine();
        }

        @Override
        public void onException(int from, int to, int target, String type) {
            append("exception", type);
            append("from", from);
            append("to", to);
            append("target", target);
            endLine();
        }

        @Override
        public void onSourcefile(String sourcefile) {
            append("sourcefile", sourcefile);
            endLine();
        }

        private void append(String name, @Nullable Object text) {
            append(name, text, null);
        }

        private void append(String name, @Nullable Object text, @Nullable Object details) {
            boolean hasText = text != null && !text.toString().isEmpty();
            boolean hasDetails = details != null && !details.toString()
                    .isEmpty();

            if (hasText || hasDetails) {
                if (result.length() > lineStartOffset) {
                    result.append("; ");
                }
                result.append(name);
                if (hasText) {
                    result.append(": ");
                    result.append(text);
                }
                if (hasDetails) {
                    result.append(" (");
                    result.append(details);
                    result.append(")");
                }
            }
        }

        private void endLine() {
            result.append("\n");
            lineStartOffset = result.length();
        }
    }

    @Test
    @Disabled("test case only runnable in a special (private) environment")
    void bigSampleTest() {
        File file = TestData.getBigSampleJavapFile();

        JavapEventsLogger logger = new JavapEventsLogger();
        JavapParser javapParser = newJavapParser();
        javapParser.parseFile(file, logger);

        FileUtil.writeText(new File("javap-big-sample-events.txt"), logger.getText());
    }

    @Test
    void methodTest() {
        String text = "  void methodSub3(java.lang.String, int);\n";
        assertEquals("method: methodSub3; returnType: void; parameter: java.lang.String, int\n", parseText(text));

        text = "  public com.example.inheritance.Sub1();\n";
        assertEquals("method: com.example.inheritance.Sub1; access: public\n", parseText(text));

        text = "  public com.example.Result<long, java.lang.Object> calc();\n";
        assertEquals("method: calc; access: public; returnType: com.example.Result<long, java.lang.Object>\n", parseText(text));
    }

    private static String parseText(String text) {
        BufferedReader reader = new BufferedReader(new StringReader(text));
        JavapEventsLogger logger = new JavapEventsLogger();
        JavapParser javapParser = newJavapParser();
        javapParser.parseReader(reader, logger);
        return logger.getText();
    }

    @Test
    void smoketest(@TempDir File tempDir) {
        File file = new File(tempDir, "sample.txt");
        FileUtil.copyResourceToFile(getClass(), "javap-sample1.txt", file);

        JavapEventsLogger logger = new JavapEventsLogger();
        JavapParser javapParser = newJavapParser();
        javapParser.parseFile(file, logger);

        assertEquals("""
                        classfile: jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/sample2/SynSub.class
                        bytecodeSize: 455 (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/sample2/SynSub.class)
                        md5: 00c40cc1ac951f7750deaa04f3d84a94 (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/sample2/SynSub.class)
                        javafilename: SynSub.java (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/sample2/SynSub.class)
                        class: com.example.sample2.SynSub; access: public; type: class; extends: com.example.sample2.SynBase
                        method: com.example.sample2.SynSub; class: com.example.sample2.SynSub; access: public
                        instruction: 0: aload_0; className: com.example.sample2.SynSub; methodName: com.example.sample2.SynSub
                        instruction: 1: invokespecial; arguments: #1; className: com.example.sample2.SynSub; methodName: com.example.sample2.SynSub; comment: Method com/example/sample2/SynBase."<init>":()V
                        instruction: 4: return; className: com.example.sample2.SynSub; methodName: com.example.sample2.SynSub
                        exception: this   Lcom/example/sample2/SynSub;; from: 0; to: 5; target: 0
                        method: value; class: com.example.sample2.SynSub; access: public; returnType: java.lang.Integer
                        instruction: 0: iconst_1; className: com.example.sample2.SynSub; methodName: value; returnType: java.lang.Integer
                        instruction: 1: invokestatic; arguments: #2; className: com.example.sample2.SynSub; methodName: value; returnType: java.lang.Integer; comment: Method java/lang/Integer.valueOf:(I)Ljava/lang/Integer;
                        instruction: 4: areturn; className: com.example.sample2.SynSub; methodName: value; returnType: java.lang.Integer
                        exception: this   Lcom/example/sample2/SynSub;; from: 0; to: 5; target: 0
                        class-end (com.example.sample2.SynSub)
                        sourcefile: SynSub.java
                        classfile-end: jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/sample2/SynSub.class
                        classfile: jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/sample2/SynBase.class
                        bytecodeSize: 286 (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/sample2/SynBase.class)
                        md5: f294b7c765c2e30784c3b1de1d538afb (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/sample2/SynBase.class)
                        javafilename: SynBase.java (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/sample2/SynBase.class)
                        class: com.example.sample2.SynBase; access: public; type: class
                        method: com.example.sample2.SynBase; class: com.example.sample2.SynBase; access: public
                        instruction: 0: aload_0; className: com.example.sample2.SynBase; methodName: com.example.sample2.SynBase
                        instruction: 1: invokespecial; arguments: #1; className: com.example.sample2.SynBase; methodName: com.example.sample2.SynBase; comment: Method java/lang/Object."<init>":()V
                        instruction: 4: return; className: com.example.sample2.SynBase; methodName: com.example.sample2.SynBase
                        exception: this   Lcom/example/sample2/SynBase;; from: 0; to: 5; target: 0
                        class-end (com.example.sample2.SynBase)
                        sourcefile: SynBase.java
                        classfile-end: jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/sample2/SynBase.class
                        classfile: jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Sub1.class
                        bytecodeSize: 615 (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Sub1.class)
                        md5: d4c4216c62e4b94d9c9f969bb9db90ec (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Sub1.class)
                        javafilename: Sub1.java (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Sub1.class)
                        class: com.example.inheritance.Sub1; access: public; type: class; extends: com.example.inheritance.Base
                        method: com.example.inheritance.Sub1; class: com.example.inheritance.Sub1; access: public
                        instruction: 0: aload_0; className: com.example.inheritance.Sub1; methodName: com.example.inheritance.Sub1
                        instruction: 1: invokespecial; arguments: #1; className: com.example.inheritance.Sub1; methodName: com.example.inheritance.Sub1; comment: Method com/example/inheritance/Base."<init>":()V
                        instruction: 4: return; className: com.example.inheritance.Sub1; methodName: com.example.inheritance.Sub1
                        exception: this   Lcom/example/inheritance/Sub1;; from: 0; to: 5; target: 0
                        method: methodSub1; class: com.example.inheritance.Sub1; returnType: void
                        instruction: 0: return; className: com.example.inheritance.Sub1; methodName: methodSub1; returnType: void
                        exception: this   Lcom/example/inheritance/Sub1;; from: 0; to: 1; target: 0
                        method: methodBase1; class: com.example.inheritance.Sub1; returnType: void; parameter: java.lang.String
                        instruction: 0: aload_0; className: com.example.inheritance.Sub1; methodName: methodBase1; parameters: java.lang.String; returnType: void
                        instruction: 1: aload_1; className: com.example.inheritance.Sub1; methodName: methodBase1; parameters: java.lang.String; returnType: void
                        instruction: 2: invokespecial; arguments: #2; className: com.example.inheritance.Sub1; methodName: methodBase1; parameters: java.lang.String; returnType: void; comment: Method com/example/inheritance/Base.methodBase1:(Ljava/lang/String;)V
                        instruction: 5: return; className: com.example.inheritance.Sub1; methodName: methodBase1; parameters: java.lang.String; returnType: void
                        exception: this   Lcom/example/inheritance/Sub1;; from: 0; to: 6; target: 0
                        exception: a   Ljava/lang/String;; from: 0; to: 6; target: 1
                        class-end (com.example.inheritance.Sub1)
                        sourcefile: Sub1.java
                        classfile-end: jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Sub1.class
                        classfile: jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Sub1$Sub1_InnerClass.class
                        bytecodeSize: 642 (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Sub1$Sub1_InnerClass.class)
                        md5: b9698434c553a129b2ee309a0c260ebc (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Sub1$Sub1_InnerClass.class)
                        javafilename: Sub1.java (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Sub1$Sub1_InnerClass.class)
                        class: com.example.inheritance.Sub1$Sub1_InnerClass; access: public; type: class; extends: com.example.inheritance.Base$Base_InnerClass
                        method: com.example.inheritance.Sub1$Sub1_InnerClass; class: com.example.inheritance.Sub1$Sub1_InnerClass; access: public
                        instruction: 0: aload_0; className: com.example.inheritance.Sub1$Sub1_InnerClass; methodName: com.example.inheritance.Sub1$Sub1_InnerClass
                        instruction: 1: invokespecial; arguments: #1; className: com.example.inheritance.Sub1$Sub1_InnerClass; methodName: com.example.inheritance.Sub1$Sub1_InnerClass; comment: Method com/example/inheritance/Base$Base_InnerClass."<init>":()V
                        instruction: 4: return; className: com.example.inheritance.Sub1$Sub1_InnerClass; methodName: com.example.inheritance.Sub1$Sub1_InnerClass
                        exception: this   Lcom/example/inheritance/Sub1$Sub1_InnerClass;; from: 0; to: 5; target: 0
                        method: innerMethodBase1; class: com.example.inheritance.Sub1$Sub1_InnerClass; returnType: void; parameter: java.lang.String
                        instruction: 0: aload_0; className: com.example.inheritance.Sub1$Sub1_InnerClass; methodName: innerMethodBase1; parameters: java.lang.String; returnType: void
                        instruction: 1: aload_1; className: com.example.inheritance.Sub1$Sub1_InnerClass; methodName: innerMethodBase1; parameters: java.lang.String; returnType: void
                        instruction: 2: invokespecial; arguments: #2; className: com.example.inheritance.Sub1$Sub1_InnerClass; methodName: innerMethodBase1; parameters: java.lang.String; returnType: void; comment: Method com/example/inheritance/Base$Base_InnerClass.innerMethodBase1:(Ljava/lang/String;)V
                        instruction: 5: return; className: com.example.inheritance.Sub1$Sub1_InnerClass; methodName: innerMethodBase1; parameters: java.lang.String; returnType: void
                        exception: this   Lcom/example/inheritance/Sub1$Sub1_InnerClass;; from: 0; to: 6; target: 0
                        exception: a   Ljava/lang/String;; from: 0; to: 6; target: 1
                        class-end (com.example.inheritance.Sub1$Sub1_InnerClass)
                        sourcefile: Sub1.java
                        classfile-end: jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Sub1$Sub1_InnerClass.class
                        classfile: jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/InterfaceA.class
                        bytecodeSize: 158 (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/InterfaceA.class)
                        md5: 7e0e340801fe9388e5e7db8c660ce2e5 (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/InterfaceA.class)
                        javafilename: InterfaceA.java (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/InterfaceA.class)
                        class: com.example.inheritance.InterfaceA; access: public; type: interface
                        method: methodInterfaceA; class: com.example.inheritance.InterfaceA; access: public; modifier: abstract; returnType: void
                        class-end (com.example.inheritance.InterfaceA)
                        sourcefile: InterfaceA.java
                        classfile-end: jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/InterfaceA.class
                        classfile: jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Sub2.class
                        bytecodeSize: 473 (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Sub2.class)
                        md5: 57d02ff67b918ad3e0bfdc72a4cc313c (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Sub2.class)
                        javafilename: Sub2.java (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Sub2.class)
                        class: com.example.inheritance.Sub2; access: public; type: class; implements: com.example.inheritance.InterfaceA
                        method: com.example.inheritance.Sub2; class: com.example.inheritance.Sub2; access: public
                        instruction: 0: aload_0; className: com.example.inheritance.Sub2; methodName: com.example.inheritance.Sub2
                        instruction: 1: invokespecial; arguments: #1; className: com.example.inheritance.Sub2; methodName: com.example.inheritance.Sub2; comment: Method java/lang/Object."<init>":()V
                        instruction: 4: return; className: com.example.inheritance.Sub2; methodName: com.example.inheritance.Sub2
                        exception: this   Lcom/example/inheritance/Sub2;; from: 0; to: 5; target: 0
                        method: methodSub2; class: com.example.inheritance.Sub2; returnType: void
                        instruction: 0: return; className: com.example.inheritance.Sub2; methodName: methodSub2; returnType: void
                        exception: this   Lcom/example/inheritance/Sub2;; from: 0; to: 1; target: 0
                        method: methodInterfaceA; class: com.example.inheritance.Sub2; access: public; returnType: void
                        instruction: 0: return; className: com.example.inheritance.Sub2; methodName: methodInterfaceA; returnType: void
                        exception: this   Lcom/example/inheritance/Sub2;; from: 0; to: 1; target: 0
                        class-end (com.example.inheritance.Sub2)
                        sourcefile: Sub2.java
                        classfile-end: jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Sub2.class
                        classfile: jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Base$Base_InnerClass.class
                        bytecodeSize: 621 (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Base$Base_InnerClass.class)
                        md5: fe3529e5cb598d4f98421f6c591204c0 (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Base$Base_InnerClass.class)
                        javafilename: Base.java (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Base$Base_InnerClass.class)
                        class: com.example.inheritance.Base$Base_InnerClass; access: public; type: class
                        method: com.example.inheritance.Base$Base_InnerClass; class: com.example.inheritance.Base$Base_InnerClass; access: public
                        instruction: 0: aload_0; className: com.example.inheritance.Base$Base_InnerClass; methodName: com.example.inheritance.Base$Base_InnerClass
                        instruction: 1: invokespecial; arguments: #1; className: com.example.inheritance.Base$Base_InnerClass; methodName: com.example.inheritance.Base$Base_InnerClass; comment: Method java/lang/Object."<init>":()V
                        instruction: 4: return; className: com.example.inheritance.Base$Base_InnerClass; methodName: com.example.inheritance.Base$Base_InnerClass
                        exception: this   Lcom/example/inheritance/Base$Base_InnerClass;; from: 0; to: 5; target: 0
                        method: innerMethodBase1; class: com.example.inheritance.Base$Base_InnerClass; returnType: void; parameter: java.lang.String
                        instruction: 0: return; className: com.example.inheritance.Base$Base_InnerClass; methodName: innerMethodBase1; parameters: java.lang.String; returnType: void
                        exception: this   Lcom/example/inheritance/Base$Base_InnerClass;; from: 0; to: 1; target: 0
                        exception: a   Ljava/lang/String;; from: 0; to: 1; target: 1
                        method: innerMethodBase2; class: com.example.inheritance.Base$Base_InnerClass; returnType: void; parameter: java.lang.String
                        instruction: 0: return; className: com.example.inheritance.Base$Base_InnerClass; methodName: innerMethodBase2; parameters: java.lang.String; returnType: void
                        exception: this   Lcom/example/inheritance/Base$Base_InnerClass;; from: 0; to: 1; target: 0
                        exception: a   Ljava/lang/String;; from: 0; to: 1; target: 1
                        class-end (com.example.inheritance.Base$Base_InnerClass)
                        sourcefile: Base.java
                        classfile-end: jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Base$Base_InnerClass.class
                        classfile: jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Base.class
                        bytecodeSize: 595 (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Base.class)
                        md5: 96e68b2f8820dd6ff4657efbb34b0c9d (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Base.class)
                        javafilename: Base.java (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Base.class)
                        class: com.example.inheritance.Base; access: public; type: class
                        method: com.example.inheritance.Base; class: com.example.inheritance.Base; access: public
                        instruction: 0: aload_0; className: com.example.inheritance.Base; methodName: com.example.inheritance.Base
                        instruction: 1: invokespecial; arguments: #1; className: com.example.inheritance.Base; methodName: com.example.inheritance.Base; comment: Method java/lang/Object."<init>":()V
                        instruction: 4: return; className: com.example.inheritance.Base; methodName: com.example.inheritance.Base
                        exception: this   Lcom/example/inheritance/Base;; from: 0; to: 5; target: 0
                        method: methodBase1; class: com.example.inheritance.Base; returnType: void; parameter: java.lang.String
                        instruction: 0: return; className: com.example.inheritance.Base; methodName: methodBase1; parameters: java.lang.String; returnType: void
                        exception: this   Lcom/example/inheritance/Base;; from: 0; to: 1; target: 0
                        exception: a   Ljava/lang/String;; from: 0; to: 1; target: 1
                        method: methodBase2; class: com.example.inheritance.Base; returnType: void; parameter: java.lang.String
                        instruction: 0: return; className: com.example.inheritance.Base; methodName: methodBase2; parameters: java.lang.String; returnType: void
                        exception: this   Lcom/example/inheritance/Base;; from: 0; to: 1; target: 0
                        exception: a   Ljava/lang/String;; from: 0; to: 1; target: 1
                        class-end (com.example.inheritance.Base)
                        sourcefile: Base.java
                        classfile-end: jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Base.class
                        classfile: jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Main.class
                        bytecodeSize: 1053 (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Main.class)
                        md5: 6e494152938262fe18047d5b6ef659a8 (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Main.class)
                        javafilename: Main.java (jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Main.class)
                        class: com.example.inheritance.Main; access: public; type: class
                        method: com.example.inheritance.Main; class: com.example.inheritance.Main; access: public
                        instruction: 0: aload_0; className: com.example.inheritance.Main; methodName: com.example.inheritance.Main
                        instruction: 1: invokespecial; arguments: #1; className: com.example.inheritance.Main; methodName: com.example.inheritance.Main; comment: Method java/lang/Object."<init>":()V
                        instruction: 4: return; className: com.example.inheritance.Main; methodName: com.example.inheritance.Main
                        exception: this   Lcom/example/inheritance/Main;; from: 0; to: 5; target: 0
                        method: entry; class: com.example.inheritance.Main; access: public; modifier: static; returnType: void; parameter: com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2
                        instruction: 0: aload_0; className: com.example.inheritance.Main; methodName: entry; parameters: com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2; returnType: void
                        instruction: 1: ldc; arguments: #2; className: com.example.inheritance.Main; methodName: entry; parameters: com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2; returnType: void; comment: String
                        instruction: 3: invokevirtual; arguments: #3; className: com.example.inheritance.Main; methodName: entry; parameters: com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2; returnType: void; comment: Method com/example/inheritance/Base.methodBase1:(Ljava/lang/String;)V
                        instruction: 6: aload_0; className: com.example.inheritance.Main; methodName: entry; parameters: com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2; returnType: void
                        instruction: 7: ldc; arguments: #2; className: com.example.inheritance.Main; methodName: entry; parameters: com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2; returnType: void; comment: String
                        instruction: 9: invokevirtual; arguments: #4; className: com.example.inheritance.Main; methodName: entry; parameters: com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2; returnType: void; comment: Method com/example/inheritance/Base.methodBase2:(Ljava/lang/String;)V
                        instruction: 12: aload_1; className: com.example.inheritance.Main; methodName: entry; parameters: com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2; returnType: void
                        instruction: 13: invokeinterface; arguments: #5,  1; className: com.example.inheritance.Main; methodName: entry; parameters: com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2; returnType: void; comment: InterfaceMethod com/example/inheritance/InterfaceA.methodInterfaceA:()V
                        instruction: 18: aload_2; className: com.example.inheritance.Main; methodName: entry; parameters: com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2; returnType: void
                        instruction: 19: ldc; arguments: #2; className: com.example.inheritance.Main; methodName: entry; parameters: com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2; returnType: void; comment: String
                        instruction: 21: invokevirtual; arguments: #6; className: com.example.inheritance.Main; methodName: entry; parameters: com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2; returnType: void; comment: Method com/example/inheritance/Sub1.methodBase1:(Ljava/lang/String;)V
                        instruction: 24: aload_2; className: com.example.inheritance.Main; methodName: entry; parameters: com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2; returnType: void
                        instruction: 25: ldc; arguments: #2; className: com.example.inheritance.Main; methodName: entry; parameters: com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2; returnType: void; comment: String
                        instruction: 27: invokevirtual; arguments: #7; className: com.example.inheritance.Main; methodName: entry; parameters: com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2; returnType: void; comment: Method com/example/inheritance/Sub1.methodBase2:(Ljava/lang/String;)V
                        instruction: 30: aload_2; className: com.example.inheritance.Main; methodName: entry; parameters: com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2; returnType: void
                        instruction: 31: invokevirtual; arguments: #8; className: com.example.inheritance.Main; methodName: entry; parameters: com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2; returnType: void; comment: Method com/example/inheritance/Sub1.methodSub1:()V
                        instruction: 34: aload_3; className: com.example.inheritance.Main; methodName: entry; parameters: com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2; returnType: void
                        instruction: 35: invokevirtual; arguments: #9; className: com.example.inheritance.Main; methodName: entry; parameters: com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2; returnType: void; comment: Method com/example/inheritance/Sub2.methodSub2:()V
                        instruction: 38: aload_3; className: com.example.inheritance.Main; methodName: entry; parameters: com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2; returnType: void
                        instruction: 39: invokevirtual; arguments: #10; className: com.example.inheritance.Main; methodName: entry; parameters: com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2; returnType: void; comment: Method com/example/inheritance/Sub2.methodInterfaceA:()V
                        instruction: 42: return; className: com.example.inheritance.Main; methodName: entry; parameters: com.example.inheritance.Base, com.example.inheritance.InterfaceA, com.example.inheritance.Sub1, com.example.inheritance.Sub2; returnType: void
                        exception: b   Lcom/example/inheritance/Base;; from: 0; to: 43; target: 0
                        exception: ia   Lcom/example/inheritance/InterfaceA;; from: 0; to: 43; target: 1
                        exception: s1   Lcom/example/inheritance/Sub1;; from: 0; to: 43; target: 2
                        exception: s2   Lcom/example/inheritance/Sub2;; from: 0; to: 43; target: 3
                        class-end (com.example.inheritance.Main)
                        sourcefile: Main.java
                        classfile-end: jar:file:/Users/ub/work/project/sd/project/sampleproject/sampleproject/target/sampleproject-0.1.0-SNAPSHOT.jar!/com/example/inheritance/Main.class
                        """,
                logger.getText());
    }
}
