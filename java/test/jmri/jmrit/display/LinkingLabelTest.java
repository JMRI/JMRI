package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * LinkingLabelTest.java
 * <p>
 * Description:
 *
 * @author	Bob Jacobsen
 */
public class LinkingLabelTest {

    LinkingLabel to = null;
    jmri.jmrit.display.panelEditor.PanelEditor panel;

    @Test
    public void testShow() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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

        Assert.assertEquals("Display Level ", to.getDisplayLevel(), jmri.jmrit.display.Editor.LABELS);

        to = new LinkingLabel("Target link", panel, "frame:LinkingLabel Target Panel");
        to.setBounds(120, 0, 80, 80);
        to.setDisplayLevel(jmri.jmrit.display.Editor.LABELS);
        panel.putItem(to);

        InstanceManager.getDefault(PanelMenu.class).addEditorPanel(panel);
        panel.setLocation(150, 150);

        panel.setTitle();

        panel.pack();
        panel.setVisible(true);

        // close the frame.
        JFrameOperator jfo = new JFrameOperator(jf);
        jfo.requestClose();
        jfo.waitClosed();

    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        if (panel != null) {
            // now close panel window
            java.awt.event.WindowListener[] listeners = panel.getTargetFrame().getWindowListeners();
            for (WindowListener listener : listeners) {
                panel.getTargetFrame().removeWindowListener(listener);
            }

            // close the frame.
            JFrameOperator jfo = new JFrameOperator(panel.getTargetFrame());
            jfo.requestClose();
        }
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TurnoutIconTest.class);
}
