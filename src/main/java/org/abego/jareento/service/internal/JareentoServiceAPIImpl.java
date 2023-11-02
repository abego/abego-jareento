package org.abego.jareento.service.internal;

import org.abego.commons.util.ServiceLoaderUtil;
import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaMethod;
import org.abego.jareento.javaanalysis.internal.JavaAnalysisFiles;
import org.abego.jareento.javarefactoring.JavaRefactoringAPI;
import org.abego.jareento.service.JareentoServiceAPI;
import org.abego.jareento.service.SelectedAndOverridingMethods;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.abego.jareento.javaanalysis.internal.JavaAnalysisProjectImpl.toInternal;

public class JareentoServiceAPIImpl implements JareentoServiceAPI {
    private final JavaRefactoringAPI javaRefactoringAPI = ServiceLoaderUtil.loadService(JavaRefactoringAPI.class);

    public JareentoServiceAPIImpl() {
    }


    @Override
    public SelectedAndOverridingMethods selectedAndOverridingMethods(
            JavaAnalysisProject javaAnalysisProject,
            Predicate<JavaMethod> methodSelector,
            String[] typesToCheckForMethods,
            Consumer<String> progress) {

        return new SelectedAndOverridingMethodsOperation()
                .getSelectedAndOverridingMethods(
                        toInternal(javaAnalysisProject),
                        methodSelector,
                        typesToCheckForMethods,
                        progress);
    }

    @Override
    public void removeSelectedMethodsAndFixOverrides(
            JavaAnalysisProject javaAnalysisProject,
            Predicate<JavaMethod> methodSelector,
            String[] typesToCheckForMethods,
            Consumer<String> progress) {

        SelectedAndOverridingMethods result = selectedAndOverridingMethods(
                javaAnalysisProject, methodSelector, typesToCheckForMethods, progress);

        JavaAnalysisFiles javaAnalysisFiles = javaAnalysisProject.getJavaAnalysisFiles();
        javaRefactoringAPI.removeMethods(javaAnalysisFiles, result.selectedMethods(), progress);
        javaRefactoringAPI.removeMethodAnnotations(javaAnalysisFiles, "java.lang.Override", result.overridingMethods(), progress);
    }
}
