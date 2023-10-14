package org.abego.jareento.base;

import java.util.Set;
import java.util.stream.Stream;

/**
 * An assembly of zero or more "identifiable" elements of a given type.
 * <p>
 * When used as an {@link Iterable} it will iterate over the elements of this
 * instance.
 */
public interface Many<T extends WithId, M extends Many<T, M>> extends Iterable<T> {

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

    /**
     * Returns a {@link Stream} with the IDs of the elements of this instance.
     */
    Stream<String> idStream();

    /**
     * Returns a {@link Set} with the IDs of the elements of this instance.
     */
    Set<String> idSet();

    /**
     * Returns a new instance of this type containing the elements of both this
     * and the other instance.
     * <p>
     * If an element is in both instances it is included only once.
     */
    M unitedWith(M other);

    /**
     * Returns a new instance of this type containing the elements of
     * this instance and the given {@code element}.
     * <p>
     * If the element is already in instance it will be in the new instance
     * only once.
     */
    M unitedWith(T element);

    /**
     * Returns a new instance of this type containing the elements of
     * this instance and the element with the given {@code elementId}.
     * <p>
     * If the element is already in instance it will be in the new instance
     * only once.
     */
    //TODO really in public API? may break type safety (as we don't check the element's type)
    M unitedWithElementWithId(String elementId);

    /**
     * Returns a new instance of this type containing the intersection of this
     * instance's elements and the other instance's elements, i.e. the elements
     * that are both in this and the other instance.
     */
    M intersectedWith(M other);

}
