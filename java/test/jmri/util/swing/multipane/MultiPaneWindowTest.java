package jmri.util.swing.multipane;

import javax.swing.JButton;
import javax.swing.JFrame;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;
import jmri.util.swing.ButtonTestAction;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Invokes complete set of tests in the jmri.util tree
 *
 * @author Bob Jacobsen Copyright 2003
 */
public class MultiPaneWindowTest {

    @Test
    @DisabledIfHeadless
    public void testShow() {
        new MultiPaneWindow("Test of empty Multi Pane Window",
                "xml/config/apps/panelpro/Gui3LeftTree.xml",
                "xml/config/apps/panelpro/Gui3Menus.xml",
                "xml/config/apps/panelpro/Gui3MainToolBar.xml"
        ).setVisible(true);

        JFrame f = jmri.util.JmriJFrame.getFrame("Test of empty Multi Pane Window");
        assertNotNull( f, "found frame");
        JUnitUtil.dispose(f);
    }

    @Test
    @DisabledIfHeadless
    public void testAction() {
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
        assertNotNull( f, "found frame");
        JUnitUtil.dispose(f);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
