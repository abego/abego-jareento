package org.abego.jareento.shared;


import org.abego.commons.lang.StringUtil;
import org.abego.jareento.base.JareentoSyntax;

import javax.annotation.Syntax;

public class JavaMethodDeclarator {
    private final String text;
    private final int hashIndex;
    private final int colonIndex;
    private final int openParenthesisIndex;

    private JavaMethodDeclarator(String text) {
        this.text = text;
        hashIndex = text.indexOf('#');
        colonIndex = text.indexOf(':');
        openParenthesisIndex = text.indexOf('(');

        //TODO do we allow methodId with classname (i.e. hashIndex <= 0)?
        if ((colonIndex <= hashIndex) ||
                (openParenthesisIndex <= hashIndex)) {
            throw new IllegalArgumentException("Invalid method declarator: " + text);
        }
    }

    public static JavaMethodDeclarator newJavaMethodDeclarator(String declarator) {
        return new JavaMethodDeclarator(declarator);
    }

    public static JavaMethodDeclarator newJavaMethodDeclarator(String classname, String methodSignature, String returnType) {
        return new JavaMethodDeclarator(
                methodDeclaratorText(classname, methodSignature, returnType));
    }

    @Syntax(JareentoSyntax.METHOD_DECLARATOR_SYNTAX)
    public static String methodDeclaratorText(String classname, String methodSignature, String returnType) {
        return classname + "#" + methodSignature + ":" + returnType;
    }

    @Syntax(JareentoSyntax.SIMPLE_METHOD_DECLARATOR_SYNTAX)
    public static String simpleMethodDeclaratorText(String classname, String methodSignature) {
        return classname + "#" + methodSignature;
    }

    public String getText() {
        return text;
    }

    public String getSignatureText() {
        return hashIndex >= 0
                ? text.substring(hashIndex + 1, colonIndex)
                : text.substring(0, colonIndex);
    }

    public String getName() {
        return hashIndex >= 0
                ? text.substring(hashIndex + 1, openParenthesisIndex)
                : text.substring(0, openParenthesisIndex);
    }

    public String getClassname() {
        return hashIndex >= 0 ? text.substring(0, hashIndex) : "";
    }

    public String getSimpleClassname() {
        String classname = getClassname();
        int i = classname.lastIndexOf('.');
        return i >= 0 ? classname.substring(i + 1) : classname;
    }

    //TODO: do we need this?
    public static String simpleClassnameOfMethodDeclarator(
            @Syntax(JareentoSyntax.METHOD_DECLARATOR_SYNTAX) String methodDeclarator) {
        String s = StringUtil.prefixBefore(methodDeclarator, "#");
        int i = s.lastIndexOf('.');
        return i >= 0 ? s.substring(i + 1) : s;
    }

    /**
     * Return the MethodDeclarator of the given methodDeclarator,
     * i.e. the methodDeclarator without return type information.
     */
    @Syntax(JareentoSyntax.SIMPLE_METHOD_DECLARATOR_SYNTAX)
    public static String simpleMethodDeclaratorOfMethodDeclarator(
            @Syntax(JareentoSyntax.METHOD_DECLARATOR_SYNTAX) String methodDeclarator) {
        return StringUtil.prefixBefore(methodDeclarator, ":");
    }
}
