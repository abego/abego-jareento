package org.abego.jareento.javaanalysis;

import javax.annotation.Syntax;
import java.io.File;
import java.util.function.BiConsumer;
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
    JavaTypes implementedInterfaces(String className);

    //TODO: hide from API
    JavaTypes extendedTypes(String typeName);

    //endregion
    //region Method-related

    /**
     * Returns all methods contained in this project.
     */
    JavaMethods getMethods();

    JavaClasses classesContainingMethodWithSignature(String methodSignature);

    boolean isClassInitializationMethod(String methodId);

    boolean isObjectInitializationMethod(String methodId);

    /**
     * Returns the text of the MethodDeclarator of the method with the given
     * {@code methodId}.
     */
    String methodDeclaratorTextOfMethodWithId(String methodId);

    /**
     * Returns the id of the method with the given {@code methodDeclaratorText},
     * or throws an exception when the project does not contain such a method.
     * <p>
     * See also {@link #methodDeclaratorTextOfMethodWithId(String)}
     **/
    String idOfMethodDeclaredAs(String methodDeclaratorText);

    String signatureOfMethod(String methodId);

    String nameOfMethod(String methodId);

    JavaMethodSignatures methodSignaturesOfClass(String className);

    JavaMethodSignatures inheritedMethodSignaturesOfClass(String className);

    String returnTypeOfMethod(String methodId);

    String classOfMethod(String methodId);

    String packageOfMethod(String methodId);

    //TODO: hide from API
    JavaMethods methodsOfClass(String className);

    boolean hasMethodOverrideAnnotation(String methodId);

    boolean isConstructor(String methodId);

    boolean isMethodSynthetic(String methodId);

    JavaMethods methodsDirectlyOverridingMethod(String methodId);

    //endregion
    //region MethodCall-related
    JavaMethodCalls methodCalls();

    JavaMethodCalls methodCallsInMethod(String methodId);

    JavaMethodCalls methodCallsToMethod(String methodId);

    JavaMethodCalls methodCallsWithSignature(String methodSignature);

    JavaMethodCalls methodCallsWithSignatureOnClass(String methodSignature, String className);

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

    String fileOfMethodCall(String methodCallId);

    String idOfMethodContainingMethodCall(String methodCallId);

    String classContainingMethodCall(String methodCallId);

    default void withMethodCallsToMethodsOfClassDo(String className, BiConsumer<String, JavaMethodCalls> calledMethodAndMethodCalls) {
        methodsOfClass(className)
                .idStream().sorted().forEach(calledMethodId -> {
                    JavaMethodCalls methodCalls = methodCallsWithSignatureOnClass(signatureOfMethod(calledMethodId), className);
                    calledMethodAndMethodCalls.accept(calledMethodId, methodCalls);
                });
    }

    default String methodCallsSummary(JavaMethodCalls methodCalls) {
        return methodCalls.idStream()
                .map(this::signatureOfMethodCall)
                .sorted()
                .collect(Collectors.joining(";"));
    }

    //endregion
}
