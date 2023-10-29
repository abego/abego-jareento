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
    public JavaType getSuperclass() {
        return project.superClass(id);
    }

    @Override
    public JavaTypes getSubClasses() {
        return project.subClasses(id);
    }

    @Override
    public JavaTypes getSubClassesAndClass() {
        return project.subClassesAndClass(id);
    }

    @Override
    public JavaTypes getAllSubClasses() {
        return project.allSubClasses(id);
    }

    @Override
    public JavaTypes getAllSubClassesAndClass() {
        return project.allSubClassesAndClass(id);
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
    public JavaTypes getReferencingClasses() {
        return project.classesReferencingClass(id);
    }

    @Override
    public JavaMethods getMethods() {
        return project.methodsOfClass(id);
    }

    @Override
    public JavaMethodSignatures getMethodSignatures() {
        return project.methodSignaturesOfClass(id);
    }

    @Override
    public JavaMethodSignatures getInheritedMethodSignatures() {
        return project.inheritedMethodSignaturesOfClass(id);
    }

    @Override
    public JavaMethodCalls getMethodCallsToClassWithSignature(String methodSignature) {
        return project.methodCallsWithSignatureOnClass(methodSignature,id);
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
