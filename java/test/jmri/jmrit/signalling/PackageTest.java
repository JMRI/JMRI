package jmri.jmrit.signalling;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invokes complete set of tests in the jmri.jmrit.signalling tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.signalling.PackageTest");   // no tests in this class itself

        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.signalling.entryexit.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.signalling.configurexml.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AddEntryExitPairFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(EntryExitPairsTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SignallingActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SignallingFrameActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SignallingFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SignallingSourceActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SignallingSourceFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SignallingGuiToolsTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AddEntryExitPairActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AddEntryExitPairPanelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SignallingSourcePanelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SignallingPanelTest.class));
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
