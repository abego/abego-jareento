package org.abego.jareento.javarefactoring;

import org.abego.jareento.base.JavaMethodSet;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface JavaRefactoringAPI {
    //region Factory
    JavaRefactoringProject newJavaRefactoringProject(File... sourceRootsAndDependencies);

    //endregion
    //region removeMethods
    void removeMethods(JavaRefactoringProject project,
                       JavaMethodSet methodSet,
                       Consumer<String> progress);

    void removeMethods(JavaRefactoringProject project,
                       Predicate<MethodDescriptor> selector,
                       Predicate<File> javaFileFilter,
                       Consumer<MethodDescriptor> willBeRemovedCallback,
                       Consumer<String> progress);

    //endregion
    //region removeMethodAnnotations
    void removeMethodAnnotations(JavaRefactoringProject project,
                                 String annotationType,
                                 JavaMethodSet methodSet,
                                 Consumer<String> progress);

    void removeMethodAnnotations(JavaRefactoringProject project,
                                 Predicate<MethodAnnotationDescriptor> annotationSelector,
                                 Predicate<File> javaFileFilter,
                                 Consumer<MethodAnnotationDescriptor> willBeRemovedCallback,
                                 Consumer<String> progress);
    //endregion
}
