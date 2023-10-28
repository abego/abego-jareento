package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.base.JareentoException;
import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaClass;
import org.abego.jareento.javaanalysis.JavaClasses;
import org.abego.jareento.javaanalysis.JavaMethodCalls;
import org.abego.jareento.javaanalysis.JavaMethodSignatures;
import org.abego.jareento.javaanalysis.JavaMethods;
import org.abego.jareento.javaanalysis.JavaTypes;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import static org.abego.jareento.javaanalysis.internal.EmptyIDs.emptyIDs;
import static org.abego.jareento.javaanalysis.internal.IDsImpl.newIDs;
import static org.abego.jareento.javaanalysis.internal.JavaClassImpl.newJavaClass;
import static org.abego.jareento.javaanalysis.internal.JavaMethodCallsImpl.newJavaMethodCalls;
import static org.abego.jareento.javaanalysis.internal.JavaMethodSignaturesImpl.newJavaMethodSignatures;
import static org.abego.jareento.javaanalysis.internal.JavaTypesImpl.newJavaTypes;
import static org.abego.jareento.shared.JavaMethodDeclaratorUtil.newJavaMethodDeclarator;

public class JavaAnalysisProjectImpl implements JavaAnalysisProject {
    private static final Map<String, String> OBJECT_METHOD_SIGNATURES_TO_RETURN_TYPE = newObjectMethodSignatures();

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

    static JavaAnalysisProject newJavaAnalysisProject(JavaAnalysisProjectState state) {
        return new JavaAnalysisProjectImpl(state);
    }

    @Override
    public JavaMethods methodsOfClass(String className) {
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
                    .set()) {
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
            for (String classname : types.names()) {
                if (methodSignaturesOfClass(classname).contains(methodSignature)) {
                    return idOfMethodOfClassWithSignature(classname, methodSignature);
                } else {
                    @Nullable String id = idOfMethodWithSignatureInheritedByClassOrNull(
                            methodSignature, classname);
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
                .set()) {
            if (classOfMethod(methodId).equals(classId)) {
                return methodId;
            }
        }
        throw new JareentoException(String.format(
                "No method with signature `%s` defined in class %s",
                methodSignature, classId));
    }

    @Override
    public JavaMethodCalls methodCalls() {
        return newJavaMethodCalls(state.methodCalls(), this);
    }

    @Override
    public JavaMethodCalls methodCallsInMethod(String methodId) {
        return newJavaMethodCalls(state.methodCallsInMethod(methodId), this);
    }

    @Override
    public JavaMethodCalls methodCallsToMethod(String methodId) {
        return methodCallsWithSignatureOnClass(
                signatureOfMethod(methodId), classOfMethod(methodId));
    }

    @Override
    public JavaMethodCalls methodCallsWithSignature(String methodSignature) {
        return newJavaMethodCalls(state.methodCallsWithSignature(methodSignature), this);
    }

    @Override
    public JavaMethodCalls methodCallsWithSignatureOnClass(String methodSignature, String className) {
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
    public String fileOfMethodCall(String methodCallId) {
        return state.fileOfMethodCall(methodCallId);
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
    public String superClass(String className) {
        if (isInterface(className)) {
            return "java.lang.Object";
        }
        return state.extendedType(className);
    }

    @Override
    public JavaClasses subClasses(String className) {
        if (isInterface(className)) {
            return newJavaClasses(emptyIDs());
        }

        IDs ids = state.classesExtending(className);
        return newJavaClasses(ids);
    }

    private JavaClasses newJavaClasses(IDs ids) {
        return JavaClassesImpl.newJavaClasses(ids, this);
    }


    @Override
    public JavaClasses subClassesAndClass(String className) {
        return subClasses(className).unitedWithClassNamed(className);
    }

    @Override
    public JavaClasses allSubClasses(String className) {
        if (isInterface(className)) {
            return newJavaClasses(emptyIDs());
        }
        return JavaClassesImpl.newJavaClasses(newIDs(() -> {
            Set<String> result = new HashSet<>();
            addAllSubClasses(className, result);
            return result;
        }), this);
    }

    @Override
    public JavaClasses allSubClassesAndClass(String className) {
        return allSubClasses(className).unitedWithClassNamed(className);
    }

    @Override
    public JavaClasses allSubClasses(JavaClasses classes) {
        return JavaClassesImpl.newJavaClasses(newIDs(() -> {
            Set<String> result = new HashSet<>();
            classes.idStream().forEach(c -> addAllSubClasses(c, result));
            return result;
        }), this);
    }

    @Override
    public JavaClasses allSubClassesAndClasses(JavaClasses classes) {
        return allSubClasses(classes).unitedWith(classes);
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
    public JavaMethods methods() {
        return JavaMethodsImpl.newJavaMethods(state.methods(), this);
    }

    @Override
    public String idOfMethodDeclaredAs(String methodDeclaratorText) {
        return state.idOfMethodDeclaredAs(methodDeclaratorText);
    }

    @Override
    public String methodDeclaratorTextOfMethodWithId(String methodId) {
        return state.methodDeclaratorTextOfMethodWithId(methodId);
    }

    @Override
    public JavaClasses classesContainingMethodWithSignature(String methodSignature) {
        return newJavaClasses(state.classesContainingMethodWithSignature(methodSignature));
    }

    public JavaMethodSignatures methodSignaturesOfClass(String className) {
        return newJavaMethodSignatures(
                state.methodSignatureSpecificationsOfClass(className), this);
    }

    @Override
    public JavaMethodSignatures inheritedMethodSignaturesOfClass(String className) {
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
                        .set());
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
    public JavaClasses classes() {
        return newJavaClasses(state.classes());
    }

    @Override
    public JavaClasses classesOfJavaFile(String file) {
        return newJavaClasses(state.classesOfJavaFile(file));
    }

    @Override
    public JavaClasses classesReferencingClass(String classname) {
        return newJavaClasses(state.classesReferencingClass(classname));
    }

    @Override
    public JavaClass classWithName(String className) {
        return newJavaClass(className, this);
    }

    @Override
    public Optional<String> javaFileOfClass(String classname) {
        return state.javaFileOfClass(classname);
    }

    @Override
    public Optional<String> classFileOfClass(String classname) {
        return state.classFileOfClass(classname);
    }

    @Override
    public OptionalInt bytecodeSizeOfClass(String classname) {
        return state.bytecodeSizeOfClass(classname);
    }

    @Override
    public Optional<String> md5OfClass(String classname) {
        return state.md5OfClass(classname);
    }

    @Override
    public boolean isInterface(String classname) {
        return state.isInterface(classname);
    }

    @Override
    public boolean isClassDeclared(String classname) {
        return state.isClassDeclared(classname);
    }

    @Override
    public void dump(PrintWriter writer) {
        state.dump(writer);
    }

    private void addAllSubClasses(String className, Set<String> target) {
        if (!isInterface(className)) {
            subClasses(className).idStream().forEach(c -> {
                if (!target.contains(c)) {
                    addAllSubClasses(c, target);
                }
                target.add(c);
            });
        }
    }
}
