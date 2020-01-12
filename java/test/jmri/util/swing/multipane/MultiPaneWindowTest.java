package jmri.util.swing.multipane;

import java.awt.GraphicsEnvironment;
import javax.swing.JButton;
import javax.swing.JFrame;
import jmri.util.JUnitUtil;
import jmri.util.swing.ButtonTestAction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Invokes complete set of tests in the jmri.util tree
 *
 * @author	Bob Jacobsen Copyright 2003
 */
public class MultiPaneWindowTest {

    @Test
    public void testShow() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new MultiPaneWindow("Test of empty Multi Pane Window",
                "xml/config/apps/panelpro/Gui3LeftTree.xml",
                "xml/config/apps/panelpro/Gui3Menus.xml",
                "xml/config/apps/panelpro/Gui3MainToolBar.xml"
        ).setVisible(true);

        JFrame f = jmri.util.JmriJFrame.getFrame("Test of empty Multi Pane Window");
        Assert.assertTrue("found frame", f != null);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        MultiPaneWindow m = new MultiPaneWindow("Test of Multi Pane Window function",
                "xml/config/apps/panelpro/Gui3LeftTree.xml",
                "xml/config/apps/panelpro/Gui3Menus.xml",
                "xml/config/apps/panelpro/Gui3MainToolBar.xml");
        {
            JButton b = new JButton(new ButtonTestAction(
                    "Open new frame", new jmri.util.swing.sdi.JmriJFrameInterface()));
            m.getUpperRight().add(b);
        }
        {
            JButton b = new JButton(new ButtonTestAction(
                    "Open in upper panel", new PanedInterface(m)));
            m.getLowerRight().add(b);

            b = new JButton(new jmri.jmrit.powerpanel.PowerPanelAction(
                    "power", new PanedInterface(m)));
            m.getLowerRight().add(b);
        }
        m.setVisible(true);

        JFrame f = jmri.util.JmriJFrame.getFrame("Test of Multi Pane Window function");
        Assert.assertTrue("found frame", f != null);
        JUnitUtil.dispose(f);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
