package jmri.jmrit.beantable.beanedit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for classes in the jmri.jmrit.beantable.beanedit package
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
        suite.addTest(new junit.framework.JUnit4TestAdapter(BeanItemPanelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BlockEditActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SensorDebounceEditActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SensorEditActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TurnoutEditActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SensorPullUpEditActionTest.class));

        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
