package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaType;
import org.abego.jareento.javaanalysis.JavaTypes;

import static org.abego.jareento.javaanalysis.internal.JavaTypeImpl.newJavaType;

class JavaTypesImpl extends ManyWithIdDefault<JavaType, JavaTypes> implements JavaTypes {

    private JavaTypesImpl(IDs ids) {
        super(ids);
    }

    @Override
    protected JavaType elementWithId(String id) {
        return newJavaType(id);
    }

    @Override
    protected JavaTypes newInstance(IDs ids) {
        return newJavaTypes(ids);
    }

    public static JavaTypesImpl newJavaTypes(IDs ids) {
        return new JavaTypesImpl(ids);
    }

    @Override
    public Iterable<String> names() {
        return idSet();
    }
}
