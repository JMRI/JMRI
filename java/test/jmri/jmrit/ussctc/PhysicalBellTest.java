package jmri.jmrit.ussctc;

import jmri.util.JUnitUtil;
import jmri.*;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for PhysicalBell class in the jmri.jmrit.ussctc package
 *
 * @author Bob Jacobsen Copyright 2007
 */
public class PhysicalBellTest {

    @Test
    public void testConstruction() {
        Assert.assertNotNull( new PhysicalBell("Bell output"));
    }
 
    @Test
    public void testBellStroke() {

        Turnout layoutTurnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1"); layoutTurnout.setUserName("Bell output");
        layoutTurnout.setCommandedState(Turnout.CLOSED);
 
        PhysicalBell bell = new PhysicalBell("Bell output");

        Assert.assertEquals(Turnout.CLOSED, layoutTurnout.getState());
        bell.ring();
        Assert.assertEquals(Turnout.THROWN, layoutTurnout.getState());
        JUnitUtil.waitFor(()->{return layoutTurnout.getState()==Turnout.CLOSED;}, "stroke didn't end");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        
   }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
