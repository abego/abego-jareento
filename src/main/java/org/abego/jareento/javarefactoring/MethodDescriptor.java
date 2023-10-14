package org.abego.jareento.javarefactoring;

/**
 * Describes a method (declaration).
 */
public interface MethodDescriptor {

    /**
     * Returns the name of the method.
     */
    String methodName();

    /**
     * Returns the qualified name of the method.
     */
    String qualifiedMethodName();

    /**
     * Returns the signature of the method.
     */
    String methodSignature();

    /**
     * Returns the types of parameters of the method.
     */
    String[] methodParameterTypes();

    /**
     * Returns the qualified name of the type declaring the method.
     */
    String typeDeclaringMethod();

    /**
     * Return the package containing the type the method belongs to.
     */
    String methodPackageName();

    String methodSignatureWithRawTypes();
}
