package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.WithId;

public interface JavaMethod extends WithId {
    String getName();

    String getSignature();

    String getReturnTypeName();

    String getClassName();

    String getPackage();

    String getMethodDeclaratorText();

    boolean isConstructor();

    boolean isSynthetic();

    boolean isClassInitializationMethod();

    boolean isObjectInitializationMethod();

    JavaMethodCalls getMethodCallsToMe();

    JavaMethodCalls getMethodCallsFromMe();
}
