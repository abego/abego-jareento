package org.abego.jareento.service.internal;

import org.abego.commons.lang.ArrayUtil;
import org.abego.commons.util.ServiceLoaderUtil;
import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaMethod;
import org.abego.jareento.javarefactoring.JavaRefactoringAPI;
import org.abego.jareento.javarefactoring.JavaRefactoringProject;
import org.abego.jareento.service.JareentoServiceAPI;
import org.abego.jareento.service.SelectedAndOverridingMethods;

import java.io.File;
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

        File[] sourceRootsAndDependencies = ArrayUtil.concatenate(
                javaAnalysisProject.getSourceRoots(),
                javaAnalysisProject.getDependencies());
        JavaRefactoringProject javaRefactoringProject =
                javaRefactoringAPI.newJavaRefactoringProject(sourceRootsAndDependencies);

        javaRefactoringAPI.removeMethods(javaRefactoringProject, result.selectedMethods(), progress);
        javaRefactoringAPI.removeMethodAnnotations(javaRefactoringProject, "java.lang.Override", result.overridingMethods(), progress);
    }
}
