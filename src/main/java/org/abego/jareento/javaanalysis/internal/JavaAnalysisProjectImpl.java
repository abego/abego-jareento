package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.base.JareentoException;
import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaMethod;
import org.abego.jareento.javaanalysis.JavaMethodCalls;
import org.abego.jareento.javaanalysis.JavaMethodSignatures;
import org.abego.jareento.javaanalysis.JavaMethods;
import org.abego.jareento.javaanalysis.JavaType;
import org.abego.jareento.javaanalysis.JavaTypes;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static org.abego.jareento.javaanalysis.internal.EmptyIDs.emptyIDs;
import static org.abego.jareento.javaanalysis.internal.IDsImpl.newIDs;
import static org.abego.jareento.javaanalysis.internal.JavaMethodCallsImpl.newJavaMethodCalls;
import static org.abego.jareento.javaanalysis.internal.JavaMethodSignaturesImpl.newJavaMethodSignatures;
import static org.abego.jareento.javaanalysis.internal.JavaTypeImpl.newJavaType;
import static org.abego.jareento.shared.JavaMethodDeclaratorUtil.newJavaMethodDeclarator;

public class JavaAnalysisProjectImpl implements JavaAnalysisProjectInternal {
    private static final Map<String, String> OBJECT_METHOD_SIGNATURES_TO_RETURN_TYPE = newObjectMethodSignatures();

    public static JavaAnalysisProject newJavaAnalysisProjectFromInput(
            URI uri,
            JavaAnalysisProjectInput input,
            File[] sourceRoots,
            File[] dependencies,
            Consumer<String> problemConsumer) {
        JavaAnalysisProjectStateBuilder builder =
                JavaAnalysisInternalFactories.newJavaAnalysisProjectBuilder(uri);
        builder.setSourceRoots(sourceRoots);
        builder.setDependencies(dependencies);
        input.feed(builder, problemConsumer);
        JavaAnalysisProjectStateWithSave state = builder.build();
        state.save();
        return newJavaAnalysisProject(state);
    }

    private static Map<String, String> newObjectMethodSignatures() {
        Map<String, String> result = new HashMap<>();
        result.put("clone()", "java.lang.Object");
        result.put("equals(java.lang.Object)", "boolean");
        result.put("finalize()", "void");
        result.put("getClass()", "java.lang.Class");
        result.put("hashCode()", "int");
        result.put("notify()", "void");
        result.put("notifyAll()", "void");
        result.put("toString()", "java.lang.String");
        result.put("wait()", "void");
        result.put("wait(long)", "void");
        result.put("wait(long,int)", "void");
        return result;
    }

    private final JavaAnalysisProjectState state;

    private JavaAnalysisProjectImpl(JavaAnalysisProjectState state) {
        this.state = state;
    }

    static JavaAnalysisProjectInternal newJavaAnalysisProject(JavaAnalysisProjectState state) {
        return new JavaAnalysisProjectImpl(state);
    }

    public static JavaAnalysisProjectInternal toInternal(JavaAnalysisProject project) {
        if (project instanceof JavaAnalysisProjectInternal internalProject) {
            return internalProject;
        }
        throw new JareentoException(
                "Invalid JavaAnalysisProject %s".formatted(project));
    }

    @Override
    public JavaMethods methodsOfType(String className) {
        return JavaMethodsImpl.newJavaMethods(state.methodsOfClass(className), this);
    }

    @Override
    public boolean hasMethodOverrideAnnotation(String methodId) {
        return state.hasMethodOverrideAnnotation(methodId);
    }

    @Override
    public boolean isConstructor(String methodId) {
        return state.isConstructor(methodId);
    }

    @Override
    public boolean isMethodSynthetic(String methodId) {
        return state.isMethodSynthetic(methodId);
    }

    @Override
    public JavaMethods methodsDirectlyOverridingMethod(String methodId) {
        return JavaMethodsImpl.newJavaMethods(newIDs(() -> {
            String signature = signatureOfMethod(methodId);
            Set<String> result = new HashSet<>();
            for (String candidateMethodId : state.idsOfMethodsWithSignature(signature)
                    .toSet()) {
                // don't include the original method itself, or synthetic methods
                if (candidateMethodId.equals(methodId) || isMethodSynthetic(candidateMethodId)) {
                    continue;
                }

                @Nullable String overriddenMethod =
                        idOfMethodOverriddenByMethodOrNull(candidateMethodId);
                if (methodId.equals(overriddenMethod)) {
                    result.add(candidateMethodId);
                }
            }
            return result;
        }), this);
    }

    /**
     * Returns the id of the method overridden by the method with the given
     * {@code methodId}, or {@code null} when no method is overridden.
     */
    @Nullable
    private String idOfMethodOverriddenByMethodOrNull(String methodId) {
        return idOfMethodWithSignatureInheritedByClassOrNull(
                signatureOfMethod(methodId), classOfMethod(methodId));
    }

    /**
     * Returns the id of the first method in the inheritance hierarchy below
     * the given class that has the given {@code methodSignature},
     * or {@code null} when no method such method exists.
     */
    @Nullable
    private String idOfMethodWithSignatureInheritedByClassOrNull(
            String methodSignature, String className) {
        JavaTypes extendedTypes = extendedTypes(className);
        JavaTypes interfaces = implementedInterfaces(className);

        JavaTypes types = extendedTypes.unitedWith(interfaces);

        if (types.getSize() == 0) {
            @Nullable
            String returnType =
                    OBJECT_METHOD_SIGNATURES_TO_RETURN_TYPE.get(methodSignature);
            String fullClassname = Object.class.getName();
            return returnType != null
                    ? idOfMethodDeclaredAs(newJavaMethodDeclarator(fullClassname, methodSignature, returnType).getText())
                    : null;

        } else {
            for (String typeName : types.getNames()) {
                if (methodSignaturesOfType(typeName).contains(methodSignature)) {
                    return idOfMethodOfClassWithSignature(typeName, methodSignature);
                } else {
                    @Nullable String id = idOfMethodWithSignatureInheritedByClassOrNull(
                            methodSignature, typeName);
                    if (id != null) {
                        return id;
                    }
                }
            }
        }
        return null;
    }

    private String idOfMethodOfClassWithSignature(String classId, String methodSignature) {
        //TODO or use the methodsOfClass(...)? More local.
        for (String methodId : state.idsOfMethodsWithSignature(methodSignature)
                .toSet()) {
            if (classOfMethod(methodId).equals(classId)) {
                return methodId;
            }
        }
        throw new JareentoException(String.format(
                "No method with signature `%s` defined in class %s",
                methodSignature, classId));
    }

    @Override
    public JavaMethodCalls getMethodCalls() {
        return newJavaMethodCalls(state.methodCalls(), this);
    }

    @Override
    public JavaMethodCalls methodCallsInMethod(String methodId) {
        return newJavaMethodCalls(state.methodCallsInMethod(methodId), this);
    }

    @Override
    public JavaMethodCalls methodCallsToMethod(String methodId) {
        return methodCallsWithSignatureOnType(
                signatureOfMethod(methodId), classOfMethod(methodId));
    }

    @Override
    public JavaMethodCalls methodCallsWithSignature(String methodSignature) {
        return newJavaMethodCalls(state.methodCallsWithSignature(methodSignature), this);
    }

    @Override
    public JavaMethodCalls methodCallsWithSignatureOnType(String methodSignature, String className) {
        return newJavaMethodCalls(state.methodCallsWithSignatureOnClass(methodSignature, className), this);
    }

    @Override
    public String scopeOfMethodCall(String methodCallId) {
        return state.scopeOfMethodCall(methodCallId);
    }

    @Override
    public String baseScopeOfMethodCall(String methodCallId) {
        return state.baseScopeOfMethodCall(methodCallId);
    }

    @Override
    public String signatureOfMethodCall(String methodCallId) {
        return state.signatureOfMethodCall(methodCallId);
    }

    @Override
    public String idOfMethodContainingMethodCall(String methodCallId) {
        return state.idOfMethodContainingMethodCall(methodCallId);
    }

    @Override
    public String classContainingMethodCall(String methodCallId) {
        return classOfMethod(idOfMethodContainingMethodCall(methodCallId));
    }

    @Override
    public boolean isClassInitializationMethod(String methodId) {
        return signatureOfMethod(methodId).equals("\"<cinit>\"()");
    }

    @Override
    public boolean isObjectInitializationMethod(String methodId) {
        return signatureOfMethod(methodId).equals("\"<init>\"()");
    }

    @Override
    public JavaType superType(String typeName) {
        return newJavaType(
                isInterface(typeName)
                        ? "java.lang.Object" : state.extendedType(typeName),
                this);
    }

    @Override
    public JavaTypes subTypes(String typeName) {
        if (isInterface(typeName)) {
            return newJavaTypes(emptyIDs());
        }

        IDs ids = state.typesExtending(typeName);
        return newJavaTypes(ids);
    }

    private JavaTypes newJavaTypes(IDs ids) {
        return JavaTypesImpl.newJavaTypes(ids, this);
    }


    @Override
    public JavaTypes subTypesAndType(String className) {
        return subTypes(className).unitedWithTypeNamed(className);
    }

    @Override
    public JavaTypes allSubTypes(String className) {
        if (isInterface(className)) {
            return newJavaTypes(emptyIDs());
        }
        return JavaTypesImpl.newJavaTypes(newIDs(() -> {
            Set<String> result = new HashSet<>();
            addAllSubTypes(className, result);
            return result;
        }), this);
    }

    @Override
    public JavaTypes allSubTypesAndType(String className) {
        return allSubTypes(className).unitedWithTypeNamed(className);
    }

    @Override
    public JavaTypes getAllSubTypes(JavaTypes types) {
        return JavaTypesImpl.newJavaTypes(newIDs(() -> {
            Set<String> result = new HashSet<>();
            types.idStream().forEach(c -> addAllSubTypes(c, result));
            return result;
        }), this);
    }

    @Override
    public JavaTypes getAllSubTypesAndTypes(JavaTypes types) {
        return getAllSubTypes(types).unitedWith(types);
    }

    @Override
    public JavaTypes implementedInterfaces(String className) {
        return newJavaTypes(state.implementedInterfaces(className));
    }

    @Override
    public JavaTypes extendedTypes(String typeName) {
        return newJavaTypes(state.extendedTypes(typeName));
    }

    @Override
    public JavaMethods getMethods() {
        return JavaMethodsImpl.newJavaMethods(state.methods(), this);
    }

    @Override
    public JavaMethod getMethodWithMethodDeclarator(String methodDeclaratorText) {
        return JavaMethodImpl.newJavaMethod(
                idOfMethodDeclaredAs(methodDeclaratorText), this);
    }


    @Override
    public String methodDeclaratorTextOfMethodWithId(String methodId) {
        return state.methodDeclaratorTextOfMethodWithId(methodId);
    }

    @Override
    public JavaTypes typesContainingMethodWithSignature(String methodSignature) {
        return newJavaTypes(state.typesContainingMethodWithSignature(methodSignature));
    }

    public JavaMethodSignatures methodSignaturesOfType(String className) {
        return newJavaMethodSignatures(
                state.methodSignatureSpecificationsOfClass(className), this);
    }

    @Override
    public JavaMethodSignatures inheritedMethodSignaturesOfType(String className) {
        return newJavaMethodSignatures(newIDs(() -> {
            Set<String> result = new HashSet<>();
            addInheritedMethodSignatureSpecificationsOfClass(result, className);
            return result;
        }), this);
    }

    private void addInheritedMethodSignatureSpecificationsOfClass(
            Set<String> collection, String className) {
        JavaTypes extendedTypes = extendedTypes(className);
        JavaTypes interfaces = implementedInterfaces(className);

        JavaTypes types = extendedTypes.unitedWith(interfaces);
        if (types.getSize() == 0) {
            collection.addAll(OBJECT_METHOD_SIGNATURES_TO_RETURN_TYPE.keySet());
        } else {
            types.idStream().forEach(t -> {
                collection.addAll(state.methodSignatureSpecificationsOfClass(t)
                        .toSet());
                addInheritedMethodSignatureSpecificationsOfClass(collection, t);
            });
        }
    }

    @Override
    public String signatureOfMethod(String methodId) {
        return state.signatureOfMethod(methodId);
    }

    @Override
    public String nameOfMethod(String methodId) {
        return state.nameOfMethod(methodId);
    }

    @Override
    public String returnTypeOfMethod(String methodId) {
        return state.returnTypeOfMethod(methodId);
    }

    @Override
    public String classOfMethod(String methodId) {
        return state.classOfMethod(methodId);
    }

    @Override
    public String packageOfMethod(String methodId) {
        return state.packageOfMethod(methodId);
    }

    @Override
    public File[] getSourceRoots() {
        return state.sourceRoots();
    }

    @Override
    public File[] getDependencies() {
        return state.dependencies();
    }

    @Override
    public JavaAnalysisFiles getJavaAnalysisFiles() {
        return JavaAnalysisFilesImpl.newJavaAnalysisFilesImpl(
                getSourceRoots(), getDependencies());
    }

    @Override
    public JavaTypes getTypes() {
        return newJavaTypes(state.types());
    }

    @Override
    public JavaTypes typesReferencingType(String typeName) {
        return newJavaTypes(state.typesReferencingClass(typeName));
    }

    @Override
    public JavaType getTypeWithName(String typeName) {
        return newJavaType(typeName, this);
    }

    @Override
    public boolean isInterface(String typeName) {
        return state.isInterface(typeName);
    }

    @Override
    public boolean hasTypeWithName(String typeName) {
        return state.isClassDeclared(typeName);
    }

    private void addAllSubTypes(String className, Set<String> target) {
        if (!isInterface(className)) {
            subTypes(className).idStream().forEach(c -> {
                if (!target.contains(c)) {
                    addAllSubTypes(c, target);
                }
                target.add(c);
            });
        }
    }

    /**
     * Returns the id of the method with the given {@code methodDeclaratorText},
     * or throws an exception when the project does not contain such a method.
     * <p>
     * See also {@link #methodDeclaratorTextOfMethodWithId(String)}
     **/
    private String idOfMethodDeclaredAs(String methodDeclaratorText) {
        return state.idOfMethodDeclaredAs(methodDeclaratorText);
    }
}
