package org.abego.jareento.service.internal;

import org.abego.commons.lang.ArrayUtil;
import org.abego.commons.util.ServiceLoaderUtil;
import org.abego.jareento.javaanalysis.JavaAnalysisAPI;
import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaMethodSelector;
import org.abego.jareento.javarefactoring.JavaRefactoringAPI;
import org.abego.jareento.javarefactoring.JavaRefactoringProject;
import org.abego.jareento.service.JareentoServiceAPI;
import org.abego.jareento.service.SelectedAndOverridingMethods;

import java.io.File;
import java.util.function.Consumer;

public class JareentoServiceAPIImpl implements JareentoServiceAPI {
    private final JavaAnalysisAPI javaAnalysisAPI = ServiceLoaderUtil.loadService(JavaAnalysisAPI.class);
    private final JavaRefactoringAPI javaRefactoringAPI = ServiceLoaderUtil.loadService(JavaRefactoringAPI.class);

    public JareentoServiceAPIImpl() {
    }


    @Override
    public SelectedAndOverridingMethods selectedAndOverridingMethods(
            JavaAnalysisProject javaAnalysisProject,
            JavaMethodSelector methodSelector,
            String[] classesToCheckForMethods,
            Consumer<String> progress) {
        return new SelectedAndOverridingMethodsOperation(javaAnalysisAPI)
                .getSelectedAndOverridingMethods(
                        javaAnalysisProject, methodSelector, classesToCheckForMethods, progress);
    }

    @Override
    public void removeSelectedMethodsAndFixOverrides(
            JavaAnalysisProject javaAnalysisProject,
            JavaMethodSelector methodSelector,
            String[] classesToCheckForMethods,
            Consumer<String> progress) {

        SelectedAndOverridingMethods result = selectedAndOverridingMethods(
                javaAnalysisProject, methodSelector, classesToCheckForMethods, progress);

        File[] sourceRootsAndDependencies = ArrayUtil.concatenate(
                javaAnalysisProject.sourceRoots(),
                javaAnalysisProject.dependencies());
        JavaRefactoringProject javaRefactoringProject =
                javaRefactoringAPI.newJavaRefactoringProject(sourceRootsAndDependencies);

        javaRefactoringAPI.removeMethods(javaRefactoringProject, result.selectedMethods(), progress);
        javaRefactoringAPI.removeMethodAnnotations(javaRefactoringProject, "java.lang.Override", result.overridingMethods(), progress);
    }
}
