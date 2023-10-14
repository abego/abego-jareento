package org.abego.jareento.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.abego.jareento.util.JavaLangUtil.parseParameters;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JavaLangUtilTest {
    @Test
    void parseParametersTest() {
        List<String> parts = parseParameters("foo");

        assertEquals(1, parts.size());
        assertEquals("foo", String.join(" | ", parts));

        parts = parseParameters("foo,bar");

        assertEquals(2, parts.size());
        assertEquals("foo | bar", String.join(" | ", parts));

        parts = parseParameters("foo<bar,baz>,doo");

        assertEquals(2, parts.size());
        assertEquals("foo<bar,baz> | doo", String.join(" | ", parts));
    }
}
