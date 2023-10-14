package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.Many;

public interface JavaMethodSignatures extends Many<JavaMethodSignature, JavaMethodSignatures> {
    boolean contains(String methodSignature);

    JavaMethodSignatures intersectedWith(JavaMethodSignatures otherSignatures);
}
