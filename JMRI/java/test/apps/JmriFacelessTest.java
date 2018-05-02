package apps;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * Description: Tests for the JmriFaceless application.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class JmriFacelessTest {

    @Test
    public void testCtor() {
        String Args[] = {};
        AppsBase a = new JmriFaceless(Args) {
            // force the application to not actually start.
            // Just checking construction.
            @Override
            public void start() {
            }

            @Override
            protected void configureProfile() {
                JUnitUtil.resetInstanceManager();
            }

            @Override
            protected void installConfigurationManager() {
                JUnitUtil.initConfigureManager();
                JUnitUtil.initDefaultUserMessagePreferences();
            }

            @Override
            protected void installManagers() {
                JUnitUtil.initInternalTurnoutManager();
                JUnitUtil.initInternalLightManager();
                JUnitUtil.initInternalSensorManager();
                JUnitUtil.initRouteManager();
                JUnitUtil.initMemoryManager();
                JUnitUtil.initDebugThrottleManager();
            }

            @Override
            protected void installShutDownManager() {
                JUnitUtil.initShutDownManager();
            }
        };
        Assert.assertNotNull(a);
        // shutdown the application
        AppsBase.handleQuit();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.resetApplication();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetApplication();
        apps.tests.Log4JFixture.tearDown();
    }

}
