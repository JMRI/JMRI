package jmri.jmrit.throttle;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of StoreDefaultXmlThrottlesLayoutAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class StoreDefaultXmlThrottlesLayoutActionTest {

    @Test
    public void testCtor() {
        StoreDefaultXmlThrottlesLayoutAction panel = new StoreDefaultXmlThrottlesLayoutAction("test");
        Assert.assertNotNull("exists", panel);
    }

    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();

    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();

    }
}
