package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.ThrottleListener;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DCCppThrottleManagerTest.java
 * <p>
 * Test for the jmri.jmrix.dccpp.DCCppThrottleManager class
 *
 * @author Paul Bender
 * @author Mark Underwood (C) 2015
 */
public class DCCppThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    private DccThrottle throttle;
    boolean failedThrottleRequest = false;
    DCCppCommandStation cs = null;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        cs = new DCCppCommandStation();
        cs.setCommandStationMaxNumSlots(12); // the "traditional" value for DCC++
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(cs);
        tm = new DCCppThrottleManager(new DCCppSystemConnectionMemo(tc));
    }

    @Test
    public void testCreateLnThrottleRunAndRelease() {
        ThrottleListener throtListen = new ThrottleListener() {
            @Override
            public void notifyThrottleFound(DccThrottle t) {
                throttle = t;
                log.error("created a throttle");
            }

            @Override
            public void notifyFailedThrottleRequest(jmri.LocoAddress address, String reason) {
                log.error("Throttle request failed for {} because {}", address, reason);
                failedThrottleRequest = true;
            }

            @Override
            public void notifyDecisionRequired(jmri.LocoAddress address, DecisionType question) {
                // this is a never-stealing impelementation.
                InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
            }
        };
        DccLocoAddress locoAddress = new DccLocoAddress(1203,true);
        tm.requestThrottle(1203, throtListen,true);

        Assert.assertNotNull("have created a throttle", throttle);
        Assert.assertEquals("is DCCppThrottle", throttle.getClass(), jmri.jmrix.dccpp.DCCppThrottle.class);
        Assert.assertEquals(true, (((DCCppThrottleManager)tm).throttles.containsKey(locoAddress))); // now you see it
        Assert.assertEquals(1,tm.getThrottleUsageCount(locoAddress));
        Assert.assertEquals(1, cs.getRegisterNum(1203));  // now you see it
        jmri.util.JUnitAppender.assertErrorMessage("created a throttle");

        tm.releaseThrottle(throttle, throtListen);
        Assert.assertEquals(0,tm.getThrottleUsageCount(locoAddress));
        Assert.assertEquals(false, (((DCCppThrottleManager)tm).throttles.containsKey(locoAddress))); //now you dont
        Assert.assertEquals(-1, cs.getRegisterNum(1203)); //now you dont

    }

    @AfterEach
    public void tearDown() {
        tm =null;
        cs = null;
        JUnitUtil.resetWindows(false, false);
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    private final static Logger log = LoggerFactory.getLogger(DCCppThrottleManagerTest.class);

}
