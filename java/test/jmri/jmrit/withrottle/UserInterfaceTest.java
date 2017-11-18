package jmri.jmrit.withrottle;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of UserInterface
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class UserInterfaceTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        UserInterface panel = new UserInterface() {
            @Override
            public void createServerThread() {
            }
        };

        Assert.assertNotNull("exists", panel);
        JUnitUtil.dispose(panel);
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
        JUnitUtil.resetInstanceManager();
    }
}
