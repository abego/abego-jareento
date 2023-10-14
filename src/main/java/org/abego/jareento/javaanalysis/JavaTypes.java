package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.Many;

//TODO explain difference between JavaClasses and JavaTypes
public interface JavaTypes extends Many<JavaType, JavaTypes> {
    Iterable<String> classnames();
}
