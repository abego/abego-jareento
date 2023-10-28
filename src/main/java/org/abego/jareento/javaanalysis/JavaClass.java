package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.WithId;

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

    JavaMethodSignatures getMethodSignatures();
}
