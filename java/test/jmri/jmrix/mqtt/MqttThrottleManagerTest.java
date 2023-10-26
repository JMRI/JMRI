package jmri.jmrix.mqtt;

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
        Assert.assertEquals("is MqttThrottle", throttle.getClass(), jmri.jmrix.mqtt.MqttThrottle.class);
        Assert.assertEquals(true, (((MqttThrottleManager)tm).throttles.containsKey(locoAddress))); // now you see it
        Assert.assertEquals(1,tm.getThrottleUsageCount(locoAddress));
        jmri.util.JUnitAppender.assertErrorMessage("created a throttle");

        tm.releaseThrottle(throttle, throtListen);
        Assert.assertEquals(0,tm.getThrottleUsageCount(locoAddress));
        Assert.assertEquals(false, (((MqttThrottleManager)tm).throttles.containsKey(locoAddress))); //now you dont
    }

    private MqttSystemConnectionMemo memo;
    private DccThrottle throttle;
    boolean failedThrottleRequest = false;
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

    private final static Logger log = LoggerFactory.getLogger(MqttThrottleManagerTest.class);

}
