package jmri.jmrit.throttle;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of StoreXmlThrottlesLayoutAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class StoreXmlThrottlesLayoutActionTest {

    @Test
    public void testCtor() {
        StoreXmlThrottlesLayoutAction panel = new StoreXmlThrottlesLayoutAction();
        Assertions.assertNotNull( panel, "exists");
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
