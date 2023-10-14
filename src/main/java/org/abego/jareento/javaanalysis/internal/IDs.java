package org.abego.jareento.javaanalysis.internal;

import java.util.Set;
import java.util.stream.Stream;

public interface IDs extends Iterable<String> {
    int getSize();

    default boolean isEmpty() {
        return getSize() == 0;
    }

    Stream<String> stream();

    Set<String> set();
}
