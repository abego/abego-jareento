package org.abego.jareento.shared;


import org.abego.commons.lang.StringUtil;

public class FullMethodDeclarator {
    private final String fullMethodDeclarator;
    private final int hashIndex;
    private final int colonIndex;
    private final int openParenthesisIndex;

    private FullMethodDeclarator(String fullMethodDeclarator) {
        this.fullMethodDeclarator = fullMethodDeclarator;
        hashIndex = fullMethodDeclarator.indexOf('#');
        colonIndex = fullMethodDeclarator.indexOf(':');
        openParenthesisIndex = fullMethodDeclarator.indexOf('(');

        //TODO do we allow methodId with classname (i.e. hashIndex <= 0)?
        if ((colonIndex <= hashIndex) ||
                (openParenthesisIndex <= hashIndex)) {
            throw new IllegalArgumentException("Invalid full method declarator: " + fullMethodDeclarator);
        }
    }

    public static FullMethodDeclarator newFullMethodDeclarator(String declarator) {
        return new FullMethodDeclarator(declarator);
    }

    public static FullMethodDeclarator newFullMethodDeclarator(String fullClassname, String methodSignature, String returnType) {
        return new FullMethodDeclarator(
                fullMethodDeclaratorText(fullClassname, methodSignature, returnType));
    }

    public static String fullMethodDeclaratorText(String fullClassname, String methodSignature, String returnType) {
        return fullClassname + "#" + methodSignature + ":" + returnType;
    }

    public static String methodDeclaratorText(String fullClassname, String methodSignature) {
        return fullClassname + "#" + methodSignature;
    }

    public String text() {
        return fullMethodDeclarator;
    }

    public String signature() {
        return hashIndex >= 0
                ? fullMethodDeclarator.substring(hashIndex + 1, colonIndex)
                : fullMethodDeclarator.substring(0, colonIndex);
    }

    public String name() {
        return hashIndex >= 0
                ? fullMethodDeclarator.substring(hashIndex + 1, openParenthesisIndex)
                : fullMethodDeclarator.substring(0, openParenthesisIndex);
    }

    public String classname() {
        return hashIndex >= 0 ? fullMethodDeclarator.substring(0, hashIndex) : "";
    }

    public static String simpleClassname(String fullMethodDeclarator) {
        String s = StringUtil.prefixBefore(fullMethodDeclarator, "#");
        int i = s.lastIndexOf('.');
        return i >= 0 ? s.substring(i + 1) : s;
    }

    /**
     * Return the MethodDeclarator of the given fullMethodDeclarator,
     * i.e. the fullMethodDeclarator without return type information.
     */
    public static String methodDeclarator(String fullMethodDeclarator) {
        return StringUtil.prefixBefore(fullMethodDeclarator, ":");
    }

}
