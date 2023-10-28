package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaClass;
import org.abego.jareento.javaanalysis.JavaClasses;
import org.abego.jareento.javaanalysis.JavaMethodSignatures;

import java.util.HashSet;
import java.util.Set;

import static org.abego.jareento.javaanalysis.internal.IDsImpl.newIDs;
import static org.abego.jareento.javaanalysis.internal.JavaMethodSignaturesImpl.newJavaMethodSignatures;

class JavaClassesImpl extends ManyWithIdDefault<JavaClass, JavaClasses> implements JavaClasses {
    private final JavaAnalysisProject project;

    private JavaClassesImpl(IDs ids, JavaAnalysisProject project) {
        super(ids);
        this.project = project;
    }

    public static JavaClasses newJavaClasses(IDs ids, JavaAnalysisProject project) {
        return new JavaClassesImpl(ids, project);
    }

    @Override
    protected JavaClass elementWithId(String id) {
        return JavaClassImpl.newJavaClass(id, project);
    }

    @Override
    protected JavaClasses newInstance(IDs ids) {
        return newJavaClasses(ids, project);
    }

    @Override
    public JavaMethodSignatures getMethodSignatures() {

        return newJavaMethodSignatures(newIDs(() -> {
            Set<String> result = new HashSet<>();
            idStream().forEach(classId ->
                    project.methodSignaturesOfClass(classId).idStream()
                            .forEach(result::add));
            return result;
        }), project);
    }

    @Override
    public JavaClasses unitedWithClassNamed(String classname) {
        return unitedWithElementWithId(classname);
    }
}
