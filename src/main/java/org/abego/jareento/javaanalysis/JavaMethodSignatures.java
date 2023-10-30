package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.ManyWithId;

import javax.annotation.Syntax;

import static org.abego.jareento.base.JareentoSyntax.METHOD_SIGNATURE_SYNTAX;

public interface JavaMethodSignatures extends ManyWithId<JavaMethodSignature, JavaMethodSignatures> {
    boolean contains(@Syntax(METHOD_SIGNATURE_SYNTAX) String methodSignatureText);

    JavaMethodSignatures intersectedWith(JavaMethodSignatures otherSignatures);
}
