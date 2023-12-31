package org.abego.jareento.javaanalysis.internal;

import javax.annotation.Syntax;
import java.io.File;

import static org.abego.jareento.base.JareentoSyntax.QUALIFIED_TYPE_OR_ARRAY_NAME_SYNTAX;

public interface JavaAnalysisProjectStateBuilder {
    void setSourceRoots(File[] sourceRoots);

    void addClass(@Syntax(QUALIFIED_TYPE_OR_ARRAY_NAME_SYNTAX) String name);

    void addReference(String from, String to);
    
    void setIsInterfaceOfClass(String typeName, boolean value);

    void addTypeExtends(String typeName, String otherTypename);

    void addTypeImplements(String typeName, String otherTypename);

    String addMethod(String typeName, String methodSignature, String returnType);

    String getMethodId(String typeName, String methodSignature, String returnType);

    void setMethodHasOverride(String methodId, boolean hasOverride);

    void setMethodIsSynthetic(String methodId, boolean value);

    /**
     * Adds a method call.
     *
     * @param callKind                "invoke..."
     * @param callScopeTypename       the scope of the call. When empty it can be defined later using {@link #setMethodCallScope(String, String)}
     * @param calledMethodSignature   the signature of the called method
     * @param callingMethodTypename   the class containing the calling method
     * @param callingMethodSignature  the signature of the calling method
     * @param callingMethodReturnType the return type of the calling method
     * @param locationInCallingMethod the location of the call within the calling method. Typically, an integer, used to disambiguate the call from other calls in the method.
     * @return the id of the method call
     */
    String addMethodCall(
            String callKind,
            String callScopeTypename, String calledMethodSignature,
            String callingMethodTypename, String callingMethodSignature,
            String callingMethodReturnType, String locationInCallingMethod);

    void setMethodCallScope(String methodCallId, String callScopeTypename);

    void setMethodCallBaseScope(String methodCallId, String baseScopeTypename);

    void addClassHasRawType(String typeName, String rawClassname);

    void addClassInClassFile(String typeName, String classfileName);

    void setClassIsDeclared(String typeName, boolean value);

    JavaAnalysisProjectStateWithSave build();

    void setDependencies(File[] files);
}
