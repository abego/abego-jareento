package org.abego.jareento.javaanalysis.internal;

import org.abego.commons.util.ListUtil;
import org.abego.jareento.base.Many;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

class ManyDefault<T> implements Many<T> {
    private final List<T> list;

    ManyDefault(Iterable<T> iterable) {
        this.list = ListUtil.toList(iterable);
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public int getSize() {
        return list.size();
    }

    @Override
    public Stream<T> stream() {
        return list.stream();
    }
}
