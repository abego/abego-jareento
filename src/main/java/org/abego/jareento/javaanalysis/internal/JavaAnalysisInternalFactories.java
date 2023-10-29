package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaAnalysisProject;

import java.net.URI;

public class JavaAnalysisInternalFactories {

    public static JavaAnalysisProjectInternal newJavaAnalysisProject(JavaAnalysisProjectState state) {
        return JavaAnalysisProjectImpl.newJavaAnalysisProject(state);
    }

    public static JavaAnalysisProjectStateBuilder newJavaAnalysisProjectBuilder(URI uri) {
        return new JavaAnalysisProjectStateUsingStringGraph.BuilderImpl(uri);
    }

}
