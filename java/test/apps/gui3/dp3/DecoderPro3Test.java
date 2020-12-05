package apps.gui3.dp3;

import apps.AppsBase;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * Tests for the DecoderPro3 application.
 *
 * @author Paul Bender Copyright (C) 2016
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class DecoderPro3Test {

    @Test
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
        
        // remove a frame opened by DecoderPro3
        JUnitUtil.disposeFrame("DecoderPro Wizard", false, false);
        // shutdown the application
        AppsBase.handleQuit();
        
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetApplication();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        // eventually want to test ShutDownTasks?
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.resetApplication();
        JUnitUtil.tearDown();
    }

}
