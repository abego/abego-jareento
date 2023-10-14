package org.abego.jareento.javaanalysis.internal.input.jdeps;

import org.abego.commons.io.FileUtil;
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

}
