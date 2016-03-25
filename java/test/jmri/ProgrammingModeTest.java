// ProgrammingModeTest.java
package jmri;

import jmri.managers.DefaultProgrammerManager;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the ProgrammingMode class
 *
 * @author Bob Jacobsen Copyright (C) 2014
 */
public class ProgrammingModeTest extends TestCase {

    public void testStateCtors() {
        // tests that statics exist, are not equal
        Assert.assertTrue(DefaultProgrammerManager.PAGEMODE.equals(DefaultProgrammerManager.PAGEMODE));
        Assert.assertTrue(!DefaultProgrammerManager.REGISTERMODE.equals(DefaultProgrammerManager.PAGEMODE));
    }

    // from here down is testing infrastructure
    public ProgrammingModeTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ProgrammingModeTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ProgrammingModeTest.class);
        return suite;
    }

}
