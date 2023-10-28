package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.ManyWithId;

public interface JavaMethodSignatures extends ManyWithId<JavaMethodSignature, JavaMethodSignatures> {
    boolean contains(String methodSignature);

    JavaMethodSignatures intersectedWith(JavaMethodSignatures otherSignatures);
}
