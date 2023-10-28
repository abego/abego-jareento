package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaMethodCall;

class JavaMethodCallImpl implements JavaMethodCall {
    private final String id;

    static JavaMethodCallImpl newJavaMethodCall(String id) {
        return new JavaMethodCallImpl(id);
    }

    private JavaMethodCallImpl(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

}
