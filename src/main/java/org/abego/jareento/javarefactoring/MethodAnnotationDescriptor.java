package org.abego.jareento.javarefactoring;

import org.abego.jareento.base.JareentoSyntax;

import javax.annotation.Syntax;

/**
 * Describes an annotation annotating a method declaration.
 */
public interface MethodAnnotationDescriptor extends MethodDescriptor {

    /**
     * Returns the text of the annotation as used in the source code,
     * e.g. {@code "@Override"} or {@code "@SuppressWarnings(\"all\")"} .
     */
    String getAnnotationText();

    /**
     * Returns the qualified name of the annotation type,
     * e.g. {@code "java.lang.Override"} or {@code "java.lang.SuppressWarnings"}.
     */
    @Syntax(JareentoSyntax.QUALIFIED_TYPE_NAME_SYNTAX)
    String getAnnotationTypeName();

    /**
     * Returns the name of the annotated method.
     */
    String getMethodName();

    /**
     * Returns the qualified name of the annotated method.
     */
    String getQualifiedMethodName();

    /**
     * Returns the signature of the annotated method.
     */
    String getMethodSignature();

    /**
     * Returns the type names of the parameters of the annotated method.
     */
    String[] getMethodParameterTypeNames();

    /**
     * Returns the qualified name of the type declaring the annotated method.
     */
    String getTypeDeclaringMethod();

    /**
     * Returns the package containing the type the annotated method belongs to.
     */
    String getMethodPackageName();
}
