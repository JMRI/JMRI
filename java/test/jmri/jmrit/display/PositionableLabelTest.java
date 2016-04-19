
package jmri.jmrit.display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import jmri.InstanceManager;
import jmri.util.JmriJFrame;
import junit.extensions.jfcunit.finder.JLabelFinder;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test of PositionableLabel
 * 
 * Includes tests <ul>
 * <li>Image transparency and backgrounds
 * <li>Rotating icons and text
 * <li>Animated GIFs
 * </ul>
 * along with some combinations
 *
 * @author	Bob Jacobsen   Copyright 2015
 */
public class PositionableLabelTest extends jmri.util.SwingTestCase {

    PositionableLabel to = null;
    jmri.jmrit.display.panelEditor.PanelEditor panel;

    public void testSmallPanel() {
        if (!System.getProperty("jmri.headlesstest","false").equals("false")) { return; }
        
        panel = new jmri.jmrit.display.panelEditor.PanelEditor("PositionableLabel Test Panel");
        
        JFrame jf = new JFrame();
        JPanel p = new JPanel();
        jf.getContentPane().add(p);
        p.setPreferredSize(new Dimension(200, 200));
        p.setLayout(null);

        // test button in upper left
        JButton doButton = new JButton("change label");
        doButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (to.getText().equals("one"))
                    to.setText("two");
                else 
                    to.setText("one");
            }
        });
        doButton.setBounds(0, 0, 120, 40);
        p.add(doButton);

        to = new PositionableLabel("one", panel);
        to.setBounds(80, 80, 40, 40);
        panel.putItem(to);
        to.setDisplayLevel(jmri.jmrit.display.Editor.LABELS);
        assertEquals("Display Level ", to.getDisplayLevel(), jmri.jmrit.display.Editor.LABELS);

        p.add(to);

        jf.pack();
        jf.setVisible(true);
    }
    
    // Load file showing four labels with backgrounds and make sure they have right color
    // The file used was written with 4.0.1, and behaves as expected from panel names
    public void testBackgroundColorFile() throws Exception {
        if (!System.getProperty("jmri.headlesstest","false").equals("false")) { return; }
        
        // make four windows
        InstanceManager.configureManagerInstance()
                .load(new java.io.File("java/test/jmri/jmrit/display/configurexml/verify/backgrounds.xml"));
        flushAWT();

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

        flushAWT();

        // Find window by name
        JmriJFrame ft = JmriJFrame.getFrame(name);
        Assert.assertNotNull("frame: "+name, ft);
        
        // find label within that
        JLabelFinder finder = new JLabelFinder("....");
        java.util.List list = finder.findAll(ft);
        Assert.assertNotNull("list: "+name, list);
        Assert.assertTrue("length: "+name+": "+list.size(), list.size()>0);
                
        JComponent component = (JComponent)list.get(0);
        int[] content = getDisplayedContent(component, component.getSize(), new Point(0,0));
        
        int color = content[0];
        
        // Unless in demo mode, close table window
        if (System.getProperty("jmri.demo", "false").equals("false")) {
            ft.setVisible(false);
        }
        return color;
    }
    
    // Explicit tests of PositionableLabel features
    
    public void testDisplayTransparent() {
        if (!System.getProperty("jmri.headlesstest","false").equals("false")) { return; }
    
        JFrame f = new JFrame();
        f.getContentPane().setBackground(Color.blue);
        f.setUndecorated(true); // skip frame decoration, which can force a min size.
        
        jmri.jmrit.catalog.NamedIcon icon = new jmri.jmrit.catalog.NamedIcon("resources/icons/redTransparentBox.gif","box"); // 13x13
        
        PositionableLabel label = new PositionableLabel(icon, null);
        
        f.add(label);
        f.pack();
        flushAWT();
        Assert.assertEquals("icon size", new Dimension(13,13).toString(),label.getSize().toString());
         
        int[] val = getDisplayedContent(label, label.getSize(), new Point(0,0));
        
        Assert.assertEquals("icon arraylength", 13*13, val.length);
        
        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.RED, Pixel.RED, Pixel.RED,
                Pixel.RED, Pixel.TRANSPARENT, Pixel.RED,
                Pixel.RED, Pixel.RED, Pixel.RED);
                        
        // now check that background shows through
        // Need to find the icon location first
        Point p = SwingUtilities.convertPoint(label,0,0,f.getContentPane());
        
        val = getDisplayedContent(f.getContentPane(), label.getSize(), p);

        Assert.assertEquals("frame arraylength", 13*13, val.length);

        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.RED, Pixel.RED, Pixel.RED,
                Pixel.RED, Pixel.BLUE, Pixel.RED,
                Pixel.RED, Pixel.RED, Pixel.RED);
                        
        f.dispose();
    }
    
    public void testDisplayTransparent45degrees() {
        if (!System.getProperty("jmri.headlesstest","false").equals("false")) { return; }
    
        JFrame f = new JFrame();
        f.getContentPane().setBackground(Color.blue);
        f.setUndecorated(true); // skip frame decoration, which can force a min size.
        
        jmri.jmrit.catalog.NamedIcon icon = new jmri.jmrit.catalog.NamedIcon("resources/icons/redTransparentBox.gif","box"); // 13x13
        
        PositionableLabel label = new PositionableLabel(icon, null);
        
        f.add(label);
        f.pack();
        flushAWT();
        Assert.assertEquals("icon size", new Dimension(13,13).toString(),label.getSize().toString());
        
        // do the rotation, which transforms 13x13 to sqrt(2) bigger, 19x19
        label.rotate(45);
        Assert.assertEquals("icon size", new Dimension(19,19).toString(),label.getSize().toString());

        f.pack();
        flushAWT();
        Assert.assertEquals("icon size", new Dimension(19,19).toString(),label.getSize().toString());
        
        // and check
        int[] val = getDisplayedContent(label, label.getSize(), new Point(0,0));
        
        Assert.assertEquals("icon arraylength", 19*19, val.length);
        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.TRANSPARENT, Pixel.RED, Pixel.TRANSPARENT,
                Pixel.RED, Pixel.TRANSPARENT, Pixel.TRANSPARENT,  // not sure why mid-right is TRANSPARENT; misaligned? Clipping?
                Pixel.TRANSPARENT, Pixel.TRANSPARENT, Pixel.TRANSPARENT); // not sure why bottom mid is TRANSPARENT; misaligned? Clipping?

        // now check that background shows through
        // Need to find the icon location first
        Point p = SwingUtilities.convertPoint(label,0,0,f.getContentPane());
        
        val = getDisplayedContent(f.getContentPane(), label.getSize(), p);

        Assert.assertEquals("frame arraylength", 19*19, val.length);
        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.BLUE, Pixel.RED, Pixel.BLUE,
                Pixel.RED, Pixel.BLUE, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE);
                
        f.dispose();
    }
    

    // test with an RGB animated 13x13 GIF, 0.1 sec per frame
    public void testDisplayAnimatedRGB() throws IOException {
        if (!System.getProperty("jmri.headlesstest","false").equals("false")) { return; }
   
        JFrame f = new JFrame();
        f.getContentPane().setBackground(Color.blue);
        f.setUndecorated(true); // skip frame decoration, which can force a min size.
        
        jmri.jmrit.catalog.NamedIcon icon = new jmri.jmrit.catalog.NamedIcon("resources/icons/RGB-animated-once-Square.gif","box"); // 13x13
        
        PositionableLabel label = new PositionableLabel(icon, null);
        
        f.add(label);
        f.pack();
        flushAWT();
        Assert.assertEquals("icon size", new Dimension(13,13).toString(),label.getSize().toString());
         
        // wait for a bit
        f.setVisible(true);  // needed to get initial animation contents
        flushAWT();

        // check for initial red
        int[] val = getDisplayedContent(label, label.getSize(), new Point(0,0));
        
        Assert.assertEquals("icon arraylength", 13*13, val.length);

        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.RED, Pixel.RED, Pixel.RED,
                Pixel.RED, Pixel.RED, Pixel.RED,
                Pixel.RED, Pixel.RED, Pixel.RED);
        
        // Need to find the icon location in frame first
        Point p = SwingUtilities.convertPoint(label,0,0,f.getContentPane());
        
        val = getDisplayedContent(f.getContentPane(), label.getSize(), p);

        Assert.assertEquals("frame arraylength", 13*13, val.length);

        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.RED, Pixel.RED, Pixel.RED,
                Pixel.RED, Pixel.RED, Pixel.RED,
                Pixel.RED, Pixel.RED, Pixel.RED);

        // wait for long enough to reach final red, skipping intermediate green as timing too fussy
        waitAtLeast(250);        

        val = getDisplayedContent(label, label.getSize(), new Point(0,0));
        
        Assert.assertEquals("icon arraylength", 13*13, val.length);

        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE);
        
        // now check that background shows through
        // Need to find the icon location first
        p = SwingUtilities.convertPoint(label,0,0,f.getContentPane());
        
        val = getDisplayedContent(f.getContentPane(), label.getSize(), p);

        Assert.assertEquals("frame arraylength", 13*13, val.length);
        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE);

        // finally done
        f.dispose();
    }
    
    // test with an RGB animated 13x13 GIF, 0.1 sec per frame, rotate
    public void testDisplayAnimatedRGBrotated45degrees() throws IOException {
        if (!System.getProperty("jmri.headlesstest","false").equals("false")) { return; } 
    
         if (System.getProperty("jmri.migrationtests","false").equals("false")) { // skip test for migration, but warn about it
            log.warn("skipping testDisplayAnimatedRGBrotated45degrees because jmri.migrationtests not set true");
            return;
        }

        JFrame f = new JFrame();
        f.getContentPane().setBackground(Color.green);
        f.setUndecorated(true); // skip frame decoration, which can force a min size.
        
        jmri.jmrit.catalog.NamedIcon icon = new jmri.jmrit.catalog.NamedIcon("resources/icons/RGB-animated-once-Square2.gif","box"); // 13x13
        
        PositionableLabel label = new PositionableLabel(icon, null);
        
        f.add(label);
        f.pack();
        flushAWT();
        Assert.assertEquals("icon size", new Dimension(13,13).toString(),label.getSize().toString());
        
        // do the rotation, which transforms 13x13 to sqrt(2) bigger, 19x19
        label.rotate(45);
        Assert.assertEquals("icon size", new Dimension(19,19).toString(),label.getSize().toString());

        f.pack();
        flushAWT();
        Assert.assertEquals("icon size", new Dimension(19,19).toString(),label.getSize().toString());
        
        // wait for a bit
        f.setVisible(true);  // needed to get initial animation contents
        flushAWT();
        
        // and check
        int[] val = getDisplayedContent(label, label.getSize(), new Point(0,0));
        
        Assert.assertEquals("icon arraylength", 19*19, val.length);

        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.TRANSPARENT, Pixel.RED, Pixel.TRANSPARENT,
                Pixel.RED, Pixel.RED, Pixel.RED,
                Pixel.TRANSPARENT, Pixel.RED, Pixel.TRANSPARENT);
        
        // now check that background shows through
        // Need to find the icon location first
        Point p = SwingUtilities.convertPoint(label,0,0,f.getContentPane());
        
        val = getDisplayedContent(f.getContentPane(), label.getSize(), p);

        Assert.assertEquals("frame arraylength", 19*19, val.length);
        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.GREEN, Pixel.RED, Pixel.GREEN,
                Pixel.RED, Pixel.RED, Pixel.RED,
                Pixel.GREEN, Pixel.RED, Pixel.GREEN);

        // wait for long enough to reach final blue, skipping intermediate green as timing too fussy
        waitAtLeast(250);        

        // and check
        val = getDisplayedContent(label, label.getSize(), new Point(0,0));
        
        Assert.assertEquals("icon arraylength", 19*19, val.length);
        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.TRANSPARENT, Pixel.BLUE, Pixel.TRANSPARENT,
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE,
                Pixel.TRANSPARENT, Pixel.BLUE, Pixel.TRANSPARENT);
        
        // now check that background shows through
        // Need to find the icon location first
        p = SwingUtilities.convertPoint(label,0,0,f.getContentPane());
        
        val = getDisplayedContent(f.getContentPane(), label.getSize(), p);

        Assert.assertEquals("frame arraylength", 19*19, val.length);
        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.GREEN, Pixel.BLUE, Pixel.GREEN,
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE,
                Pixel.GREEN, Pixel.BLUE, Pixel.GREEN);

        f.dispose();
    }
    
    // c.f. http://www.ssec.wisc.edu/~tomw/java/unicode.html#x2580
    final String sampleText = "  \u25CF  "; // note spaces
    
    // FULL BLOCK \u2588
    // BLACK SQUARE \u25A0
    // BLACK CIRCLE \u25CF
    // BLACK SMALL SQUARE \u25AA
    // BLACK DIAMOND \u25C6
    // HEAVY MULTIPLICATION X \u2716

    public void testDisplayText() {
        if (!System.getProperty("jmri.headlesstest","false").equals("false")) { return; }
    
        JFrame f = new JFrame();
        f.getContentPane().setBackground(Color.blue);
        f.setUndecorated(true); // skip frame decoration, which can force a min size.
                        
        PositionableLabel label = new PositionableLabel(sampleText, null);
        label.setForeground(Color.black); // this is a direct set, not through the UI
        label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        f.add(label);
        f.pack();
        flushAWT();

        Assert.assertTrue("Expect size "+label.getSize()+" wider than height", label.getSize().width > label.getSize().height);
                 
        int[] val = getDisplayedContent(label, label.getSize(), new Point(0,0));
        
        //for (int i=0; i<val.length; i++) System.out.println(" "+i+" "+String.format("0x%8s", Integer.toHexString(val[i])).replace(' ', '0'));
 
        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.TRANSPARENT, Pixel.TRANSPARENT, Pixel.TRANSPARENT,
                Pixel.TRANSPARENT, Pixel.BLACK, Pixel.TRANSPARENT,
                Pixel.TRANSPARENT, Pixel.TRANSPARENT, Pixel.TRANSPARENT);
        
        // now check that background shows through
        // Need to find the icon location first
        Point p = SwingUtilities.convertPoint(label,0,0,f.getContentPane());
        
        val = getDisplayedContent(f.getContentPane(), label.getSize(), p);

        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLACK, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE);

        f.dispose();
    }
    

    public void testDisplayTextRotated90() {
        if (!System.getProperty("jmri.headlesstest","false").equals("false")) { return; }
    
        JFrame f = new JFrame();
        f.getContentPane().setBackground(Color.blue);
        f.setUndecorated(true); // skip frame decoration, which can force a min size.
                        
        PositionableLabel label = new PositionableLabel(sampleText, null);
        label.setForeground(Color.black); // this is a direct set, not through the UI
        label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        f.add(label);
        f.pack();
        flushAWT();

        label.rotate(90);
        
        f.pack();
        flushAWT();

        Assert.assertTrue("Expect size "+label.getSize()+" higher than width", label.getSize().width < label.getSize().height);
        
        int[] val = getDisplayedContent(label, label.getSize(), new Point(0,0));
        
        //for (int i=0; i<val.length; i++) System.out.println(" "+i+" "+formatPixel(val[i]));
 
        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.TRANSPARENT, Pixel.TRANSPARENT, Pixel.TRANSPARENT,
                Pixel.TRANSPARENT, Pixel.BLACK, Pixel.TRANSPARENT,
                Pixel.TRANSPARENT, Pixel.TRANSPARENT, Pixel.TRANSPARENT);
        
        // now check that background shows through
        // Need to find the icon location first
        Point p = SwingUtilities.convertPoint(label,0,0,f.getContentPane());

        val = getDisplayedContent(f.getContentPane(), label.getSize(), p);

        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLACK, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE);

        f.dispose();
    }
    
    public void testDisplayTextRotated45() {
        if (!System.getProperty("jmri.headlesstest","false").equals("false")) { return; }
    
        JFrame f = new JFrame();
        f.getContentPane().setBackground(Color.blue);
        f.setUndecorated(true); // skip frame decoration, which can force a min size.
                        
        PositionableLabel label = new PositionableLabel(sampleText, null);
        label.setForeground(Color.black); // this is a direct set, not through the UI
        label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        f.add(label);
        f.pack();
        flushAWT();

        label.rotate(45);
        
        f.pack();
        flushAWT();

        int[] val = getDisplayedContent(label, label.getSize(), new Point(0,0));
        
        //for (int i=0; i<val.length; i++) System.out.println(" "+i+" "+String.format("0x%8s", Integer.toHexString(val[i])).replace(' ', '0'));
 
        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.TRANSPARENT, Pixel.TRANSPARENT, Pixel.TRANSPARENT,
                Pixel.TRANSPARENT, Pixel.BLACK, Pixel.TRANSPARENT,
                Pixel.TRANSPARENT, Pixel.TRANSPARENT, Pixel.TRANSPARENT);
        
        // now check that background shows through
        // Need to find the icon location first
        Point p = SwingUtilities.convertPoint(label,0,0,f.getContentPane());

        val = getDisplayedContent(f.getContentPane(), label.getSize(), p);

        assertImageNinePoints("icon", val, label.getSize(),
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLACK, Pixel.BLUE,
                Pixel.BLUE, Pixel.BLUE, Pixel.BLUE);

        f.dispose();
    }


    // from here down is testing infrastructure

    public PositionableLabelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PositionableLabelTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PositionableLabelTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    protected void tearDown() {
        // now close panel window
        if (panel != null) {
            java.awt.event.WindowListener[] listeners = panel.getTargetFrame().getWindowListeners();
            for (int i = 0; i < listeners.length; i++) {
                panel.getTargetFrame().removeWindowListener(listeners[i]);
            }
            junit.extensions.jfcunit.TestHelper.disposeWindow(panel.getTargetFrame(), this);
            panel = null;
        }
        
        apps.tests.Log4JFixture.tearDown();
    }

	static private org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PositionableLabelTest.class.getName());
}
