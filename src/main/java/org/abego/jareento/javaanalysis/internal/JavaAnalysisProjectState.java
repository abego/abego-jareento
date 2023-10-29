package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.base.ID;
import org.abego.jareento.base.JareentoSyntax;

import javax.annotation.Syntax;
import java.io.File;
import java.util.Optional;

import static org.abego.jareento.base.JareentoSyntax.CLASS_FILE_PATH_SYNTAX;
import static org.abego.jareento.base.JareentoSyntax.QUALIFIED_TYPE_NAME_SYNTAX;

public interface JavaAnalysisProjectState {
    @ID("JavaMethod")
    IDs methodsOfClass(String className);

    boolean hasMethodOverrideAnnotation(String methodId);

    boolean isConstructor(String methodId);

    boolean isMethodSynthetic(String methodId);

    @ID("JavaMethodCall")
    IDs methodCalls();

    @ID("JavaMethodCall")
    IDs methodCallsInMethod(String methodId);

    @ID("JavaMethodCall")
    IDs methodCallsWithSignature(String methodSignature);

    @ID("JavaMethodCall")
    IDs methodCallsWithSignatureOnClass(String methodSignature, String className);

    String scopeOfMethodCall(String methodCallId);

    String baseScopeOfMethodCall(String methodCallId);

    String signatureOfMethodCall(String methodCallId);
    
    String extendedType(String className);

    IDs classesExtending(String className);

    IDs implementedInterfaces(String className);

    IDs extendedTypes(String typeName);

    IDs methods();

    String returnTypeOfMethod(String methodId);

    IDs idsOfMethodsWithSignature(String signature);
    
    IDs classes();

    IDs classesReferencingClass(
            @Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String typeName);

    IDs classesContainingMethodWithSignature(
            @Syntax(JareentoSyntax.METHOD_SIGNATURE_SYNTAX) String methodSignature);

    @Syntax(CLASS_FILE_PATH_SYNTAX)
    Optional<String> classFileOfClass(
            @Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String typeName);
    
    boolean isInterface(@Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String typeName);

    boolean isClassDeclared(@Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String typeName);

    //TODO name?
    IDs methodSignatureSpecificationsOfClass(
            @Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String className);

    String idOfMethodContainingMethodCall(String methodCallId);

    String signatureOfMethod(String methodId);

    String nameOfMethod(String methodId);

    String idOfMethodDeclaredAs(String methodDeclaratorText);

    String methodDeclaratorTextOfMethodWithId(String methodId);

    String classOfMethod(String methodId);

    String packageOfMethod(String methodId);

    File[] sourceRoots();

    File[] dependencies();
}
