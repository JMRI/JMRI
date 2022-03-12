package jmri.jmrix.rps.rpsmon;

import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;
import org.netbeans.jemmy.operators.JFrameOperator;

import jmri.jmrix.rps.RpsSystemConnectionMemo;

/**
 * Tests for the jmri.jmrix.rps.rpsmon package.
 *
 * @author Bob Jacobsen Copyright 2006
 */
public class RpsMonTest {

    private RpsSystemConnectionMemo memo = null;

    // show the window
    @Test
    public void testDisplay() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new RpsMonAction(memo).actionPerformed(null);
        JFrame f = JFrameOperator.waitJFrame("RPS Monitor", true, true);
        Assert.assertNotNull("found frame", f);
        f.dispose();
    }

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();

        memo = new RpsSystemConnectionMemo();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }
}
