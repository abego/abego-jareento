package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaType;
import org.abego.jareento.javaanalysis.JavaTypes;
import org.abego.jareento.javaanalysis.JavaMethodCalls;
import org.abego.jareento.javaanalysis.JavaMethodSignatures;
import org.abego.jareento.javaanalysis.JavaMethods;

import javax.annotation.Syntax;

import java.util.stream.Collectors;

import static org.abego.jareento.base.JareentoSyntax.QUALIFIED_TYPE_NAME_SYNTAX;

public interface JavaAnalysisProjectInternal extends JavaAnalysisProject {
    //TODO: hide from API
    JavaTypes classesReferencingClass(
            @Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String classname);

    //TODO: hide from API
    boolean isInterface(@Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String classname);

    //TODO: hide from API
    JavaType superClass(@Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String className);

    //TODO: hide from API
    JavaTypes subClasses(@Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String className);

    //TODO: hide from API
    JavaTypes subClassesAndClass(String className);

    //TODO: hide from API
    JavaTypes allSubClasses(String className);

    //TODO: hide from API
    JavaTypes allSubClassesAndClass(String className);

    //TODO: hide from API
    JavaTypes implementedInterfaces(String className);

    //TODO: hide from API
    JavaTypes extendedTypes(String typeName);


    //TODO: hide from API
    JavaTypes classesContainingMethodWithSignature(String methodSignature);

    //TODO: hide from API
    boolean isClassInitializationMethod(String methodId);

    //TODO: hide from API
    boolean isObjectInitializationMethod(String methodId);

    //TODO: hide from API
    /**
     * Returns the text of the MethodDeclarator of the method with the given
     * {@code methodId}.
     */
    String methodDeclaratorTextOfMethodWithId(String methodId);

    //TODO: hide from API
    String signatureOfMethod(String methodId);

    //TODO: hide from API
    String nameOfMethod(String methodId);

    //TODO: hide from API
    JavaMethodSignatures methodSignaturesOfClass(String className);

    //TODO: hide from API
    JavaMethodSignatures inheritedMethodSignaturesOfClass(String className);

    //TODO: hide from API
    String returnTypeOfMethod(String methodId);

    //TODO: hide from API
    String classOfMethod(String methodId);

    //TODO: hide from API
    String packageOfMethod(String methodId);

    //TODO: hide from API
    JavaMethods methodsOfClass(String className);

    //TODO: hide from API
    boolean hasMethodOverrideAnnotation(String methodId);

    //TODO: hide from API
    boolean isConstructor(String methodId);

    //TODO: hide from API
    boolean isMethodSynthetic(String methodId);

    //TODO: hide from API
    JavaMethods methodsDirectlyOverridingMethod(String methodId);

    //TODO: hide from API
    JavaMethodCalls methodCallsInMethod(String methodId);

    //TODO: hide from API
    JavaMethodCalls methodCallsToMethod(String methodId);

    //TODO: hide from API
    JavaMethodCalls methodCallsWithSignature(String methodSignature);

    //TODO: hide from API
    JavaMethodCalls methodCallsWithSignatureOnClass(String methodSignature, String className);

    //TODO: hide from API
    /**
     * Returns the scope of the method call with the given {@code methodCallId}
     * or an empty string when no scope is defined for that method call.
     */
    String scopeOfMethodCall(String methodCallId);

    //TODO: hide from API
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

    //TODO: hide from API
    String signatureOfMethodCall(String methodCallId);

    //TODO: hide from API
    String idOfMethodContainingMethodCall(String methodCallId);

    //TODO: hide from API
    String classContainingMethodCall(String methodCallId);

    //TODO: hide from API
    default String methodCallsSummary(JavaMethodCalls methodCalls) {
        return methodCalls.idStream()
                .map(this::signatureOfMethodCall)
                .sorted()
                .collect(Collectors.joining(";"));
    }    
}
