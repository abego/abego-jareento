package org.abego.jareento.javaanalysis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WithenumTest {

    @Test
    void smoketest(@TempDir File tempDir) {
        JavaAnalysisProject project = 
                SampleProjectUtil.setupSampleProject("withenum", tempDir);

        JavaType myEnum = project.getTypeWithName("withenum.MyEnum");
        
        assertEquals("withenum.MyEnum", myEnum.getId());
    }
}
