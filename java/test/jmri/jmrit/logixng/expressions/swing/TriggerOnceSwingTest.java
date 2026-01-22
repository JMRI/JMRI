package jmri.jmrit.logixng.expressions.swing;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.swing.JPanel;

import jmri.NamedBean;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.util.JUnitUtil;
import jmri.jmrit.logixng.expressions.TriggerOnce;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test TriggerOnceSwing
 *
 * @author Daniel Bergqvist 2018
 */
public class TriggerOnceSwingTest {

    @Test
    @DisabledIfHeadless
    public void testCtor() {

        TriggerOnceSwing t = new TriggerOnceSwing();
        assertNotNull( t, "exists");
    }

    @Test
    @DisabledIfHeadless
    public void testPanel() {

        TriggerOnceSwing t = new TriggerOnceSwing();
        JPanel panel = t.getConfigPanel(new JPanel());
        assertNotNull( panel, "exists");
    }

    @Test
    @DisabledIfHeadless
    public void testCreatePanel() throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException, SocketAlreadyConnectedException {

        assertNotNull( new TriggerOnceSwing().getConfigPanel(new JPanel()), "panel is not null");
        assertNotNull( new TriggerOnceSwing().getConfigPanel(new TriggerOnce("IQDE1", null), new JPanel()), "panel is not null");
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
