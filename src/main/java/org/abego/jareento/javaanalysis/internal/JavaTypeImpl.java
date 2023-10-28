package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaType;

class JavaTypeImpl implements JavaType {
    private final String id;

    static JavaType newJavaType(String id) {
        return new JavaTypeImpl(id);
    }

    private JavaTypeImpl(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

}
