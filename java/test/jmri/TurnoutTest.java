package jmri;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the Turnout class
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 */
public class TurnoutTest extends TestCase {

    @SuppressWarnings("all")
    public void testStateConstants() {
        Assert.assertTrue("Thrown and Closed differ", (Turnout.THROWN & Turnout.CLOSED) == 0);
        Assert.assertTrue("Thrown and Unknown differ", (Turnout.THROWN & Turnout.UNKNOWN) == 0);
        Assert.assertTrue("Off and Unknown differ", (Turnout.CLOSED & Turnout.UNKNOWN) == 0);
        Assert.assertTrue("Thrown and Inconsistent differ", (Turnout.THROWN & Turnout.INCONSISTENT) == 0);
        Assert.assertTrue("Closed and Inconsistent differ", (Turnout.CLOSED & Turnout.INCONSISTENT) == 0);
    }

    // from here down is testing infrastructure
    public TurnoutTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {TurnoutTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(TurnoutTest.class);
        return suite;
    }

}
