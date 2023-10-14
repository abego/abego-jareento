package org.abego.jareento.javarefactoring.internal;

import com.github.javaparser.ast.CompilationUnit;
import org.abego.jareento.base.JareentoException;
import org.abego.jareento.javarefactoring.JavaRefactoringProject;
import org.abego.jareento.shared.commons.javaparser.JavaParserUtil;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class JavaRefactoringProjectImpl implements JavaRefactoringProject {
    private final File[] sourceRootsAndDependencies;

    public JavaRefactoringProjectImpl(File... sourceRootsAndDependencies) {
        this.sourceRootsAndDependencies = sourceRootsAndDependencies;
    }

    static JavaRefactoringProjectImpl javaRefactoringProjectImpl(JavaRefactoringProject project) {
        if (!(project instanceof JavaRefactoringProjectImpl)) {
            throw new JareentoException("Invalid JavaRefactoringProject implementation. Got: " + project);
        }
        return (JavaRefactoringProjectImpl) project;
    }

    public void withCompilationUnitsDo(
            Consumer<CompilationUnit> operation,
            Predicate<File> fileFilter,
            Consumer<String> progress) {
        JavaParserUtil.forEveryJavaFileDo(
                sourceRootsAndDependencies, fileFilter, operation, progress);
    }
}
