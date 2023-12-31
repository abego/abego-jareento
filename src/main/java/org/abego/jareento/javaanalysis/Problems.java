package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.Many;

import java.util.Comparator;

public interface Problems extends Many<Problem> {

    /**
     * Returns a new {@link Problems} instance with the same {@link Problem}s as
     * in this instance, but sorted as defined by the {@code comparator}.
     */
    Problems sorted(Comparator<Problem> comparator);

    /**
     * Returns a new {@link Problems} instance with the same {@link Problem}s as
     * in this instance, but sorted by their absolute file path, their line
     * number and their description text.
     */
    Problems sortedByFile();

    /**
     * Returns a new {@link Problems} instance with the same {@link Problem}s as
     * in this instance, but sorted by their description text, their absolute
     * file path and their line number.
     */
    Problems sortedByDescription();
}
