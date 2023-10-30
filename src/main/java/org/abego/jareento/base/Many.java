package org.abego.jareento.base;

import java.util.stream.Stream;

/**
 * An assembly of zero or more elements "with ID" of a given type.
 */
public interface Many<T> extends Iterable<T> {

    /**
     * Returns the number of elements.
     */
    int getSize();

    /**
     * Returns {@code true} when this instance contains no elements,
     * {@code true} otherwise.
     */
    default boolean isEmpty() {
        return getSize() == 0;
    }

    /**
     * Returns a {@link Stream} with the elements of this instance.
     */
    Stream<T> stream();
}
