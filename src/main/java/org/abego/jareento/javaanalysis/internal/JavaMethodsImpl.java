package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaMethod;
import org.abego.jareento.javaanalysis.JavaMethods;
import org.abego.jareento.util.JavaLangUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.abego.jareento.javaanalysis.internal.JavaMethodImpl.newJavaMethod;


class JavaMethodsImpl extends ManyWithIdDefault<JavaMethod, JavaMethods> implements JavaMethods {
    private final JavaAnalysisProject project;

    private JavaMethodsImpl(IDs ids, JavaAnalysisProject project) {
        super(ids);
        this.project = project;
    }

    public static JavaMethods newJavaMethods(
            IDs ids, JavaAnalysisProject project) {
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
