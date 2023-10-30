package org.abego.jareento.javaanalysis.internal.input.javap;

import org.abego.commons.lang.IntUtil;
import org.abego.commons.lang.SeparatedItemScanner;
import org.abego.commons.lineprocessing.LineProcessing;
import org.abego.commons.lineprocessing.LineProcessing.ScriptBuilder;
import org.abego.jareento.base.JareentoException;
import org.abego.jareento.shared.SyntaxUtil;
import org.eclipse.jdt.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;

/**
 * Parses the output of running the command {@code javap}
 * and delivers the extracted information to an event listener.
 * <p>
 * Tip: to get the infos of all classes in `target/classes` you may use
 * this script:
 * <pre>
 * find target/classes -name "*.class" |xargs javap  -c -p -sysinfo >target/youNameIt.javap.txt
 * </pre>
 * Tip: to get the infos of all classes in a jar file you may use this script:
 * <pre>
 *     JAR={path-to-jar-file}; jar -tf $JAR | grep "class$" | sed s/\.class$// | xargs javap -c -p -sysinfo -classpath $JAR
 * </pre>
 */
class JavapParser {
    private static final Logger LOGGER = getLogger(JavapParser.class.getName());
    private static final String ACCESS_REGEX = "((?:public )|(?:protected )|(?:private ))?";

    public interface EventHandler {

        default void onClassfile(String classfile) {
        }

        default void onClassfileEnd(String classFile) {
        }

        default void onClassfileBytecodeSize(String classfile, int size) {
        }

        default void onClassfileMD5(String classfile, String md5) {
        }

        default void onClassfileSHA256(String classfile, String sha256) {
        }

        default void onJavaFilename(String classfile, String javaFilename) {
        }

        default void onClass(String typeName, String access, String modifier, String type, String[] extendedTypes, String[] implementedTypes) {
        }

        default void onClassEnd(String typeName) {
        }

        default void onMethod(String typeName, String methodName, String access, String modifier, String returnType, String parameter, String exceptions, String typeParametersOfMethod) {
        }

        default void onField(String className, String fieldName, String access, String modifier, String type) {
        }

        default void onStaticInitialization(String className) {
        }

        default void onInstruction(String className, String methodName, String parameters, String returnType, int offset, String mnemonic, String arguments, String comment) {
        }

        default void onFlags(String className, String methodName, String parameters, String returnType, String[] flags) {
        }

        default void onException(int from, int to, int target, String type) {
        }

        default void onEnd() {
        }

        default void onSourcefile(String sourcefile) {
        }
    }

    private JavapParser() {
    }

    public static JavapParser newJavapParser() {
        return new JavapParser();
    }


    private static class MyState {
        String classFile = "";
        String className = "";
        String methodName = "";
        String parameters = "";
        String returnType = "";
        boolean isInInstructionBlock = false;
        boolean isInConstantPoolBlock = false;
        boolean isInBootstrapMethodsBlock = false;
        boolean isInInnerClassesBlock = false;
        boolean isNestMembersBlock = false;
        boolean isInRuntimeAnnotationsBlock = false;
        @Nullable Runnable descriptorAction = null;

        public boolean isInAnyBlock() {
            return isInInstructionBlock ||
                    isInConstantPoolBlock ||
                    isInBootstrapMethodsBlock ||
                    isInInnerClassesBlock ||
                    isNestMembersBlock ||
                    isInRuntimeAnnotationsBlock;
        }

        public void endAllBlocks() {
            isInInstructionBlock = false;
            isInConstantPoolBlock = false;
            isInInnerClassesBlock = false;
            isNestMembersBlock = false;
            isInBootstrapMethodsBlock = false;
            isInRuntimeAnnotationsBlock = false;
        }
    }

    private static LineProcessing.Script newScript(EventHandler handler) {
        ScriptBuilder<MyState> b = LineProcessing.newScriptBuilder(MyState::new);

        b.onMatch("\\S.*", (c, s) -> {
            // any line without indention ends some blocks, like
            // "InnerClasses" or "Runtime...Annotations" 
            s.isInInnerClassesBlock = false;
            s.isNestMembersBlock = false;
            s.isInRuntimeAnnotationsBlock = false;
            s.isInConstantPoolBlock = false;

            c.more(); // check for more rules
        });

        b.onMatch((c, s) -> s.isInConstantPoolBlock, (c, s) -> {});

        b.onMatch("Classfile (.+)", (c, s) -> {
            if (!s.classFile.isEmpty()) {
                handler.onClassfileEnd(s.classFile);
            }
            s.classFile = c.m().group(1);
            handler.onClassfile(c.m().group(1));
        });

        b.onMatch("  Last modified (?:[^;]+); size (\\d+) bytes", (c, s) -> {
            String sizeText = c.m().group(1);
            int size = IntUtil.parseInt(sizeText)
                    .orElseThrow(() -> new JareentoException(
                            String.format("Bytecode size is not an int (\"%s\", near line: %d)",
                                    sizeText, c.lineNumber())));

            handler.onClassfileBytecodeSize(s.classFile, size);
        });

        b.onMatch("  MD5 checksum (\\w+)", (c, s) ->
                handler.onClassfileMD5(s.classFile, c.m().group(1)));

        b.onMatch(
                "  SHA-256 checksum (\\w+)",
                (c, s) -> handler.onClassfileSHA256(s.classFile, c.m()
                        .group(1)));

        b.onMatch("\\s*Compiled from \"([^\"]+)\"", (c, s) ->
                handler.onJavaFilename(s.classFile, c.m().group(1)));

        // static initializer
        b.onMatch("  static \\{\\};", (c, s) -> {
            checkNoDescriptorAction(c, s);

            s.descriptorAction = () -> {
                s.methodName = "\"<cinit>\"";
                s.parameters = "";
                s.returnType = "";
                handler.onStaticInitialization(s.className);
            };
        });

        // class
        b.onMatch(ACCESS_REGEX + "((?:abstract )?(?:final )?)?((?:class )|(?:interface )|(?:enum )|(?:@interface ))(.+)", (c, s) -> {
            String access = trim(c.m().group(1));
            String modifier = trim(c.m().group(2));
            String type = trim(c.m().group(3));
            // Regular expressions are not sufficient to parse the 
            // remaining part of the line, mainly because we have to 
            // take care of the potentially nested "< ... >" construct
            // in generic type. Therefore, we are using a dedicated 
            // parser to cover this.
            SeparatedItemScanner scanner = SeparatedItemScanner.newSeparatedItemScanner(c.m()
                    .group(4));

            s.className = scanner.nextItem();
            List<String> extendsTexts = new ArrayList<>();
            List<String> implementsTexts = new ArrayList<>();
            String nextItem = scanner.nextItem();
            if (nextItem.equals("extends")) {
                nextItem = scanner.nextItem();
                while (!nextItem.isEmpty() && !nextItem.equals("implements")) {
                    extendsTexts.add(nextItem);
                    nextItem = scanner.nextItem();
                }
            }
            if (nextItem.equals("implements")) {
                nextItem = scanner.nextItem();
                while (!nextItem.isEmpty()) {
                    implementsTexts.add(nextItem);
                    nextItem = scanner.nextItem();
                }
            }
            handler.onClass(s.className,
                    access,
                    modifier,
                    type,
                    extendsTexts.toArray(new String[0]),
                    implementsTexts.toArray(new String[0]));
        });

        // "}" - end of class/instruction block/constant pool block
        b.onMatch("}", (c, s) -> {
            if (s.isInAnyBlock()) {
                s.endAllBlocks();
            } else {
                handler.onClassEnd(s.className);
            }
        });


        // instruction line
        //
        // (Notice we need to switch on DOTALL mode in the final regex block 
        // "(?s:...)" to cover the case the comment includes a NEL control 
        // character. 
        // We observed this case in some sample code when the comment contained
        // the "String" value of an operand.)
        b.onMatch("\\s+(\\d+): (\\w+)( [^\\/]*)?(?:\\/\\/ (?s:(.*)))?", (c, s) -> {
            int offset = Integer.parseInt(c.m().group(1));
            String mnemonic = c.m().group(2);
            String arguments = trim(c.m().group(3));
            String comment = trim(c.m().group(4));

            // instructions like `lookupswitch` or `tableswitch`  will 
            // start a `{...}`, e.g.:
            //
            //       13: lookupswitch  { // 2
            // 
            // Make sure to handle the corresponding '}' properly and 
            // don't assume this always to be the end of a 
            // `class`/`interface` etc.
            if (arguments.trim().endsWith("{")) {
                s.endAllBlocks();
                s.isInInstructionBlock = true;
            }
            handler.onInstruction(s.className, s.methodName, s.parameters, s.returnType, offset, mnemonic, arguments, comment);
        });

        // flags line
        b.onMatch("\\s+flags: (.+)", (c, s) -> {
            String[] flags = c.m().group(1).split(", ");
            handler.onFlags(s.className, s.methodName, s.parameters, s.returnType, flags);
        });

        // SourceFile line
        b.onMatch("SourceFile: \"([^\"]+)\"", (c, s) -> {
            String sourceFile = c.m().group(1);
            handler.onSourcefile(sourceFile);
        });

        // Constant pool line
        b.onMatch("Constant pool:", (c, s) -> {
            s.endAllBlocks();
            s.isInConstantPoolBlock = true;
        });

        // BootstrapMethods line
        b.onMatch("BootstrapMethods:", (c, s) -> {
            s.endAllBlocks();
            s.isInBootstrapMethodsBlock = true;
        });

        // Signature line
        b.onMatch("Signature: .+", (c, s) -> {
        });

        // EnclosingMethod line
        b.onMatch("EnclosingMethod: .+", (c, s) -> {
        });

        // ignore the "Code:" line
        b.onMatch("    Code:", (c, s) -> {
        });

        // ignore the "stack=..., locals=..., args_size=..." line
        b.onMatch("\\s+stack=\\d+, locals=\\d+, args_size=\\d+", (c, s) -> {
        });

        // ignore the "LineNumberTable:" line
        b.onMatch("\\s+LineNumberTable:", (c, s) -> {
        });

        // ignore the "line ...: ..." line
        b.onMatch("\\s+line \\d+: \\d+", (c, s) -> {
        });

        // ignore the "LocalVariableTable:" line
        b.onMatch("\\s+LocalVariableTable:", (c, s) -> {
        });

        // ignore the "Start  Length  Slot  Name   Signature" line
        b.onMatch("\\s+Start\\s+Length\\s+Slot\\s+Name\\s+Signature", (c, s) -> {
        });

        // ignore the "flags:" line
        b.onMatch("\\s+flags:", (c, s) -> {
        });


        // ignore the "minor/major version" line
        b.onMatch("  ((minor)|(major)) version: \\d+", (c, s) -> {
        });

        // ignore the "this_class" line
        b.onMatch("  this_class: .+", (c, s) -> {
        });

        // ignore the "super_class" line
        b.onMatch("  super_class: .+", (c, s) -> {
        });

        // ignore the "super_class" line
        b.onMatch("\\s+Signature: .+", (c, s) -> {
        });

        // ignore the "interfaces/fields/methods/attributes" line
        b.onMatch("  interfaces: \\d+, fields: \\d+, methods: \\d+, attributes: \\d+", (c, s) -> {
        });

        // ignore empty lines
        b.onMatch("\\s*", (c, s) -> {
        });

        // Exception table related stuff
        b.onMatch("    Exception table:", (c, s) -> {
        }); // ignore
        b.onMatch("       from    to  target type", (c, s) -> {
        }); // ignore
        b.onMatch("\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(?:Class )?(.+)", (c, s) -> {
            int from = Integer.parseInt(c.m().group(1));
            int to = Integer.parseInt(c.m().group(2));
            int target = Integer.parseInt(c.m().group(3));
            String type = trim(c.m().group(4));
            handler.onException(from, to, target, type);
        });

        // method
        b.onMatch(
                "  " + ACCESS_REGEX + "((?:default )?(?:abstract )?(?:static )?(?:final )?(?:native )?(?:synchronized )?)?(?:(<[^>]+>) )?([^\\(]+)\\(([^\\)]*)\\)(?: throws ([^;]+))?;",
                (c, s) -> {
                    checkNoDescriptorAction(c, s);
                    String returnTypeAndMethodName = trim(c.m().group(4));
                    SeparatedItemScanner scanner = SeparatedItemScanner.newSeparatedItemScanner(returnTypeAndMethodName);
                    String item1 = scanner.nextItem();
                    String item2 = scanner.nextItem();
                    if (item2.isEmpty()) {
                        // only one item (the method name) found
                        s.methodName = trim(item1);
                    } else {
                        // return type and method name found; methodName 2nd
                        s.methodName = trim(item2);
                    }
                    if (s.methodName.equals(s.className)) {
                        // constructor
                        s.methodName = SyntaxUtil.simpleName(s.methodName);
                    }
                    // we need to postpone the onMethod event because the
                    // parameter types and return type information in this line
                    // is not always correct (e.g. for enum constructors).
                    // The correct info is in the `descriptor` line immediately 
                    // following.
                    String access = trim(c.m().group(1));
                    String modifier = trim(c.m().group(2));
                    String exceptions = trim(c.m().group(6));
                    String typeParametersOfMethod = trim(c.m().group(3));
                    s.descriptorAction = () ->
                            handler.onMethod(s.className,
                                    s.methodName,
                                    access,
                                    modifier,
                                    s.returnType,
                                    s.parameters,
                                    exceptions,
                                    typeParametersOfMethod);
                });

        // descriptor line
        b.onMatch("\\s+descriptor: (.+)", (c, s) -> {
            if (s.descriptorAction == null) {
                throw new IllegalStateException(
                        "Unexpected `descriptor:` line, no action defined: " + c.line());
            }
            ParameterAndReturnTypes types = JavapUtil.parseJavapDescriptor(c.m()
                    .group(1));
            s.parameters = parameterTypesListTextOrNull(types.parameterTypes());
            s.returnType = types.returnType();
            s.descriptorAction.run();
            s.descriptorAction = null;
        });

        // field 
        // (must come after "method" rule, as it also matches some method pattern)
        b.onMatch("  " + ACCESS_REGEX + "((?:static ))?([^ ]+ )?([^\\(]+);", (c, s) -> {
            checkNoDescriptorAction(c, s);

            String fieldName = trim(c.m().group(4));
            String access = trim(c.m().group(1));
            String modifier = trim(c.m().group(2));
            s.descriptorAction = () ->
                    handler.onField(s.className,
                            fieldName,
                            access,
                            modifier,
                            s.returnType);
        });

        // InnerClasses line
        b.onMatch("InnerClasses:", (c, s) -> s.isInInnerClassesBlock = true);
        // NestMembers line
        b.onMatch("NestMembers:", (c, s) -> s.isNestMembersBlock = true);
        // Inner class line
        b.onMatch("\\s+((public )|(private ))?(static )?(final )?#\\d+(= #\\d+ of #\\d+)?;.+", (c, s) -> {
        });
//        // Inner class line (variante 2)
//        b.onMatch("     #\\d+; //.+", (c, s) -> {
//        });

        // Runtime[...]Annotations line
        b.onMatch("Runtime.*Annotations:", (c, s) ->
                s.isInRuntimeAnnotationsBlock = true);
        // Runtime Visible Annotation
        b.onMatch("  \\d+: #\\d+\\((#\\d+=c#\\d+)?\\)", (c, s) -> {
        });

        // Deprecated line
        b.onMatch("Deprecated: true", (c, s) -> {
        });

        // report the "unmatched" lines 
        b.onDefault((c, s) -> {
            if (s.isInAnyBlock()) {
                // ignore all text inside an instruction/constant pool block
                return;
            }
            LOGGER.warning("Unmatched in JavapParser - " + c.lineNumber() + ": " + c.line());
        });

        // ensure every "onClassfile" event has a matching "onClassfileEnd"
        // and we signal the "end" of the processing to the handler, e.g. to do
        // some cleanup etc.
        b.onEndOfText((c, s) -> {
            if (!s.classFile.isEmpty()) {
                handler.onClassfileEnd(s.classFile);
            }
            handler.onEnd();
        });

        return b.build();
    }

    private static void checkNoDescriptorAction(LineProcessing.Context c, MyState s) {
        if (s.descriptorAction != null) {
            System.out.println("Previous descriptor action was not processed. Defined before: " + c.line());
        }
    }

    private static String trim(@Nullable String string) {
        return string != null ? string.trim() : "";
    }

    public void parseStream(InputStream inputStream, EventHandler handler) {
        LineProcessing.Script script = newScript(handler);
        try {
            script.process(inputStream);
        } catch (JareentoException e) {
            throw e;
        } catch (Exception e) {
            throw new JareentoException("Error when reading javap output", e); //NON-NLS
        }
    }

    public void parseFile(File file, EventHandler handler) {
        try (BufferedInputStream inputStream = new BufferedInputStream(
                Files.newInputStream(file.toPath()))) {
            parseStream(inputStream, handler);
        } catch (IOException e) {
            throw new JareentoException(
                    String.format("Error when parsing javap output file '%s'", file.getAbsolutePath()),
                    e);
        }
    }
    
    @Nullable
    private static String parameterTypesListTextOrNull(String @Nullable [] types) {
        return types != null ? String.join(", ", types) : null;
    }
}
