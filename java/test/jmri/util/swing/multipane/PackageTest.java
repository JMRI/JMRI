package jmri.util.swing.multipane;

import jmri.util.JUnitUtil;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invokes complete set of tests in the jmri.util tree
 *
 * @author	Bob Jacobsen Copyright 2003
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
        TestSuite suite = new TestSuite(PackageTest.class.getName());

        suite.addTest(MultiJfcUnitTest.suite());
        suite.addTest(new JUnit4TestAdapter(MultiPaneWindowTest.class));
        suite.addTest(new JUnit4TestAdapter(PanedInterfaceTest.class));
        suite.addTest(new JUnit4TestAdapter(ThreePaneTLRWindowTest.class));

        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JUnitUtil.setUp();

    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
