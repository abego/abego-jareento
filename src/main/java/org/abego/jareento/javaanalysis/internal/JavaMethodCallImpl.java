package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaMethod;
import org.abego.jareento.javaanalysis.JavaMethodCall;
import org.abego.jareento.javaanalysis.JavaMethodSignature;
import org.abego.jareento.javaanalysis.JavaType;

import java.util.Objects;

import static org.abego.jareento.javaanalysis.internal.JavaMethodImpl.newJavaMethod;
import static org.abego.jareento.javaanalysis.internal.JavaMethodSignatureImpl.newJavaMethodSignature;
import static org.abego.jareento.javaanalysis.internal.JavaTypeImpl.newJavaType;

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
    public JavaType getCallingType() {
        return newJavaType(project.classContainingMethodCall(id), project);
    }

    @Override
    public String getCallingTypeName() {
        return project.classContainingMethodCall(id);
    }

    @Override
    public String getScope() {
        return project.scopeOfMethodCall(id);
    }

    @Override
    public String getBaseScope() {
        return project.baseScopeOfMethodCall(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JavaMethodCallImpl that)) return false;
        return id.equals(that.id) && project.equals(that.project);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, project);
    }
}
