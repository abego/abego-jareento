package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaType;
import org.abego.jareento.javaanalysis.JavaTypes;
import org.abego.jareento.javaanalysis.JavaMethodCalls;
import org.abego.jareento.javaanalysis.JavaMethodSignatures;
import org.abego.jareento.javaanalysis.JavaMethods;
import org.abego.jareento.shared.SyntaxUtil;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Objects;

class JavaTypeImpl implements JavaType {
    private final String id;
    private final JavaAnalysisProjectInternal project;

    static JavaTypeImpl newJavaType(String id, JavaAnalysisProjectInternal project) {
        return new JavaTypeImpl(id, project);
    }

    private JavaTypeImpl(String id, JavaAnalysisProjectInternal project) {
        this.id = id;
        this.project = project;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isInterface() {
        return project.isInterface(id);
    }

    @Override
    public String getName() {
        return id;
    }

    @Override
    public String getSimpleName() {
        return SyntaxUtil.simpleName(getName());
    }

    @Override
    public JavaType getSuperType() {
        return project.superType(id);
    }

    @Override
    public JavaTypes getSubTypes() {
        return project.subTypes(id);
    }

    @Override
    public JavaTypes getSubTypesAndType() {
        return project.subTypesAndType(id);
    }

    @Override
    public JavaTypes getAllSubTypes() {
        return project.allSubTypes(id);
    }

    @Override
    public JavaTypes getAllSubTypesAndType() {
        return project.allSubTypesAndType(id);
    }

    @Override
    public JavaTypes getImplementedInterfaces() {
        return project.implementedInterfaces(id);
    }

    @Override
    public JavaTypes getExtendedTypes() {
        return project.extendedTypes(id);
    }

    @Override
    public JavaTypes getReferencingTypes() {
        return project.typesReferencingType(id);
    }

    @Override
    public JavaMethods getMethods() {
        return project.methodsOfType(id);
    }

    @Override
    public JavaMethodSignatures getMethodSignatures() {
        return project.methodSignaturesOfType(id);
    }

    @Override
    public JavaMethodSignatures getInheritedMethodSignatures() {
        return project.inheritedMethodSignaturesOfType(id);
    }

    @Override
    public JavaMethodCalls getMethodCallsToTypeWithSignature(String methodSignature) {
        return project.methodCallsWithSignatureOnType(methodSignature,id);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavaTypeImpl type = (JavaTypeImpl) o;
        return id.equals(type.id) && project.equals(type.project);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, project);
    }
}
