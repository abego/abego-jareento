package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.Many;

public interface JavaClasses extends Many<JavaClass, JavaClasses> {

    JavaMethodSignatures methodSignatures();

    JavaClasses unitedWithClassNamed(String classname);

}
