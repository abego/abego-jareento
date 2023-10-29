package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.ManyWithId;

public interface JavaClasses extends ManyWithId<JavaClass, JavaClasses> {

    /**
     * Returns the names of all classes in this instance.
     */
    Iterable<String> getNames();

    JavaMethodSignatures getMethodSignatures();

    JavaClasses unitedWithClassNamed(String classname);
}
