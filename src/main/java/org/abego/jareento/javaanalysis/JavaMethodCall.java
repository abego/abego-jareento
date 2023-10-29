package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.WithId;

public interface JavaMethodCall extends WithId {
    JavaMethodSignature getMethodSignature();

    /**
     * Returns the scope of this method call
     * or an empty string when no scope is defined for this method call.
     */
    String getScope();

    /**
     * Returns the base scope of this method call 
     * or an empty string when no base scope is defined
     * for this method call.
     * <p>
     * The base scope is identical to the scope of the method when the (scope)
     * class holds a method matching the signature of the method call.
     * Otherwise, it is the "closest" ancestor class of the scope that
     * implements such a method and that would be called when there are no
     * overrides of the method by a subclass of the scope.
     */
    String getBaseScope();

    /**
     * Returns the {@link JavaMethod} containing this method call.
     */
    JavaMethod getCallingMethod();

    /**
     * Returns the {@link JavaType} containing this method call.
     */
    JavaType getCallingType();

}
