package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.WithId;

public interface JavaMethodSignature extends WithId {
    default String getText() {
        return getId();
    }

    JavaMethodCalls methodCallsOnClass(String className);
}
