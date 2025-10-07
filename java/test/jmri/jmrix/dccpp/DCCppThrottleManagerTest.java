package jmri.jmrix.dccpp;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.util.JUnitUtil;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.ThrottleListener;

import org.junit.jupiter.api.*;

/**
 * Test for the jmri.jmrix.dccpp.DCCppThrottleManager class
 *
 * @author Paul Bender
 * @author Mark Underwood (C) 2015
 * @author Egbert Broerse 2021
 */
public class DCCppThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    @Test
    public void testCreateLnThrottleRunAndRelease() {

        var throtListen = new ThrottleListener() {

            private String throtListenstatus = "";

            @Override
            public void notifyThrottleFound(DccThrottle t) {
                throttle = t;
                throtListenstatus = "created a throttle";
            }

            @Override
            public void notifyFailedThrottleRequest(jmri.LocoAddress address, String reason) {
                throtListenstatus = "Throttle request failed for " + address + " because" + reason;
            }

            @Override
            public void notifyDecisionRequired(jmri.LocoAddress address, DecisionType question) {
                // this is a never-stealing impelementation.
                InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
            }
        };
        DccLocoAddress locoAddress = new DccLocoAddress(1203,true);
        tm.requestThrottle(1203, throtListen,true);

        assertNotNull( throttle, "have created a throttle");
        assertInstanceOf(DCCppThrottle.class, throttle);
        assertTrue( (((DCCppThrottleManager)tm).throttles.containsKey(locoAddress))); // now you see it
        assertEquals(1,tm.getThrottleUsageCount(locoAddress));
        assertEquals(1, cs.getRegisterNum(1203));  // now you see it
        assertEquals("created a throttle", throtListen.throtListenstatus);

        tm.releaseThrottle(throttle, throtListen);
        assertEquals(0,tm.getThrottleUsageCount(locoAddress));
        assertFalse( (((DCCppThrottleManager)tm).throttles.containsKey(locoAddress))); //now you dont
        assertEquals(-1, cs.getRegisterNum(1203)); //now you dont
    }

    private DCCppSystemConnectionMemo memo;
    private DCCppInterfaceScaffold tc;
    private DccThrottle throttle;
    private DCCppCommandStation cs = null;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        cs = new DCCppCommandStation();
        cs.setCommandStationMaxNumSlots(12); // the "traditional" value for DCC++
        tc = new DCCppInterfaceScaffold(cs);
        memo = new DCCppSystemConnectionMemo(tc);
        tm = new DCCppThrottleManager(memo);
    }

    @AfterEach
    public void tearDown() {
        DCCppThrottleManager dtm = (DCCppThrottleManager)tm;
        dtm.dispose();
        tm = null;
        memo.dispose();
        memo = null;
        tc.terminateThreads();
        tc = null;
        cs = null;
        JUnitUtil.resetWindows(false, false);
        JUnitUtil.tearDown();
    }

}
