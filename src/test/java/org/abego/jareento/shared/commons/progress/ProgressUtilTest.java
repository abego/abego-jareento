package org.abego.jareento.shared.commons.progress;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProgressUtilTest {
    @Test
    void withProgressAndDurationDo$throws() {
        UncheckedIOException e = Assertions.assertThrows(UncheckedIOException.class, () ->
                ProgressUtil.withProgressAndDurationDo(
                        () -> "start",
                        n -> "end",
                        s -> {},
                        () -> {throw new IOException("foo");}));
        assertEquals("foo", e.getCause().getMessage());
    }
}
