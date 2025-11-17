package jmri.jmrit.display;

import static jmri.util.JUnitSwingUtil.assertImageNinePoints;
import static jmri.util.JUnitSwingUtil.assertPixel;
import static jmri.util.JUnitSwingUtil.getDisplayedContent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import jmri.*;
import jmri.jmrit.catalog.NamedIcon;
import jmri.util.JUnitSwingUtil;
import jmri.util.JUnitSwingUtil.Pixel;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JLabelOperator;

/**
 * Test of PositionableLabel
 * <p>
 * Includes tests
 * <ul>
 * <li>Image transparency and backgrounds
 * <li>Rotating icons and text
 * <li>Animated GIFs
 * </ul>
 * along with some combinations
 *
 * @author Bob Jacobsen Copyright 2015
 */
@DisabledIfHeadless
public class PositionableLabelTest extends PositionableTestBase {

    protected PositionableLabel to = null;

    @Test
    public void testSmallPanel() throws Positionable.DuplicateIdException {

        JFrame jf = new JFrame();
        JPanel jpanel = new JPanel();
        jf.getContentPane().add(jpanel);
        jpanel.setPreferredSize(new Dimension(200, 200));
        jpanel.setLayout(null);

        // test button in upper left
        JButton doButton = new JButton("change label");
        doButton.addActionListener((ActionEvent e) -> {
            if (to.getText().equals("one")) {
                to.setText("two");
            } else {
                to.setText("one");
            }
        });
        doButton.setBounds(0, 0, 120, 40);
        jpanel.add(doButton);

        to = new PositionableLabel("one", editor);
        to.setBounds(80, 80, 40, 40);
        editor.putItem(to);
        to.setDisplayLevel(Editor.LABELS);
        Assertions.assertEquals(Editor.LABELS, to.getDisplayLevel(), "Display Level ");

        jpanel.add(to);

        jf.pack();
        jf.setVisible(true);
    }

    // Load file showing four labels with backgrounds and make sure they have right color
    // The file used was written with 4.0.1, and behaves as expected from panel names
    @Test
    public void testBackgroundColorFile() throws JmriException {

        // make four windows
        InstanceManager.getDefault(ConfigureManager.class)
                .load(new File("java/test/jmri/jmrit/display/configurexml/valid/backgrounds.xml"));
        new QueueTool().waitEmpty(100);

        // Find color in label by frame name
        int color1 = getColor("F Bkg none, label Bkg none"); // transparent background

        int color2 = getColor("F Bkg blue, label Bkg none"); // transparent background shows blue

        int color3 = getColor("F Bkg none, label Bkg yellow"); // yellow

        int color4 = getColor("F Bkg blue, label Bkg yellow");

        assertPixel("F Bkg none, label Bkg none color", Pixel.TRANSPARENT, color1); // transparent shows neutral frame background
        assertPixel("F Bkg blue, label Bkg none color", Pixel.TRANSPARENT, color2); // no blue, looking at transparent label
        assertPixel("F Bkg none, label Bkg yellow color", Pixel.YELLOW, color3);
        assertPixel("F Bkg blue, label Bkg yellow color", Pixel.YELLOW, color4);
    }

    int getColor(String name) {

        new QueueTool().waitEmpty(100);

        // Find window by name
        JmriJFrame frame = JmriJFrame.getFrame(name);
        assertNotNull( frame, () -> "frame: " + name);

        // find label within that
        JLabel jl = JLabelOperator.findJLabel(frame, new ComponentChooser() {
            @Override
            public boolean checkComponent(Component comp) {
                if (comp == null) {
                    return false;
                } else {
                    return (comp instanceof JLabel);
                }
            }

            @Override
            public String getDescription() {
                return "find the first JLabel";
            }
        });

        int[] content = JUnitSwingUtil.getDisplayedContent(jl, jl.getSize(), new Point(0, 0));

        int color = content[0];

        // Unless in demo mode, close table window
        if (System.getProperty("jmri.demo", "false").equals("false")) {
            frame.setVisible(false);
        }
        return color;
    }

    // Explicit tests of PositionableLabel features
    @Test
    public void testDisplayTransparent() {

        JFrame f = new JFrame();
        f.getContentPane().setBackground(Color.blue);
        f.setUndecorated(true); // skip frame decoration, which can force a min size.

        NamedIcon icon = new NamedIcon("resources/icons/redTransparentBox.gif", "box"); // 13x13

        PositionableLabel label = new PositionableLabel(icon, editor);

        f.add(label);
        f.pack();
        new QueueTool().waitEmpty(100);
        assertEquals( new Dimension(13, 13).toString(), label.getSize().toString(),
            "icon size");

        int[] val = getDisplayedContent(label, label.getSize(), new Point(0, 0));

        assertEquals( 13 * 13, val.length, "icon arraylength");

        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.RED, Pixel.RED, Pixel.RED,
                Pixel.RED, Pixel.TRANSPARENT, Pixel.RED,
                Pixel.RED, Pixel.RED, Pixel.RED);

        // now check that background shows through
        // Need to find the icon location first
        Point point = SwingUtilities.convertPoint(label, 0, 0, f.getContentPane());

        val = getDisplayedContent(f.getContentPane(), label.getSize(), point);

        assertEquals( 13 * 13, val.length, "frame arraylength");

        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.RED, Pixel.RED, Pixel.RED,
                Pixel.RED, Pixel.BLUE, Pixel.RED,
                Pixel.RED, Pixel.RED, Pixel.RED);

        JUnitUtil.dispose(f);
    }

    @Test
    public void testDisplayTransparent45degrees() {

        JFrame f = new JFrame();
        f.getContentPane().setBackground(Color.blue);
        f.setUndecorated(true); // skip frame decoration, which can force a min size.

        NamedIcon icon = new NamedIcon("resources/icons/redTransparentBox.gif", "box"); // 13x13

        PositionableLabel label = new PositionableLabel(icon, editor);

        f.add(label);
        f.pack();
        new QueueTool().waitEmpty(100);
        assertEquals( new Dimension(13, 13).toString(),
            label.getSize().toString(), "icon size");

        // do the rotation, which transforms 13x13 to sqrt(2) bigger, 19x19
        label.rotate(45);
        assertEquals( new Dimension(19, 19).toString(),
            label.getSize().toString(), "icon size");

        f.pack();
        new QueueTool().waitEmpty(100);
        assertEquals( new Dimension(19, 19).toString(),
            label.getSize().toString(), "icon size");

        // and check
        int[] val = getDisplayedContent(label, label.getSize(), new Point(0, 0));

        assertEquals( 19 * 19, val.length, "icon arraylength");
        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.TRANSPARENT, Pixel.RED, Pixel.TRANSPARENT,
                Pixel.RED, Pixel.TRANSPARENT, Pixel.TRANSPARENT, // not sure why mid-right is TRANSPARENT; misaligned? Clipping?
                Pixel.TRANSPARENT, Pixel.TRANSPARENT, Pixel.TRANSPARENT); // not sure why bottom mid is TRANSPARENT; misaligned? Clipping?

        // now check that background shows through
        // Need to find the icon location first
        Point point = SwingUtilities.convertPoint(label, 0, 0, f.getContentPane());

        val = getDisplayedContent(f.getContentPane(), label.getSize(), point);

        assertEquals( 19 * 19, val.length, "frame arraylength");
        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.BLUE, Pixel.RED, Pixel.BLUE,
                Pixel.RED, Pixel.BLUE, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE);

        JUnitUtil.dispose(f);
    }

    // c.f. http://www.ssec.wisc.edu/~tomw/java/unicode.html#x2580
    final static String SAMPLE_TEXT_U25CF = "  \u25CF  "; // note spaces

    // FULL BLOCK \u2588
    // BLACK SQUARE \u25A0
    // BLACK CIRCLE \u25CF
    // BLACK SMALL SQUARE \u25AA
    // BLACK DIAMOND \u25C6
    // HEAVY MULTIPLICATION X \u2716
    @Test
    public void testDisplayText() {

        JFrame f = new JFrame();
        f.getContentPane().setBackground(Color.blue);
        f.setUndecorated(true); // skip frame decoration, which can force a min size.

        PositionableLabel label = new PositionableLabel(SAMPLE_TEXT_U25CF, editor);
        label.setForeground(Color.black); // this is a direct set, not through the UI
        label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        f.add(label);
        f.pack();
        new QueueTool().waitEmpty(100);

        assertTrue( label.getSize().width > label.getSize().height,
            () -> "Expect size " + label.getSize() + " wider than height");

        int[] val = getDisplayedContent(label, label.getSize(), new Point(0, 0));

        //for (int i=0; i<val.length; i++) System.out.println(" "+i+" "+String.format("0x%8s", Integer.toHexString(val[i])).replace(' ', '0'));
        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.TRANSPARENT, Pixel.TRANSPARENT, Pixel.TRANSPARENT,
                Pixel.TRANSPARENT, Pixel.BLACK, Pixel.TRANSPARENT,
                Pixel.TRANSPARENT, Pixel.TRANSPARENT, Pixel.TRANSPARENT);

        // now check that background shows through
        // Need to find the icon location first
        Point point = SwingUtilities.convertPoint(label, 0, 0, f.getContentPane());

        val = getDisplayedContent(f.getContentPane(), label.getSize(), point);

        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLACK, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE);

        JUnitUtil.dispose(f);
    }

    @Test
    public void testDisplayTextRotated90() {

        JFrame f = new JFrame();
        f.getContentPane().setBackground(Color.blue);
        f.setUndecorated(true); // skip frame decoration, which can force a min size.

        PositionableLabel label = new PositionableLabel(SAMPLE_TEXT_U25CF, editor);
        label.setForeground(Color.black); // this is a direct set, not through the UI
        label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        f.add(label);
        f.pack();
        new QueueTool().waitEmpty(100);

        label.rotate(90);

        f.pack();
        new QueueTool().waitEmpty(100);

        assertTrue( label.getSize().width < label.getSize().height,
            () -> "Expect size " + label.getSize() + " higher than width");

        int[] val = getDisplayedContent(label, label.getSize(), new Point(0, 0));

        //for (int i=0; i<val.length; i++) System.out.println(" "+i+" "+formatPixel(val[i]));
        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.TRANSPARENT, Pixel.TRANSPARENT, Pixel.TRANSPARENT,
                Pixel.TRANSPARENT, Pixel.BLACK, Pixel.TRANSPARENT,
                Pixel.TRANSPARENT, Pixel.TRANSPARENT, Pixel.TRANSPARENT);

        // now check that background shows through
        // Need to find the icon location first
        Point point = SwingUtilities.convertPoint(label, 0, 0, f.getContentPane());

        val = getDisplayedContent(f.getContentPane(), label.getSize(), point);

        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLACK, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE);

        JUnitUtil.dispose(f);
    }

    @Test
    public void testDisplayTextRotated45() {

        JFrame f = new JFrame();
        f.getContentPane().setBackground(Color.blue);
        f.setUndecorated(true); // skip frame decoration, which can force a min size.

        PositionableLabel label = new PositionableLabel(SAMPLE_TEXT_U25CF, editor);
        label.setForeground(Color.black); // this is a direct set, not through the UI
        label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        f.add(label);
        f.pack();
        new QueueTool().waitEmpty(100);

        label.rotate(45);

        f.pack();
        new QueueTool().waitEmpty(100);

        int[] val = getDisplayedContent(label, label.getSize(), new Point(0, 0));

        //for (int i=0; i<val.length; i++) System.out.println(" "+i+" "+String.format("0x%8s", Integer.toHexString(val[i])).replace(' ', '0'));
        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.TRANSPARENT, Pixel.TRANSPARENT, Pixel.TRANSPARENT,
                Pixel.TRANSPARENT, Pixel.BLACK, Pixel.TRANSPARENT,
                Pixel.TRANSPARENT, Pixel.TRANSPARENT, Pixel.TRANSPARENT);

        // now check that background shows through
        // Need to find the icon location first
        Point point = SwingUtilities.convertPoint(label, 0, 0, f.getContentPane());

        val = getDisplayedContent(f.getContentPane(), label.getSize(), point);

        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLACK, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE);

        JUnitUtil.dispose(f);
    }

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initDefaultUserMessagePreferences();

        editor = new EditorScaffold("PositionableLabel Test Panel");
        to = new PositionableLabel("one", editor);
        p = to;
        NamedIcon icon = new NamedIcon("resources/icons/redTransparentBox.gif", "box"); // 13x13
        to.setIcon(icon);

    }

    @Override
    @AfterEach
    public void tearDown() {
        to = null;
        super.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PositionableLabelTest.class);
}
