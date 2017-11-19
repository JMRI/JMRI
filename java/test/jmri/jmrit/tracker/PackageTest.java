package jmri.jmrit.tracker;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.blocktrack package
 *
 * @author Bob Jacobsen Copyright (C) 2006
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.tracker.PackageTest"); // no tests in class itself
        suite.addTest(StoppingBlockTest.suite());
        suite.addTest(MemoryTrackerTest.suite());
        return suite;
    }

}
