package jmri.jmrit.beantable.oblock;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for classes in the jmri.jmrit.beantable.oblock package
 *
 * @author	Bob Jacobsen Copyright 2014
 */
public class PackageTest extends TestCase {

    public void testCreate() {
    }

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
        TestSuite suite = new TestSuite(PackageTest.class);

        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BlockPathTableModelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PathTurnoutTableModelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TableFramesTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(OBlockTableModelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BlockPortalTableModelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PortalTableModelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SignalTableModelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DnDJTableTest.class));
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
