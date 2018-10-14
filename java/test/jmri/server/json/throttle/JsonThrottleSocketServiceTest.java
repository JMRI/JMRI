package jmri.server.json.throttle;

import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitUtil;

import java.io.DataOutputStream;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class JsonThrottleSocketServiceTest {

    @Test
    public void testCTor() {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonThrottleSocketService t = new JsonThrottleSocketService(connection);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
