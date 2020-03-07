package jmri.jmrit.withrottle;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
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

    private UserInterface panel = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", panel);
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        InstanceManager.setDefault(DeviceManager.class, new FacelessServer() {
            @Override
            public void listen() {
            }
        });
        if (!GraphicsEnvironment.isHeadless()) {
            panel = new UserInterface();
        }
    }

    @After
    public void tearDown() throws Exception {
        if (!GraphicsEnvironment.isHeadless()) {
            try {
                panel.disableServer();
                JUnitUtil.waitFor(() -> {
                    return panel.isListen;
                });
                JUnitUtil.dispose(panel);
            } catch (java.lang.NullPointerException npe) {
                // not all tests fully configure the server, so an
                // NPE here is ok.
            }
        }
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }
}
