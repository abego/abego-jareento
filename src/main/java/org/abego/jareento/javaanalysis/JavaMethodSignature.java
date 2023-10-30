package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.WithId;

public interface JavaMethodSignature extends WithId {
    default String getText() {
        return getId();
    }

    /**
     * Returns the {@link JavaTypes} containing a method with this signature.
     */
    JavaTypes getTypesWithMethod();

    /**
     * Returns the {@link JavaMethodCalls} to methods with this signature.
     */
    JavaMethodCalls getMethodCalls();

    /**
     * Returns the {@link JavaMethodCalls} to the method with this signature
     * in the class with the given {@code typeName}.
     */
    JavaMethodCalls getMethodCallsToType(String typeName);
}
