package jmri.jmrit.logixng.expressions.swing;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.swing.JPanel;

import jmri.util.JUnitUtil;
import jmri.jmrit.logixng.expressions.True;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test TrueSwing
 *
 * @author Daniel Bergqvist 2018
 */
public class TrueSwingTest {

    @Test
    @DisabledIfHeadless
    public void testCtor() {

        TrueSwing t = new TrueSwing();
        assertNotNull( t, "exists");
    }

    @Test
    @DisabledIfHeadless
    public void testPanel() {

        TrueSwing t = new TrueSwing();
        JPanel panel = t.getConfigPanel(new JPanel());
        assertNotNull( panel, "exists");
    }

    @Test
    @DisabledIfHeadless
    public void testCreatePanel() {

        assertNotNull( new TrueSwing().getConfigPanel(new JPanel()),
                "panel is not null");
        assertNotNull( new TrueSwing().getConfigPanel(new True("IQDE1", null), new JPanel())
                , "panel is not null");
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
