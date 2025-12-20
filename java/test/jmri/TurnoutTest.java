package jmri;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the Turnout class.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class TurnoutTest {

    @SuppressWarnings("all")
    @Test
    public void testStateConstants() {
        assertTrue( (Turnout.THROWN & Turnout.CLOSED) == 0, "Thrown and Closed differ");
        assertTrue( (Turnout.THROWN & Turnout.UNKNOWN) == 0, "Thrown and Unknown differ");
        assertTrue( (Turnout.CLOSED & Turnout.UNKNOWN) == 0, "Off and Unknown differ");
        assertTrue( (Turnout.THROWN & Turnout.INCONSISTENT) == 0, "Thrown and Inconsistent differ");
        assertTrue( (Turnout.CLOSED & Turnout.INCONSISTENT) == 0, "Closed and Inconsistent differ");
        assertTrue( (Turnout.UNKNOWN & Turnout.INCONSISTENT) == 0, "Unknown and Inconsistent differ");
    }

    @Test 
    public void testInvertTurnoutState() {
        assertEquals( Turnout.THROWN, Turnout.invertTurnoutState(Turnout.CLOSED), "Closed state Inverted");
        assertEquals( Turnout.CLOSED, Turnout.invertTurnoutState(Turnout.THROWN), "Thrown state Inverted");
        assertEquals( Turnout.INCONSISTENT, Turnout.invertTurnoutState(Turnout.INCONSISTENT), "Inconsistent state Inverted");
        assertEquals( Turnout.UNKNOWN, Turnout.invertTurnoutState(Turnout.UNKNOWN), "Unknown state Inverted");
    }

}
