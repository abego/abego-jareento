package org.abego.jareento.javaanalysis.internal;

import org.eclipse.jdt.annotation.Nullable;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableSet;

final class IDsImpl implements IDs {
    private final Supplier<Set<String>> idsSupplier;
    @Nullable
    private Set<String> ids;

    private IDsImpl(Supplier<Set<String>> idsSupplier) {
        this.idsSupplier = idsSupplier;
    }

    public static IDs newIDs(Supplier<Set<String>> idsSupplier) {
        return new IDsImpl(idsSupplier);
    }

    @Override
    public int getSize() {
        return toSet().size();
    }

    @Override
    public Set<String> toSet() {
        if (ids == null) {
            ids = unmodifiableSet(idsSupplier.get());
        }
        return ids;
    }

    @Override
    public Stream<String> stream() {
        return toSet().stream();
    }

    @Override
    public Iterator<String> iterator() {
        return toSet().iterator();
    }
}
