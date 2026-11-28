package jmri.implementation;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DefaultRouteTest {

    @Test
    public void testLockingTheRouteLocksItsTurnouts() {
        JUnitUtil.initInternalTurnoutManager();
        jmri.Turnout turnout = jmri.InstanceManager.getDefault(jmri.TurnoutManager.class).provideTurnout("IT1");
        turnout.setCommandedState(jmri.Turnout.CLOSED);
        DefaultRoute r = new DefaultRoute("IO1");
        Assertions.assertFalse( r.canLock(), "no turnouts, nothing to lock");
        r.addOutputTurnout("IT1", jmri.Turnout.CLOSED);
        Assertions.assertTrue( r.canLock(), "an internal turnout can be locked");

        r.setLocked(true);
        Assertions.assertTrue( turnout.isLocked(), "the route's turnout is locked");
        turnout.setCommandedState(jmri.Turnout.THROWN);
        Assertions.assertEquals( jmri.Turnout.CLOSED, turnout.getCommandedState(), "throw refused");
        jmri.util.JUnitAppender.assertWarnMessageStartsWith("Turnout IT1 not set");

        r.setLocked(false);
        Assertions.assertFalse( turnout.isLocked(), "unlocked with the route");
    }

    @Test
    public void testCTor() {
        DefaultRoute t = new DefaultRoute("Test Route");
        Assert.assertNotNull("exists",t);
    }

    /**
     * Internal output sensors are set without a 50 ms gap after each, so the
     * route's turnouts are not held back by them.
     */
    @Test
    public void testInternalOutputSensorsDoNotDelayTurnouts() {
        DefaultRoute r = new DefaultRoute("IR1", "route");
        for (int i = 1; i <= 20; i++) {
            jmri.InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS" + i);
            r.addOutputSensor("IS" + i, jmri.Sensor.ACTIVE);
        }
        jmri.Turnout t = jmri.InstanceManager.getDefault(jmri.TurnoutManager.class).provideTurnout("IT1");
        r.addOutputTurnout("IT1", jmri.Turnout.THROWN);
        r.activateRoute();
        long start = System.currentTimeMillis();
        r.setRoute();
        JUnitUtil.waitFor(() -> t.getCommandedState() == jmri.Turnout.THROWN, "turnout set");
        long took = System.currentTimeMillis() - start;
        Assert.assertTrue("turnout set after " + took + " ms, not after 20 x 50 ms", took < 700);
        Assert.assertEquals(jmri.Sensor.ACTIVE,
                jmri.InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS20").getKnownState());
        r.deActivateRoute();
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

    // private static final Logger log = LoggerFactory.getLogger(DefaultRouteTest.class);

}
