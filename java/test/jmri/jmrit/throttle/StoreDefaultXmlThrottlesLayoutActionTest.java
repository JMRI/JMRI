package jmri.jmrit.throttle;

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
        Assertions.assertNotNull(panel, "exists");
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
