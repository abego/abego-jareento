package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaClass;
import org.abego.jareento.javaanalysis.JavaClasses;
import org.abego.jareento.javaanalysis.JavaMethodCalls;
import org.abego.jareento.javaanalysis.JavaMethodSignatures;
import org.abego.jareento.javaanalysis.JavaMethods;
import org.abego.jareento.shared.SyntaxUtil;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Objects;

class JavaClassImpl implements JavaClass {
    private final String id;
    private final JavaAnalysisProjectInternal project;

    static JavaClassImpl newJavaClass(String id, JavaAnalysisProjectInternal project) {
        return new JavaClassImpl(id, project);
    }

    private JavaClassImpl(String id, JavaAnalysisProjectInternal project) {
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
    public JavaClass getSuperclass() {
        return project.superClass(id);
    }

    @Override
    public JavaClasses getSubClasses() {
        return project.subClasses(id);
    }

    @Override
    public JavaClasses getSubClassesAndClass() {
        return project.subClassesAndClass(id);
    }

    @Override
    public JavaClasses getAllSubClasses() {
        return project.allSubClasses(id);
    }

    @Override
    public JavaClasses getAllSubClassesAndClass() {
        return project.allSubClassesAndClass(id);
    }

    @Override
    public JavaClasses getImplementedInterfaces() {
        return project.implementedInterfaces(id);
    }

    @Override
    public JavaClasses getExtendedTypes() {
        return project.extendedTypes(id);
    }

    @Override
    public JavaClasses getReferencingClasses() {
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
        JavaClassImpl javaClass = (JavaClassImpl) o;
        return id.equals(javaClass.id) && project.equals(javaClass.project);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, project);
    }
}
