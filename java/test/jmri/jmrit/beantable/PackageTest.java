package jmri.jmrit.beantable;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for classes in the jmri.jmrit.beantable package
 *
 * @author	Bob Jacobsen Copyright 2004
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
        suite.addTest(new JUnit4TestAdapter(BlockTableActionTest.class));
        suite.addTest(LogixTableActionTest.suite());
        suite.addTest(LRouteTableActionTest.suite());
        suite.addTest(OBlockTableActionTest.suite());
        suite.addTest(new JUnit4TestAdapter(RouteTableActionTest.class));
        suite.addTest(SensorTableWindowTest.suite());
        suite.addTest(new JUnit4TestAdapter(SignalGroupTableActionTest.class));
        suite.addTest(new JUnit4TestAdapter(SignalHeadTableActionTest.class));
        suite.addTest(TurnoutTableWindowTest.suite());
        suite.addTest(new JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.beantable.signalmast.PackageTest.class));
        suite.addTest(jmri.jmrit.beantable.sensor.PackageTest.suite());
        suite.addTest(jmri.jmrit.beantable.oblock.PackageTest.suite());
        suite.addTest(jmri.jmrit.beantable.beanedit.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.beantable.usermessagepreferences.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(MemoryTableActionTest.class));
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @Override
    protected void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
