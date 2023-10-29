package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaMethodSignature;
import org.abego.jareento.javaanalysis.JavaMethodSignatures;

import java.util.HashSet;
import java.util.Set;

import static org.abego.jareento.javaanalysis.internal.IDsImpl.newIDs;
import static org.abego.jareento.javaanalysis.internal.JavaMethodSignatureImpl.newJavaMethodSignature;

class JavaMethodSignaturesImpl extends ManyWithIdDefault<JavaMethodSignature, JavaMethodSignatures> implements JavaMethodSignatures {
    private final JavaAnalysisProject project;

    private JavaMethodSignaturesImpl(IDs ids, JavaAnalysisProject project) {
        super(ids);
        this.project = project;
    }

    public static JavaMethodSignaturesImpl newJavaMethodSignatures(IDs ids, JavaAnalysisProject project) {
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
        return idSet().contains(methodSignatureText);
    }

    @Override
    public JavaMethodSignatures intersectedWith(JavaMethodSignatures otherSignatures) {
        return newJavaMethodSignatures(newIDs(() -> {
            Set<String> result = new HashSet<>(idSet());
            result.retainAll(otherSignatures.idSet());
            return result;
        }), project);
    }
}
