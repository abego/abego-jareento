package org.abego.jareento.javaanalysis;

import org.abego.commons.io.FileUtil;
import org.abego.jareento.javaanalysis.internal.input.javap.InputFromJavap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.regex.Pattern;

import static org.abego.commons.io.ResourceUtil.textOfResource;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CallsTest {

    @Test
    void smoketest(@TempDir File tempDir) {
        SampleProjectUtil.setupSampleProject("calls", tempDir);

        String expected = textOfResource(InputFromJavap.class, "javap-CallsSample.txt");
        String actual = FileUtil.textOf(new File(tempDir,"storage/calls/disassembly.txt"));
        actual = actual.replaceAll(Pattern.quote(tempDir.getAbsolutePath()),"{tempDir}");
        assertEquals(expected,actual);
    }
}
