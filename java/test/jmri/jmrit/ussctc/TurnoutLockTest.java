package jmri.jmrit.ussctc;

import org.junit.*;

import jmri.*;

import java.util.*;

/**
 * Tests for OccupancyLock classes in the jmri.jmrit.ussctc package
 *
 * @author	Bob Jacobsen Copyright 2007
  */
public class TurnoutLockTest {

    @Test
    public void testPass() throws JmriException {
        
        Turnout t = InstanceManager.getDefault(jmri.TurnoutManager.class).provideTurnout("IT1");
        
        TurnoutLock lock = new TurnoutLock("IT1", Turnout.CLOSED);
        
        t.setCommandedState(Turnout.CLOSED);
        
        Assert.assertTrue(lock.isLockClear());
    }

    @Test
    public void testFailOther() throws JmriException {
        
        Turnout t = InstanceManager.getDefault(jmri.TurnoutManager.class).provideTurnout("IT1");
        
        TurnoutLock lock = new TurnoutLock("IT1", Turnout.CLOSED);
        
        t.setCommandedState(Turnout.THROWN);
        
        Assert.assertTrue(! lock.isLockClear());
    }

    @Test
    public void testFailInconsistent() throws JmriException {
        
        Turnout t = InstanceManager.getDefault(jmri.TurnoutManager.class).provideTurnout("IT1");
        
        TurnoutLock lock = new TurnoutLock("IT1", Turnout.INCONSISTENT);
        
        t.setCommandedState(Turnout.CLOSED);
        
        Assert.assertTrue(! lock.isLockClear());
    }

    @Test
    public void testFailUnknown() throws JmriException {
        
        Turnout t = InstanceManager.getDefault(jmri.TurnoutManager.class).provideTurnout("IT1");
        
        TurnoutLock lock = new TurnoutLock("IT1", Turnout.UNKNOWN);
        
        t.setCommandedState(Turnout.CLOSED);
        
        Assert.assertTrue(! lock.isLockClear());
    }
       
    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initConfigureManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
