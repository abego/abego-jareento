package org.abego.jareento.service;

import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaMethodSelector;

import java.util.function.Consumer;

public interface JareentoServiceAPI {

    SelectedAndOverridingMethods selectedAndOverridingMethods(
            JavaAnalysisProject javaAnalysisProject,
            JavaMethodSelector methodSelector,
            String[] classesToCheckForMethods,
            Consumer<String> progress);

    void removeSelectedMethodsAndFixOverrides(
            JavaAnalysisProject javaAnalysisProject,
            JavaMethodSelector methodSelector,
            String[] classesToCheckForMethods,
            Consumer<String> progress);
}
