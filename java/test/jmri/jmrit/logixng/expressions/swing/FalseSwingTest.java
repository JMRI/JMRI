package jmri.jmrit.logixng.expressions.swing;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.swing.JPanel;

import jmri.util.JUnitUtil;
import jmri.jmrit.logixng.expressions.False;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test FalseSwing
 *
 * @author Daniel Bergqvist 2018
 */
public class FalseSwingTest {

    @Test
    @DisabledIfHeadless
    public void testCtor() {

        FalseSwing t = new FalseSwing();
        assertNotNull( t, "exists");
    }

    @Test
    @DisabledIfHeadless
    public void testPanel() {

        FalseSwing t = new FalseSwing();
        JPanel panel = t.getConfigPanel(new JPanel());
        assertNotNull( panel, "exists");
    }

    @Test
    @DisabledIfHeadless
    public void testCreatePanel() {

        assertNotNull( new FalseSwing().getConfigPanel(new JPanel()), "panel is not null");
        assertNotNull( new FalseSwing().getConfigPanel(new False("IQDE1", null), new JPanel()), "panel is not null");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initLogixNGManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
