package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.WithId;

public interface JavaMethod extends WithId {
    String getName();

    String getTypeName();

    JavaType getJavaType();

    String getPackage();
    
    String getReturnTypeName();

    String getMethodDeclaratorText();

    JavaMethodSignature getMethodSignature();

    String getMethodSignatureText();

    boolean isConstructor();

    boolean isSynthetic();

    boolean isClassInitializationMethod();

    boolean isObjectInitializationMethod();

    boolean isAnnotatedWithOverride();

    JavaMethods getMethodsDirectlyOverridingMe();

    JavaMethodCalls getMethodCallsToMe();

    JavaMethodCalls getMethodCallsFromMe();
}
