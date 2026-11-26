package jmri.jmrit.logixng.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test ActionThrottleFunction: the throttle protocol.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class ActionThrottleFunctionTest {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ActionThrottleFunction action;

    @Test
    public void testProtocolUsedForThrottleRequest() throws JmriException {
        ThrottleManager tm = InstanceManager.getDefault(ThrottleManager.class);
        DccLocoAddress mfx = new DccLocoAddress(3, LocoAddress.Protocol.MFX);

        assertNull( action.getProtocol(), "no protocol by default");
        action.setProtocol(LocoAddress.Protocol.MFX);
        assertEquals( LocoAddress.Protocol.MFX, action.getProtocol());

        conditionalNG.execute();

        assertEquals( 1, tm.getThrottleUsageCount(mfx), "MFX throttle requested");
        assertEquals( 0, tm.getThrottleUsageCount(3), "no DCC throttle requested");

        action.disposeMe();
        assertEquals( 0, tm.getThrottleUsageCount(mfx), "MFX throttle released");
    }

    @Test
    public void testNoProtocolRequestsAsBefore() throws JmriException {
        ThrottleManager tm = InstanceManager.getDefault(ThrottleManager.class);

        conditionalNG.execute();

        assertEquals( 1, tm.getThrottleUsageCount(3), "DCC throttle requested by number");

        action.disposeMe();
        assertEquals( 0, tm.getThrottleUsageCount(3), "DCC throttle released");
    }

    @Test
    public void testProtocolChangeRequestsNewThrottle() throws JmriException {
        ThrottleManager tm = InstanceManager.getDefault(ThrottleManager.class);
        DccLocoAddress motorola = new DccLocoAddress(3, LocoAddress.Protocol.MOTOROLA);

        conditionalNG.execute();
        assertEquals( 1, tm.getThrottleUsageCount(3), "DCC throttle requested by number");

        // same address number, new protocol: the DCC throttle is released and a Motorola one requested
        action.setProtocol(LocoAddress.Protocol.MOTOROLA);
        conditionalNG.execute();

        assertEquals( 0, tm.getThrottleUsageCount(3), "DCC throttle released");
        assertEquals( 1, tm.getThrottleUsageCount(motorola), "Motorola throttle requested");

        action.disposeMe();
    }

    @Test
    public void testLongDescriptionWithProtocol() {
        assertEquals( "Set throttle function 2 to On for address 3",
                action.getLongDescription(), "strings are equal");
        action.setProtocol(LocoAddress.Protocol.MOTOROLA);
        assertEquals( "Set throttle function 2 to On for address 3 with protocol Motorola",
                action.getLongDescription(), "strings are equal");
    }

    @BeforeEach
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initLogixNGManager();

        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        conditionalNG.setRunDelayed(false);
        action = new ActionThrottleFunction("IQDA321", null);
        action.getSelectAddress().setValue(3);
        action.getSelectFunction().setValue(2);
        action.getSelectOnOff().setEnum(ActionThrottleFunction.FunctionState.On);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        conditionalNG.getChild(0).connect(maleSocket);
        assertTrue( logixNG.setParentForAllChildren(new ArrayList<>()));
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
