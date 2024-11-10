package apps.gui3.paned;

import java.io.File;
import java.io.IOException;

import apps.AppsBase;

import jmri.InstanceManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class PanedTest {

    @Test
    public void testCTor() {
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

        };
        Assert.assertNotNull(a);
        // shutdown the application
        Assertions.assertFalse(AppsBase.handleQuit());
        JUnitUtil.disposeFrame("DecoderPro Wizard", true, true);
        
        JUnitUtil.waitFor(() -> {
            return JUnitAppender.checkForMessageStartingWith("No pre-existing config file found, searched for ") != null;
        }, "no existing config Info line seen");

        JUnitUtil.waitFor( () ->
            ((jmri.managers.DefaultShutDownManager)jmri.InstanceManager.getDefault(
            jmri.ShutDownManager.class)).isShutDownComplete(),"Shutdown complete");
    }

    @BeforeEach
    public void setUp(@TempDir File tempDir) throws IOException  {
        JUnitUtil.setUp();

        JUnitUtil.resetApplication();
        JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir));
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();  // eventually want to test ShutDownTasks?
        JUnitUtil.resetApplication();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PanedTest.class);

}
