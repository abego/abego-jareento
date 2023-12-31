package org.abego.jareento.service;

import org.abego.jareento.javaanalysis.JavaAnalysisAPI;
import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaAnalysisProjectConfiguration;
import org.abego.jareento.javaanalysis.JavaAnalysisProjectStorage;
import org.abego.jareento.javaanalysis.JavaMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.abego.commons.io.FileUtil.copyResourcesInLocationDeep;
import static org.abego.commons.test.JUnit5Util.assertEqualFiles;
import static org.abego.commons.util.ServiceLoaderUtil.loadService;

public class JareentoServiceAPITest {
    private final JavaAnalysisAPI javaAnalysisAPI = loadService(JavaAnalysisAPI.class);
    private final JareentoServiceAPI jareentoServiceAPI = loadService(JareentoServiceAPI.class);

    @Test
    void removeSelectedMethodsAndFixOverrides(@TempDir File mavenDir) {

        File sourcesDir = new File(mavenDir, "src/main/java");
        File expectedDir = new File(mavenDir, "expected/removeSelectedMethodsAndFixOverrides-expected");
        File storageDirectory = new File(mavenDir, "storage");

        copyResourcesInLocationDeep(getClass(),
                "/org/abego/jareento/sample-projects/sample2", mavenDir);

        String[] typesToCheckForMethods = {
                "com.example.sample2.SynBase"
        };

        JavaAnalysisProjectConfiguration javaAnalysisProjectConfiguration =
                javaAnalysisAPI.newJavaAnalysisProjectConfiguration(
                        "sample1",
                        mavenDir,
                        new File[]{sourcesDir});

        // remove all methods but the constructor
        Predicate<JavaMethod> methodSelector = m -> !m.isConstructor();

        Consumer<String> progress = System.out::println;

        JavaAnalysisProjectStorage storage = javaAnalysisAPI
                .getJavaAnalysisProjectStorage(storageDirectory.toURI());
        JavaAnalysisProject project = storage.createAndLoadJavaAnalysisProject(
                javaAnalysisProjectConfiguration, progress);

        jareentoServiceAPI.removeSelectedMethodsAndFixOverrides(
                project,
                methodSelector,
                typesToCheckForMethods,
                progress);

        assertEqualFiles(expectedDir, sourcesDir);
    }
}
