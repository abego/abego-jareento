package org.abego.jareento.javaanalysis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProblemTypeTest {
    public static class ProblemTypeSample implements ProblemType {
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

    public static class ProblemTypeSample2 implements ProblemType {
        @Override
        public String getID() {
            return "pt2ID";
        }

        @Override
        public String getTitle() {
            return "pt2Title";
        }

        @Override
        public String getDetails() {
            return "pt2Details";
        }

        @Override
        public String getDescriptionTemplate() {
            return "pt2Description-{foo}";
        }
    }

    @Test
    void smokeTest() {
        ProblemType pt = new ProblemTypeSample();

        assertEquals("myId", pt.getID());
        assertEquals("myTitle", pt.getTitle());
        assertEquals("myDetails", pt.getDetails());
        assertEquals("myTitle", pt.getDescriptionTemplate());
    }
}
