package org.abego.jareento.javaanalysis;

import java.util.Comparator;
import java.util.stream.Stream;

public interface Problems extends Iterable<Problem> {

    int getSize();

    default boolean isEmpty() {
        return getSize() == 0;
    }

    Stream<Problem> stream();

    /**
     * Returns a new {@link Problems} instance with the same {@link Problem}s as
     * in this object, but sorted as defined by the {@code comparator}.
     */
    Problems sorted(Comparator<Problem> comparator);

    /**
     * Returns a new {@link Problems} instance with the same {@link Problem}s as
     * in this object, but sorted by their absolute file path, their line
     * number and their description text.
     */
    Problems sortedByFile();

    /**
     * Returns a new {@link Problems} instance with the same {@link Problem}s as
     * in this object, but sorted by their description text, their absolute
     * file path and their line number.
     */
    Problems sortedByDescription();
}
