package org.abego.jareento.javaanalysis.internal;

import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class EmptyIDsTest {
    @Test
    void smokeTest() {
        IDs ids = EmptyIDs.emptyIDs();
        Set<String> idSet = ids.toSet();
        Stream<String> idStream = ids.stream();
        Iterator<String> idIterator = ids.iterator();

        assertEquals(0, ids.getSize());
        assertEquals(0, idSet.size());
        assertEquals(0, idStream.count());
        assertFalse(idIterator.hasNext());
    }
}
