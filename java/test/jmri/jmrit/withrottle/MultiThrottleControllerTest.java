package jmri.jmrit.withrottle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyChangeEvent;

import jmri.DccThrottle;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.Throttle;
import jmri.ThrottleManager;
import jmri.ThrottleListener.DecisionType;
import jmri.jmrix.debugthrottle.DebugThrottle;
import jmri.jmrit.throttle.ThrottlesPreferences;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of MultiThrottleController
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class MultiThrottleControllerTest {

    private ControllerInterfaceScaffold cis = null;
    private ThrottleControllerListenerScaffold tcls = null;
    private MultiThrottleController controller = null;

    @Test
    public void testCtor() {
        assertNotNull( controller, "exists");
    }

    @Test
    public void testSetShortAddress() {
        // tests setting the address from the input.
        // Does not include the prefix.
        assertTrue( controller.sort("S1"), "Continue after address");
        assertTrue( tcls.hasAddressBeenFound(), "Address Found");
    }

    @Test
    public void testSetLongAddress() {
        // tests setting the address from the input.
        // Does not include the prefix.
        assertTrue( controller.sort("L1234"), "Continue after address");
        assertTrue( tcls.hasAddressBeenFound(), "Address Found");
    }

    @Test
    public void testSetAndReleaseLongAddress() {
        // set the address
        assertTrue( controller.sort("L1234"), "Continue after address");
        assertTrue( tcls.hasAddressBeenFound(), "Address Found");
        // then release it.
        assertTrue( controller.sort("r"), "Continue after release");
        assertTrue( tcls.hasAddressBeenReleased(), "Address Released");
    }

    @Test
    public void testSetAndDispatchLongAddress() {
        // set the address
        assertTrue( controller.sort("L1234"), "Continue after address");
        assertTrue( tcls.hasAddressBeenFound(), "Address Found");
        // then dispatch it.
        assertTrue( controller.sort("d"), "Continue after release");
        assertTrue( tcls.hasAddressBeenReleased(), "Address Released");
    }

    @Test
    public void testSetVelocityChange() {
        DccThrottle t = new DebugThrottle(new DccLocoAddress(1, false), memo);
        controller.notifyThrottleFound(t);
        assertTrue( controller.sort("V63"), "Continue after velocity");
        assertEquals( 0.5f, t.getSpeedSetting(), 0.0005f, "Velocity set");
    }

    @Test
    public void testSetEStop() {
        DccThrottle t = new DebugThrottle(new DccLocoAddress(1, false), memo);
        controller.notifyThrottleFound(t);
        assertTrue( controller.sort("X"), "Continue after EStop");
        assertTrue( t.getSpeedSetting() < 0.0f, "Estop");
    }

    @Test
    public void testSetFunction() {
        DccThrottle t = new DebugThrottle(new DccLocoAddress(1, false), memo);
        // before notifying the throttle is found, set a function on
        // which runs a couple of additional lines of code in
        // sendAllFunctionStates.
        t.setFunction(6, true);
        controller.notifyThrottleFound(t);
        // function "on" from withrottle represents a button click event.
        assertTrue( controller.sort("F11"), "Continue after set F1 on");
        assertTrue( t.getFunction(1), "F1 set on");
        assertTrue( controller.sort("F11"), "Continue after set F1 off");
        assertFalse( t.getFunction(1), "F1 set off");
    }

    @Test
    public void testForceFunction() {
        DccThrottle t = new DebugThrottle(new DccLocoAddress(1, false), memo);
        controller.notifyThrottleFound(t);
        assertTrue( controller.sort("f11"), "Continue after set F1 on");
        assertTrue( t.getFunction(1), "F1 set on");
        assertTrue( controller.sort("f01"), "Continue after set F1 off");
        assertFalse( t.getFunction(1), "F1 set off");
    }

    @Test
    public void testMomentaryFunction() {
        DccThrottle t = new DebugThrottle(new DccLocoAddress(1, false), memo);
        controller.notifyThrottleFound(t);
        assertTrue( controller.sort("m11"), "Continue after set F1 momentary");
        assertTrue( t.getFunctionMomentary(1), "F1 set on");
        assertTrue( controller.sort("m01"), "Continue after set F1 continuous");
        assertFalse( t.getFunctionMomentary(1), "F1 set off");
    }

    @Test
    public void testSetDirection() {
        DccThrottle t = new DebugThrottle(new DccLocoAddress(1, false), memo);
        controller.notifyThrottleFound(t);
        assertTrue( controller.sort("R1"), "Continue after set R1");
        assertTrue( t.getIsForward(), "Velocity set");
        assertTrue( controller.sort("R0"), "Continue after set R0");
        assertFalse( t.getIsForward(), "Velocity set");
    }

    @Test
    public void testSetIdle() {
        DccThrottle t = new DebugThrottle(new DccLocoAddress(1, false), memo);
        controller.notifyThrottleFound(t);
        assertTrue( controller.sort("V63"), "Continue after velocity");
        assertTrue( controller.sort("I"), "Continue after Idle");
        assertEquals( 0.0f, t.getSpeedSetting(), 0.0f, "Idle");
    }

    @Test
    public void testFunctionPropertyChange() {
        DccThrottle t = new DebugThrottle(new DccLocoAddress(1, false), memo);
        controller.notifyThrottleFound(t);
        t.setFunction(1, true);
        assertEquals( "MAAtest<;>F11", cis.getLastPacket(), "outgoing message after property change");
        t.setFunction(1, false);
        assertEquals( "MAAtest<;>F01", cis.getLastPacket(), "outgoing message after property change");
    }

    @Test
    public void testFunctionMomentaryPropertyChange() {
        controller.propertyChange(new PropertyChangeEvent(this, "F1Momentary", false, true));
        assertNull( cis.getLastPacket(), "WiThrottle Server ignores changes to Momentary Status");
        controller.propertyChange(new PropertyChangeEvent(this, "F1Momentary", true, false));
        assertNull( cis.getLastPacket(), "WiThrottle Server ignores changes to Momentary Status");
    }

    @Test
    public void testSpeedStepsPropertyChange() {
        DccThrottle t = new DebugThrottle(new DccLocoAddress(1, false), memo);
        controller.notifyThrottleFound(t);
        t.setSpeedStepMode(jmri.SpeedStepMode.NMRA_DCC_14);
        assertEquals( "MAAtest<;>s8", cis.getLastPacket(), "outgoing message after property change");
        t.setSpeedStepMode(jmri.SpeedStepMode.NMRA_DCC_28);
        assertEquals( "MAAtest<;>s2", cis.getLastPacket(), "outgoing message after property change");
        t.setSpeedStepMode(jmri.SpeedStepMode.NMRA_DCC_128);
        assertEquals( "MAAtest<;>s1", cis.getLastPacket(), "outgoing message after property change");
    }

    @Test
    public void testSpeedPropertyChange() {
        DccThrottle t = new DebugThrottle(new DccLocoAddress(1, false), memo);
        controller.notifyThrottleFound(t);
        t.setSpeedSetting(0.5f);
        assertEquals( "MAAtest<;>V63", cis.getLastPacket(), "outgoing message after property change");
        t.setSpeedSetting(1.0f);
        assertEquals( "MAAtest<;>V126", cis.getLastPacket(), "outgoing message after property change");
        t.setSpeedSetting(0.0f);
        assertEquals( "MAAtest<;>V0", cis.getLastPacket(), "outgoing message after property change");
    }

    @Test
    public void testIsForwardPropertyChange() {
        DccThrottle t = new DebugThrottle(new DccLocoAddress(1, false), memo);
        controller.notifyThrottleFound(t);
        t.setIsForward(false);
        assertEquals( "MAAtest<;>R0", cis.getLastPacket(), "outgoing message after property change");
        t.setIsForward(true);
        assertEquals( "MAAtest<;>R1", cis.getLastPacket(), "outgoing message after property change");
    }

    @Test
    public void testQuitNoAddress() {
        assertFalse( controller.sort("Q"), "Stop after quit");
    }

    @Test
    public void testQuitWithAddress() {
        DccThrottle t = new DebugThrottle(new DccLocoAddress(1, false), memo);
        controller.notifyThrottleFound(t);
        assertTrue( controller.sort("V63"), "Continue after velocity");
        assertEquals( 0.5f, t.getSpeedSetting(), 0.0005f, "Velocity set");
        assertFalse( controller.sort("Q"), "Stop after quit");
        // current behavior is to set the speed to 0 after quit.
        assertEquals( 0.0f, t.getSpeedSetting(), 0.0005f, "Velocity set");
    }

    @Test
    public void testVelocityChangeSequence() {
        DccThrottle t = new DebugThrottle(new DccLocoAddress(1, false), memo) {
            @Override
            public synchronized void setSpeedSetting(float s) {
                // override so we can send property changes in sequence.
            }
        };
        controller.notifyThrottleFound(t);
        cis.reset();
        // withrottle may actually sends more than one speed change when
        // moving the slider.
        assertTrue( controller.sort("V7"), "Continue after velocity");
        assertTrue( controller.sort("V15"), "Continue after velocity");
        assertTrue( controller.sort("V25"), "Continue after velocity");
        controller.propertyChange(new PropertyChangeEvent(this, Throttle.SPEEDSETTING, 0.0f, 7.0f / 126.0f));
        assertNull( cis.getLastPacket(), "outgoing message after property change");
        assertTrue( controller.sort("V32"), "Continue after velocity");
        assertTrue( controller.sort("V45"), "Continue after velocity");
        assertTrue( controller.sort("V63"), "Continue after velocity");
        controller.propertyChange(new PropertyChangeEvent(this, Throttle.SPEEDSETTING, 7.0f / 126.0f, 15.0f / 126.0f));
        assertNull( cis.getLastPacket(), "outgoing message after property change");
        controller.propertyChange(new PropertyChangeEvent(this, Throttle.SPEEDSETTING, 15.0f / 126.0f, 25.0f / 126.0f));
        assertNull( cis.getLastPacket(), "outgoing message after property change");
        controller.propertyChange(new PropertyChangeEvent(this, Throttle.SPEEDSETTING, 25.0f / 126.0f, 32.0f / 126.0f));
        assertNull( cis.getLastPacket(), "outgoing message after property change");
        controller.propertyChange(new PropertyChangeEvent(this, Throttle.SPEEDSETTING, 32.0f / 126.0f, 45.0f / 126.0f));
        assertNull( cis.getLastPacket(), "outgoing message after property change");
        controller.propertyChange(new PropertyChangeEvent(this, Throttle.SPEEDSETTING, 0.0f, 63.0f / 126.0f));
        assertNull( cis.getLastPacket(), "outgoing message after property change");
        controller.propertyChange(new PropertyChangeEvent(this, "SpeedSetting", 0.0f, 63.0f / 126.0f));
        assertEquals( "MAAtest<;>V63", cis.getLastPacket(), "outgoing message after property change");
    }

    @Test
    public void testQueryVelocity() {
        DccThrottle t = new DebugThrottle(new DccLocoAddress(1, false), memo);
        controller.notifyThrottleFound(t);
        assertTrue( controller.sort("qV"), "Continue after query velocity");
        assertEquals( "MAAtest<;>V0", cis.getLastPacket(), "outgoing message after property change");
    }

    @Test
    public void testQueryDirection() {
        DccThrottle t = new DebugThrottle(new DccLocoAddress(1, false), memo);
        controller.notifyThrottleFound(t);
        assertTrue( controller.sort("qR"), "Continue after query velocity");
        assertEquals( "MAAtest<;>R1", cis.getLastPacket(), "outgoing message after property change");
    }

    @Test
    public void testQuerySpeedStepMode() {
        DccThrottle t = new DebugThrottle(new DccLocoAddress(1, false), memo);
        controller.notifyThrottleFound(t);
        assertTrue( controller.sort("qs"), "Continue after query velocity");
        assertEquals( "MAAtest<;>s1", cis.getLastPacket(), "outgoing message after property change");
    }

    @Test
    public void testQueryMomentary() {
        DccThrottle t = new DebugThrottle(new DccLocoAddress(1, false), memo);
        controller.notifyThrottleFound(t);
        assertTrue( controller.sort("qm"), "Continue after query velocity");
        assertEquals( "MAAtest<;>m028", cis.getLastPacket(), "outgoing message after property change");
    }

    @Test
    public void testSharingThrottleManager() {
        // install a ThrottleManager which will request a Share / Cancel response
        InstanceManager.reset(ThrottleManager.class);
        memo.dispose();
        memo = InstanceManager.getDefault(InternalSystemConnectionMemo.class);
        var tm = new jmri.managers.SharingThrottleManager(memo);
        InstanceManager.setThrottleManager(tm);
        memo.store(tm, ThrottleManager.class);

        // set to Silently Share
        InstanceManager.getDefault(ThrottlesPreferences.class).setSilentShare(true);

        assertTrue( controller.sort("L279"),"request address 279L");
        assertTrue( tcls.hasAddressBeenFound(),"throttle created");

        assertTrue( controller.sort("r"), "request loco released");
        assertTrue( tcls.hasAddressBeenReleased(), "tcls Address Released");

    }

    @Test
    public void testStealOrSharingThrottleManager() {
        // install a ThrottleManager which will request a Share / Steal / Cancel response
        InstanceManager.reset(ThrottleManager.class);
        memo.dispose();
        memo = InstanceManager.getDefault(InternalSystemConnectionMemo.class);
        var tm = new jmri.managers.StealingOrSharingThrottleManager(memo);
        memo.store(tm, ThrottleManager.class);
        InstanceManager.setThrottleManager(tm);

        // set to Silently Share
        InstanceManager.getDefault(ThrottlesPreferences.class).setSilentShare(true);

        assertTrue( controller.sort("L280"),"request address 280L");
        assertTrue( tcls.hasAddressBeenFound(),"throttle created");
        assertEquals( DecisionType.SHARE, tm.lastResponse, "a share response was sent");

        assertTrue( controller.sort("r"), "request loco released");
        assertTrue( tcls.hasAddressBeenReleased(), "tcls Address Released");

        // set to Silently Steal
        InstanceManager.getDefault(ThrottlesPreferences.class).setSilentShare(false);
        InstanceManager.getDefault(ThrottlesPreferences.class).setSilentSteal(true);

        assertTrue( controller.sort("L281"),"request address 281L");
        assertTrue( tcls.hasAddressBeenFound(),"throttle created");
        assertEquals( DecisionType.STEAL, tm.lastResponse, "a steal response was sent");

        assertTrue( controller.sort("r"), "request loco released");
        assertTrue( tcls.hasAddressBeenReleased(), "tcls Address Released");

    }

    private InternalSystemConnectionMemo memo;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = InstanceManager.getDefault(InternalSystemConnectionMemo.class);
        JUnitUtil.initDebugThrottleManager(memo);
        JUnitUtil.initRosterConfigManager();
        cis = new ControllerInterfaceScaffold();
        tcls = new ThrottleControllerListenerScaffold();
        controller = new MultiThrottleController('A', "test", tcls, cis);
    }

    @AfterEach
    public void tearDown() {
        cis = null;
        tcls = null;
        controller = null;
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }
}
