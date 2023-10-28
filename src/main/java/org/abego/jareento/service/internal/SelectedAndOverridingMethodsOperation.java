package org.abego.jareento.service.internal;

import org.abego.jareento.javaanalysis.JavaMethodDeclarators;
import org.abego.jareento.javaanalysis.JavaMethodDeclaratorsBuilder;
import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaMethodSelector;
import org.abego.jareento.javaanalysis.JavaMethods;
import org.abego.jareento.service.SelectedAndOverridingMethods;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.abego.commons.lang.StringUtil.indent;
import static org.abego.jareento.javaanalysis.JavaMethodDeclaratorsBuilder.newJavaMethodDeclaratorsBuilder;

public class SelectedAndOverridingMethodsOperation {

    public SelectedAndOverridingMethodsOperation() {
    }

    public SelectedAndOverridingMethods getSelectedAndOverridingMethods(
            JavaAnalysisProject javaAnalysisProject,
            JavaMethodSelector methodSelector,
            String[] classesToCheckForMethods,
            Consumer<String> progress) {


        JavaMethodDeclaratorsBuilder selectedMethodsSetBuilder = newJavaMethodDeclaratorsBuilder();
        JavaMethodDeclaratorsBuilder overridingMethodsSetBuilder = newJavaMethodDeclaratorsBuilder();

        addSelectedAndOverridingMethods(
                javaAnalysisProject, methodSelector, classesToCheckForMethods,
                selectedMethodsSetBuilder, overridingMethodsSetBuilder, progress);

        JavaMethodDeclarators selectedMethods = selectedMethodsSetBuilder.build();
        JavaMethodDeclarators overridingMethods = overridingMethodsSetBuilder.build();

        return new SelectedAndOverridingMethods() {

            @Override
            public JavaMethodDeclarators selectedMethods() {
                return selectedMethods;
            }

            @Override
            public JavaMethodDeclarators overridingMethods() {
                return overridingMethods;
            }
        };
    }

    private void addSelectedAndOverridingMethods(
            JavaAnalysisProject javaAnalysisProject,
            JavaMethodSelector methodSelector,
            String[] classnames,
            JavaMethodDeclaratorsBuilder selectedMethods,
            JavaMethodDeclaratorsBuilder overridingMethods,
            Consumer<String> progress) {

        progress.accept("Finding selected methods and affected overrides...");
        Consumer<String> innerProgress = indent(progress);
        long afterLoad = System.currentTimeMillis();
        for (String classname : classnames) {
            innerProgress.accept(String.format("Checking '%s'...", classname));
            addSelectedAndOverridingMethodsOfClass(
                    javaAnalysisProject, methodSelector, classname, selectedMethods, overridingMethods);
        }
        long afterCalc = System.currentTimeMillis();
        progress.accept(String.format("%d selected methods and %d affected overrides found. [%d ms]",
                selectedMethods.getSize(), overridingMethods.getSize(),
                afterCalc - afterLoad));
    }

    private void addSelectedAndOverridingMethodsOfClass(
            JavaAnalysisProject project,
            JavaMethodSelector methodSelector,
            String className,
            JavaMethodDeclaratorsBuilder selectedMethods,
            JavaMethodDeclaratorsBuilder overridingMethods) {

        Set<String> selectedMethodsOfClass =
                idsOfSelectedMethodsOfClass(project, className, methodSelector);
        selectedMethods.addAllMethods(selectedMethodsOfClass);

        overridingMethods.addAllMethods(
                idsOfDirectlyOverridingMethods(project, selectedMethodsOfClass));
    }

    private Set<String> idsOfDirectlyOverridingMethods(
            JavaAnalysisProject project, Set<String> idsOfDeadMethods) {
        Set<String> allOverridingMethods = new HashSet<>();
        for (String methodId : idsOfDeadMethods) {
            JavaMethods overridingMethods = project.methodsDirectlyOverridingMethod(methodId);
            allOverridingMethods.addAll(overridingMethods.idStream()
                    .collect(Collectors.toSet()));
        }
        return allOverridingMethods;
    }

    private Set<String> idsOfSelectedMethodsOfClass(
            JavaAnalysisProject project, String className, JavaMethodSelector methodSelector) {

        Set<String> result = new HashSet<>();
        project.methodsOfClass(className)
                .idStream()
                .sorted()
                .forEach(methodId -> {  //TODO use filter
                    if (methodSelector.isMethodSelected(methodId, project)) {
                        result.add(methodId);
                    }
                });
        return result;
    }
}
