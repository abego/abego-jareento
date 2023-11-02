package org.abego.jareento.javarefactoring;

import org.abego.jareento.javaanalysis.JavaMethodDeclarators;
import org.abego.jareento.javaanalysis.internal.JavaAnalysisFiles;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface JavaRefactoringAPI {
    //region removeMethods
    void removeMethods(JavaAnalysisFiles javaAnalysisFiles,
                       JavaMethodDeclarators methodDeclarators,
                       Consumer<String> progress);

    void removeMethods(JavaAnalysisFiles javaAnalysisFiles,
                       Predicate<MethodDescriptor> selector,
                       Predicate<File> javaFileFilter,
                       Consumer<MethodDescriptor> willBeRemovedCallback,
                       Consumer<String> progress);

    //endregion
    //region removeMethodAnnotations
    void removeMethodAnnotations(JavaAnalysisFiles javaAnalysisFiles,
                                 String annotationType,
                                 JavaMethodDeclarators methodSet,
                                 Consumer<String> progress);

    void removeMethodAnnotations(JavaAnalysisFiles javaAnalysisFiles,
                                 Predicate<MethodAnnotationDescriptor> annotationSelector,
                                 Predicate<File> javaFileFilter,
                                 Consumer<MethodAnnotationDescriptor> willBeRemovedCallback,
                                 Consumer<String> progress);
    //endregion
}
