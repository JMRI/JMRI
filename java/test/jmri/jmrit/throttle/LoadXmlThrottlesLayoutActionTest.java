package jmri.jmrit.throttle;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LoadXmlThrottlesLayoutAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LoadXmlThrottlesLayoutActionTest {

    @Test
    public void testCtor() {
        LoadXmlThrottlesLayoutAction panel = new LoadXmlThrottlesLayoutAction();
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
