package jmri.jmrit.logixng.expressions.swing;

import java.awt.GraphicsEnvironment;

import javax.swing.JPanel;

import jmri.util.JUnitUtil;
import jmri.jmrit.logixng.expressions.And;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test AndSwing
 *
 * @author Daniel Bergqvist 2018
 */
public class AndSwingTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        AndSwing t = new AndSwing();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testPanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        AndSwing t = new AndSwing();
        JPanel panel = t.getConfigPanel(new JPanel());
        Assert.assertNotNull("exists",panel);
    }

    @Test
    public void testCreatePanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertTrue("panel is not null",
            null != new AndSwing().getConfigPanel(new JPanel()));
        Assert.assertTrue("panel is not null",
            null != new AndSwing().getConfigPanel(new And("IQDE1", null), new JPanel()));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
