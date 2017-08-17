package jmri.jmrit.throttle;

import java.awt.GraphicsEnvironment;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of ThrottleFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ThrottleFrameTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThrottleWindow window = new ThrottleWindow();
        ThrottleFrame panel = new ThrottleFrame(window);
        Assert.assertNotNull("exists", panel );
    }

    @After
    public void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
    }
    
    @Before
    public void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
    }
}
