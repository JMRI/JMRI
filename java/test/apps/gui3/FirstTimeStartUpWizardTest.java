package apps.gui3;

import apps.gui3.dp3.DecoderPro3;
import java.awt.GraphicsEnvironment;
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
public class FirstTimeStartUpWizardTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        String[] args = {"DecoderProConfig3.xml"};
        Apps3 a = new DecoderPro3(args) {
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

            @Override
            public void createAndDisplayFrame() {
                // called when wizard is disposed, but do nothing in tests
            }
        };
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("Decoder Pro Wizard", false, false);
        FirstTimeStartUpWizard t = new FirstTimeStartUpWizard(jf, a);
        Assert.assertNotNull("exists", t);
        t.dispose();
        JUnitUtil.dispose(jf);
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

    // private final static Logger log = LoggerFactory.getLogger(FirstTimeStartUpWizardTest.class);
}
