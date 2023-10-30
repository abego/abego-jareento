package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaMethodSignature;
import org.abego.jareento.javaanalysis.JavaMethodSignatures;

import java.util.HashSet;
import java.util.Set;

import static org.abego.jareento.javaanalysis.internal.IDsImpl.newIDs;
import static org.abego.jareento.javaanalysis.internal.JavaMethodSignatureImpl.newJavaMethodSignature;

class JavaMethodSignaturesImpl extends ManyWithIdDefault<JavaMethodSignature, JavaMethodSignatures> implements JavaMethodSignatures {
    private final JavaAnalysisProjectInternal project;

    private JavaMethodSignaturesImpl(IDs ids, JavaAnalysisProjectInternal project) {
        super(ids);
        this.project = project;
    }

    public static JavaMethodSignaturesImpl newJavaMethodSignatures(IDs ids, JavaAnalysisProjectInternal project) {
        return new JavaMethodSignaturesImpl(ids, project);
    }

    @Override
    protected JavaMethodSignature elementWithId(String id) {
        return newJavaMethodSignature(id, project);
    }

    @Override
    protected JavaMethodSignatures newInstance(IDs ids) {
        return new JavaMethodSignaturesImpl(ids, project);
    }

    @Override
    public boolean contains(String methodSignatureText) {
        return toSet().contains(methodSignatureText);
    }

    @Override
    public JavaMethodSignatures intersectedWith(JavaMethodSignatures otherSignatures) {
        return newJavaMethodSignatures(newIDs(() -> {
            Set<String> result = new HashSet<>(toSet());
            result.retainAll(otherSignatures.toSet());
            return result;
        }), project);
    }
}
