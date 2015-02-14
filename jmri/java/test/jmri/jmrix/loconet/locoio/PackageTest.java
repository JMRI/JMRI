// PackageTest.java

package jmri.jmrix.loconet.locoio;

import junit.framework.*;

/**
 * Tests for the jmri.jmrix.loconet.locoio package
 * @author	Bob Jacobsen Copyright (C) 2002, 2010
 * @version     $Revision$
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure

    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.loconet.locoio.LocoIOTest");  // no tests in this class itself
        suite.addTest(LocoIOPanelTest.suite());
        suite.addTest(LocoIOTableModelTest.suite());
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
