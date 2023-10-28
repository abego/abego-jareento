package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.ManyWithId;

//TODO explain difference between JavaClasses and JavaTypes
public interface JavaTypes extends ManyWithId<JavaType, JavaTypes> {
    Iterable<String> names();
}
