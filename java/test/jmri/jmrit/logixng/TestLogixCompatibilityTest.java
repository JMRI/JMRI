package jmri.jmrit.logixng;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test that LogixNG has at least the same features as Logix
 * 
 * @author Daniel Bergqvist 2019
 */
public class TestLogixCompatibilityTest {

    // Logix can read a sensor
    // Logix can read a turnout
    // Logix can read a light
    // Logix can read a signal head
    // Logix can read a signal mast
    // Logix can read a memory
    // Logix can read a conditional
    // Logix can read a logix
    // Logix can read a warrant
    // Logix can read a clock
    // Logix can read a oblock
    // Logix can read a entry/exit
    
    // Logix can have an AND condition
    // Logix can have an OR condition
    // Logix can have an Mixed condition
    
    // Logix can execute actions on every change of state
    // Logix can execute actions whenether triggered
    
    // Logix can execute action on change to true
    // Logix can execute action on change to false
    // Logix can execute action on change
    
    // Logix can execute action sensor - set sensor, delayed set sensor, reset delayed set sensor, cancel timers for sensor
    // Logix can execute action turnout - set turnout, delayed set turnout, turnout lock, cancel timers for turnout, reset delayed set turnout
    // Logix can execute action light - set light , set light intensity, set light transition time
    // Logix can execute action signal head - set signal head appearance, set signal head held, clear signal head held, set signal head dark, set signal head lit
    // Logix can execute action signal mast - set signal mast aspect, set signal mast held, clear signal mast held, set signal mast dark, clear signal mast dark
    // Logix can execute action memory - set memory, copy memory to memory
    // Logix can execute action conditional
    // Logix can execute action logix - enable logix, disable logix
    // Logix can execute action warrant - allocate warrant route, deallocate warrant, set route turnouts, auto run train, manually run train, control auto train, set train ID, set train name, set throttle factor
    // Logix can execute action clock - set fast clock time, start fast clock, stop fast clock
    // Logix can execute action oblock - deallocate block, set block value, set block error, clear block error, set block OutOfService, clear block OutOfService
    // Logix can execute action entry/exit - set NX pair enabled, set NX pair disabled, set NX pair segment active/inactive
    // Logix can execute action audio - play sound file, control audio object
    // Logix can execute action audio - control audio object
    // Logix can execute action script - run script, execute jython command
    // Logix can execute action other - trigger route
/*    
    @Test
    public void testEnum() {
        Assert.assertTrue("TRUE".equals(DigitalExpressionBean.TriggerCondition.TRUE.name()));
        Assert.assertTrue("FALSE".equals(DigitalExpressionBean.TriggerCondition.FALSE.name()));
        Assert.assertTrue("CHANGE".equals(DigitalExpressionBean.TriggerCondition.CHANGE.name()));
    }
*/    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
