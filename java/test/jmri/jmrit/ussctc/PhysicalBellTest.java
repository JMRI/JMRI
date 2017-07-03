package jmri.jmrit.ussctc;

import org.junit.*;

import jmri.*;
import jmri.util.*;

/**
 * Tests for PhysicalBell class in the jmri.jmrit.ussctc package
 *
 * @author	Bob Jacobsen Copyright 2007
  */
public class PhysicalBellTest {

    @Test
    public void testConstruction() {
        new PhysicalBell("Bell output");
    }
 
    @Test 
    public void testBellStroke() {
        layoutTurnout.setCommandedState(Turnout.CLOSED);
 
        PhysicalBell bell = new PhysicalBell("Bell output");

        Assert.assertEquals(Turnout.CLOSED, layoutTurnout.getState());
        bell.ring();
        Assert.assertEquals(Turnout.THROWN, layoutTurnout.getState());
        jmri.util.JUnitUtil.waitFor(()->{return layoutTurnout.getState()==Turnout.CLOSED;}, "stroke didn't end");
    }
    
    Turnout layoutTurnout;
    
    // The minimal setup for log4J
    @org.junit.Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initShutDownManager();
        JUnitUtil.resetProfileManager();
        
        layoutTurnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1"); layoutTurnout.setUserName("Bell output");
    }

    @org.junit.After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
