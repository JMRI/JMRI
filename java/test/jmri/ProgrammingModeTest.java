package jmri;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the ProgrammingMode class
 *
 * @author Bob Jacobsen Copyright (C) 2014
 */
public class ProgrammingModeTest extends TestCase {

    public void testStateCtors() {
        // tests that statics exist, are not equal
        Assert.assertTrue(ProgrammingMode.PAGEMODE.equals(ProgrammingMode.PAGEMODE));
        Assert.assertTrue(!ProgrammingMode.REGISTERMODE.equals(ProgrammingMode.PAGEMODE));
    }

    // from here down is testing infrastructure
    public ProgrammingModeTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ProgrammingModeTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ProgrammingModeTest.class);
        return suite;
    }

}
