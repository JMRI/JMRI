package jmri;

import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Turnout class.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 */
public class TurnoutTest {

    @SuppressWarnings("all")
    @Test
    public void testStateConstants() {
        Assert.assertTrue("Thrown and Closed differ", (Turnout.THROWN & Turnout.CLOSED) == 0);
        Assert.assertTrue("Thrown and Unknown differ", (Turnout.THROWN & Turnout.UNKNOWN) == 0);
        Assert.assertTrue("Off and Unknown differ", (Turnout.CLOSED & Turnout.UNKNOWN) == 0);
        Assert.assertTrue("Thrown and Inconsistent differ", (Turnout.THROWN & Turnout.INCONSISTENT) == 0);
        Assert.assertTrue("Closed and Inconsistent differ", (Turnout.CLOSED & Turnout.INCONSISTENT) == 0);
        Assert.assertTrue("Unknown and Inconsistent differ", (Turnout.UNKNOWN & Turnout.INCONSISTENT) == 0);
    }

    @Test 
    public void testInvertTurnoutState() {
        Assert.assertEquals("Closed state Inverted",Turnout.THROWN,Turnout.invertTurnoutState(Turnout.CLOSED));
        Assert.assertEquals("Thrown state Inverted",Turnout.CLOSED,Turnout.invertTurnoutState(Turnout.THROWN));
        Assert.assertEquals("Inconsistent state Inverted",Turnout.INCONSISTENT,Turnout.invertTurnoutState(Turnout.INCONSISTENT));
        Assert.assertEquals("Unknown state Inverted",Turnout.UNKNOWN,Turnout.invertTurnoutState(Turnout.UNKNOWN));
    }

}
