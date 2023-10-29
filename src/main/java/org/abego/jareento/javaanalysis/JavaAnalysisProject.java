package org.abego.jareento.javaanalysis;

import javax.annotation.Syntax;
import java.io.File;
import java.util.stream.Collectors;

import static org.abego.jareento.base.JareentoSyntax.QUALIFIED_TYPE_NAME_SYNTAX;

/**
 * A collection of Java language elements (classes, methods, ...) and their
 * relations, to be used for (static) program analysis.
 */
public interface JavaAnalysisProject {

    //region Project-related
    File[] getSourceRoots();

    File[] getDependencies();

    //endregion
    //region Class-related

    JavaClasses getClasses();

    boolean hasClassWithName(@Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String classname);

    JavaClass getClassWithName(@Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String classname);
    
    JavaClasses getAllSubClasses(JavaClasses classes);

    JavaClasses getAllSubClassesAndClasses(JavaClasses classes);

    //TODO: hide from API
    JavaClasses classesReferencingClass(
            @Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String classname);

    //TODO: hide from API
    boolean isInterface(@Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String classname);

    //TODO: hide from API
    JavaClass superClass(@Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String className);

    //TODO: hide from API
    JavaClasses subClasses(@Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String className);

    //TODO: hide from API
    JavaClasses subClassesAndClass(String className);

    //TODO: hide from API
    JavaClasses allSubClasses(String className);

    //TODO: hide from API
    JavaClasses allSubClassesAndClass(String className);

    //TODO: hide from API
    JavaClasses implementedInterfaces(String className);

    //TODO: hide from API
    JavaClasses extendedTypes(String typeName);

    //endregion
    //region Method-related

    /**
     * Returns all methods contained in this project.
     */
    JavaMethods getMethods();
    
    JavaMethod getMethodWithMethodDeclarator(String methodDeclaratorText);

    //TODO: hide from API
    JavaClasses classesContainingMethodWithSignature(String methodSignature);

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

    //endregion
    //region MethodCall-related
    JavaMethodCalls getMethodCalls();

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
    //endregion
}
