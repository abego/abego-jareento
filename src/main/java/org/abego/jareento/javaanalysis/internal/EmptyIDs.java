package org.abego.jareento.javaanalysis.internal;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

final class EmptyIDs implements IDs {

    private final static IDs EMPTY_IDS = new EmptyIDs();

    public static IDs emptyIDs() {
        return EMPTY_IDS;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public Set<String> toSet() {
        return Collections.emptySet();
    }

    @Override
    public Stream<String> stream() {
        return Stream.empty();
    }

    @Override
    public Iterator<String> iterator() {
        return Collections.emptyIterator();
    }
}
