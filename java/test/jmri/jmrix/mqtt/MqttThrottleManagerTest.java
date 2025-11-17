package jmri.jmrix.mqtt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.ThrottleListener;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test for the jmri.jmrix.mqtt.MqttThrottleManager class
 *
 * @author Paul Bender
 * @author Mark Underwood (C) 2015
 * @author Egbert Broerse 2021
 * @author Dean Cording 2023
 */
public class MqttThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    @Test
    public void testCreateLnThrottleRunAndRelease() {

        var throtListen = new ThrottleListener() {

            private String status = "";

            @Override
            public void notifyThrottleFound(DccThrottle t) {
                throttle = t;
                status = "created a throttle";
            }

            @Override
            public void notifyFailedThrottleRequest(jmri.LocoAddress address, String reason) {
                status = "Throttle request failed for " + address + " because "+ reason;
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

        assertNotNull( throttle, "have created a throttle");
        assertInstanceOf(MqttThrottle.class , throttle);
        assertTrue( (((MqttThrottleManager)tm).throttles.containsKey(locoAddress))); // now you see it
        assertEquals(1,tm.getThrottleUsageCount(locoAddress));
        assertEquals( "created a throttle", throtListen.status);
        assertFalse(failedThrottleRequest);

        tm.releaseThrottle(throttle, throtListen);
        assertEquals(0,tm.getThrottleUsageCount(locoAddress));
        assertFalse( (((MqttThrottleManager)tm).throttles.containsKey(locoAddress))); //now you dont
    }

    private MqttSystemConnectionMemo memo;
    private DccThrottle throttle;
    private boolean failedThrottleRequest = false;
    private MqttConsistManager cm;
    private MqttAdapterScaffold a;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        a = new MqttAdapterScaffold(true);
        memo = new MqttSystemConnectionMemo();
        memo.setMqttAdapter(a);
        cm = new MqttConsistManager(memo);
        cm.setSendTopic("cab/3/consist");
        memo.setConsistManager(cm);
        tm = new MqttThrottleManager(memo);
    }

    @AfterEach
    public void tearDown() {
        MqttThrottleManager dtm = (MqttThrottleManager)tm;
        dtm.dispose();
        tm = null;
        memo.dispose();
        memo = null;
        cm = null;
        a = null;

        JUnitUtil.resetWindows(false, false);
        JUnitUtil.tearDown();
    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MqttThrottleManagerTest.class);

}
