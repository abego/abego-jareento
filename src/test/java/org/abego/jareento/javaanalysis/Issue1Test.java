package org.abego.jareento.javaanalysis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * See issue <a href="https://github.com/abego/abego-jareento/issues/1">
 *     Exception when calling method of non-first extended Interface</a>.
 */
public class Issue1Test {

    @Test
    void smoketest(@TempDir File tempDir) {
        JavaAnalysisProject project = 
                SampleProjectUtil.setupSampleProject("issue1", tempDir);

        StringBuilder info = infoOfMethodCallsInMethod(
                project, "issue1.InterfaceC#methodInterfaceC():void");

        assertEquals("""
                # info of methodCalls in method issue1.InterfaceC#methodInterfaceC():void
                invokeinterface-issue1.InterfaceC#methodInterfaceC():void@1
                - signature: methodInterfaceB()
                - scope: issue1.InterfaceB
                """, info.toString());
    }

    @SuppressWarnings("SameParameterValue")
    private static StringBuilder infoOfMethodCallsInMethod(JavaAnalysisProject project, String methodId) {
        StringBuilder output = new StringBuilder();
        output
                .append("# info of methodCalls in method ")
                .append(methodId).append('\n');
        for (var c : project.methodCallsInMethod(methodId)) {
            output.append(c.id()).append('\n');
            output.append("- signature: ")
                    .append(project.signatureOfMethodCall(c.id())).append('\n');
            output.append("- scope: ")
                    .append(project.scopeOfMethodCall(c.id())).append('\n');
        }
        return output;
    }

}
