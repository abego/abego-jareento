package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.Many;

public interface JavaClasses extends Many<JavaClass, JavaClasses> {

    JavaMethodSignatures getMethodSignatures();

    JavaClasses unitedWithClassNamed(String classname);

}
