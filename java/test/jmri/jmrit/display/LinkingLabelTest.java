package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.jmrit.catalog.NamedIcon;
import org.junit.*;

/**
 * LinkingLabelTest.java
 * <p>
 * Description:
 *
 * @author Bob Jacobsen
 */
public class LinkingLabelTest extends PositionableTestBase {

    private LinkingLabel to = null;

    @Override
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

    @Test
    public void testGetAndSetURL() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LinkingLabel l = (LinkingLabel) p;
        Assert.assertEquals("URL before set", "http://jmri.org", l.getURL());
        l.setULRL("bar");
        Assert.assertEquals("URL after set", "bar", l.getURL());
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        super.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            editor = new jmri.jmrit.display.panelEditor.PanelEditor("LinkingLabel Test Panel");
            p = to = new LinkingLabel("JMRI Link", editor, "http://jmri.org");
            NamedIcon icon = new NamedIcon("resources/icons/redTransparentBox.gif", "box"); // 13x13
            to.setIcon(icon);
        }
    }

    @Override
    @After
    public void tearDown() {
        to = null;
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TurnoutIconTest.class);
}
