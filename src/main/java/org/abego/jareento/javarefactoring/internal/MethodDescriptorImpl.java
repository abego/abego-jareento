package org.abego.jareento.javarefactoring.internal;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import org.abego.jareento.javarefactoring.MethodDescriptor;
import org.abego.jareento.shared.commons.javaparser.JavaParserUtil;

import java.util.Optional;

import static org.abego.jareento.shared.commons.javaparser.JavaParserUtil.parameterTypes;

//TODO: increase test coverage
class MethodDescriptorImpl implements MethodDescriptor {
    private final ResolvedMethodDeclaration resolvedMethodDeclaration;

    /**
     * Constructs a {@link MethodDescriptor} for the given
     * {@code methodDeclaration}.
     */
    MethodDescriptorImpl(MethodDeclaration methodDeclaration) {
        this.resolvedMethodDeclaration = methodDeclaration.resolve();
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
    public String[] methodParameterTypes() {
        return parameterTypes(resolvedMethodDeclaration);
    }

    @Override
    public String methodSignature() {
        return resolvedMethodDeclaration.getSignature();
    }

    @Override
    public String methodSignatureWithRawTypes() {
        return JavaParserUtil.methodSignatureWithRawTypes(resolvedMethodDeclaration);
    }

    @Override
    public String toString() {
        return typeDeclaringMethod() + "." + methodSignature();
    }

    Optional<Node> getNode() {
        return resolvedMethodDeclaration.toAst();
    }


}
