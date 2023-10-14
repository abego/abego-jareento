package org.abego.jareento.javaanalysis.internal.input.javap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JavapUtilTest {
    @Test
    void parseFromJavapInvokeComment() {
        String text = "Method java/lang/Enum.valueOf:(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;";

        JavapMethodDescriptor desc =
                JavapMethodDescriptor.parseFromJavapInvokeComment(text);

        assertEquals("java.lang.Enum", desc.className);
        assertEquals("valueOf", desc.methodName);
        assertEquals("java.lang.Enum", desc.returnType);
        assertEquals("java.lang.Class|java.lang.String", String.join("|", desc.parameters));
        assertEquals("valueOf(java.lang.Class, java.lang.String)", desc.getSignature());
    }

    @Test
    void parseFromJavapInvokeComment_quotedClassName() {
        String text = "Method \"[Ljava/lang/String;\".clone:()Ljava/lang/Object;";

        JavapMethodDescriptor desc =
                JavapMethodDescriptor.parseFromJavapInvokeComment(text);

        assertEquals("java.lang.String[]", desc.className);
        assertEquals("clone", desc.methodName);
        assertEquals("java.lang.Object", desc.returnType);
        assertEquals("", String.join("|", desc.parameters));
        assertEquals("clone()", desc.getSignature());
    }

    @Test
    void parseFromJavapInvokeComment_arrayParameter() {
        String text = "Method fillRectangle:(Ljava/lang/Object;[Ljava/awt/Rectangle;)V";

        JavapMethodDescriptor desc =
                JavapMethodDescriptor.parseFromJavapInvokeComment(text);

        assertEquals("", desc.className);
        assertEquals("fillRectangle", desc.methodName);
        assertEquals("void", desc.returnType);
        assertEquals("java.lang.Object|java.awt.Rectangle[]", String.join("|", desc.parameters));
        assertEquals("fillRectangle(java.lang.Object, java.awt.Rectangle[])", desc.getSignature());
    }

    @Test
    void parseFromJavapInvokeComment_init() {
        String text = "Method com/example/inheritance/Base.\"<init>\":()V";

        JavapMethodDescriptor desc =
                JavapMethodDescriptor.parseFromJavapInvokeComment(text);

        assertEquals("com.example.inheritance.Base", desc.className);
        assertEquals("\"<init>\"", desc.methodName);
        assertEquals("void", desc.returnType);
        assertEquals("", String.join("|", desc.parameters));
        assertEquals("\"<init>\"()", desc.getSignature());
    }

    @Test
    void parseFromJavapInvokeComment_init_noClass() {
        String text = "Method \"<init>\":(Ljava/lang/Number;)V";

        JavapMethodDescriptor desc =
                JavapMethodDescriptor.parseFromJavapInvokeComment(text);

        assertEquals("", desc.className);
        assertEquals("\"<init>\"", desc.methodName);
        assertEquals("void", desc.returnType);
        assertEquals("java.lang.Number", String.join("|", desc.parameters));
        assertEquals("\"<init>\"(java.lang.Number)", desc.getSignature());
    }


}
