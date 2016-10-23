package jmri.jmrit.beantable;

import javax.swing.JFrame;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for classes in the jmri.jmrit.beantable package
 *
 * @author	Bob Jacobsen Copyright 2004
  */
public class PackageTest extends TestCase {

    public void testCreate() {
        new MemoryTableAction();
    }

    public void testExecute() {
        new MemoryTableAction().actionPerformed(null);
//    }
//  test order isn't guaranteed!
//    public void testXCreation() {
        JFrame f = jmri.util.JmriJFrame.getFrame(Bundle.getMessage("TitleMemoryTable"));
        Assert.assertTrue("found frame", f != null);
        f.dispose();
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
        suite.addTest(BlockTableActionTest.suite());
        suite.addTest(LogixTableActionTest.suite());
        suite.addTest(LRouteTableActionTest.suite());
        suite.addTest(OBlockTableActionTest.suite());
        suite.addTest(RouteTableActionTest.suite());
        suite.addTest(SensorTableWindowTest.suite());
        suite.addTest(SignalHeadTableActionTest.suite());
        suite.addTest(TurnoutTableWindowTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));

        suite.addTest(jmri.jmrit.beantable.signalmast.PackageTest.suite());
        suite.addTest(jmri.jmrit.beantable.sensor.PackageTest.suite());
        suite.addTest(jmri.jmrit.beantable.oblock.PackageTest.suite());
        suite.addTest(jmri.jmrit.beantable.beanedit.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.beantable.usermessagepreferences.PackageTest.class));
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    protected void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
