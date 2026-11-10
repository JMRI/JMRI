package jmri.jmrit.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link Message}.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class MessageTest {

    @Test
    public void testTextAndEnglish() {
        Message m = new Message("PointsAgainst", "TO1");
        assertEquals("Turnout \"TO1\" is set against the train.", m.text());
        assertEquals("Turnout \"TO1\" is set against the train.", m.english());
        assertEquals(m.english(), m.toString());
    }

    @Test
    public void testEquals() {
        assertEquals(new Message("PointsAgainst", "TO1"), new Message("PointsAgainst", "TO1"));
        assertEquals(new Message("PointsAgainst", "TO1").hashCode(), new Message("PointsAgainst", "TO1").hashCode());
        assertNotEquals(new Message("PointsAgainst", "TO1"), new Message("PointsAgainst", "TO2"));
        assertNotEquals(new Message("PointsAgainst", "TO1"), new Message("TurnoutNotSet", "TO1"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
