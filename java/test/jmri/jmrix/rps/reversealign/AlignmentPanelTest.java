package jmri.jmrix.rps.reversealign;

import apps.tests.Log4JFixture;
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

/**
 * JUnit tests for the rps.AlignmentPanel class.
 *
 * @author	Bob Jacobsen Copyright 2006
 */
public class AlignmentPanelTest {

    @Test
    public void testShow() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.util.JmriJFrame f = new jmri.util.JmriJFrame("RPS Alignment");
        f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));

        AlignmentPanel panel = new AlignmentPanel();
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
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        InstanceManager.setDefault(RosterConfigManager.class, new RosterConfigManager());
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }
}
