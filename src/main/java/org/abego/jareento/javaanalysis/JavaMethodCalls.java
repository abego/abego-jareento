package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.ManyWithId;

public interface JavaMethodCalls extends ManyWithId<JavaMethodCall, JavaMethodCalls> {
    /**
     * Returns a text with a brief summary of the method calls, mainly listing
     * the signatures of the methods called.
     */
    String getBriefSummary();
}
