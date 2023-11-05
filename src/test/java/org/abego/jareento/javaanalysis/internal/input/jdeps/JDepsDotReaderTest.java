package org.abego.jareento.javaanalysis.internal.input.jdeps;

import org.abego.commons.io.FileUtil;
import org.abego.commons.test.JUnit5Util;
import org.abego.jareento.base.JareentoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.abego.jareento.javaanalysis.internal.input.jdeps.JDepsDotReader.newJDepsDotReader;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JDepsDotReaderTest {

    @Test
    void readFile(@TempDir File tempDir) {
        File dotFile = new File(tempDir, "sample.dot");
        FileUtil.copyResourceToFile(getClass(), "jdeps-sample.dot", dotFile);

        JDepsDotReaderEventsLogger logger =
                new JDepsDotReaderEventsLogger();

        newJDepsDotReader().readFile(dotFile, logger);

        assertEquals("""
                        1\tStart digraph abego-commons-base-0.12.0-SNAPSHOT.jar
                        2\t//  Path: target/abego-commons-base-0.12.0-SNAPSHOT.jar
                        3\torg.abego.commons.annotation.SPI -> java.lang.Object
                        3\t//  "normal" item
                        4\torg.abego.commons.annotation.package-info -> java.lang.Object
                        4\t//  item with "-" in class name
                        5\torg.abego.commons.blackboard.Blackboard -> org.abego.commons.annotation.SPI
                        5\t//  reference to an existing node
                        6\torg.abego.commons.blackboard.Blackboard -> org.abego.commons.seq.Seq (abego-commons-base-0.12.0-SNAPSHOT.jar)
                        6\t//  item with non-JRE reference
                        7\torg.abego.commons.blackboard.BlackboardDefault -> java.lang.invoke.MethodHandles$Lookup
                        7\t//  item with "to" class name containing "$"
                        8\torg.abego.commons.diff.FileDiffUtil$DirectoryDifferencesOptions -> java.lang.Class
                        8\t//  item with "from" class name containing "$"
                        9\torg.abego.commons.diff.internal.DiffImpl$1 -> org.abego.commons.seq.AbstractSeq (abego-commons-base-0.12.0-SNAPSHOT.jar)
                        9\t//  item with "from" class name containing "$1"\s
                        10\torg.abego.commons.diff.internal.DiffImpl$1$1 -> java.util.NoSuchElementException
                        10\t//  item with "from" class name containing "$1$1"
                        11\t}
                        """,
                logger.getText());
    }

    @Test
    void readFile$invalidHeader(@TempDir File tempDir) {
        File dotFile = new File(tempDir, "sample.dot");
        FileUtil.writeText(dotFile, "foo");

        JDepsDotReaderEventsLogger logger =
                new JDepsDotReaderEventsLogger();
        JUnit5Util.assertThrowsWithMessage(
                JareentoException.class,
                "Error when reading jdeps dot output from file '%s'"
                        .formatted(dotFile.getAbsolutePath()),
                () -> newJDepsDotReader().readFile(dotFile, logger));
    }

    @Test
    void readFile$invalidHeaderButContinue(@TempDir File tempDir) {
        File dotFile = new File(tempDir, "sample.dot");
        FileUtil.writeText(dotFile, "foo\nbar\n");

        StringBuilder log = new StringBuilder();
        JDepsDotReader.EventHandler logger = new JDepsDotReader.EventHandler() {
            @Override
            public void onError(JareentoException exception,
                                @SuppressWarnings("unused") int lineNumber) {
                log.append(lineNumber);
                log.append(": ");
                log.append(exception.getMessage());
                log.append("\n");
            }
        };
        newJDepsDotReader().readFile(dotFile, logger);

        assertEquals("""
                        1: Unexpected header 'foo'
                        2: Unexpected line 'bar'
                        3: Reached end of file but missing '}'
                        """,
                log.toString());
    }

    @Test
    void readFile$onComments(@TempDir File tempDir) {
        File dotFile = new File(tempDir, "sample.dot");
        FileUtil.copyResourceToFile(getClass(), "jdeps-sample.dot", dotFile);

        StringBuilder log = new StringBuilder();
        JDepsDotReader.EventHandler logger = new JDepsDotReader.EventHandler() {
            @Override
            public void onComment(String commentText, int lineNumber) {
                log.append(lineNumber);
                log.append(": ");
                log.append(commentText);
                log.append("\n");
            }
        };

        newJDepsDotReader().readFile(dotFile, logger);

        assertEquals("""
                        2:  Path: target/abego-commons-base-0.12.0-SNAPSHOT.jar
                        3:  "normal" item
                        4:  item with "-" in class name
                        5:  reference to an existing node
                        6:  item with non-JRE reference
                        7:  item with "to" class name containing "$"
                        8:  item with "from" class name containing "$"
                        9:  item with "from" class name containing "$1"\s
                        10:  item with "from" class name containing "$1$1"
                        """,
                log.toString());
    }
}
