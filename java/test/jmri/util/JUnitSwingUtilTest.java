package jmri.util;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

import jmri.util.JUnitSwingUtil.Pixel;

/**
 * Tests for the jmri.util.JUnitSwingUtil class.
 *
 * @author Bob Jacobsen Copyright 2009
 */
public class JUnitSwingUtilTest {

    /**
     * Simple test of creating a Swing frame with a checkbox, checking the box,
     * and seeing that the check changed its state.
     */
    @Test
    public void testCheckBox() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // create a little GUI with a single check box
        JFrame f = new JFrame("SwingTextCaseTest");
        f.setSize(100, 100); // checkbox must be visible for test to work
        JCheckBox b = new JCheckBox("Check");
        b.setName("Check");
        f.add(b);
        f.setVisible(true);

        // find the check box and confirm not yet checked
        JCheckBox testBox = JCheckBoxOperator.findJCheckBox(f, new NameComponentChooser("Check"));
        Assert.assertNotNull(testBox);
        Assert.assertTrue(!testBox.isSelected());

        // set the check in the box by clicking it
        new JCheckBoxOperator(testBox).doClick();

        // test for selected
        Assert.assertTrue(testBox.isSelected());

        JUnitUtil.dispose(f);
    }

    /**
     * Test formatting
     */
    @Test
    public void testFormatPixel() {
        Assert.assertEquals("0x00000000", JUnitSwingUtil.formatPixel(0));
        Assert.assertEquals("0x00000001", JUnitSwingUtil.formatPixel(1));
        Assert.assertEquals("0xffffffff", JUnitSwingUtil.formatPixel(0xffffffff));
        Assert.assertEquals("0xffffff0f", JUnitSwingUtil.formatPixel(0xffffff0f));
    }

    @Test
    public void testAssertPixel() {
        JUnitSwingUtil.assertPixel("test RED", Pixel.RED, 0xffff0000);
    }

    @Test
    public void testAssertImageNinePoints() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // special target to make sure we're doing the right points
        JFrame f = new JFrame();
        f.getContentPane().setBackground(java.awt.Color.blue);
        f.setUndecorated(true); // skip frame decoration, which can force a min size.

        JLabel wIcon = new JLabel(new ImageIcon("resources/icons/CornerBits.gif")); // 13 high 39 wide
        wIcon.setOpaque(false);

        f.add(wIcon);
        f.pack();
        new QueueTool().waitEmpty();
        Assert.assertEquals("icon size", new Dimension(39, 13).toString(), wIcon.getSize().toString());

        int[] val = JUnitSwingUtil.getDisplayedContent(wIcon, wIcon.getSize(), new Point(0, 0));

        Assert.assertEquals("icon arraylength", 39 * 13, val.length);

        JUnitSwingUtil.assertImageNinePoints("test image", val, wIcon.getSize(),
                Pixel.RED, Pixel.GREEN, Pixel.BLUE,
                Pixel.GREEN, Pixel.BLUE, Pixel.RED,
                Pixel.RED, Pixel.GREEN, Pixel.BLUE);

        JUnitUtil.dispose(f);
    }

    /**
     * Confirm methodology to test the content of a JLabel
     */
    @Test
    public void testGetDisplayedContentGreen() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame f = new JFrame();
        f.setUndecorated(true); // skip frame decoration, which can force a min size.

        JLabel wIcon = new JLabel(new ImageIcon("resources/icons/greenSquare.gif")); // 13x13
        f.add(wIcon);
        f.pack();
        new QueueTool().waitEmpty();

        Assert.assertEquals("icon size", new Dimension(13, 13).toString(), wIcon.getSize().toString());

        int[] val = JUnitSwingUtil.getDisplayedContent(wIcon, wIcon.getSize(), new Point(0, 0));

        Assert.assertEquals("icon arraylength", 13 * 13, val.length);

        JUnitSwingUtil.assertPixel("icon first", Pixel.GREEN, val[0]);
        JUnitSwingUtil.assertPixel("icon middle", Pixel.GREEN,
                val[(int) Math.floor(wIcon.getSize().height / 2) * wIcon.getSize().width +
                        (int) Math.floor(wIcon.getSize().width / 2) -
                        1]);
        JUnitSwingUtil.assertPixel("icon last", Pixel.GREEN, val[wIcon.getSize().height * wIcon.getSize().width - 1]);

        Assert.assertEquals("icon first", "0xff00ff00", JUnitSwingUtil.formatPixel(val[0])); // compare strings to make error readable

        JUnitSwingUtil.assertImageNinePoints("icon", val, wIcon.getSize(),
                Pixel.GREEN, Pixel.GREEN, Pixel.GREEN,
                Pixel.GREEN, Pixel.GREEN, Pixel.GREEN,
                Pixel.GREEN, Pixel.GREEN, Pixel.GREEN);

        JUnitUtil.dispose(f);

    }

    /**
     * Confirm methodology to test transparent pixels in a JLabel
     */
    @Test
    public void testGetDisplayedContentRedTransparentBkg() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame f = new JFrame();
        f.setUndecorated(true); // skip frame decoration, which can force a min size.

        JLabel wIcon = new JLabel(new ImageIcon("resources/icons/redTransparentBox.gif")); // 13x13
        wIcon.setOpaque(true);
        wIcon.setBackground(java.awt.Color.blue);

        f.add(wIcon);
        f.pack();
        new QueueTool().waitEmpty();
        Assert.assertEquals("icon size", new Dimension(13, 13).toString(), wIcon.getSize().toString());

        int[] val = JUnitSwingUtil.getDisplayedContent(wIcon, wIcon.getSize(), new Point(0, 0));

        Assert.assertEquals("icon arraylength", 13 * 13, val.length);

        JUnitSwingUtil.assertPixel("icon first", Pixel.RED, val[0]);
        JUnitSwingUtil.assertPixel("icon middle", Pixel.BLUE,
                val[(int) Math.floor(wIcon.getSize().height / 2) * wIcon.getSize().width +
                        (int) Math.floor(wIcon.getSize().width / 2) -
                        1]);
        JUnitSwingUtil.assertPixel("icon last", Pixel.RED, val[wIcon.getSize().height * wIcon.getSize().width - 1]);

        Assert.assertEquals("icon first", "0xffff0000", JUnitSwingUtil.formatPixel(val[0])); // compare strings to make error readable

        JUnitSwingUtil.assertImageNinePoints("icon", val, wIcon.getSize(),
                Pixel.RED, Pixel.RED, Pixel.RED,
                Pixel.RED, Pixel.BLUE, Pixel.RED,
                Pixel.RED, Pixel.RED, Pixel.RED);

        JUnitUtil.dispose(f);
    }

    @Test
    public void testGetDisplayedContentRedTransparentTransp() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame f = new JFrame();
        f.getContentPane().setBackground(java.awt.Color.blue);
        f.setUndecorated(true); // skip frame decoration, which can force a min size.

        JLabel wIcon = new JLabel(new ImageIcon("resources/icons/redTransparentBox.gif")); // 13x13
        wIcon.setOpaque(false);

        f.add(wIcon);
        f.pack();
        new QueueTool().waitEmpty();
        Assert.assertEquals("icon", new Dimension(13, 13).toString(), wIcon.getSize().toString());

        int[] val = JUnitSwingUtil.getDisplayedContent(wIcon, wIcon.getSize(), new Point(0, 0));

        JUnitSwingUtil.assertImageNinePoints("test image", val, wIcon.getSize(),
                Pixel.RED, Pixel.RED, Pixel.RED,
                Pixel.RED, Pixel.TRANSPARENT, Pixel.RED,
                Pixel.RED, Pixel.RED, Pixel.RED);

        // now check that background shows through
        // Need to find the icon location first
        Point p = SwingUtilities.convertPoint(wIcon, 0, 0, f.getContentPane());

        val = JUnitSwingUtil.getDisplayedContent(f.getContentPane(), wIcon.getSize(), p);
        Assert.assertEquals("frame arraylength", 13 * 13, val.length);

        JUnitSwingUtil.assertImageNinePoints("frame", val, wIcon.getSize(),
                Pixel.RED, Pixel.RED, Pixel.RED,
                Pixel.RED, Pixel.BLUE, Pixel.RED,
                Pixel.RED, Pixel.RED, Pixel.RED);

        JUnitUtil.dispose(f);
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JUnitSwingUtilTest.class);
}
