package org.abego.jareento.javaanalysis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProblemTypeTest {
    public static class ProblemTypeSample implements ProblemType {
        @Override
        public String getID() {
            return "ProblemTypeSample";
        }

        @Override
        public String getTitle() {
            return "ProblemType introduced for tests";
        }

        @Override
        public String getDetails() {
            return "Details of ProblemTypeSample";
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

        assertEquals("ProblemTypeSample", pt.getID());
        assertEquals("ProblemType introduced for tests", pt.getTitle());
        assertEquals("Details of ProblemTypeSample", pt.getDetails());
        assertEquals("ProblemType introduced for tests", pt.getDescriptionTemplate());
    }
}
