package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.ManyWithId;

public interface JavaClasses extends ManyWithId<JavaClass, JavaClasses> {

    JavaMethodSignatures getMethodSignatures();

    JavaClasses unitedWithClassNamed(String classname);

}
