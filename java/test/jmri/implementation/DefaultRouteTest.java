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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(DefaultRouteTest.class);

}
