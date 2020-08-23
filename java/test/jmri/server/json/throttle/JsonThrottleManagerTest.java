package jmri.server.json.throttle;

import java.io.DataOutputStream;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.DccLocoAddress;
import jmri.server.json.JsonMockConnection;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Randall Wood Copyright (C) 2019
 */
public class JsonThrottleManagerTest {

    /**
     * Testing
     * {@link jmri.server.json.throttle.JsonThrottleManager#put(JsonThrottle, JsonThrottleSocketService)}
     * because testing via JsonThrottleSocketService does not trigger the
     * complete method.
     */
    @Test
    public void testPutThrottleService() {
        JsonThrottleManager manager = new JsonThrottleManager();
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonThrottleSocketService service = new JsonThrottleSocketService(connection);
        JsonThrottle throttle = new JsonThrottle(new DccLocoAddress(3, true), service);
        manager.put(throttle, service);
        Assert.assertEquals(service, manager.getServers(throttle).get(0));
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
