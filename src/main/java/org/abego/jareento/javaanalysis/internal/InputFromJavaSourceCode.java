package org.abego.jareento.javaanalysis.internal;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.abego.commons.lang.StringUtil;
import org.abego.jareento.shared.commons.javaparser.JavaParserUtil;

import java.io.File;
import java.util.function.Consumer;

import static org.abego.commons.lang.ThrowableUtil.messageOrToString;
import static org.abego.jareento.shared.commons.javaparser.JavaParserUtil.asQualifiedTypeNames;
import static org.abego.jareento.shared.commons.javaparser.JavaParserUtil.getStorageFileName;

/**
 * Extracts data from Java source code, as in Java files included under any of
 * the "source root" directories provided.
 */
//TODO: include a summary of the data currently added to the project
//TODO: Make this accessible through API? Currently not used
class InputFromJavaSourceCode implements JavaAnalysisProjectInput {
    private final File[] sourceRoots;
    private final Consumer<String> progress;

    private static class AddToProjectVisitor extends VoidVisitorAdapter<Object> {
        private final JavaAnalysisProjectStateBuilder builder;
        private final CompilationUnit cu;
        private final Consumer<String> problemConsumer;

        public AddToProjectVisitor(
                JavaAnalysisProjectStateBuilder builder,
                CompilationUnit cu,
                Consumer<String> problemConsumer) {
            this.builder = builder;
            this.cu = cu;
            this.problemConsumer = problemConsumer;
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Object arg) {
            super.visit(n, arg);

            String qname = n.getFullyQualifiedName()
                    .orElse("<unknown class>");
            builder.addClass(qname);
            if (n.isInterface()) {
                builder.setIsInterfaceOfClass(qname, true);
            }

            asQualifiedTypeNames(n.getExtendedTypes()).forEach(t ->
                    builder.addTypeExtends(qname, t)
            );
            if (!n.isInterface() && n.getExtendedTypes().isEmpty()) {
                builder.addTypeExtends(qname, "java.lang.Object");
            }
            asQualifiedTypeNames(n.getImplementedTypes()).forEach(t ->
                    builder.addTypeImplements(qname, t)
            );
            n.getMethods().forEach(m -> {
                try {
                    String methodSignature = m.resolve().getSignature();
                    String returnType = m.getType().asString();
                    boolean hasOverride = m.getAnnotations().stream()
                            .anyMatch(a -> a.getName().getIdentifier()
                                    .equals("Override"));
                    String methodId =
                            builder.addMethod(qname, methodSignature, returnType);
                    builder.setMethodHasOverride(methodId, hasOverride);
                } catch (Exception e) {
                    String message = String.format(
                            "Error when analysing method %s in '%s': %s",
                            StringUtil.quoted2(m.toString()),
                            getStorageFileName(cu),
                            messageOrToString(e));
                    problemConsumer.accept(message);
                }
            });
        }
    }

    private InputFromJavaSourceCode(File[] sourceRoots, Consumer<String> progress) {
        this.progress = progress;
        this.sourceRoots = sourceRoots;
    }

    public static InputFromJavaSourceCode newInputFromJavaSourceCode(
            File[] sourceRoots, Consumer<String> progress) {
        return new InputFromJavaSourceCode(sourceRoots, progress);
    }

    @Override
    public void feed(JavaAnalysisProjectStateBuilder builder, Consumer<String> problemConsumer) {
        JavaParserUtil.forEveryJavaFileDo(sourceRoots, cu -> 
                new AddToProjectVisitor(builder, cu, problemConsumer).visit(cu, ""), progress);
    }
}
