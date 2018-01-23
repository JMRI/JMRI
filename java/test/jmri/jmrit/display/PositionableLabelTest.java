package jmri.jmrit.display;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.ComponentChooser;

/**
 * Test of PositionableLabel
 * <p>
 * Includes tests <ul>
 * <li>Image transparency and backgrounds
 * <li>Rotating icons and text
 * <li>Animated GIFs
 * </ul>
 * along with some combinations
 *
 * @author Bob Jacobsen Copyright 2015
 */
public class PositionableLabelTest extends PositionableTestBase {

    PositionableLabel to = null;

    @Test
    public void testSmallPanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        editor = new EditorScaffold("PositionableLabel Test Panel");

        JFrame jf = new JFrame();
        JPanel p = new JPanel();
        jf.getContentPane().add(p);
        p.setPreferredSize(new Dimension(200, 200));
        p.setLayout(null);

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
        p.add(doButton);

        to = new PositionableLabel("one", editor);
        to.setBounds(80, 80, 40, 40);
        editor.putItem(to);
        to.setDisplayLevel(Editor.LABELS);
        Assert.assertEquals("Display Level ", to.getDisplayLevel(), Editor.LABELS);

        p.add(to);

        jf.pack();
        jf.setVisible(true);
    }

    // Load file showing four labels with backgrounds and make sure they have right color
    // The file used was written with 4.0.1, and behaves as expected from panel names
    @Test
    public void testBackgroundColorFile() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // make four windows
        InstanceManager.getDefault(ConfigureManager.class)
                .load(new File("java/test/jmri/jmrit/display/configurexml/verify/backgrounds.xml"));
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);

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

        new org.netbeans.jemmy.QueueTool().waitEmpty(100);

        // Find window by name
        JmriJFrame frame = JmriJFrame.getFrame(name);
        Assert.assertNotNull("frame: " + name, frame);

        // find label within that
        JLabel jl = JLabelOperator.findJLabel(frame,new ComponentChooser(){
               public boolean checkComponent(Component comp){
                   if(comp == null){
                      return false;
                   } else {
                     return (comp instanceof JLabel);
                   }
               }
               public String getDescription(){
                  return "find the first JLabel";
               }
        });

        int[] content = jmri.util.SwingTestCase.getDisplayedContent(jl, jl.getSize(), new Point(0, 0));

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
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JFrame f = new JFrame();
        f.getContentPane().setBackground(Color.blue);
        f.setUndecorated(true); // skip frame decoration, which can force a min size.

        NamedIcon icon = new NamedIcon("resources/icons/redTransparentBox.gif", "box"); // 13x13

        PositionableLabel label = new PositionableLabel(icon, null);

        f.add(label);
        f.pack();
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertEquals("icon size", new Dimension(13, 13).toString(), label.getSize().toString());

        int[] val = jmri.util.SwingTestCase.getDisplayedContent(label, label.getSize(), new Point(0, 0));

        Assert.assertEquals("icon arraylength", 13 * 13, val.length);

        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.RED, Pixel.RED, Pixel.RED,
                Pixel.RED, Pixel.TRANSPARENT, Pixel.RED,
                Pixel.RED, Pixel.RED, Pixel.RED);

        // now check that background shows through
        // Need to find the icon location first
        Point p = SwingUtilities.convertPoint(label, 0, 0, f.getContentPane());

        val = jmri.util.SwingTestCase.getDisplayedContent(f.getContentPane(), label.getSize(), p);

        Assert.assertEquals("frame arraylength", 13 * 13, val.length);

        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.RED, Pixel.RED, Pixel.RED,
                Pixel.RED, Pixel.BLUE, Pixel.RED,
                Pixel.RED, Pixel.RED, Pixel.RED);

        JUnitUtil.dispose(f);
    }

    @Test
    public void testDisplayTransparent45degrees() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JFrame f = new JFrame();
        f.getContentPane().setBackground(Color.blue);
        f.setUndecorated(true); // skip frame decoration, which can force a min size.

        NamedIcon icon = new NamedIcon("resources/icons/redTransparentBox.gif", "box"); // 13x13

        PositionableLabel label = new PositionableLabel(icon, null);

        f.add(label);
        f.pack();
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertEquals("icon size", new Dimension(13, 13).toString(), label.getSize().toString());

        // do the rotation, which transforms 13x13 to sqrt(2) bigger, 19x19
        label.rotate(45);
        Assert.assertEquals("icon size", new Dimension(19, 19).toString(), label.getSize().toString());

        f.pack();
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertEquals("icon size", new Dimension(19, 19).toString(), label.getSize().toString());

        // and check
        int[] val = jmri.util.SwingTestCase.getDisplayedContent(label, label.getSize(), new Point(0, 0));

        Assert.assertEquals("icon arraylength", 19 * 19, val.length);
        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.TRANSPARENT, Pixel.RED, Pixel.TRANSPARENT,
                Pixel.RED, Pixel.TRANSPARENT, Pixel.TRANSPARENT, // not sure why mid-right is TRANSPARENT; misaligned? Clipping?
                Pixel.TRANSPARENT, Pixel.TRANSPARENT, Pixel.TRANSPARENT); // not sure why bottom mid is TRANSPARENT; misaligned? Clipping?

        // now check that background shows through
        // Need to find the icon location first
        Point p = SwingUtilities.convertPoint(label, 0, 0, f.getContentPane());

        val = jmri.util.SwingTestCase.getDisplayedContent(f.getContentPane(), label.getSize(), p);

        Assert.assertEquals("frame arraylength", 19 * 19, val.length);
        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.BLUE, Pixel.RED, Pixel.BLUE,
                Pixel.RED, Pixel.BLUE, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE);

        JUnitUtil.dispose(f);
    }

    // test with an RGB animated 13x13 GIF, 0.1 sec per frame
    @Test
    public void testDisplayAnimatedRGB() throws IOException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        if (System.getProperty("jmri.migrationtests", "false").equals("false")) { // skip test for migration, but warn about it
            log.info("skipping testDisplayAnimatedRGB because jmri.migrationtests not set true");
            return;
        }

        if (System.getProperty("jmri.migrationtests", "false").equals("false")) { // skip test for migration, but warn about it
            log.warn("skipping testDisplayAnimatedRGB because jmri.migrationtests not set true");
            return;
        }

        JFrame f = new JFrame();
        f.getContentPane().setBackground(Color.blue);
        f.setUndecorated(true); // skip frame decoration, which can force a min size.

        NamedIcon icon = new NamedIcon("resources/icons/RGB-animated-once-Square.gif", "box"); // 13x13

        PositionableLabel label = new PositionableLabel(icon, null);

        f.add(label);
        f.pack();
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertEquals("icon size", new Dimension(13, 13).toString(), label.getSize().toString());

        // wait for a bit
        f.setVisible(true);  // needed to get initial animation contents
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);

        // check for initial red
        int[] val = jmri.util.SwingTestCase.getDisplayedContent(label, label.getSize(), new Point(0, 0));

        Assert.assertEquals("icon arraylength", 13 * 13, val.length);

        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.RED, Pixel.RED, Pixel.RED,
                Pixel.RED, Pixel.RED, Pixel.RED,
                Pixel.RED, Pixel.RED, Pixel.RED);

        // Need to find the icon location in frame first
        Point p = SwingUtilities.convertPoint(label, 0, 0, f.getContentPane());

        val = jmri.util.SwingTestCase.getDisplayedContent(f.getContentPane(), label.getSize(), p);

        Assert.assertEquals("frame arraylength", 13 * 13, val.length);

        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.RED, Pixel.RED, Pixel.RED,
                Pixel.RED, Pixel.RED, Pixel.RED,
                Pixel.RED, Pixel.RED, Pixel.RED);

        // wait for long enough to reach final red, skipping intermediate green as timing too fussy
        new org.netbeans.jemmy.QueueTool().waitEmpty(250);

        val = jmri.util.SwingTestCase.getDisplayedContent(label, label.getSize(), new Point(0, 0));

        Assert.assertEquals("icon arraylength", 13 * 13, val.length);

        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE);

        // now check that background shows through
        // Need to find the icon location first
        p = SwingUtilities.convertPoint(label, 0, 0, f.getContentPane());

        val = jmri.util.SwingTestCase.getDisplayedContent(f.getContentPane(), label.getSize(), p);

        Assert.assertEquals("frame arraylength", 13 * 13, val.length);
        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE);

        // finally done
        JUnitUtil.dispose(f);
    }

    // test with an RGB animated 13x13 GIF, 0.1 sec per frame, rotate
    @Test
    public void testDisplayAnimatedRGBrotated45degrees() throws IOException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        if (System.getProperty("jmri.migrationtests", "false").equals("false")) { // skip test for migration, but warn about it
            log.info("skipping testDisplayAnimatedRGBrotated45degrees because jmri.migrationtests not set true");
            return;
        }

        JFrame f = new JFrame();
        f.getContentPane().setBackground(Color.green);
        f.setUndecorated(true); // skip frame decoration, which can force a min size.

        NamedIcon icon = new NamedIcon("resources/icons/RGB-animated-once-Square2.gif", "box"); // 13x13

        PositionableLabel label = new PositionableLabel(icon, null);

        f.add(label);
        f.pack();
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertEquals("icon size", new Dimension(13, 13).toString(), label.getSize().toString());

        // do the rotation, which transforms 13x13 to sqrt(2) bigger, 19x19
        label.rotate(45);
        Assert.assertEquals("icon size", new Dimension(19, 19).toString(), label.getSize().toString());

        f.pack();
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertEquals("icon size", new Dimension(19, 19).toString(), label.getSize().toString());

        // wait for a bit
        f.setVisible(true);  // needed to get initial animation contents
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);

        // and check
        int[] val = jmri.util.SwingTestCase.getDisplayedContent(label, label.getSize(), new Point(0, 0));

        Assert.assertEquals("icon arraylength", 19 * 19, val.length);

        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.TRANSPARENT, Pixel.RED, Pixel.TRANSPARENT,
                Pixel.RED, Pixel.RED, Pixel.RED,
                Pixel.TRANSPARENT, Pixel.RED, Pixel.TRANSPARENT);

        // now check that background shows through
        // Need to find the icon location first
        Point p = SwingUtilities.convertPoint(label, 0, 0, f.getContentPane());

        val = jmri.util.SwingTestCase.getDisplayedContent(f.getContentPane(), label.getSize(), p);

        Assert.assertEquals("frame arraylength", 19 * 19, val.length);
        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.GREEN, Pixel.RED, Pixel.GREEN,
                Pixel.RED, Pixel.RED, Pixel.RED,
                Pixel.GREEN, Pixel.RED, Pixel.GREEN);

        // wait for long enough to reach final blue, skipping intermediate green as timing too fussy
        new org.netbeans.jemmy.QueueTool().waitEmpty(250);

        // and check
        val = jmri.util.SwingTestCase.getDisplayedContent(label, label.getSize(), new Point(0, 0));

        Assert.assertEquals("icon arraylength", 19 * 19, val.length);
        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.TRANSPARENT, Pixel.BLUE, Pixel.TRANSPARENT,
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE,
                Pixel.TRANSPARENT, Pixel.BLUE, Pixel.TRANSPARENT);

        // now check that background shows through
        // Need to find the icon location first
        p = SwingUtilities.convertPoint(label, 0, 0, f.getContentPane());

        val = jmri.util.SwingTestCase.getDisplayedContent(f.getContentPane(), label.getSize(), p);

        Assert.assertEquals("frame arraylength", 19 * 19, val.length);
        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.GREEN, Pixel.BLUE, Pixel.GREEN,
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE,
                Pixel.GREEN, Pixel.BLUE, Pixel.GREEN);

        JUnitUtil.dispose(f);
    }

    // c.f. http://www.ssec.wisc.edu/~tomw/java/unicode.html#x2580
    final String sampleText = "  \u25CF  "; // note spaces

    // FULL BLOCK \u2588
    // BLACK SQUARE \u25A0
    // BLACK CIRCLE \u25CF
    // BLACK SMALL SQUARE \u25AA
    // BLACK DIAMOND \u25C6
    // HEAVY MULTIPLICATION X \u2716
    @Test
    public void testDisplayText() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JFrame f = new JFrame();
        f.getContentPane().setBackground(Color.blue);
        f.setUndecorated(true); // skip frame decoration, which can force a min size.

        PositionableLabel label = new PositionableLabel(sampleText, null);
        label.setForeground(Color.black); // this is a direct set, not through the UI
        label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        f.add(label);
        f.pack();
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);

        Assert.assertTrue("Expect size " + label.getSize() + " wider than height", label.getSize().width > label.getSize().height);

        int[] val = jmri.util.SwingTestCase.getDisplayedContent(label, label.getSize(), new Point(0, 0));

        //for (int i=0; i<val.length; i++) System.out.println(" "+i+" "+String.format("0x%8s", Integer.toHexString(val[i])).replace(' ', '0'));
        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.TRANSPARENT, Pixel.TRANSPARENT, Pixel.TRANSPARENT,
                Pixel.TRANSPARENT, Pixel.BLACK, Pixel.TRANSPARENT,
                Pixel.TRANSPARENT, Pixel.TRANSPARENT, Pixel.TRANSPARENT);

        // now check that background shows through
        // Need to find the icon location first
        Point p = SwingUtilities.convertPoint(label, 0, 0, f.getContentPane());

        val = jmri.util.SwingTestCase.getDisplayedContent(f.getContentPane(), label.getSize(), p);

        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLACK, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE);

        JUnitUtil.dispose(f);
    }

    @Test
    public void testDisplayTextRotated90() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JFrame f = new JFrame();
        f.getContentPane().setBackground(Color.blue);
        f.setUndecorated(true); // skip frame decoration, which can force a min size.

        PositionableLabel label = new PositionableLabel(sampleText, null);
        label.setForeground(Color.black); // this is a direct set, not through the UI
        label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        f.add(label);
        f.pack();
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);

        label.rotate(90);

        f.pack();
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);

        Assert.assertTrue("Expect size " + label.getSize() + " higher than width", label.getSize().width < label.getSize().height);

        int[] val = jmri.util.SwingTestCase.getDisplayedContent(label, label.getSize(), new Point(0, 0));

        //for (int i=0; i<val.length; i++) System.out.println(" "+i+" "+formatPixel(val[i]));
        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.TRANSPARENT, Pixel.TRANSPARENT, Pixel.TRANSPARENT,
                Pixel.TRANSPARENT, Pixel.BLACK, Pixel.TRANSPARENT,
                Pixel.TRANSPARENT, Pixel.TRANSPARENT, Pixel.TRANSPARENT);

        // now check that background shows through
        // Need to find the icon location first
        Point p = SwingUtilities.convertPoint(label, 0, 0, f.getContentPane());

        val = jmri.util.SwingTestCase.getDisplayedContent(f.getContentPane(), label.getSize(), p);

        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLACK, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE);

        JUnitUtil.dispose(f);
    }

    @Test
    public void testDisplayTextRotated45() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JFrame f = new JFrame();
        f.getContentPane().setBackground(Color.blue);
        f.setUndecorated(true); // skip frame decoration, which can force a min size.

        PositionableLabel label = new PositionableLabel(sampleText, null);
        label.setForeground(Color.black); // this is a direct set, not through the UI
        label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        f.add(label);
        f.pack();
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);

        label.rotate(45);

        f.pack();
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);

        int[] val = jmri.util.SwingTestCase.getDisplayedContent(label, label.getSize(), new Point(0, 0));

        //for (int i=0; i<val.length; i++) System.out.println(" "+i+" "+String.format("0x%8s", Integer.toHexString(val[i])).replace(' ', '0'));
        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.TRANSPARENT, Pixel.TRANSPARENT, Pixel.TRANSPARENT,
                Pixel.TRANSPARENT, Pixel.BLACK, Pixel.TRANSPARENT,
                Pixel.TRANSPARENT, Pixel.TRANSPARENT, Pixel.TRANSPARENT);

        // now check that background shows through
        // Need to find the icon location first
        Point p = SwingUtilities.convertPoint(label, 0, 0, f.getContentPane());

        val = jmri.util.SwingTestCase.getDisplayedContent(f.getContentPane(), label.getSize(), p);

        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLACK, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE);

        JUnitUtil.dispose(f);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        if(!GraphicsEnvironment.isHeadless()) {
           editor = new EditorScaffold();
           p = to = new PositionableLabel("one", editor);
           NamedIcon icon = new NamedIcon("resources/icons/redTransparentBox.gif", "box"); // 13x13
           to.setIcon(icon);
        }
    }

    // All of the Pixel enum, and related methods, below was copied from 
    // jmri.util.SwingTestCase.  The tests above use Pixel values extensively. 
    protected enum Pixel { // protected to limit leakage outside Swing tests

        TRANSPARENT(0x00000000),
        RED(0xFFFF0000),
        GREEN(0xFF00FF00),
        BLUE(0xFF0000FF),
        WHITE(0xFFFFFFFF),
        BLACK(0xFF000000),
        YELLOW(0xFFFFFF00);

        @Override
        public String toString() {
            return formatPixel(value);
        }

        public boolean equals(int v) {
            return value == v;
        }
        private final int value;

        private Pixel(int value) {
            this.value = value;
        }
    }

    /**
     * Standard parsing of ARCG pixel (int) value to String
     */
    public static String formatPixel(int pixel) {
        return String.format("0x%8s", Integer.toHexString(pixel)).replace(' ', '0');
    }

    /**
     * Clean way to assert against a pixel value.
     *
     * @param name  Condition being asserted
     * @param value Correct ARGB value for test
     * @param pixel ARGB piel value being tested
     */
    protected static void assertPixel(String name, Pixel value, int pixel) {
        Assert.assertEquals(name, value.toString(), formatPixel(pixel));
    }

    /**
     * Check four corners and center of an image
     *
     * @param name   Condition being asserted
     * @param pixels Image ARCG array
     */
    protected static void assertImageNinePoints(String name, int[] pixels, Dimension size,
            Pixel upperLeft, Pixel upperCenter, Pixel upperRight,
            Pixel midLeft, Pixel center, Pixel midRight,
            Pixel lowerLeft, Pixel lowerCenter, Pixel lowerRight
    ) {
        int rows = size.height;
        int cols = size.width;

        Assert.assertEquals("size consistency", pixels.length, rows * cols);

        assertPixel(name + " upper left", upperLeft, pixels[0]);
        assertPixel(name + " upper middle", upperCenter, pixels[0 + cols / 2]);
        assertPixel(name + " upper right", upperRight, pixels[0 + (cols - 1)]);

        assertPixel(name + " middle left", midLeft, pixels[(rows / 2) * cols]);
        assertPixel(name + " middle right", midRight, pixels[(rows / 2) * cols + (cols - 1)]);

        assertPixel(name + " lower left", lowerLeft, pixels[(rows * cols - 1) - (cols - 1)]);
        assertPixel(name + " lower middle", lowerCenter, pixels[(rows * cols - 1) - (cols - 1) + cols / 2]);
        assertPixel(name + " lower right", lowerRight, pixels[rows * cols - 1]);

        // we've checked the corners first on purpose, to see they're all right
        assertPixel(name + " center", center, pixels[(rows / 2) * cols + cols / 2]);

    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PositionableLabelTest.class);
}
