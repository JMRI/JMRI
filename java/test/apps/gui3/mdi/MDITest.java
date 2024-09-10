package apps.gui3.mdi;

import java.io.File;
import java.io.IOException;

import apps.AppsBase;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * Tests for the MDI application.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class MDITest {

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void testCtor() {
        String[] args = {"DecoderProConfig3.xml"};
        AppsBase a = new MDI(args) {
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
        };
        Assertions.assertNotNull(a);

        JFrameOperator wizardOperator = new JFrameOperator("DecoderPro Wizard");
        Assertions.assertNotNull(wizardOperator);

        jmri.util.swing.JemmyUtil.pressButton(wizardOperator, "Cancel");

        // struggles to find Frame in Win CI
        // JFrameOperator jfo = new JFrameOperator("JMRI GUI3 Demo");
        // Assertions.assertNotNull(jfo);

        // shutdown the application
        jmri.InstanceManager.getDefault(jmri.configurexml.ShutdownPreferences.class).setEnableStoreCheck(false);
        JUnitUtil.deregisterBlockManagerShutdownTask();

        //   jfo.requestClose(); // fails to close
        Assertions.assertFalse(AppsBase.handleQuit());

        JUnitUtil.waitFor( () ->
            ((jmri.managers.DefaultShutDownManager)jmri.InstanceManager.getDefault(
            jmri.ShutDownManager.class)).isShutDownComplete(),"Shutdown complete");

        // JUnitUtil.disposeFrame("JMRI GUI3 Demo", false, true); // still leaves Frame present ?

    }

    @BeforeEach
    public void setUp(@TempDir File folder) {
        JUnitUtil.setUp();
        JUnitUtil.resetApplication();
        try {
            JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder));
        } catch (IOException ioe) {
            Assertions.fail("failed to reset the profile", ioe);
        }
        jmri.InstanceManager.setDefault(jmri.ShutDownManager.class, new jmri.util.MockShutDownManager());
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();  // eventually want to test ShutDownTasks?
        JUnitUtil.resetApplication();
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }

}
