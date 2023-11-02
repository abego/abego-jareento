package org.abego.jareento.javarefactoring.internal;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import org.abego.jareento.javaanalysis.JavaMethodDeclarators;
import org.abego.jareento.javaanalysis.internal.JavaAnalysisFiles;
import org.abego.jareento.javarefactoring.MethodAnnotationDescriptor;
import org.abego.jareento.shared.commons.javaparser.JavaParserUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.logging.Logger.getLogger;
import static org.abego.commons.io.FileUtil.removeFileExtension;
import static org.abego.commons.lang.StringUtil.indent;

class RemoveMethodAnnotationsOperation {
    private static final Logger LOGGER = getLogger(RemoveMethodAnnotationsOperation.class.getName());
    private final Predicate<MethodAnnotationDescriptor> annotationSelector;
    /**
     * While processing a compilation unit we collect all annotations
     * that should be removed in the field {@code annotationsInCompilationUnitToRemove}
     * and remove them collectively when we are done processing that compilation unit.
     */
    private final List<MethodAnnotationDescriptorImpl>
            annotationsInCompilationUnitToRemove = new ArrayList<>();
    private final Consumer<MethodAnnotationDescriptor> willBeRemovedCallback;

    private RemoveMethodAnnotationsOperation(
            Predicate<MethodAnnotationDescriptor> annotationSelector,
            Consumer<MethodAnnotationDescriptor> willBeRemovedCallback) {
        this.annotationSelector = annotationSelector;
        this.willBeRemovedCallback = willBeRemovedCallback;
    }

    private static RemoveMethodAnnotationsOperation newRemoveMethodAnnotationOperation(
            Predicate<MethodAnnotationDescriptor> annotationSelector,
            Consumer<MethodAnnotationDescriptor> willBeRemovedCallback) {
        return new RemoveMethodAnnotationsOperation(
                annotationSelector, willBeRemovedCallback);
    }

    public static void removeMethodAnnotations(
            JavaAnalysisFiles javaAnalysisFiles,
            Predicate<MethodAnnotationDescriptor> annotationSelector,
            Predicate<File> javaFileFilter,
            Consumer<MethodAnnotationDescriptor> willBeRemovedCallback,
            Consumer<String> progress) {
        progress.accept("Removing method annotations...");

        RemoveMethodAnnotationsOperation operation = newRemoveMethodAnnotationOperation(
                annotationSelector, willBeRemovedCallback);
        operation.applyOn(javaAnalysisFiles, javaFileFilter, indent(progress));

        progress.accept("Method annotations removed.");
    }

    public static void removeMethodAnnotations(
            JavaAnalysisFiles javaAnalysisFiles, 
            String annotationType, 
            JavaMethodDeclarators methodSet, 
            Consumer<String> progress) {
        progress.accept("Removing method annotations...");
        Consumer<String> innerProgress = indent(progress);
        Consumer<String> innerInnerProgress = indent(innerProgress);

        innerProgress.accept(String.format("%d method annotations to remove.", methodSet.getSize()));

        AtomicInteger removeCount = new AtomicInteger();
        Set<String> remainingMethods = methodSet.textStream()
                .collect(Collectors.toSet());
        RemoveMethodAnnotationsOperation operation = newRemoveMethodAnnotationOperation(
                m -> m.getAnnotationTypeName().equals(annotationType)
                        && methodSet.containsMethodOfTypeWithSignature(m.getTypeDeclaringMethod(), m.getMethodSignatureWithRawTypes()),
                m -> {
                    innerInnerProgress.accept(
                            String.format("Removing annotation '%s' from method %s#%s",
                                    annotationType,
                                    m.getTypeDeclaringMethod(), m.getMethodSignature()));
                    remainingMethods.remove(
                            methodSet.getMethodDeclaratorTextOfMethodOfTypeWithSignatureOrNull(
                                    m.getTypeDeclaringMethod(), m.getMethodSignatureWithRawTypes()));
                    removeCount.getAndIncrement();
                }
        );

        operation.applyOn(javaAnalysisFiles,
                f -> methodSet.containsMethodOfType(removeFileExtension(f.getName())),
                innerProgress);

        progress.accept(String.format("Removed %d method annotations.", removeCount.get()));
        if (!remainingMethods.isEmpty()) {
            LOGGER.warning(String.format("%d method annotations not removed: %s",
                    remainingMethods.size(),
                    remainingMethods.stream()
                            .sorted()
                            .collect(Collectors.joining("; "))));
        }
    }

    private class MyVisitor extends VoidVisitorAdapter<Consumer<String>> {
        @Override
        public void visit(CompilationUnit cu, Consumer<String> progress) {

            // make sure to clear the list, as it may contain entries
            // from "prior" CompilationUnits.
            annotationsInCompilationUnitToRemove.clear();
            String cuName = JavaParserUtil.getStorageFileName(cu);

            progress.accept(String.format("Processing CompilationUnit %s ...", cuName));
            super.visit(cu, progress);

            // done processing this compilation unit (/file).
            // Now we remove all annotations we collected
            if (!annotationsInCompilationUnitToRemove.isEmpty()) {
                int n = annotationsInCompilationUnitToRemove.size();
                indent(progress).accept(String.format("Removing %d method annotations in %s ...", n, cuName));
                LexicalPreservingPrinter.setup(cu);
                for (MethodAnnotationDescriptorImpl annotationDescriptor :
                        annotationsInCompilationUnitToRemove) {

                    // remove the annotation
                    willBeRemovedCallback.accept(annotationDescriptor);
                    annotationDescriptor.getNode().remove();
                }
                JavaParserUtil.saveToFile(cu);
            } else {
                progress.accept(String.format("No method annotations to remove in %s ...", cuName));
            }
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Consumer<String> progress) {
            super.visit(n, progress);

            for (MethodDeclaration mdecl : n.getMethods()) {
                NodeList<AnnotationExpr> annotations = mdecl.getAnnotations();
                for (AnnotationExpr annotation : annotations) {
                    MethodAnnotationDescriptorImpl descriptor =
                            new MethodAnnotationDescriptorImpl(annotation);
                    if (annotationSelector.test(descriptor)) {
                        annotationsInCompilationUnitToRemove.add(descriptor);
                    }
                }
            }
        }
    }

    private void applyOn(
            JavaAnalysisFiles javaAnalysisFiles,
            Predicate<File> javaFileFilter,
            Consumer<String> progress) {
        javaAnalysisFiles.withCompilationUnitsDo(
                        cu -> new MyVisitor().visit(cu, progress),
                        javaFileFilter,
                        progress);
    }
}
