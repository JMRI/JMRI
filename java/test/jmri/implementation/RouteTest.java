package jmri.implementation;

import jmri.*;
import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Tests for the Route interface
 *
 * @author	Bob Jacobsen Copyright (C) 2006, 2007
 */
public class RouteTest {

    @Test
    public void testSetConstants() {
        Assert.assertTrue("ACTIVE not TOGGLE", Sensor.ACTIVE != Route.TOGGLE);
        Assert.assertTrue("INACTIVE not TOGGLE", Sensor.INACTIVE != Route.TOGGLE);
        Assert.assertTrue("CLOSED not TOGGLE", Turnout.THROWN != Route.TOGGLE);
        Assert.assertTrue("THROWN not TOGGLE", Turnout.CLOSED != Route.TOGGLE);
    }

    /**
     * The following equalities are needed so that old files can be read
     */
    @Test
    @SuppressWarnings("all") // to suppress "Comparing identical expressions"
    public void testRouteAndTurnoutConstants() {
        Assert.assertTrue("CLOSED is ONCLOSED", Turnout.CLOSED == Route.ONCLOSED);
        Assert.assertTrue("THROWN is ONTHROWN", Turnout.THROWN == Route.ONTHROWN);
    }

    @Test
    public void testSignalFireConstants() {
        int[] constants = new int[]{Route.ONACTIVE, Route.ONINACTIVE, Route.VETOACTIVE, Route.VETOINACTIVE,
            Route.ONCHANGE};

        String[] names = new String[]{"ONACTIVE", "ONINACTIVE", "VETOACTIVE", "VETOINACTIVE",
            "ONCHANGE"};

        // check consistency of test
        Assert.assertTrue("arrays must be same length", constants.length == names.length);

        // check all constants different
        for (int i = 0; i < constants.length - 1; i++) {
            for (int j = i + 1; j < constants.length; j++) {
                Assert.assertTrue(names[i] + " must be not equal " + names[j],
                        constants[i] != constants[j]);
            }
        }
    }

    @Test
    public void testTurnoutFireConstants() {
        int[] constants = new int[]{Route.ONCHANGE,
            Route.ONCLOSED, Route.ONTHROWN, Route.VETOCLOSED, Route.VETOTHROWN};

        String[] names = new String[]{"ONCHANGE",
            "ONCLOSED", "ONTHROWN", "VETOCLOSED", "VETOTHROWN"};

        // check consistency of test
        Assert.assertTrue("arrays must be same length", constants.length == names.length);

        // check all constants different
        for (int i = 0; i < constants.length - 1; i++) {
            for (int j = i + 1; j < constants.length; j++) {
                Assert.assertTrue(names[i] + " must be not equal " + names[j],
                        constants[i] != constants[j]);
            }
        }
    }

    @Test
    public void testEnable() {
        Route r = new DefaultRoute("test");
        // get default
        Assert.assertTrue("default enabled", r.getEnabled());
        // check change
        r.setEnabled(false);
        Assert.assertTrue("set enabled false", !r.getEnabled());
        r.setEnabled(true);
        Assert.assertTrue("set enabled true", r.getEnabled());
    }

    @Test
    public void testIsVetoed() {
        DefaultRoute r = new DefaultRoute("test");
        // check disabled
        r.setEnabled(false);
        Assert.assertTrue("vetoed when disabled", r.isVetoed());
        // check enabled
        r.setEnabled(true);
        Assert.assertTrue("not vetoed when enabled", !r.isVetoed());
    }

    @Test
    public void testTurnoutsAlignedSensor() {
        DefaultRoute r = new DefaultRoute("test");
        r.setTurnoutsAlignedSensor("IS123");
        Assert.assertEquals("Sensor name stored", "IS123", r.getTurnoutsAlignedSensor());     
        r.activateRoute();
        
    }

    @Test
    public void testLockControlTurnout() {
        DefaultRoute r = new DefaultRoute("test");
        r.setLockControlTurnout("IT123");
        Assert.assertEquals("Turnout name stored", "IT123", r.getLockControlTurnout());     
        r.activateRoute();
        
    }

    // There's a comment in DefaultRoute that says the following
    // are "constraints due to implementation", so let's test those here
    //
    @SuppressWarnings("all")
    @Test
    public void testImplementationConstraint() {
        // check a constraint required by this implementation!
        Assert.assertTrue("ONACTIVE", Route.ONACTIVE == 0);
        Assert.assertTrue("ONINACTIVE", Route.ONINACTIVE == 1);
        Assert.assertTrue("VETOACTIVE", Route.VETOACTIVE == 2);
        Assert.assertTrue("VETOINACTIVE", Route.VETOINACTIVE == 3);
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
   
}
