package apps.gui3.dp3;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import apps.AppsBase;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 *
 * Tests for the DecoderPro3 application.
 *
 * @author Paul Bender Copyright (C) 2016
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class DecoderPro3Test {

    @Test
    @Disabled("Fails consistently on Jenkins and travis GUI tests")
    public void testCtor() {
        String[] args = {"DecoderProConfig3.xml"};
        AppsBase a = new DecoderPro3(args) {
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
        assertThat(a).isNotNull();

        jmri.util.JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame("DecoderPro Wizard") != null;
        }, "wait for frame to appear");

        // remove a frame opened by DecoderPro3
        JUnitUtil.disposeFrame("DecoderPro Wizard", false, false);
        // shutdown the application
        // the following line terminates the Junit testing early
//        AppsBase.handleQuit();

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetApplication();
        JUnitUtil.resetProfileManager();
        // 12/07/2020 tried to improve initialization of test without any luck DAB
//        JUnitUtil.resetInstanceManager();
//        JUnitUtil.resetProfileManager();
//        JUnitUtil.initRosterConfigManager();
//        JUnitUtil.initInternalTurnoutManager();
//        JUnitUtil.initInternalLightManager();
//        JUnitUtil.initInternalSensorManager();
//        JUnitUtil.initDebugThrottleManager();
//        JUnitUtil.clearShutDownManager();
    }

    @AfterEach
    public void tearDown() {
        // eventually want to test ShutDownTasks?
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.resetApplication();
        JUnitUtil.tearDown();
    }

}
