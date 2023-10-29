package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.ManyWithId;

public interface JavaTypes extends ManyWithId<JavaType, JavaTypes> {

    /**
     * Returns the names of all types in this instance.
     */
    Iterable<String> getNames();

    JavaMethodSignatures getMethodSignatures();

    JavaTypes unitedWithTypeNamed(String typeName);
}
