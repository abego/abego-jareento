package org.abego.jareento.javarefactoring;

/**
 * Describes an annotation annotating a method declaration.
 */
public interface MethodAnnotationDescriptor extends MethodDescriptor {

    /**
     * Returns the text of the annotation as used in the source code,
     * e.g. {@code "@Override"} or {@code "@SuppressWarnings(\"all\")"} .
     */
    String annotationText();

    /**
     * Returns the qualified name of the annotation type,
     * e.g. {@code "java.lang.Override"} or {@code "java.lang.SuppressWarnings"}.
     */
    String annotationType();

    /**
     * Returns the name of the annotated method.
     */
    String methodName();

    /**
     * Returns the qualified name of the annotated method.
     */
    String qualifiedMethodName();

    /**
     * Returns the signature of the annotated method.
     */
    String methodSignature();

    /**
     * Returns the types of parameters of the annotated method.
     */
    String[] methodParameterTypes();

    /**
     * Returns the qualified name of the type declaring the annotated method.
     */
    String typeDeclaringMethod();

    /**
     * Returns the package containing the type the annotated method belongs to.
     */
    String methodPackageName();
}
