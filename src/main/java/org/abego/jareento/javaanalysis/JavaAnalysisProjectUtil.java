package org.abego.jareento.javaanalysis;

import java.util.stream.Collectors;

public class JavaAnalysisProjectUtil {
    public static String calledMethodsSummary(JavaAnalysisProject project, String methodId) {
        return project.methodCallsInMethod(methodId)
                .idStream()
                .map(project::signatureOfMethodCall)
                .collect(Collectors.joining(";"));
    }

}
