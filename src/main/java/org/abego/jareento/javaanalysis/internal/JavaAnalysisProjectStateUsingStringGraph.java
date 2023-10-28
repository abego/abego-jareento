package org.abego.jareento.javaanalysis.internal;

import org.abego.commons.io.FileUtil;
import org.abego.commons.lang.IntUtil;
import org.abego.commons.lang.StringUtil;
import org.abego.jareento.base.JareentoException;
import org.abego.jareento.javaanalysis.JavaMethodDeclarator;
import org.abego.stringgraph.core.Node;
import org.abego.stringgraph.core.Nodes;
import org.abego.stringgraph.core.StringGraph;
import org.abego.stringgraph.core.StringGraphBuilder;
import org.abego.stringgraph.core.StringGraphDump;
import org.abego.stringgraph.core.StringGraphs;

import javax.annotation.Syntax;
import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.logging.Logger.getLogger;
import static org.abego.jareento.base.JareentoSyntax.QUALIFIED_TYPE_OR_ARRAY_NAME_SYNTAX;
import static org.abego.jareento.javaanalysis.internal.IDsImpl.newIDs;
import static org.abego.jareento.shared.JavaMethodDeclaratorUtil.newJavaMethodDeclarator;
import static org.abego.jareento.shared.JavaMethodDeclaratorUtil.methodDeclaratorText;
import static org.abego.jareento.shared.SyntaxUtil.qualifier;
import static org.abego.jareento.util.JavaLangUtil.rawNameNoArray;

public class JavaAnalysisProjectStateUsingStringGraph implements JavaAnalysisProjectStateWithSave {
    private static final Logger LOGGER = getLogger(JavaAnalysisProjectStateUsingStringGraph.class.getName());

    private static final String PROJECT_CONFIGURATION_ID = "org.abego.jareento.javaanalysis.core.JavaAnalysisProject-Configuration";

    private static final String RDFS_RESOURCE = "http://www.w3.org/2000/01/rdf-schema#Resource";
    private static final String RDFS_SUB_CLASS_OF = "http://www.w3.org/2000/01/rdf-schema#subClassOf";
    private static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    private static final String PACKAGE = "Package";
    private static final String CLASS = "Class";
    private static final String METHOD = "Method";
    private static final String METHOD_SIGNATURE = "MethodSignature";
    private static final String METHOD_CALL = "MethodCall";

    private static final String REFS = "refs";
    private static final String CONTAINS = "contains";
    private static final String EXTENDS = "extends";
    private static final String IMPLEMENTS = "implements";
    // we have different labels for "signature"-kind relations. This makes
    // some queries easier/faster. DON'T merge them.
    private static final String HAS_SIGNATURE = "hasSignature";
    private static final String RETURN_TYPE = "returnType";
    private static final String SIGNATURE = "signature";
    private static final String HAS_SCOPE = "hasScope";
    private static final String BASE_SCOPE = "baseScope";
    private static final String HAS_RAW_TYPE = "hasRawType";
    private static final String IS_ARRAY_OF = "isArrayOf";
    private static final String CALLS = "calls";
    private static final String IN_CLASSFILE = "inClassfile";

    private static final String BYTECODE_SIZE = "bytecodeSize";
    private static final String IS_DECLARED = "isDeclared";
    private static final String IS_INTERFACE = "isInterface";
    private static final String MD5 = "md5";
    private static final String HAS_OVERRIDE = "hasOverride";
    private static final String IS_SYNTHETIC = "isSynthetic";
    private static final String SOURCE_ROOTS = "sourceRoots";
    private static final String DEPENDENCIES = "dependencies";

    private final URI uri;
    private final StringGraph graph;

    JavaAnalysisProjectStateUsingStringGraph(URI uri, StringGraph graph) {
        this.uri = uri;
        this.graph = graph;
    }

    public static class BuilderImpl implements JavaAnalysisProjectStateBuilder {

        private final StringGraphBuilder graphBuilder =
                StringGraphs.getInstance().createStringGraphBuilder();
        private final URI uri;

        public BuilderImpl(URI uri) {
            this.uri = uri;
        }

        @Override
        public void addClass(@Syntax(QUALIFIED_TYPE_OR_ARRAY_NAME_SYNTAX) String name) {
            // Check for a typical error
            if (name.indexOf('<') >= 0) {
                throw new IllegalArgumentException("Class name must not contain '<...>'. Got: " + name);
            }
            
            String packagePath = qualifier(name);
            addInstance(graphBuilder, CLASS, name);
            if (!packagePath.isEmpty()) {
                addInstance(graphBuilder, PACKAGE, packagePath);
                graphBuilder.addEdge(packagePath, CONTAINS, name);
            }

            if (name.endsWith("[]")) {
                String arrayElementType = name.substring(0, name.length() - 2);
                addClass(arrayElementType);
                graphBuilder.addEdge(name,IS_ARRAY_OF, arrayElementType);
            }
        }

        private void addMethod(String methodId) {
            addInstance(graphBuilder, METHOD, methodId);
        }

        private void addMethodSignature(String methodSignature) {
            addInstance(graphBuilder, METHOD_SIGNATURE, methodSignature);
        }

        @Override
        public void addReference(String from, String to) {
            graphBuilder.addEdge(from, REFS, to);
        }
        
        @Override
        public void setMD5OfClass(String classname, String md5) {
            graphBuilder.setNodeProperty(classname, MD5, md5);
        }

        @Override
        public void setBytecodeSizeOfClass(String classname, int bytecodeSize) {
            if (bytecodeSize < 0) {
                throw new JareentoException(String.format(
                        "Bytecode size must not be negative. Got %d", bytecodeSize));
            }
            graphBuilder.setNodeProperty(classname, BYTECODE_SIZE, String.valueOf(bytecodeSize));
        }

        @Override
        public void setIsInterfaceOfClass(String classname, boolean value) {
            graphBuilder.setNodeProperty(classname, IS_INTERFACE, String.valueOf(value));
        }

        @Override
        public JavaAnalysisProjectStateWithSave build() {
            return new JavaAnalysisProjectStateUsingStringGraph(uri, graphBuilder.build());
        }

        @Override
        public void addTypeExtends(String typename, String otherTypename) {
            addClass(typename);
            addClass(otherTypename);
            graphBuilder.addEdge(typename, EXTENDS, otherTypename);
        }

        @Override
        public void addTypeImplements(String typename, String otherTypename) {
            addClass(typename);
            addClass(otherTypename);
            graphBuilder.addEdge(typename, IMPLEMENTS, otherTypename);

        }

        @Override
        public String addMethod(
                String typename, String methodSignature, String returnType) {
            addClass(typename);
            addMethodSignature(methodSignature);
            if (!returnType.isEmpty()) {
                addClass(rawNameNoArray(returnType));
            }

            String methodId = getMethodId(typename, methodSignature, returnType);
            addMethod(methodId);
            graphBuilder.addEdge(typename, CONTAINS, methodId);
            addMethodSignature(methodSignature);
            graphBuilder.addEdge(methodId, HAS_SIGNATURE, methodSignature);
            if (!returnType.isEmpty()) {
                graphBuilder.addEdge(methodId, RETURN_TYPE, returnType);
            }
            return methodId;
        }

        @Override
        public String getMethodId(String typename, String methodSignature, String returnType) {
            return methodDeclaratorText(typename, methodSignature, returnType);
        }

        @Override
        public void setMethodHasOverride(String methodId, boolean value) {
            //TODO: helper in Builder for "setNodeProperty"
            graphBuilder.setNodeProperty(methodId, HAS_OVERRIDE, String.valueOf(value));
        }

        @Override
        public void setMethodIsSynthetic(String methodId, boolean value) {
            //TODO: helper in Builder for "setNodeProperty"
            graphBuilder.setNodeProperty(methodId, IS_SYNTHETIC, String.valueOf(value));
        }

        @Override
        public String addMethodCall(String callKind,
                                    String callScopeTypename,
                                    String calledMethodSignature,
                                    String callingMethodTypename,
                                    String callingMethodSignature,
                                    String callingMethodReturnType,
                                    String locationInCallingMethod) {
            String callingMethodId = addMethod(
                    callingMethodTypename, callingMethodSignature, callingMethodReturnType);

            String methodCallId = IDSyntaxUtil.newMethodCallId(
                    callKind, callingMethodId, locationInCallingMethod);

            addInstance(graphBuilder, METHOD_CALL, methodCallId);
            graphBuilder.addEdge(callingMethodId, CALLS, methodCallId);

            addMethodSignature(calledMethodSignature);
            graphBuilder.addEdge(methodCallId, SIGNATURE, calledMethodSignature);

            if (!callScopeTypename.isEmpty()) {
                setMethodCallScope(methodCallId, callScopeTypename);


                //TODO shall we also add a "CALLS" link to the actual method 
                //  in the scope?
                // Problem: the calledMethodSignature does not include no return 
                // type but the methodId does. So we cannot construct the 
                // id of the method now.
            }

            return methodCallId;
        }

        @Override
        public void setMethodCallScope(String methodCallId, String callScopeTypename) {
            addClass(callScopeTypename);
            graphBuilder.addEdge(methodCallId, HAS_SCOPE, callScopeTypename);
        }

        @Override
        public void setMethodCallBaseScope(String methodCallId, String baseScopeTypename) {
            addClass(baseScopeTypename);
            graphBuilder.addEdge(methodCallId, BASE_SCOPE, baseScopeTypename);
        }

        @Override
        public void addClassHasRawType(String classname, String rawClassname) {
            graphBuilder.addEdge(classname, HAS_RAW_TYPE, rawClassname);
        }

        @Override
        public void addClassInClassFile(String classname, String classfileName) {
            graphBuilder.addEdge(classname, IN_CLASSFILE, classfileName);
        }

        @Override
        public void setClassIsDeclared(String classname, boolean value) {
            graphBuilder.setNodeProperty(classname, IS_DECLARED, String.valueOf(value));
        }

        @Override
        public void setSourceRoots(File[] sourceRoots) {
            graphBuilder.addNode(PROJECT_CONFIGURATION_ID);
            graphBuilder.setNodeProperty(PROJECT_CONFIGURATION_ID, SOURCE_ROOTS,
                    FileUtil.filePathLines(sourceRoots));
        }

        @Override
        public void setDependencies(File[] files) {
            graphBuilder.addNode(PROJECT_CONFIGURATION_ID);
            graphBuilder.setNodeProperty(PROJECT_CONFIGURATION_ID, DEPENDENCIES,
                    FileUtil.filePathLines(files));
        }

        //TODO: no need for the builder parameter, is instance field
        private void addInstance(
                StringGraphBuilder builder, String classNode, String name) {
            builder.addEdge(classNode, RDFS_SUB_CLASS_OF, RDFS_RESOURCE);
            builder.addEdge(name, RDF_TYPE, classNode);
        }

    }

    @Override
    public void save() {
        StringGraphs.getInstance().writeStringGraph(graph, uri);
    }

    @Override
    public IDs methodsOfClass(String className) {
        return ids(graph.nodes(className, CONTAINS, "?"));
    }

    @Override
    public boolean hasMethodOverrideAnnotation(String methodId) {
        return graph.getBooleanNodePropertyValue(methodId, HAS_OVERRIDE);
    }

    @Override
    public boolean isConstructor(String methodId) {
        JavaMethodDeclarator decl = newJavaMethodDeclarator(methodId);
        return decl.getMethodName().equals(decl.getSimpleClassname());
    }

    @Override
    public boolean isMethodSynthetic(String methodId) {
        return graph.getBooleanNodePropertyValue(methodId, IS_SYNTHETIC);
    }

    @Override
    public IDs methodCalls() {
        return ids(graph.nodes("?", RDF_TYPE, METHOD_CALL));
    }

    @Override
    public IDs methodCallsInMethod(String methodId) {
        return ids(graph.nodes(methodId, CALLS, "?"));
    }

    @Override
    public IDs methodCallsWithSignature(String methodSignature) {
        return ids(graph.nodes("?", SIGNATURE, methodSignature));
    }

    @Override
    public IDs methodCallsWithSignatureOnClass(String methodSignature, String className) {
        Nodes callsWithScopeOrBaseScope =
                graph.nodes("?", HAS_SCOPE, className)
                        .union(graph.nodes("?", BASE_SCOPE, className));
        Nodes result =
                graph.nodes("?", SIGNATURE, methodSignature)
                        .intersected(callsWithScopeOrBaseScope);
        return ids(result);
    }

    @Override
    public String scopeOfMethodCall(String methodCallId) {
        //TODO add Nodes.optionalNodeId() (next to "singleNodeId()")
        Nodes nodes = graph.nodes(methodCallId, HAS_SCOPE, "?");
        return nodes.getSize() == 0 ? "" : nodes.singleNodeId();
    }

    @Override
    public String baseScopeOfMethodCall(String methodCallId) {
        //TODO add Nodes.optionalNodeId() (next to "singleNodeId()")
        Nodes nodes = graph.nodes(methodCallId, BASE_SCOPE, "?");
        return nodes.getSize() == 0 ? "" : nodes.singleNodeId();
    }

    @Override
    public String signatureOfMethodCall(String methodCallId) {
        return graph.nodes(methodCallId, SIGNATURE, "?").singleNodeId();
    }

    @Override
    public String fileOfMethodCall(String methodCallId) {
        return graph.nodes("?", CONTAINS, methodCallId).singleNodeId();
    }

    @Override
    public String extendedType(String className) {
        //TODO add Nodes.optionalNodeIdOrElse(String) (next to "singleNodeId()")
        Nodes superclasses = graph.nodes(className, EXTENDS, "?");
        if (superclasses.getSize() == 0) {
            return "java.lang.Object";
        }
        //TODO fail when > 1 nodes exist.
        return superclasses.iterator().next().id();
    }

    @Override
    public IDs classesExtending(String className) {
        return ids(graph.nodes("?", EXTENDS, className));
    }

    @Override
    public IDs implementedInterfaces(String className) {
        return ids(graph.nodes(className, IMPLEMENTS, "?"));
    }

    @Override
    public IDs extendedTypes(String typeName) {
        return ids(graph.nodes(typeName, EXTENDS, "?"));
    }

    @Override
    public IDs methods() {
        return ids(graph.nodes("?", RDF_TYPE, METHOD));
    }

    @Override
    public String returnTypeOfMethod(String methodId) {
        return graph.nodes(methodId, RETURN_TYPE, "?").singleNodeId();
    }

    private Nodes methodsWithSignature(String methodSignature) {
        return graph.nodes("?", HAS_SIGNATURE, methodSignature);
    }

    @Override
    public IDs idsOfMethodsWithSignature(String signature) {
        return ids(methodsWithSignature(signature));
    }

    @Override
    public IDs classes() {
        return ids(graph.nodes("?", RDF_TYPE, CLASS));
    }
    
    @Override
    public IDs classesReferencingClass(String classname) {
        return ids(graph.nodes("?", REFS, classname));
    }

    @Override
    public IDs classesContainingMethodWithSignature(String methodSignature) {
        return newIDs(() -> methodsWithSignature(methodSignature).stream()
                .map(m -> classOfMethod(m.id()))
                .collect(Collectors.toSet()));
    }
    
    @Override
    public Optional<String> classFileOfClass(String classname) {
        Nodes nodes = graph.nodesFromNodeViaEdgeLabeled(classname, IN_CLASSFILE);
        return optionalSingleId(nodes);
    }

    public static Optional<String> optionalSingleId(Nodes nodes) {
        String[] ids = nodes.stream().map(Node::id).toArray(String[]::new);
        return ids.length == 1
                ? Optional.of(ids[0]) : Optional.empty();
    }

    @Override
    public OptionalInt bytecodeSizeOfClass(String classname) {
        if (graph.hasNodeProperty(classname, BYTECODE_SIZE)) {
            String s = graph.getNodePropertyValue(classname, BYTECODE_SIZE);
            OptionalInt result = IntUtil.parseInt(s);
            if (result.isEmpty() || result.getAsInt() < 0) {
                LOGGER.warning(() -> String.format(
                        "Invalid value stored for bytecode size of class %s: %s",
                        classname, StringUtil.quoted2(s)));
            }
            return result;
        } else {
            return OptionalInt.empty();
        }
    }

    @Override
    public Optional<String> md5OfClass(String classname) {
        return graph.getOptionalNodePropertyValue(classname, MD5);
    }

    @Override
    public boolean isInterface(String classname) {
        return graph.getBooleanNodePropertyValue(classname, IS_INTERFACE);
    }

    @Override
    public boolean isClassDeclared(String classname) {
        return graph.getBooleanNodePropertyValue(classname, IS_DECLARED);
    }

    //TODO: is this the correct place for this method
    @Override
    public void dump(PrintWriter writer) {
        Map<String, String> translation = new HashMap<>();
        translation.put(RDF_TYPE, "a");
        translation.put(RDFS_SUB_CLASS_OF, "rdfs:subClassOf");
        translation.put(RDFS_RESOURCE, "rdfs:Resource");

        StringGraphDump dump = StringGraphs.getInstance()
                .createStringGraphDump(graph, key -> translation.getOrDefault(key, key));
        dump.write(writer);
    }

    @Override
    public IDs methodSignatureSpecificationsOfClass(String className) {
        return newIDs(() -> {
            Set<String> result = new HashSet<>();
            //TODO: restrict to instances of "Method" 
            //  (maybe in the future a class may "CONTAIN" other things than methods)
            Nodes methodsOfClass = graph.nodes(className, CONTAINS, "?");

            //TODO: check if nodes are really methodSignatures
            methodsOfClass.stream()
                    .map(m -> signatureOfMethod(m.id()))
                    .forEach(result::add);
            return result;
        });
    }

    @Override
    public String idOfMethodContainingMethodCall(String methodCallId) {
        return IDSyntaxUtil.idOfCallingMethodOfMethodCall(methodCallId);
    }

    @Override
    public String signatureOfMethod(String methodId) {
        // Shortcut: no need to check the graph for the signature as
        // that information is currently encoded in the methodId/
        // methodDeclarator.
        return newJavaMethodDeclarator(
                methodDeclaratorTextOfMethodWithId(methodId)).getSignatureText();
    }

    @Override
    public String nameOfMethod(String methodId) {
        // Shortcut: no need to check the graph for the method name as
        // that information is currently encoded in the methodId/
        // methodDeclarator.
        return newJavaMethodDeclarator(
                methodDeclaratorTextOfMethodWithId(methodId)).getMethodName();
    }

    @Override
    public String idOfMethodDeclaredAs(String methodDeclaratorText) {
        // currently the methodId has the exact same value as the methodDeclarator.
        //TODO: check if the method exists using hasNode (when in StringGraph's 
        //  public API) and throw exception if not
        return methodDeclaratorText;
    }

    @Override
    public String methodDeclaratorTextOfMethodWithId(String methodId) {
        // currently the methodId has the exact same value as the methodDeclarator
        return methodId;
    }

    @Override
    public String classOfMethod(String methodId) {
        // Shortcut: no need to check the graph for the class as
        // that information is currently encoded in the methodId/
        // methodDeclarator.
        return newJavaMethodDeclarator(
                methodDeclaratorTextOfMethodWithId(methodId)).getClassname();
    }

    @Override
    public String packageOfMethod(String methodId) {
        return qualifier(classOfMethod(methodId));
    }

    @Override
    public File[] sourceRoots() {
        String filePaths = graph.getNodePropertyValueOrElse(
                PROJECT_CONFIGURATION_ID, SOURCE_ROOTS, "");
        return FileUtil.parseFiles(filePaths, "\n");
    }

    @Override
    public File[] dependencies() {
        String filePaths = graph.getNodePropertyValueOrElse(
                PROJECT_CONFIGURATION_ID, DEPENDENCIES, "");
        return FileUtil.parseFiles(filePaths, "\n");
    }

    private static String[] toStringArray(Nodes nodes) {
        return nodes.stream().map(Node::id).toArray(String[]::new);
    }

    private static IDs ids(Nodes nodes) {
        return newIDs(() -> nodes.stream()
                .map(Node::id)
                .collect(Collectors.toSet()));
    }
}
