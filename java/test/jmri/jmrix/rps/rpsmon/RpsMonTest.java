package jmri.jmrix.rps.rpsmon;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
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

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();

        memo = new RpsSystemConnectionMemo();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
