package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.WithId;

public interface JavaMethodSignature extends WithId {
    default String descriptor() {
        return id();
    }

    JavaMethodCalls methodCallsToClass(String className);
}
