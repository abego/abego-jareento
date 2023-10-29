package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.JavaType;
import org.abego.jareento.javaanalysis.JavaTypes;
import org.abego.jareento.javaanalysis.JavaMethodSignatures;

import java.util.HashSet;
import java.util.Set;

import static org.abego.jareento.javaanalysis.internal.IDsImpl.newIDs;
import static org.abego.jareento.javaanalysis.internal.JavaMethodSignaturesImpl.newJavaMethodSignatures;

class JavaTypesImpl extends ManyWithIdDefault<JavaType, JavaTypes> implements JavaTypes {
    private final JavaAnalysisProjectInternal project;

    private JavaTypesImpl(IDs ids, JavaAnalysisProjectInternal project) {
        super(ids);
        this.project = project;
    }

    public static JavaTypes newJavaTypes(IDs ids, JavaAnalysisProjectInternal project) {
        return new JavaTypesImpl(ids, project);
    }

    @Override
    protected JavaType elementWithId(String id) {
        return JavaTypeImpl.newJavaType(id, project);
    }

    @Override
    protected JavaTypes newInstance(IDs ids) {
        return newJavaTypes(ids, project);
    }

    @Override
    public Iterable<String> getNames() {
        return toSet();
    }

    @Override
    public JavaMethodSignatures getMethodSignatures() {

        return newJavaMethodSignatures(newIDs(() -> {
            Set<String> result = new HashSet<>();
            idStream().forEach(classId ->
                    project.methodSignaturesOfType(classId).idStream()
                            .forEach(result::add));
            return result;
        }), project);
    }

    @Override
    public JavaTypes unitedWithTypeNamed(String typeName) {
        return unitedWithElementWithId(typeName);
    }
}
