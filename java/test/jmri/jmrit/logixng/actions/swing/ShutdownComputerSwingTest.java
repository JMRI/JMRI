package jmri.jmrit.logixng.actions.swing;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.swing.JDialog;
import javax.swing.JPanel;

import jmri.util.JUnitUtil;
import jmri.jmrit.logixng.actions.ShutdownComputer;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test ShutdownComputerSwing
 *
 * @author Daniel Bergqvist 2018
 */
public class ShutdownComputerSwingTest {

    @Test
    @DisabledIfHeadless
    public void testCtor() {

        ShutdownComputerSwing t = new ShutdownComputerSwing();
        assertNotNull( t, "exists");
    }

    @Test
    @DisabledIfHeadless
    public void testCreatePanel() {

        JDialog dialog = new JDialog();

        assertNotNull( new ShutdownComputerSwing(dialog).getConfigPanel(new JPanel()), "panel is not null");
        assertNotNull( new ShutdownComputerSwing(dialog).getConfigPanel(new ShutdownComputer("IQDA1", null), new JPanel()),
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
