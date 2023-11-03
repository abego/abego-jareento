package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaType;
import org.abego.jareento.javaanalysis.JavaTypes;
import org.abego.jareento.javaanalysis.JavaMethodCalls;
import org.abego.jareento.javaanalysis.JavaMethodSignatures;
import org.abego.jareento.javaanalysis.JavaMethods;

import javax.annotation.Syntax;

import static org.abego.jareento.base.JareentoSyntax.QUALIFIED_TYPE_NAME_SYNTAX;

public interface JavaAnalysisProjectInternal extends JavaAnalysisProject {
    JavaTypes typesReferencingType(
            @Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String typeName);

    boolean isInterface(@Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String typeName);

    JavaType superType(@Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String className);

    JavaTypes subTypes(@Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String className);

    JavaTypes subTypesAndType(String className);

    JavaTypes allSubTypes(String className);

    JavaTypes allSubTypesAndType(String className);

    JavaTypes implementedInterfaces(String className);

    JavaTypes extendedTypes(String typeName);


    JavaTypes typesContainingMethodWithSignature(String methodSignature);

    boolean isClassInitializationMethod(String methodId);

    boolean isObjectInitializationMethod(String methodId);

    /**
     * Returns the text of the MethodDeclarator of the method with the given
     * {@code methodId}.
     */
    String methodDeclaratorTextOfMethodWithId(String methodId);

    String signatureOfMethod(String methodId);

    String nameOfMethod(String methodId);

    JavaMethodSignatures methodSignaturesOfType(String className);

    JavaMethodSignatures inheritedMethodSignaturesOfType(String className);

    String returnTypeOfMethod(String methodId);

    String classOfMethod(String methodId);

    String packageOfMethod(String methodId);

    JavaMethods methodsOfType(String className);

    boolean hasMethodOverrideAnnotation(String methodId);

    boolean isConstructor(String methodId);

    boolean isMethodSynthetic(String methodId);

    JavaMethods methodsDirectlyOverridingMethod(String methodId);

    JavaMethodCalls methodCallsInMethod(String methodId);

    JavaMethodCalls methodCallsToMethod(String methodId);

    JavaMethodCalls methodCallsWithSignature(String methodSignature);

    JavaMethodCalls methodCallsWithSignatureOnType(String methodSignature, String className);

    /**
     * Returns the scope of the method call with the given {@code methodCallId}
     * or an empty string when no scope is defined for that method call.
     */
    String scopeOfMethodCall(String methodCallId);

    /**
     * Returns the base scope of the method call with the given
     * {@code methodCallId} or an empty string when no base scope is defined
     * for that method call.
     * <p>
     * The base scope is identical to the scope of the method when the (scope)
     * class holds a method matching the signature of the method call.
     * Otherwise, it is the "closest" ancestor class of the scope that
     * implements such a method and that would be called when there are no
     * overrides of the method by a subclass of the scope.
     */
    String baseScopeOfMethodCall(String methodCallId);

    String signatureOfMethodCall(String methodCallId);

    String idOfMethodContainingMethodCall(String methodCallId);

    String classContainingMethodCall(String methodCallId);
}
