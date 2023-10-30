package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaMethod;
import org.abego.jareento.javaanalysis.JavaMethods;

import static org.abego.jareento.javaanalysis.internal.JavaMethodImpl.newJavaMethod;


class JavaMethodsImpl extends ManyWithIdDefault<JavaMethod, JavaMethods> implements JavaMethods {
    private final JavaAnalysisProjectInternal project;

    private JavaMethodsImpl(IDs ids, JavaAnalysisProjectInternal project) {
        super(ids);
        this.project = project;
    }

    public static JavaMethods newJavaMethods(
            IDs ids, JavaAnalysisProjectInternal project) {
        return new JavaMethodsImpl(ids, project);
    }

    @Override
    protected JavaMethod elementWithId(String id) {
        return newJavaMethod(id, project);
    }

    @Override
    protected JavaMethods newInstance(IDs ids) {
        return newJavaMethods(ids, project);
    }
}
