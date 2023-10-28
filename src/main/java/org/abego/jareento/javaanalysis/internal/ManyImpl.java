package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.base.ManyWithId;
import org.abego.jareento.base.WithId;
import org.eclipse.jdt.annotation.Nullable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static org.abego.jareento.javaanalysis.internal.IDsImpl.newIDs;

abstract class ManyImpl<T extends WithId, M extends ManyWithId<T, M>> implements ManyWithId<T, M> {
    private final IDs ids;

    protected ManyImpl(IDs ids) {
        this.ids = ids;
    }

    abstract protected T elementWithId(String id);

    abstract protected M newInstance(IDs ids);

    @Override
    public int getSize() {
        return ids.getSize();
    }

    @Override
    public Stream<T> stream() {
        return idStream().map(this::elementWithId);
    }

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

    @Override
    public Stream<String> idStream() {
        return ids.stream();
    }

    public Set<String> idSet() {
        return ids.set();
    }

    @Override
    public M unitedWith(M other) {
        return newInstance(newIDs(() -> {
            Set<String> result = new HashSet<>(idSet());
            other.idStream().forEach(result::add);
            return result;
        }));
    }

    @Override
    public M unitedWith(T element) {
        return unitedWithElementWithId(element.getId());
    }

    @Override
    public M unitedWithElementWithId(String elementId) {
        return newInstance(newIDs(() -> {
            Set<String> result = new HashSet<>(idSet());
            result.add(elementId);
            return result;
        }));
    }


    @Override
    public M intersectedWith(M other) {
        return newInstance(newIDs(() -> {
            Set<String> result = new HashSet<>(idSet());
            result.retainAll(other.idSet());
            return result;
        }));
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            ManyImpl<?, ?> other = (ManyImpl<?, ?>) o;
            return this.ids.equals(other.ids);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.ids);
    }
}
