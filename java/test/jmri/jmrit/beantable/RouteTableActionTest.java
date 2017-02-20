package jmri.jmrit.beantable;

import javax.swing.Action;
import javax.swing.JFrame;
import jmri.Route;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Tests for the jmri.jmrit.beantable.RouteTableAction class
 *
 * @author	Bob Jacobsen Copyright 2004, 2007
 */
public class RouteTableActionTest {

    @Test
    public void testCreate() {
        Action a = new RouteTableAction();
        Assert.assertNotNull(a);
    }

    public void testInvoke() {
        new RouteTableAction().actionPerformed(null);
        JFrame f = JFrameOperator.waitJFrame(Bundle.getMessage("TitleRouteTable"), true, true);
        Assert.assertNotNull("found frame", f);
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

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
