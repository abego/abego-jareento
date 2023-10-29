package org.abego.jareento.shared;

import org.abego.commons.lang.StringUtil;
import org.abego.jareento.base.JareentoSyntax;
import org.abego.jareento.javaanalysis.JavaMethodDeclarator;

import javax.annotation.Syntax;

public class JavaMethodDeclaratorUtil {
    @Syntax(JareentoSyntax.METHOD_DECLARATOR_SYNTAX)
    public static String methodDeclaratorText(String typeName, String methodSignature, String returnType) {
        return typeName + "#" + methodSignature + ":" + returnType;
    }

    @Syntax(JareentoSyntax.SIMPLE_METHOD_DECLARATOR_SYNTAX)
    public static String simpleMethodDeclaratorText(String typeName, String methodSignature) {
        return typeName + "#" + methodSignature;
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

    public static JavaMethodDeclarator newJavaMethodDeclarator(
            @Syntax(JareentoSyntax.METHOD_DECLARATOR_SYNTAX) String methodDeclarator) {
        return JavaMethodDeclaratorImpl.newJavaMethodDeclaratorImpl(methodDeclarator);
    }

    public static JavaMethodDeclarator newJavaMethodDeclarator(
            String typeName, String methodSignature, String returnType) {
        return JavaMethodDeclaratorImpl.newJavaMethodDeclaratorImpl(
                methodDeclaratorText(typeName, methodSignature, returnType));
    }
}
