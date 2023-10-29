package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaClass;
import org.abego.jareento.javaanalysis.JavaMethod;
import org.abego.jareento.javaanalysis.JavaMethodCall;
import org.abego.jareento.javaanalysis.JavaMethodSignature;

import static org.abego.jareento.javaanalysis.internal.JavaClassImpl.newJavaClass;
import static org.abego.jareento.javaanalysis.internal.JavaMethodImpl.newJavaMethod;
import static org.abego.jareento.javaanalysis.internal.JavaMethodSignatureImpl.newJavaMethodSignature;

class JavaMethodCallImpl implements JavaMethodCall {
    private final String id;
    private final JavaAnalysisProjectInternal project;

    static JavaMethodCallImpl newJavaMethodCall(String id, JavaAnalysisProjectInternal project) {
        return new JavaMethodCallImpl(id, project);
    }

    private JavaMethodCallImpl(String id, JavaAnalysisProjectInternal project) {
        this.id = id;
        this.project = project;
    }

    @Override
    public String getId() {
        return id;
    }


    @Override
    public JavaMethodSignature getMethodSignature() {
        return newJavaMethodSignature(project.signatureOfMethodCall(id), project);
    }

    @Override
    public JavaMethod getCallingMethod() {
        return newJavaMethod(project.idOfMethodContainingMethodCall(id), project);
    }

    @Override
    public JavaClass getCallingClass() {
        return newJavaClass(project.classContainingMethodCall(id), project);
    }

    @Override
    public String getScope() {
        return project.scopeOfMethodCall(id);
    }

    @Override
    public String getBaseScope() {
        return project.baseScopeOfMethodCall(id);
    }
}
