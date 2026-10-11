package jmri.jmrit.logixng.actions.swing;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.swing.JPanel;

import jmri.util.JUnitUtil;
import jmri.jmrit.logixng.actions.DoAnalogAction;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test DoAnalogAction
 *
 * @author Daniel Bergqvist 2018
 */
public class DoAnalogActionSwingTest {

    @Test
    @DisabledIfHeadless
    public void testCtor() {

        DoAnalogActionSwing t = new DoAnalogActionSwing();
        assertNotNull( t, "exists");
    }

    @Test
    @DisabledIfHeadless
    public void testCreatePanel() {

        assertNotNull( new DoAnalogActionSwing().getConfigPanel(new JPanel()), "panel is not null");
        assertNotNull( new DoAnalogActionSwing().getConfigPanel(new DoAnalogAction("IQDA1", null), new JPanel()),
                "panel is not null");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
