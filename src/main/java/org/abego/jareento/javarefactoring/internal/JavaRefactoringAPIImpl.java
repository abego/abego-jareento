package org.abego.jareento.javarefactoring.internal;

import org.abego.jareento.javaanalysis.JavaMethodDeclarators;
import org.abego.jareento.javarefactoring.JavaRefactoringAPI;
import org.abego.jareento.javarefactoring.JavaRefactoringProject;
import org.abego.jareento.javarefactoring.MethodAnnotationDescriptor;
import org.abego.jareento.javarefactoring.MethodDescriptor;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class JavaRefactoringAPIImpl implements JavaRefactoringAPI {

    @Override
    public JavaRefactoringProject newJavaRefactoringProject(File... sourceRootsAndDependencies) {
        return new JavaRefactoringProjectImpl(sourceRootsAndDependencies);
    }

    @Override
    public void removeMethods(JavaRefactoringProject project, JavaMethodDeclarators methodSet, Consumer<String> progress) {
        RemoveMethodsOperation.removeMethods(project, methodSet, progress);
    }

    @Override
    public void removeMethods(JavaRefactoringProject project, Predicate<MethodDescriptor> selector, Predicate<File> javaFileFilter, Consumer<MethodDescriptor> willBeRemovedCallback, Consumer<String> progress) {
        RemoveMethodsOperation.removeMethods(project, selector, javaFileFilter, willBeRemovedCallback, progress);
    }

    @Override
    public void removeMethodAnnotations(JavaRefactoringProject project, String annotationType, JavaMethodDeclarators methodSet, Consumer<String> progress) {
        RemoveMethodAnnotationsOperation.removeMethodAnnotations(project, annotationType, methodSet, progress);
    }

    @Override
    public void removeMethodAnnotations(
            JavaRefactoringProject project,
            Predicate<MethodAnnotationDescriptor> annotationSelector,
            Predicate<File> javaFileFilter,
            Consumer<MethodAnnotationDescriptor> willBeRemovedCallback,
            Consumer<String> progress) {
        RemoveMethodAnnotationsOperation.removeMethodAnnotations(project, annotationSelector, javaFileFilter, willBeRemovedCallback, progress);
    }

}
