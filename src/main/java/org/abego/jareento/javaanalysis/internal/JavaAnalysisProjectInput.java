package org.abego.jareento.javaanalysis.internal;

import java.util.function.Consumer;

public interface JavaAnalysisProjectInput {
    void feed(JavaAnalysisProjectStateBuilder builder, Consumer<String> problemsConsumer);
}
