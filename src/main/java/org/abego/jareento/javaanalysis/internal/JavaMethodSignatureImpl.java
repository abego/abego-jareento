package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaClasses;
import org.abego.jareento.javaanalysis.JavaMethodCalls;
import org.abego.jareento.javaanalysis.JavaMethodSignature;

class JavaMethodSignatureImpl implements JavaMethodSignature {
    private final JavaAnalysisProjectInternal project;
    private final String id;

    static JavaMethodSignature newJavaMethodSignature(String id, JavaAnalysisProjectInternal project) {
        return new JavaMethodSignatureImpl(id, project);
    }

    private JavaMethodSignatureImpl(String id, JavaAnalysisProjectInternal project) {
        this.id = id;
        this.project = project;
    }

    @Override
    public JavaClasses getClassesWithMethod() {
        return project.classesContainingMethodWithSignature(id);
    }

    @Override
    public JavaMethodCalls getMethodCalls() {
        return project.methodCallsWithSignature(id);
    }

    @Override
    public JavaMethodCalls getMethodCallsToClass(String classname) {
        return project.methodCallsWithSignatureOnClass(id, classname);
    }


    @Override
    public String getId() {
        return id;
    }

}
