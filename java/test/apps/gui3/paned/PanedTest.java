package apps.gui3.paned;

import apps.AppsBase;
import apps.tests.Log4JFixture;
import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

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
                // do not call JUnitUtil.resetInstanceManager() since that also
                // disposes of open windows, which is undesirable
                InstanceManager.getDefault().clearAll();
                JUnitUtil.initDefaultUserMessagePreferences();
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
        JUnitUtil.disposeFrame("Decoder Pro Wizard", true, true);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetApplication();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetApplication();
        Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PanedTest.class);

}
