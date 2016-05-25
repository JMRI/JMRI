package jmri.jmrit.tracker;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.blocktrack package
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 */
public class TrackerTest extends TestCase {

    // from here down is testing infrastructure
    public TrackerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {TrackerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.tracker.TrackerTest"); // no tests in class itself
        suite.addTest(StoppingBlockTest.suite());
        suite.addTest(MemoryTrackerTest.suite());
        return suite;
    }

}
