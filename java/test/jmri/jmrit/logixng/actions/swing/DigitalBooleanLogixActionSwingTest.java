package jmri.jmrit.logixng.actions.swing;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.swing.JPanel;

import jmri.util.JUnitUtil;
import jmri.jmrit.logixng.actions.DigitalBooleanLogixAction;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test DigitalBooleanLogixActionSwing
 *
 * @author Daniel Bergqvist 2018
 */
public class DigitalBooleanLogixActionSwingTest {

    @Test
    @DisabledIfHeadless
    public void testCtor() {

        DigitalBooleanLogixActionSwing t = new DigitalBooleanLogixActionSwing();
        assertNotNull( t, "exists");
    }

    @Test
    @DisabledIfHeadless
    public void testCreatePanel() {

        assertNotNull( new DigitalBooleanLogixActionSwing().getConfigPanel(new JPanel()), "panel is not null");
        assertNotNull( new DigitalBooleanLogixActionSwing().getConfigPanel(new DigitalBooleanLogixAction("IQDB1", null, DigitalBooleanLogixAction.When.Either), new JPanel()),
                "panel is not null");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initLogixNGManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
