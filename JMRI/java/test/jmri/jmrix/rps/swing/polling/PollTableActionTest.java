package jmri.jmrix.rps.swing.polling;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;
import jmri.jmrix.rps.RpsSystemConnectionMemo;

/**
 * Tests for the jmri.jmrix.rps.swing.polling package.
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class PollTableActionTest {

    private RpsSystemConnectionMemo memo = null;

    // Show the window
    @Test
    public void testDisplay() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new PollTableAction(memo).actionPerformed(null);
        // confirm window was created
        JFrame f = JFrameOperator.waitJFrame("RPS Polling Control", true, true);
        Assert.assertNotNull("found frame", f);
        f.dispose();
    }

    @Before
    public void setUp(){
        jmri.util.JUnitUtil.setUp();
        memo = new RpsSystemConnectionMemo();
    }

    @After
    public void tearDown(){
        jmri.util.JUnitUtil.tearDown();
    }
}
