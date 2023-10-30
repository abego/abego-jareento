package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.WithId;

/**
 * A Java type, in all its variants ({@code class}, {@code interface}, 
 * {@code enum},etc), including primitive types and {@code void}.
 * 
 */
public interface JavaType extends WithId {

    boolean isInterface();

    String getName();

    String getSimpleName();

    /**
     * Returns the supertype (e.g. "superclass") of this type.
     * <p>
     * For {@link Object}, interfaces, primitive types or void this will
     * return the Object class. (Notice: this is different from the way
     * {@code Class.getSuperclass()} is defined.)
     */
    JavaType getSuperType();

    JavaTypes getSubTypes();

    JavaTypes getSubTypesAndType();

    JavaTypes getAllSubTypes();

    JavaTypes getAllSubTypesAndType();

    JavaTypes getImplementedInterfaces();

    JavaTypes getExtendedTypes();

    JavaTypes getReferencingTypes();

    JavaMethods getMethods();

    JavaMethodSignatures getMethodSignatures();

    JavaMethodSignatures getInheritedMethodSignatures();

    JavaMethodCalls getMethodCallsToTypeWithSignature(String methodSignature);
    
}
