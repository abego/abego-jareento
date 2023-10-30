package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaTypes;
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
    public JavaTypes getTypesWithMethod() {
        return project.typesContainingMethodWithSignature(id);
    }

    @Override
    public JavaMethodCalls getMethodCalls() {
        return project.methodCallsWithSignature(id);
    }

    @Override
    public JavaMethodCalls getMethodCallsToType(String typeName) {
        return project.methodCallsWithSignatureOnType(id, typeName);
    }


    @Override
    public String getId() {
        return id;
    }

}
