package org.abego.jareento.javarefactoring.internal;

import org.abego.jareento.javaanalysis.JavaMethodDeclarators;
import org.abego.jareento.javaanalysis.internal.JavaAnalysisFiles;
import org.abego.jareento.javarefactoring.JavaRefactoringAPI;
import org.abego.jareento.javarefactoring.MethodAnnotationDescriptor;
import org.abego.jareento.javarefactoring.MethodDescriptor;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class JavaRefactoringAPIImpl implements JavaRefactoringAPI {

    @Override
    public void removeMethods(JavaAnalysisFiles javaAnalysisFiles, JavaMethodDeclarators methodDeclarators, Consumer<String> progress) {
        RemoveMethodsOperation.removeMethods(javaAnalysisFiles, methodDeclarators, progress);
    }

    @Override
    public void removeMethods(JavaAnalysisFiles javaAnalysisFiles, Predicate<MethodDescriptor> selector, Predicate<File> javaFileFilter, Consumer<MethodDescriptor> willBeRemovedCallback, Consumer<String> progress) {
        RemoveMethodsOperation.removeMethods(javaAnalysisFiles, selector, javaFileFilter, willBeRemovedCallback, progress);
    }

    @Override
    public void removeMethodAnnotations(JavaAnalysisFiles javaAnalysisFiles, String annotationType, JavaMethodDeclarators methodSet, Consumer<String> progress) {
        RemoveMethodAnnotationsOperation.removeMethodAnnotations(javaAnalysisFiles, annotationType, methodSet, progress);
    }

    @Override
    public void removeMethodAnnotations(
            JavaAnalysisFiles javaAnalysisFiles,
            Predicate<MethodAnnotationDescriptor> annotationSelector,
            Predicate<File> javaFileFilter,
            Consumer<MethodAnnotationDescriptor> willBeRemovedCallback,
            Consumer<String> progress) {
        RemoveMethodAnnotationsOperation.removeMethodAnnotations(javaAnalysisFiles, annotationSelector, javaFileFilter, willBeRemovedCallback, progress);
    }

}
