package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the Route interface
 *
 * @author Bob Jacobsen Copyright (C) 2006, 2007
 */
public class RouteTest {

    @Test
    public void testSetConstants() {
        assertNotEquals( Sensor.ACTIVE, Route.TOGGLE, "ACTIVE not TOGGLE");
        assertNotEquals( Sensor.INACTIVE, Route.TOGGLE, "INACTIVE not TOGGLE");
        assertNotEquals( Turnout.THROWN, Route.TOGGLE, "CLOSED not TOGGLE");
        assertNotEquals( Turnout.CLOSED, Route.TOGGLE, "THROWN not TOGGLE");
    }

    /**
     * The following equalities are needed so that old files can be read
     */
    @Test
    @SuppressWarnings("all") // to suppress "Comparing identical expressions"
    public void testRouteAndTurnoutConstants() {
        assertEquals( Turnout.CLOSED, Route.ONCLOSED, "CLOSED is ONCLOSED");
        assertEquals( Turnout.THROWN, Route.ONTHROWN, "THROWN is ONTHROWN");
    }

    @Test
    public void testSignalFireConstants() {
        int[] constants = new int[]{Route.ONACTIVE, Route.ONINACTIVE, Route.VETOACTIVE, Route.VETOINACTIVE,
            Route.ONCHANGE};

        String[] names = new String[]{"ONACTIVE", "ONINACTIVE", "VETOACTIVE", "VETOINACTIVE",
            "ONCHANGE"};

        // check consistency of test
        assertEquals( constants.length, names.length, "arrays must be same length");

        // check all constants different
        for (int i = 0; i < constants.length - 1; i++) {
            for (int j = i + 1; j < constants.length; j++) {
                assertNotEquals( constants[i], constants[j],
                    names[i] + " must be not equal " + names[j]);
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
        assertEquals( constants.length, names.length, "arrays must be same length");

        // check all constants different
        for (int i = 0; i < constants.length - 1; i++) {
            for (int j = i + 1; j < constants.length; j++) {
                assertNotEquals( constants[i], constants[j],
                    names[i] + " must be not equal " + names[j]);
            }
        }
    }

    @Test
    public void testEnable() {
        Route r = new DefaultRoute("test");
        // get default
        assertTrue( r.getEnabled(), "default enabled");
        // check change
        r.setEnabled(false);
        assertFalse( r.getEnabled(), "set enabled false");
        r.setEnabled(true);
        assertTrue( r.getEnabled(), "set enabled true");
    }

    @Test
    public void testIsVetoed() {
        DefaultRoute r = new DefaultRoute("test");
        // check disabled
        r.setEnabled(false);
        assertTrue( r.isVetoed(), "vetoed when disabled");
        // check enabled
        r.setEnabled(true);
        assertFalse( r.isVetoed(), "not vetoed when enabled");
    }

    @Test
    public void testTurnoutsAlignedSensor() {
        DefaultRoute r = new DefaultRoute("test");
        r.setTurnoutsAlignedSensor("IS123");
        assertEquals( "IS123", r.getTurnoutsAlignedSensor(), "Sensor name stored");
        r.activateRoute();
        
    }

    @Test
    public void testLockControlTurnout() {
        DefaultRoute r = new DefaultRoute("test");
        r.setLockControlTurnout("IT123");
        assertEquals( "IT123", r.getLockControlTurnout(), "Turnout name stored");
        r.activateRoute();
        
    }

    // There's a comment in DefaultRoute that says the following
    // are "constraints due to implementation", so let's test those here
    //
    @SuppressWarnings("all")
    @Test
    public void testImplementationConstraint() {
        // check a constraint required by this implementation!
        assertEquals( 0, Route.ONACTIVE, "ONACTIVE");
        assertEquals( 1, Route.ONINACTIVE, "ONINACTIVE");
        assertEquals( 2, Route.VETOACTIVE, "VETOACTIVE");
        assertEquals( 3, Route.VETOINACTIVE, "VETOINACTIVE");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
   
}
