package jmri.jmrit.throttle;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of ThrottleWindow
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ThrottleWindowTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThrottleWindow frame = new ThrottleWindow();
        Assert.assertNotNull("exists", frame);
        frame.dispose();
    }

    @Before
    public void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
    }
}
