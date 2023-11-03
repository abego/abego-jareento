package org.abego.jareento.javaanalysis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProblemTypeTest {
    static class MyProblemType implements ProblemType {
        @Override
        public String getID() {
            return "myId";
        }

        @Override
        public String getTitle() {
            return "myTitle";
        }

        @Override
        public String getDetails() {
            return "myDetails";
        }
    }

    @Test
    void smokeTest() {
        ProblemType pt = new MyProblemType();

        assertEquals("myId", pt.getID());
        assertEquals("myTitle", pt.getTitle());
        assertEquals("myDetails", pt.getDetails());
        assertEquals("myTitle", pt.getDescriptionTemplate());
    }
}
