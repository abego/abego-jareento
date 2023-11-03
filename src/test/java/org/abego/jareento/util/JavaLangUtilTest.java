package org.abego.jareento.util;

import org.abego.commons.lang.exception.MustNotInstantiateException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.abego.commons.test.JUnit5Util.assertThrowsWithMessage;
import static org.abego.jareento.util.JavaLangUtil.nameOfSignature;
import static org.abego.jareento.util.JavaLangUtil.parametersOfSignature;
import static org.abego.jareento.util.JavaLangUtil.parseParameters;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaLangUtilTest {
    @Test
    void constructor() {
        assertThrows(MustNotInstantiateException.class,JavaLangUtil::new);
    }

    @Test
    void parameterTypesOfSignature() {
        String[] types = JavaLangUtil.parameterTypesOfSignature(" m(com.example.Void2Block<V, java.lang.Integer>, int)");
        
        assertArrayEquals(new String[]{
                "com.example.Void2Block<V, java.lang.Integer>",
                "int"
        },types);
    }

    @Test
    void nameOfSignatureInvalid() {
           assertThrowsWithMessage(IllegalArgumentException.class,
                   "Not a valid signature (no '(' found): foo",
                   () ->nameOfSignature("foo"));
    }
    
    @Test
    void parametersOfSignatureInvalid() {
        assertThrowsWithMessage(IllegalArgumentException.class,
                "Not a valid signature (no '(' found): foo",
                () ->parametersOfSignature("foo"));
        
        assertThrowsWithMessage(IllegalArgumentException.class,
                "Not a valid signature (does not end with ')'): foo(",
                () ->parametersOfSignature("foo("));

    }
    
    
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
