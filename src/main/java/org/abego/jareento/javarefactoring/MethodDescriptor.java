package org.abego.jareento.javarefactoring;

/**
 * Describes a method (declaration).
 */
public interface MethodDescriptor {

    /**
     * Returns the name of the method.
     */
    String getMethodName();

    /**
     * Returns the qualified name of the method.
     */
    String getQualifiedMethodName();

    /**
     * Returns the signature of the method.
     */
    String getMethodSignature();

    /**
     * Returns the types of parameters of the method.
     */
    String[] getMethodParameterTypeNames();

    /**
     * Returns the qualified name of the type declaring the method.
     */
    String getTypeDeclaringMethod();

    /**
     * Return the package containing the type the method belongs to.
     */
    String getMethodPackageName();

    String getMethodSignatureWithRawTypes();
}
