package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaAnalysisAPI;
import org.abego.jareento.javaanalysis.JavaAnalysisProjectConfiguration;
import org.abego.jareento.javaanalysis.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.abego.commons.util.ServiceLoaderUtil.loadService;
import static org.abego.jareento.javaanalysis.TestUtil.assertEqualsAndHashCodeOK;

class JavaAnalysisProjectConfigurationTest {
    private final JavaAnalysisAPI javaAnalysisAPI = loadService(JavaAnalysisAPI.class);

    @Test
    void equalsAndHashCode(@TempDir File mavenDir) {
        TestUtil.writePomFile(mavenDir);
        File sourcesDir = new File(mavenDir, "src/main/java");

        JavaAnalysisProjectConfiguration configuration1 =
                javaAnalysisAPI.newJavaAnalysisProjectConfiguration(
                        "sample1",
                        mavenDir,
                        new File[]{sourcesDir});
        JavaAnalysisProjectConfiguration configuration2 =
                javaAnalysisAPI.newJavaAnalysisProjectConfiguration(
                        "sample1",
                        mavenDir,
                        new File[]{sourcesDir});

        assertEqualsAndHashCodeOK(configuration1, configuration2);
    }
}
