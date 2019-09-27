package jmri.server.json.throttle;

import java.io.DataOutputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitAppender;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Randall Wood Copyright (C) 2019
 */
public class JsonThrottleManagerTest {

    @Test
    public void testGetDefault() {
        Assert.assertEquals("Default instance", InstanceManager.getDefault(JsonThrottleManager.class),
                JsonThrottleManager.getDefault());
        JUnitAppender.assertWarnMessage("getDefault is deprecated, please remove references to it");
    }

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

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
