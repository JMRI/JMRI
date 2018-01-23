package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import jmri.jmrit.catalog.NamedIcon;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * LinkingLabelTest.java
 * <p>
 * Description:
 *
 * @author	Bob Jacobsen
 */
public class LinkingLabelTest extends PositionableTestBase {

    private LinkingLabel to = null;

    @Test
    public void testShow() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JFrame jf = new jmri.util.JmriJFrame("LinkingLabel Target Panel");
        JPanel p = new JPanel();
        jf.getContentPane().add(p);
        p.add(new JLabel("Open 'Linking Label Test Panel' from Window menu on Editor"));
        p.setLayout(new java.awt.FlowLayout());
        jf.pack();
        jf.setVisible(true);

        to.setBounds(0, 0, 80, 80);
        editor.putItem(to);
        to.setDisplayLevel(jmri.jmrit.display.Editor.LABELS);

        Assert.assertEquals("Display Level ", to.getDisplayLevel(), jmri.jmrit.display.Editor.LABELS);

        to = new LinkingLabel("Target link", editor, "frame:LinkingLabel Target Panel");
        to.setBounds(120, 0, 80, 80);
        to.setDisplayLevel(jmri.jmrit.display.Editor.LABELS);
        editor.putItem(to);

        InstanceManager.getDefault(PanelMenu.class).addEditorPanel(editor);
        editor.setLocation(150, 150);

        editor.setTitle();

        editor.pack();
        editor.setVisible(true);

        // close the frame.
        EditorFrameOperator jfo = new EditorFrameOperator(jf);
        jfo.requestClose();
        jfo.waitClosed();

    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
           editor = new jmri.jmrit.display.panelEditor.PanelEditor("LinkingLabel Test Panel");
           p = to = new LinkingLabel("JMRI Link", editor, "http://jmri.org");
           NamedIcon icon = new NamedIcon("resources/icons/redTransparentBox.gif", "box"); // 13x13
           to.setIcon(icon);
        }
    }

    // private final static Logger log = LoggerFactory.getLogger(TurnoutIconTest.class);
}
