package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.base.ID;
import org.abego.jareento.base.JareentoSyntax;

import javax.annotation.Syntax;
import java.io.File;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.OptionalInt;

import static org.abego.jareento.base.JareentoSyntax.CLASS_FILE_PATH_SYNTAX;
import static org.abego.jareento.base.JareentoSyntax.FILE_PATH_SYNTAX;
import static org.abego.jareento.base.JareentoSyntax.JAVA_FILE_PATH_SYNTAX;
import static org.abego.jareento.base.JareentoSyntax.MD5_SYNTAX;
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

    String fileOfMethodCall(String methodCallId);

    String extendedType(String className);

    IDs classesExtending(String className);

    IDs implementedInterfaces(String className);

    IDs extendedTypes(String typeName);

    IDs methods();

    String returnTypeOfMethod(String methodId);

    IDs idsOfMethodsWithSignature(String signature);

    //TODO: also introduce JavaFiles interface?!
    String[] javaFiles();

    IDs classes();

    IDs classesOfJavaFile(@Syntax(FILE_PATH_SYNTAX) String file);

    IDs classesReferencingClass(
            @Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String classname);

    IDs classesContainingMethodWithSignature(
            @Syntax(JareentoSyntax.QUALIFIED_METHOD_SIGNATURE_SYNTAX) String methodSignature);

    boolean isJavaFile(@Syntax(FILE_PATH_SYNTAX) String name);

    @Syntax(JAVA_FILE_PATH_SYNTAX)
    Optional<String> javaFileOfClass(
            @Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String classname);

    @Syntax(CLASS_FILE_PATH_SYNTAX)
    Optional<String> classFileOfClass(
            @Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String classname);

    OptionalInt bytecodeSizeOfClass(
            @Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String classname);

    @Syntax(MD5_SYNTAX)
    Optional<String> md5OfClass(
            @Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String classname);

    boolean isInterface(@Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String classname);

    boolean isClassDeclared(@Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String classname);

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

    void dump(PrintWriter writer);

}
