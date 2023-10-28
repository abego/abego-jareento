package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaMethod;

class JavaMethodImpl implements JavaMethod {
    private final String id;
    private final JavaAnalysisProject project;

    private JavaMethodImpl(String id, JavaAnalysisProject project) {
        this.id = id;
        this.project = project;
    }

    static JavaMethod newJavaMethod(String id, JavaAnalysisProject project) {
        return new JavaMethodImpl(id, project);
    }

    @Override
    public String getName() {
        return project.nameOfMethod(id);
    }

    @Override
    public String getSignature() {
        return project.signatureOfMethod(id);
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
    public String getPackage() {
        return project.packageOfMethod(id);
    }

    @Override
    public String getFullDeclarator() {
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
    public String id() {
        return id;
    }
}
