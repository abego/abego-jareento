package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.WithId;

/**
 * A Java class, in all its variants ({@code class}, {@code interface}, 
 * {@code enum},etc), including primitive types and {@code void}.
 * 
 */
public interface JavaClass extends WithId {

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
    JavaClass getSuperclass();

    JavaClasses getSubClasses();

    JavaClasses getSubClassesAndClass();

    JavaClasses getAllSubClasses();

    JavaClasses getAllSubClassesAndClass();

    JavaTypes getImplementedInterfaces();

    JavaTypes getExtendedTypes();

    JavaClasses getReferencingClasses();

    JavaMethods getMethods();

    JavaMethodSignatures getMethodSignatures();

    JavaMethodSignatures getInheritedMethodSignatures();

    JavaMethodCalls getMethodCallsToClassWithSignature(String methodSignature);
    
}
