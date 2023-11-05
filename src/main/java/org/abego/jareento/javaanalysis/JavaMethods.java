package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.ManyWithId;

import java.util.stream.Stream;

public interface JavaMethods extends ManyWithId<JavaMethod, JavaMethods> {
    Stream<String> idStream();
}
