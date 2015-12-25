// SwingTestCaseTest.java
package jmri.util;

import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.finder.NamedComponentFinder;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.util.SwingTestCase class.
 *
 * @author	Bob Jacobsen Copyright 2009
 * @version	$Revision$
 */
public class SwingTestCaseTest extends SwingTestCase {

    /**
     * Simple test of creating a Swing frame with a checkbox, checking the box,
     * and seeing that the check changed its state.
     */
    public void testCheckBox() {
        // create a little GUI with a single check box
        JFrame f = new JFrame("SwingTextCaseTest");
        f.setSize(100, 100);	        // checkbox must be visible for test to work
        JCheckBox b = new JCheckBox("Check");
        b.setName("Check");
        f.add(b);
        f.setVisible(true);

        // find the check box and confirm not yet checked
        NamedComponentFinder finder = new NamedComponentFinder(JCheckBox.class, "Check");
        JCheckBox testBox = (JCheckBox) finder.find(f, 0);
        Assert.assertNotNull(testBox);
        Assert.assertTrue(!testBox.isSelected());

        // set the check in the box by clicking it
        getHelper().enterClickAndLeave(new MouseEventData(this, testBox));

        // test for selected
        Assert.assertTrue(testBox.isSelected());

        f.dispose();
    }

    public void testGetDisplayedContentGreen() {
    
        JFrame f = new JFrame();
        f.setUndecorated(true); // skip frame decoration, which can force a min size.

        JLabel wIcon = new JLabel(new ImageIcon("resources/icons/greenSquare.gif")); // 13x13
        f.add(wIcon);
        f.pack();
        flushAWT();
        
        Assert.assertEquals("icon size", new Dimension(13,13).toString(),wIcon.getSize().toString());
 
        int[] val = getDisplayedContent(wIcon, wIcon.getSize(), new Point(0,0));
        
        Assert.assertEquals("icon arraylength", val.length, 13*13);
        Assert.assertEquals("icon first", "0xff00ff00", 
                String.format("0x%8s", Integer.toHexString(val[0])).replace(' ', '0'));
        Assert.assertEquals("icon middle", "0xff00ff00", 
                String.format("0x%8s", Integer.toHexString(val[(int)Math.floor(wIcon.getSize().height/2)*wIcon.getSize().width+(int)Math.floor(wIcon.getSize().width/2)-1]).replace(' ', '0')));
        Assert.assertEquals("icon last", "0xff00ff00", 
                String.format("0x%8s", Integer.toHexString(val[wIcon.getSize().height*wIcon.getSize().width-1]).replace(' ', '0')));

        f.dispose();
        
    }
    
    public void testGetDisplayedContentRedTransparentBkg() {
    
        JFrame f = new JFrame();
        f.setUndecorated(true); // skip frame decoration, which can force a min size.
        
        JLabel wIcon = new JLabel(new ImageIcon("resources/icons/redTransparentBox.gif")); // 13x13
        wIcon.setOpaque(true);
        wIcon.setBackground(java.awt.Color.blue);
        
        f.add(wIcon);
        f.pack();
        flushAWT();
        Assert.assertEquals("icon size", new Dimension(13,13).toString(),wIcon.getSize().toString());
         
        int[] val = getDisplayedContent(wIcon, wIcon.getSize(), new Point(0,0));
        
        Assert.assertEquals("icon arraylength", val.length, 13*13);
        Assert.assertEquals("icon first", "0xffff0000", 
                String.format("0x%8s", Integer.toHexString(val[0])).replace(' ', '0'));
        Assert.assertEquals("icon middle", "0xff0000ff", 
                String.format("0x%8s", Integer.toHexString(val[(int)Math.floor(wIcon.getSize().height/2)*wIcon.getSize().width+(int)Math.floor(wIcon.getSize().width/2)-1])).replace(' ', '0'));
        Assert.assertEquals("icon last", "0xffff0000", 
                String.format("0x%8s", Integer.toHexString(val[13*13-1])).replace(' ', '0'));
        
        f.dispose();
    }
    
    public void testGetDisplayedContentRedTransparentTransp() {
    
        JFrame f = new JFrame();
        f.getContentPane().setBackground(java.awt.Color.blue);
        f.setUndecorated(true); // skip frame decoration, which can force a min size.
        
        JLabel wIcon = new JLabel(new ImageIcon("resources/icons/redTransparentBox.gif")); // 13x13
        wIcon.setOpaque(false);
        
        f.add(wIcon);
        f.pack();
        flushAWT();
        Assert.assertEquals("icon size", new Dimension(13,13).toString(),wIcon.getSize().toString());
         
        int[] val = getDisplayedContent(wIcon, wIcon.getSize(), new Point(0,0));
        
        Assert.assertEquals("icon arraylength", val.length, 13*13);
        Assert.assertEquals("icon first", "0xffff0000", 
                String.format("0x%8s", Integer.toHexString(val[0])).replace(' ', '0'));
        Assert.assertEquals("icon middle", "0x00000000", 
                String.format("0x%8s", Integer.toHexString(val[(int)Math.floor(wIcon.getSize().height/2)*wIcon.getSize().width+(int)Math.floor(wIcon.getSize().width/2)-1])).replace(' ', '0'));
        Assert.assertEquals("icon last", "0xffff0000", 
                String.format("0x%8s", Integer.toHexString(val[13*13-1])).replace(' ', '0'));
        
        // now check that background shows through
        // Need to find the icon location first
        Point p = SwingUtilities.convertPoint(wIcon,0,0,f.getContentPane());
        
        val = getDisplayedContent(f.getContentPane(), wIcon.getSize(), p);
        Assert.assertEquals("frame arraylength", val.length, 13*13);
        Assert.assertEquals("frame first", "0xffff0000", 
                String.format("0x%8s", Integer.toHexString(val[0])).replace(' ', '0'));
        Assert.assertEquals("frame middle", "0xff0000ff", 
                String.format("0x%8s", Integer.toHexString(val[(int)Math.floor(wIcon.getSize().height/2)*wIcon.getSize().width+(int)Math.floor(wIcon.getSize().width/2)-1])).replace(' ', '0'));
        Assert.assertEquals("frame last", "0xffff0000", 
                String.format("0x%8s", Integer.toHexString(val[13*13-1])).replace(' ', '0'));

        f.dispose();
    }

    // from here down is testing infrastructure
    public SwingTestCaseTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SwingTestCaseTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        Test suite = new TestSuite(SwingTestCaseTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        //apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        //apps.tests.Log4JFixture.tearDown();
    }

	//static Logger log = LoggerFactory.getLogger(SwingTestCaseTest.class.getName());
}
