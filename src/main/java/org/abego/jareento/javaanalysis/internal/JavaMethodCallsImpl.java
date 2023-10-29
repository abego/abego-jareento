package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaMethodCall;
import org.abego.jareento.javaanalysis.JavaMethodCalls;

import java.util.stream.Collectors;

class JavaMethodCallsImpl extends ManyWithIdDefault<JavaMethodCall, JavaMethodCalls> implements JavaMethodCalls {
    private final JavaAnalysisProjectInternal project;

    private JavaMethodCallsImpl(IDs ids, JavaAnalysisProjectInternal project) {
        super(ids);
        this.project = project;
    }

    static JavaMethodCalls newJavaMethodCalls(IDs ids, JavaAnalysisProjectInternal project) {
        return new JavaMethodCallsImpl(ids, project);
    }

    @Override
    protected JavaMethodCall elementWithId(String id) {
        return JavaMethodCallImpl.newJavaMethodCall(id, project);
    }

    @Override
    protected JavaMethodCalls newInstance(IDs ids) {
        return newJavaMethodCalls(ids, project);
    }

    @Override
    public String getBriefSummary() {
        return idStream()
                .map(project::signatureOfMethodCall)
                .sorted()
                .collect(Collectors.joining(";"));
    }

}
