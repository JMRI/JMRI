package jmri.jmrit.throttle;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LoadDefaultXmlThrottlesLayoutAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LoadDefaultXmlThrottlesLayoutActionTest {

    @Test
    public void testCtor() {
        LoadDefaultXmlThrottlesLayoutAction panel = new LoadDefaultXmlThrottlesLayoutAction();
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
