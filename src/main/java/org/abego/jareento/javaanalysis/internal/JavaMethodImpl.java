package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaClass;
import org.abego.jareento.javaanalysis.JavaMethod;
import org.abego.jareento.javaanalysis.JavaMethodCalls;
import org.abego.jareento.javaanalysis.JavaMethodSignature;
import org.abego.jareento.javaanalysis.JavaMethods;

class JavaMethodImpl implements JavaMethod {
    private final String id;
    private final JavaAnalysisProjectInternal project;

    private JavaMethodImpl(String id, JavaAnalysisProjectInternal project) {
        this.id = id;
        this.project = project;
    }

    static JavaMethod newJavaMethod(String id, JavaAnalysisProjectInternal project) {
        return new JavaMethodImpl(id, project);
    }

    @Override
    public String getName() {
        return project.nameOfMethod(id);
    }

    @Override
    public String getMethodSignatureText() {
        return project.signatureOfMethod(id);
    }

    @Override
    public JavaMethodSignature getMethodSignature() {
        return JavaMethodSignatureImpl.newJavaMethodSignature(
                getMethodSignatureText(), project);
    }

    @Override
    public String getReturnTypeName() {
        return project.returnTypeOfMethod(id);
    }

    @Override
    public String getClassName() {
        return project.classOfMethod(id);
    }

    @Override
    public JavaClass getJavaClass() {
        return project.getClassWithName(getClassName());
    }

    @Override
    public String getPackage() {
        return project.packageOfMethod(id);
    }

    @Override
    public String getMethodDeclaratorText() {
        return project.methodDeclaratorTextOfMethodWithId(id);
    }

    @Override
    public boolean isConstructor() {
        return project.isConstructor(id);
    }

    @Override
    public boolean isSynthetic() {
        return project.isMethodSynthetic(id);
    }

    @Override
    public boolean isClassInitializationMethod() {
        return project.isClassInitializationMethod(id);
    }

    @Override
    public boolean isObjectInitializationMethod() {
        return project.isObjectInitializationMethod(id);
    }

    @Override
    public boolean isAnnotatedWithOverride() {
        return project.hasMethodOverrideAnnotation(id);
    }

    @Override
    public JavaMethods getMethodsDirectlyOverridingMe() {
        return project.methodsDirectlyOverridingMethod(id);
    }

    @Override
    public JavaMethodCalls getMethodCallsToMe() {
        return project.methodCallsToMethod(id);
    }

    @Override
    public JavaMethodCalls getMethodCallsFromMe() {
        return project.methodCallsInMethod(id);
    }

    @Override
    public String getId() {
        return id;
    }
}
