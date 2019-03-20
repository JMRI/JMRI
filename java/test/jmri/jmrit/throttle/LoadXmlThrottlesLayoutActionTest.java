package jmri.jmrit.throttle;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of LoadXmlThrottlesLayoutAction
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LoadXmlThrottlesLayoutActionTest {

    @Test
    public void testCtor() {
        LoadXmlThrottlesLayoutAction panel = new LoadXmlThrottlesLayoutAction();
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
