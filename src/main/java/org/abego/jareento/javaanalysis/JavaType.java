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
     * Returns the superclass of this class.
     * <p>
     * For {@link Object}, interfaces, primitive types or void this will
     * return the Object class. (Notice: this is different from the way
     * {@code Class.getSuperclass()} is defined.)
     */
    JavaType getSuperclass();

    JavaTypes getSubClasses();

    JavaTypes getSubClassesAndClass();

    JavaTypes getAllSubClasses();

    JavaTypes getAllSubClassesAndClass();

    JavaTypes getImplementedInterfaces();

    JavaTypes getExtendedTypes();

    JavaTypes getReferencingClasses();

    JavaMethods getMethods();

    JavaMethodSignatures getMethodSignatures();

    JavaMethodSignatures getInheritedMethodSignatures();

    JavaMethodCalls getMethodCallsToClassWithSignature(String methodSignature);
    
}
