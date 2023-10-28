package org.abego.jareento.service.internal;

import org.abego.jareento.base.JavaMethodDeclaratorSet;
import org.abego.jareento.base.JavaMethodDeclaratorSetBuilder;
import org.abego.jareento.javaanalysis.JavaAnalysisAPI;
import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaMethodSelector;
import org.abego.jareento.javaanalysis.JavaMethods;
import org.abego.jareento.service.SelectedAndOverridingMethods;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.abego.commons.lang.StringUtil.indent;
import static org.abego.jareento.base.JavaMethodDeclaratorSetBuilder.newJavaMethodSetBuilder;

public class SelectedAndOverridingMethodsOperation {

    public SelectedAndOverridingMethodsOperation() {
    }

    public SelectedAndOverridingMethods getSelectedAndOverridingMethods(
            JavaAnalysisProject javaAnalysisProject,
            JavaMethodSelector methodSelector,
            String[] classesToCheckForMethods,
            Consumer<String> progress) {


        JavaMethodDeclaratorSetBuilder selectedMethodsSetBuilder = newJavaMethodSetBuilder();
        JavaMethodDeclaratorSetBuilder overridingMethodsSetBuilder = newJavaMethodSetBuilder();

        addSelectedAndOverridingMethods(
                javaAnalysisProject, methodSelector, classesToCheckForMethods,
                selectedMethodsSetBuilder, overridingMethodsSetBuilder, progress);

        JavaMethodDeclaratorSet selectedMethods = selectedMethodsSetBuilder.build();
        JavaMethodDeclaratorSet overridingMethods = overridingMethodsSetBuilder.build();

        return new SelectedAndOverridingMethods() {

            @Override
            public JavaMethodDeclaratorSet selectedMethods() {
                return selectedMethods;
            }

            @Override
            public JavaMethodDeclaratorSet overridingMethods() {
                return overridingMethods;
            }
        };
    }

    private void addSelectedAndOverridingMethods(
            JavaAnalysisProject javaAnalysisProject,
            JavaMethodSelector methodSelector,
            String[] classnames,
            JavaMethodDeclaratorSetBuilder selectedMethods,
            JavaMethodDeclaratorSetBuilder overridingMethods,
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
            JavaMethodDeclaratorSetBuilder selectedMethods,
            JavaMethodDeclaratorSetBuilder overridingMethods) {

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
