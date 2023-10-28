package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaMethodCalls;
import org.abego.jareento.javaanalysis.JavaMethodSignature;

class JavaMethodSignatureImpl implements JavaMethodSignature {
    private final JavaAnalysisProject project;
    private final String id;

    static JavaMethodSignature newJavaMethodSignature(String id, JavaAnalysisProject project) {
        return new JavaMethodSignatureImpl(id, project);
    }

    private JavaMethodSignatureImpl(String id, JavaAnalysisProject project) {
        this.id = id;
        this.project = project;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public JavaMethodCalls methodCallsOnClass(String className) {
        return project.methodCallsWithSignatureOnClass(id, className);
    }

}
