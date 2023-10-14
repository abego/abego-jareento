package org.abego.jareento.javaanalysis.internal.input.jdeps;

import org.abego.jareento.javaanalysis.internal.JavaAnalysisProjectInput;
import org.abego.jareento.javaanalysis.internal.JavaAnalysisProjectStateBuilder;
import org.abego.jareento.shared.SyntaxUtil;

import java.io.File;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.abego.jareento.javaanalysis.internal.input.jdeps.JDepsDotReader.newJDepsDotReader;

public class InputFromJDeps implements JavaAnalysisProjectInput {

    private final File dotFile;

    private InputFromJDeps(File dotFile) {
        this.dotFile = dotFile;
    }

    public static InputFromJDeps newInputFromJDeps(File dotFile) {
        return new InputFromJDeps(dotFile);
    }

    static class ParsedNodeInfo {
        // example: org.abego.commons.seq.Seq (abego-commons-base-0.12.0-SNAPSHOT.jar)
        // with only the initial qname being required
        // TODO: can we rewrite the pattern to already separate the package path from the simpleName
        private static final Pattern nodePattern = Pattern.compile(
                "([\\w.$-]+)(?: \\(([^)]+)\\))?");
        private final String name;
        private final String simpleName;
        private final String packagePath;
        private final boolean isPackageInfo;

        private ParsedNodeInfo(String text) {
            Matcher m = nodePattern.matcher(text);
            if (!m.matches()) {
                throw new IllegalArgumentException(String.format("Invalid node text: %s", text));
            }
            name = m.group(1);
            String[] packagePathAndSimpleName = SyntaxUtil.qualifierAndSimpleName(name);
            packagePath = packagePathAndSimpleName[0];
            simpleName = packagePathAndSimpleName[1];
            isPackageInfo = simpleName.equals("package-info");
        }

        public static ParsedNodeInfo parseNode(String text) {
            return new ParsedNodeInfo(text);
        }

        public String getName() {
            return name;
        }

        public String getSimpleName() {
            return simpleName;
        }

        public String getPackagePath() {
            return packagePath;
        }

        public boolean isPackageInfo() {
            return isPackageInfo;
        }
    }

    private void readGraphFromJDepsDotOutput(File file, JavaAnalysisProjectStateBuilder builder) {
        newJDepsDotReader().readFile(file, new JDepsDotReader.EventHandler() {
            @Override
            public void onEdge(String from, String to, int lineNumber) {
                ParsedNodeInfo fromInfo = ParsedNodeInfo.parseNode(from);
                ParsedNodeInfo toInfo = ParsedNodeInfo.parseNode(to);

                addNodeInfos(fromInfo, builder);
                addNodeInfos(toInfo, builder);

                builder.addReference(fromInfo.getName(), toInfo.getName());

                if (!fromInfo.getPackagePath()
                        .equals(toInfo.getPackagePath())) {
                    builder.addReference(fromInfo.getPackagePath(), toInfo.getPackagePath());
                }
            }
        });
    }

    private void addNodeInfos(ParsedNodeInfo fromInfo, JavaAnalysisProjectStateBuilder builder) {
        if (!fromInfo.isPackageInfo) {
            String name = fromInfo.getName();
            builder.addClass(name);
        }
    }

    public void feed(JavaAnalysisProjectStateBuilder builder, Consumer<String> problemConsumer) {
        readGraphFromJDepsDotOutput(dotFile, builder);
    }
}
