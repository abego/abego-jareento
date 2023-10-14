package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaMethodCall;
import org.abego.jareento.javaanalysis.JavaMethodCalls;

class JavaMethodCallsImpl extends ManyImpl<JavaMethodCall, JavaMethodCalls> implements JavaMethodCalls {
    private final JavaAnalysisProject project;

    private JavaMethodCallsImpl(IDs ids, JavaAnalysisProject project) {
        super(ids);
        this.project = project;
    }
    
    static JavaMethodCalls newJavaMethodCalls(IDs ids, JavaAnalysisProject project) {
        return new JavaMethodCallsImpl(ids, project);
    }

    @Override
    protected JavaMethodCall elementWithId(String id) {
        return JavaMethodCallImpl.newJavaMethodCall(id);
    }

    @Override
    protected JavaMethodCalls newInstance(IDs ids) {
        return newJavaMethodCalls(ids, project);
    }

}
