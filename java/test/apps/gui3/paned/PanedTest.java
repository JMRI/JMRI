package apps.gui3.paned;

import apps.AppsBase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PanedTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        String[] args = {"DecoderProConfig3.xml"};
        AppsBase a = new Paned(args) {
            // force the application to not actually start.  
            // Just checking construction.
            @Override
            protected void start() {
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

    // private final static Logger log = LoggerFactory.getLogger(PanedTest.class.getName());

}
