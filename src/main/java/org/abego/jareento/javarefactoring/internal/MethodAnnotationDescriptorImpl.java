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
    public String getAnnotationText() {
        return annotationExpr.toString();
    }

    @Override
    public String getAnnotationTypeName() {
        return annotationExpr.resolve().getQualifiedName();
    }

    @Override
    public String getTypeDeclaringMethod() {
        return resolvedMethodDeclaration.declaringType().getQualifiedName();
    }

    @Override
    public String getMethodName() {
        return resolvedMethodDeclaration.getName();
    }

    @Override
    public String getQualifiedMethodName() {
        return resolvedMethodDeclaration.getQualifiedName();
    }

    @Override
    public String getMethodPackageName() {
        return resolvedMethodDeclaration.getPackageName();
    }

    @Override
    public String getMethodSignatureWithRawTypes() {
        return JavaParserUtil.methodSignatureWithRawTypes(resolvedMethodDeclaration);
    }

    @Override
    public String[] getMethodParameterTypeNames() {
        return parameterTypes(resolvedMethodDeclaration);
    }

    @Override
    public String getMethodSignature() {
        return resolvedMethodDeclaration.getSignature();
    }

    Node getNode() {
        return annotationExpr;
    }

    @Override
    public String toString() {
        return "MyMethodAnnotationDescriptor{" +
                getAnnotationText() + " " +
                this.getTypeDeclaringMethod() + "." + this.getMethodSignature() + "}";
    }
}
