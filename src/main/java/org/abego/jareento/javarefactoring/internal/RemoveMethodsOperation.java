package org.abego.jareento.javarefactoring.internal;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import org.abego.jareento.javaanalysis.JavaMethodDeclarators;
import org.abego.jareento.javarefactoring.JavaRefactoringProject;
import org.abego.jareento.javarefactoring.MethodDescriptor;
import org.abego.jareento.shared.commons.javaparser.JavaParserUtil;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
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
import static org.abego.commons.util.DateUtil.isoDateTime;
import static org.abego.jareento.javarefactoring.internal.JavaRefactoringProjectImpl.javaRefactoringProjectImpl;
import static org.abego.jareento.shared.commons.javaparser.JavaParserUtil.saveToFile;

class RemoveMethodsOperation {
    private static final Logger LOGGER = getLogger(RemoveMethodsOperation.class.getName());

    private final Predicate<MethodDescriptor> selector;
    /**
     * While processing a compilation unit we collect all nodes that should be
     * removed in this field and remove them collectively when we are done
     * processing that compilation unit.
     */
    private final List<MethodDescriptorImpl>
            thingsInCompilationUnitToRemove = new ArrayList<>();
    private final Consumer<MethodDescriptor> willBeRemovedCallback;

    private RemoveMethodsOperation(
            Predicate<MethodDescriptor> selector,
            Consumer<MethodDescriptor> willBeRemovedCallback) {
        this.selector = selector;
        this.willBeRemovedCallback = willBeRemovedCallback;
    }

    private class MyVisitor extends VoidVisitorAdapter<Consumer<String>> {
        private static final long AUTO_SAVE_MILLIS = Duration.ofMinutes(10)
                .toMillis();

        @Override
        public void visit(CompilationUnit cu, Consumer<String> progress) {

            // make sure to clear the list, as it may contain entries
            // from "prior" CompilationUnits.
            thingsInCompilationUnitToRemove.clear();

            String cuName = JavaParserUtil.getStorageFileName(cu);

            progress.accept(String.format("Processing CompilationUnit %s ...", cuName));
            Consumer<String> innerProgress = indent(progress);
            Consumer<String> innerInnerProgress = indent(innerProgress);

            super.visit(cu, innerProgress);

            // done processing this compilation unit (/file).
            // Now we remove all things we collected
            if (!thingsInCompilationUnitToRemove.isEmpty()) {
                int n = thingsInCompilationUnitToRemove.size();
                innerProgress.accept(String.format("%d methods to remove in %s ...",
                        n, cuName));
                LexicalPreservingPrinter.setup(cu);
                int i = 0;
                long startTime = System.currentTimeMillis();
                long lastSaveTime = startTime;
                for (MethodDescriptorImpl descriptor : thingsInCompilationUnitToRemove) {

                    willBeRemovedCallback.accept(descriptor);
                    descriptor.getNode().ifPresent(Node::remove);
                    i++;
                    innerInnerProgress.accept(
                            String.format("%d methods removed (from %d), %d ms/method",
                                    i, n, ((System.currentTimeMillis() - startTime) / i)));

                    if (lastSaveTime + AUTO_SAVE_MILLIS < System.currentTimeMillis()) {
                        innerInnerProgress.accept(String.format(
                                "%s: auto-saving...%n", isoDateTime(new Date())));
                        saveToFile(cu);
                        lastSaveTime = System.currentTimeMillis();
                    }
                }

                saveToFile(cu);
                progress.accept(String.format("Removed %d methods in %s ...", i, cuName));
            } else {
                progress.accept(String.format("No methods to remove in %s ...", cuName));
            }
        }
        
        @Override
        public void visit(ClassOrInterfaceDeclaration n, Consumer<String> progress) {
            super.visit(n, progress);

            for (MethodDeclaration mdecl : n.getMethods()) {
                MethodDescriptorImpl descriptor = new MethodDescriptorImpl(mdecl);
                if (selector.test(descriptor)) {
                    thingsInCompilationUnitToRemove.add(descriptor);
                }
            }
        }
    }

    private static RemoveMethodsOperation newRemoveMethodsOperation(
            Predicate<MethodDescriptor> selector,
            Consumer<MethodDescriptor> willBeRemovedCallback) {
        return new RemoveMethodsOperation(selector, willBeRemovedCallback);
    }

    public static void removeMethods(JavaRefactoringProject project, JavaMethodDeclarators methodSet, Consumer<String> progress) {
        progress.accept("Removing methods...");
        Consumer<String> innerProgress = indent(progress);
        Consumer<String> innerInnerProgress = indent(innerProgress);

        innerProgress.accept(String.format("%d methods to remove.", methodSet.getSize()));

        AtomicInteger removeCount = new AtomicInteger();
        Set<String> remainingMethods = methodSet.textStream()
                .collect(Collectors.toSet());
        RemoveMethodsOperation operation = newRemoveMethodsOperation(
                m -> methodSet.containsMethodOfTypeWithSignature(m.getTypeDeclaringMethod(), m.getMethodSignatureWithRawTypes()),
                m -> {
                    innerInnerProgress.accept(String.format("Removing method %s", m));
                    remainingMethods.remove(
                            methodSet.getMethodDeclaratorTextOfMethodOfTypeWithSignatureOrNull(
                                    m.getTypeDeclaringMethod(), m.getMethodSignatureWithRawTypes()));
                    removeCount.getAndIncrement();
                });

        operation.applyOn(project,
                f -> methodSet.containsMethodOfType(removeFileExtension(f.getName())),
                innerProgress);

        progress.accept(String.format("Removed %d methods", removeCount.get()));
        if (!remainingMethods.isEmpty()) {
            LOGGER.warning(String.format("%d methods not removed: %s",
                    remainingMethods.size(),
                    remainingMethods.stream()
                            .sorted()
                            .collect(Collectors.joining("; "))));
        }
    }

    public static void removeMethods(JavaRefactoringProject project, Predicate<MethodDescriptor> selector, Predicate<File> javaFileFilter, Consumer<MethodDescriptor> willBeRemovedCallback, Consumer<String> progress) {
        progress.accept("Removing methods...");

        RemoveMethodsOperation operation = newRemoveMethodsOperation(selector, willBeRemovedCallback);
        operation.applyOn(project, javaFileFilter, indent(progress));

        progress.accept("Methods removed.");
    }

    private void applyOn(
            JavaRefactoringProject project,
            Predicate<File> javaFileFilter,
            Consumer<String> progress) {
        javaRefactoringProjectImpl(project)
                .withCompilationUnitsDo(
                        cu -> new MyVisitor().visit(cu, progress),
                        javaFileFilter,
                        progress);
    }
}
