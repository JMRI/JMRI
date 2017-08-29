package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * LinkingLabelTest.java
 * <p>
 * Description:
 *
 * @author	Bob Jacobsen
 */
public class LinkingLabelTest extends jmri.util.SwingTestCase {

    LinkingLabel to = null;
    jmri.jmrit.display.panelEditor.PanelEditor panel;

    public void testShow() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }
        panel = new jmri.jmrit.display.panelEditor.PanelEditor("LinkingLabel Test Panel");

        JFrame jf = new jmri.util.JmriJFrame("LinkingLabel Target Panel");
        JPanel p = new JPanel();
        jf.getContentPane().add(p);
        p.add(new JLabel("Open 'Linking Label Test Panel' from Window menu on Editor"));
        p.setLayout(new java.awt.FlowLayout());
        jf.pack();
        jf.setVisible(true);

        to = new LinkingLabel("JMRI Link", panel, "http://jmri.org");
        to.setBounds(0, 0, 80, 80);
        panel.putItem(to);
        to.setDisplayLevel(jmri.jmrit.display.Editor.LABELS);

        assertEquals("Display Level ", to.getDisplayLevel(), jmri.jmrit.display.Editor.LABELS);

        to = new LinkingLabel("Target link", panel, "frame:LinkingLabel Target Panel");
        to.setBounds(120, 0, 80, 80);
        to.setDisplayLevel(jmri.jmrit.display.Editor.LABELS);
        panel.putItem(to);

        InstanceManager.getDefault(PanelMenu.class).addEditorPanel(panel);
        panel.setLocation(150, 150);

        panel.setTitle();

        panel.pack();
        panel.setVisible(true);

        JUnitUtil.dispose(jf);
        JUnitUtil.dispose(panel);

    }

    // from here down is testing infrastructure
    public LinkingLabelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LinkingLabelTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LinkingLabelTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        if (panel != null) {
            // now close panel window
            java.awt.event.WindowListener[] listeners = panel.getTargetFrame().getWindowListeners();
            for (WindowListener listener : listeners) {
                panel.getTargetFrame().removeWindowListener(listener);
            }
            junit.extensions.jfcunit.TestHelper.disposeWindow(panel.getTargetFrame(), this);

            panel = null;
            JUnitUtil.resetWindows(false, false);  // don't log here.  should be from this class.
        }
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TurnoutIconTest.class.getName());
}
