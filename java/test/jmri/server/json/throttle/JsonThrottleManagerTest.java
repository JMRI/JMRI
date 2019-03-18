package jmri.server.json.throttle;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jmri.InstanceManager;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Randall Wood Copyright (C) 2019
 */
public class JsonThrottleManagerTest {

    @Test
    public void testGetDefault() {
        Assert.assertEquals("Default instance", InstanceManager.getDefault(JsonThrottleManager.class), JsonThrottleManager.getDefault());
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
