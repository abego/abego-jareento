package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.JareentoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.net.URISyntaxException;

import static org.abego.commons.util.ServiceLoaderUtil.loadService;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaAnalysisProjectStorageTest {
    private final JavaAnalysisAPI javaAnalysisAPI = loadService(JavaAnalysisAPI.class);

    @Test
    void missingStorage(@TempDir File tempDir) {
        JavaAnalysisProjectStorage storage = javaAnalysisAPI
                .getJavaAnalysisProjectStorage(tempDir.toURI());
        File file = new File(tempDir, "p1");
        
        JareentoException e = assertThrows(JareentoException.class, () -> 
                storage.loadJavaAnalysisProject(file.toURI(), s -> {}));
        assertTrue(e.getMessage().startsWith("Cannot find JavaAnalysis project"));
    }
}
