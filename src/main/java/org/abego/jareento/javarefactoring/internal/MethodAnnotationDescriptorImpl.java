package org.abego.jareento.javarefactoring.internal;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import org.abego.jareento.javarefactoring.MethodAnnotationDescriptor;
import org.abego.jareento.shared.commons.javaparser.JavaParserUtil;

import java.util.Optional;

import static org.abego.jareento.shared.commons.javaparser.JavaParserUtil.parameterTypes;

//TODO: increase test coverage
public class MethodAnnotationDescriptorImpl implements MethodAnnotationDescriptor {
    private final AnnotationExpr annotationExpr;
    private final ResolvedMethodDeclaration resolvedMethodDeclaration;

    /**
     * Constructs a {@link MethodAnnotationDescriptor} for the given
     * {@code annotationExpr}.
     *
     * @param annotationExpr an {@link AnnotationExpr} that belongs to a
     *                       {@link MethodDeclaration}.
     */
    MethodAnnotationDescriptorImpl(AnnotationExpr annotationExpr) {
        this.annotationExpr = annotationExpr;

        Optional<Node> node = annotationExpr.getParentNode();
        if (node.isPresent() && (node.get() instanceof MethodDeclaration methodDeclaration)) {
            this.resolvedMethodDeclaration = methodDeclaration.resolve();
        } else {
            throw new IllegalArgumentException(
                    "annotationExpr does not annotate a MethodDeclaration");
        }
    }

    @Override
    public String annotationText() {
        return annotationExpr.toString();
    }

    @Override
    public String annotationType() {
        return annotationExpr.resolve().getQualifiedName();
    }

    @Override
    public String typeDeclaringMethod() {
        return resolvedMethodDeclaration.declaringType().getQualifiedName();
    }

    @Override
    public String methodName() {
        return resolvedMethodDeclaration.getName();
    }

    @Override
    public String qualifiedMethodName() {
        return resolvedMethodDeclaration.getQualifiedName();
    }

    @Override
    public String methodPackageName() {
        return resolvedMethodDeclaration.getPackageName();
    }

    @Override
    public String methodSignatureWithRawTypes() {
        return JavaParserUtil.methodSignatureWithRawTypes(resolvedMethodDeclaration);
    }

    @Override
    public String[] methodParameterTypes() {
        return parameterTypes(resolvedMethodDeclaration);
    }

    @Override
    public String methodSignature() {
        return resolvedMethodDeclaration.getSignature();
    }

    Node getNode() {
        return annotationExpr;
    }

    @Override
    public String toString() {
        return "MyMethodAnnotationDescriptor{" +
                annotationText() + " " +
                this.typeDeclaringMethod() + "." + this.methodSignature() + "}";
    }
}
