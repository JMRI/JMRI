package jmri.jmrit.ussctc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;


/**
 * Tests for OccupancyLock classes in the jmri.jmrit.ussctc package
 *
 * @author Bob Jacobsen Copyright 2007
 */
public class TurnoutLockTest {

    @Test
    public void testPass() throws JmriException {

        Turnout t = InstanceManager.getDefault(jmri.TurnoutManager.class).provideTurnout("IT1");

        TurnoutLock lock = new TurnoutLock("IT1", Turnout.CLOSED);

        t.setCommandedState(Turnout.CLOSED);

        assertTrue(lock.isLockClear(Lock.turnoutLockLogger));
    }

    @Test
    public void testFailOther() throws JmriException {

        Turnout t = InstanceManager.getDefault(jmri.TurnoutManager.class).provideTurnout("IT1");

        TurnoutLock lock = new TurnoutLock("IT1", Turnout.CLOSED);

        t.setCommandedState(Turnout.THROWN);

        assertFalse(lock.isLockClear(Lock.turnoutLockLogger));
    }

    @Test
    public void testFailInconsistent() throws JmriException {

        Turnout t = InstanceManager.getDefault(jmri.TurnoutManager.class).provideTurnout("IT1");

        TurnoutLock lock = new TurnoutLock("IT1", Turnout.INCONSISTENT);

        t.setCommandedState(Turnout.CLOSED);

        assertFalse(lock.isLockClear(Lock.turnoutLockLogger));
    }

    @Test
    public void testFailUnknown() throws JmriException {

        Turnout t = InstanceManager.getDefault(jmri.TurnoutManager.class).provideTurnout("IT1");

        TurnoutLock lock = new TurnoutLock("IT1", Turnout.UNKNOWN);

        t.setCommandedState(Turnout.CLOSED);

        assertFalse(lock.isLockClear(Lock.turnoutLockLogger));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
