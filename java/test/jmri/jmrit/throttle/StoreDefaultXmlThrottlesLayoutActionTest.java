package jmri.jmrit.throttle;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();

    }

    @AfterEach
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();

    }
}
