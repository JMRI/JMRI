package jmri.jmrix.rps.reversealign;

import java.awt.GraphicsEnvironment;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import jmri.InstanceManager;
import jmri.jmrit.roster.RosterConfigManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;
import jmri.jmrix.rps.RpsSystemConnectionMemo;

/**
 * JUnit tests for the rps.AlignmentPanel class.
 *
 * @author	Bob Jacobsen Copyright 2006
 */
public class AlignmentPanelTest {

    private RpsSystemConnectionMemo memo = null;

    @Test
    public void testShow() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.util.JmriJFrame f = new jmri.util.JmriJFrame("RPS Alignment");
        f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));

        AlignmentPanel panel = new AlignmentPanel(memo);
        panel.initComponents();
        f.getContentPane().add(panel);
        f.pack();
        f.setVisible(true);
        JFrame f2 = JFrameOperator.waitJFrame("RPS Alignment", true, true);
        Assert.assertNotNull("found frame", f2);
        f2.dispose();
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();

        memo = new RpsSystemConnectionMemo();
        InstanceManager.setDefault(RosterConfigManager.class, new RosterConfigManager());
    }

    @After
    public void tearDown() throws Exception {        JUnitUtil.tearDown();    }
}
