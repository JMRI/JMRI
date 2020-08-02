package jmri.jmrit.throttle;

import org.junit.Assert;
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
