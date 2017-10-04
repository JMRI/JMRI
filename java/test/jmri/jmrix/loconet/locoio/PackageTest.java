package jmri.jmrix.loconet.locoio;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.loconet.locoio package
 *
 * @author	Bob Jacobsen Copyright (C) 2002, 2010
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
        TestSuite suite = new TestSuite("jmri.jmrix.loconet.locoio.LocoIOTest");  // no tests in this class itself

        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LocoIOPanelTest.class));
        suite.addTest(LocoIOTableModelTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(LocoIOModeListTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LocoIOTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LocoIODataTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LocoIOModeTest.class));
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }
}
