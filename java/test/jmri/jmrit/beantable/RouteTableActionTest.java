package jmri.jmrit.beantable;

import javax.swing.JFrame;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.Route;

/**
 * Tests for the jmri.jmrit.beantable.RouteTableAction class
 *
 * @author	Bob Jacobsen Copyright 2004, 2007
 */
public class RouteTableActionTest extends TestCase {

    public void testCreate() {
        new RouteTableAction();
//    }
//  test order isn't guaranteed!
//    public void testInvoke() {
        new RouteTableAction().actionPerformed(null);
//    }
//  test order isn't guaranteed!
//    public void testX() {
        JFrame f = jmri.util.JmriJFrame.getFrame(Bundle.getMessage("TitleRouteTable"));
        Assert.assertTrue("found frame", f != null);
        f.dispose();
    }
    
    public void testConstants() {
        // check constraints required by implementation,
        // because we assume that the codes are the same as the index
        // in a JComboBox
        Assert.assertEquals("Route.ONACTIVE", 0, Route.ONACTIVE);
        Assert.assertEquals("Route.ONINACTIVE", 1, Route.ONINACTIVE);
        Assert.assertEquals("Route.VETOACTIVE", 2, Route.VETOACTIVE);
        Assert.assertEquals("Route.VETOINACTIVE", 3, Route.VETOINACTIVE);
    }
    

    // from here down is testing infrastructure
    public RouteTableActionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", RouteTableActionTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(RouteTableActionTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
