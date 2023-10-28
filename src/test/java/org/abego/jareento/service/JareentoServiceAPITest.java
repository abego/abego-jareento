package org.abego.jareento.service;

import org.abego.jareento.javaanalysis.JavaAnalysisAPI;
import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaAnalysisProjectConfiguration;
import org.abego.jareento.javaanalysis.JavaAnalysisProjectStorage;
import org.abego.jareento.javaanalysis.JavaMethodSelector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.function.Consumer;

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

        String[] classesToCheckForMethods = {
                "com.example.sample2.SynBase"
        };

        JavaAnalysisProjectConfiguration javaAnalysisProjectConfiguration =
                javaAnalysisAPI.newJavaAnalysisProjectConfiguration(
                        "sample1",
                        mavenDir,
                        new File[]{sourcesDir});
        //TODO: don't use methodId but method
        JavaMethodSelector methodSelector = (methodId, project) ->
                // all methods but the constructor        
                !project.isConstructor(methodId);

        Consumer<String> progress = System.out::println;

        JavaAnalysisProjectStorage storage = javaAnalysisAPI
                .getJavaAnalysisProjectStorage(storageDirectory.toURI());
        JavaAnalysisProject project = storage.createAndLoadJavaAnalysisProject(
                javaAnalysisProjectConfiguration, progress);

        jareentoServiceAPI.removeSelectedMethodsAndFixOverrides(
                project,
                methodSelector,
                classesToCheckForMethods,
                progress);

        assertEqualFiles(expectedDir, sourcesDir);
    }
}
