package org.abego.jareento.util;

import org.abego.commons.test.JUnit5Util;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocationsInFileTest {
    @Test
    void smokeTest() {
        File file = new File("foo.txt");



        long id = LocationsInFile.getInstance().getId(file, 1234);
        assertLocationInFileEquals(file, 1234, id);

        long id2 = LocationsInFile.getInstance().getId(file, 0);
        assertLocationInFileEquals(file, 0, id2);

        long idMax = LocationsInFile.getInstance().getId(file, Integer.MAX_VALUE);
        assertLocationInFileEquals(file, Integer.MAX_VALUE, idMax);
    }

    private static void assertLocationInFileEquals(File file, int lineNumber, long id) {
        assertEquals(file.getAbsolutePath(),
                LocationsInFile.getInstance().getFile(id).getAbsolutePath());
        assertEquals(lineNumber, LocationsInFile.getInstance().getLineNumber(id));
    }

    @Test
    void wrongLineNumber() {
        File file = new File("foo.txt");

        JUnit5Util.assertThrowsWithMessage(IllegalArgumentException.class,
                "lineNumber must not be negative. Got: -1",
                () -> LocationsInFile.getInstance().getId(file, -1));
    }
}
