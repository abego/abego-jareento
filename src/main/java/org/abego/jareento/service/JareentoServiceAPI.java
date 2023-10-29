package org.abego.jareento.service;

import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaMethod;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface JareentoServiceAPI {

    SelectedAndOverridingMethods selectedAndOverridingMethods(
            JavaAnalysisProject javaAnalysisProject,
            Predicate<JavaMethod> methodSelector,
            String[] classesToCheckForMethods,
            Consumer<String> progress);

    void removeSelectedMethodsAndFixOverrides(
            JavaAnalysisProject javaAnalysisProject,
            Predicate<JavaMethod> methodSelector,
            String[] classesToCheckForMethods,
            Consumer<String> progress);
}
