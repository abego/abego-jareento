package org.abego.jareento.javaanalysis.internal.input.javap;

import org.abego.commons.lang.IntUtil;
import org.abego.commons.lang.SeparatedItemScanner;
import org.abego.commons.lineprocessing.LineProcessing;
import org.abego.commons.lineprocessing.LineProcessing.ScriptBuilder;
import org.abego.jareento.base.JareentoException;
import org.eclipse.jdt.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
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

        default void onClass(String classname, String access, String modifier, String type, String extendedType, String[] implementedTypes) {
        }

        default void onClassEnd(String classname) {
        }

        default void onMethod(String classname, String methodName, String access, String modifier, String returnType, String parameter, String exceptions, String typeParametersOfMethod) {
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
        boolean isInRuntimeVisibleAnnotationsBlock = false;

        public boolean isInAnyBlock() {
            return isInInstructionBlock ||
                    isInConstantPoolBlock ||
                    isInBootstrapMethodsBlock ||
                    isInInnerClassesBlock ||
                    isInRuntimeVisibleAnnotationsBlock;
        }

        public void endAllBlocks() {
            isInInstructionBlock = false;
            isInConstantPoolBlock = false;
            isInInnerClassesBlock = false;
            isInBootstrapMethodsBlock = false;
            isInRuntimeVisibleAnnotationsBlock = false;
        }
    }

    private static LineProcessing.Script newScript(EventHandler handler) {
        ScriptBuilder<MyState> b = LineProcessing.newScriptBuilder(MyState::new);

        b.onMatch("\\S.*", (c, s) -> {
            // any line without indention ends "InnerClasses" or 
            // "RuntimeVisibleAnnotations" blocks
            s.isInInnerClassesBlock = false;
            s.isInRuntimeVisibleAnnotationsBlock = false;

            c.more(); // check for more rules
        });

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
            s.methodName = "\"<cinit>\"";
            s.parameters = "";
            s.returnType = "";
            handler.onStaticInitialization(s.className);
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
            String extendsText = "";
            List<String> implementsTexts = new ArrayList<>();
            String nextItem = scanner.nextItem();
            if (nextItem.equals("extends")) {
                extendsText = scanner.nextItem();
                nextItem = scanner.nextItem();
            }
            if (nextItem.equals("implements")) {
                String i = scanner.nextItem();
                while (!i.isEmpty()) {
                    implementsTexts.add(i);
                    i = scanner.nextItem();
                }
            }
            handler.onClass(s.className,
                    access,
                    modifier,
                    type,
                    extendsText,
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
            //TODO System.out.println("SourceFile: "+sourceFile);//TODO
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

        // ignore the "minor/major version" line
        b.onMatch("  ((minor)|(major)) version: \\d+", (c, s) -> {
        });

        // ignore the "this_class" line
        b.onMatch("  this_class: .+", (c, s) -> {
        });

        // ignore the "super_class" line
        b.onMatch("  super_class: .+", (c, s) -> {
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
                    String returnTypeAndMethodName = trim(c.m().group(4));
                    SeparatedItemScanner scanner = SeparatedItemScanner.newSeparatedItemScanner(returnTypeAndMethodName);
                    String item1 = scanner.nextItem();
                    String item2 = scanner.nextItem();
                    if (item2.isEmpty()) {
                        // only one item (the method name) found
                        s.returnType = "";
                        s.methodName = trim(item1);
                    } else {
                        // return type and method name found;
                        s.returnType = trim(item1);
                        s.methodName = trim(item2);
                    }
                    s.parameters = trim(c.m().group(5));
                    handler.onMethod(s.className,
                            s.methodName,
                            trim(c.m().group(1)),
                            trim(c.m().group(2)),
                            s.returnType,
                            s.parameters,
                            trim(c.m().group(6)),
                            trim(c.m().group(3)));
                });

        // field 
        // (must come after "method" rule, as it also matches som method pattern)
        b.onMatch("  " + ACCESS_REGEX + "((?:static ))?([^ ]+ )?([^\\(]+);", (c, s) ->
                handler.onField(s.className,
                        trim(c.m().group(4)),
                        trim(c.m().group(1)),
                        trim(c.m().group(2)),
                        trim(c.m().group(3))));

        // InnerClasses line
        b.onMatch("InnerClasses:", (c, s) -> s.isInInnerClassesBlock = true);
        // Inner class line
        b.onMatch("\\s+((public )|(private ))?(static )?(final )?#\\d+(= #\\d+ of #\\d+)?;.+", (c, s) -> {
        });
//        // Inner class line (variante 2)
//        b.onMatch("     #\\d+; //.+", (c, s) -> {
//        });

        // RuntimeVisibleAnnotations line
        b.onMatch("RuntimeVisibleAnnotations:", (c, s) ->
                s.isInRuntimeVisibleAnnotationsBlock = true);
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

    public void parseReader(BufferedReader reader, EventHandler handler) {
        LineProcessing.Script script = newScript(handler);
        try {
            script.process(reader);
        } catch (JareentoException e) {
            throw e;
        } catch (Exception e) {
            throw new JareentoException("Error when reading javap output", e); //NON-NLS
        }
    }
}
