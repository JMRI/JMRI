package jmri.jmrit.display;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
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
 * PositionableLabelTest.java
 *
 * Description:
 *
 * @author	Bob Jacobsen
 * @version	$Revision$
 */
public class PositionableLabelTest extends jmri.util.SwingTestCase {

    PositionableLabel to = null;
    jmri.jmrit.display.panelEditor.PanelEditor panel
            = new jmri.jmrit.display.panelEditor.PanelEditor("PositionableLabel Test Panel");

    public void testSmallPanel() {
        if (!System.getProperty("jmri.headlesstest","false").equals("false")) {
            return;
        }
                
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
        if (!System.getProperty("jmri.headlesstest","false").equals("false")) {
            return;
        }
        // make four windows
        InstanceManager.configureManagerInstance()
                .load(new java.io.File("java/test/jmri/jmrit/display/configurexml/verify/backgrounds.xml"));
        flushAWT();

        // Find color in label by frame name
        int color1 = getColor("F Bkg none, label Bkg none"); // transparent background

        int color2 = getColor("F Bkg blue, label Bkg none"); // transparent background shows blue

        int color3 = getColor("F Bkg none, label Bkg yellow"); // yellow

        int color4 = getColor("F Bkg blue, label Bkg yellow");

        Assert.assertEquals("F Bkg none, label Bkg none color", "0x00000000",   // transparent shows neutral frame background
                            String.format("0x%8s", Integer.toHexString(color1)).replace(' ', '0'));
        Assert.assertEquals("F Bkg blue, label Bkg none color", "0x00000000",   // transparent shows blue frame background
                            String.format("0x%8s", Integer.toHexString(color2)).replace(' ', '0')); // no blue, looking at transparent label
        Assert.assertEquals("F Bkg none, label Bkg yellow color", "0xffffff00", // yellow
                            String.format("0x%8s", Integer.toHexString(color3)).replace(' ', '0'));
        Assert.assertEquals("F Bkg blue, label Bkg yellow color", "0xffffff00", // yellow
                            String.format("0x%8s", Integer.toHexString(color4)).replace(' ', '0'));
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
    
        JFrame f = new JFrame();
        f.getContentPane().setBackground(java.awt.Color.blue);
        f.setUndecorated(true); // skip frame decoration, which can force a min size.
        
        jmri.jmrit.catalog.NamedIcon icon = new jmri.jmrit.catalog.NamedIcon("resources/icons/redTransparentBox.gif","box"); // 13x13
        
        PositionableLabel label = new PositionableLabel(icon, null);
        
        f.add(label);
        f.pack();
        flushAWT();
        Assert.assertEquals("icon size", new Dimension(13,13).toString(),label.getSize().toString());
         
        int[] val = getDisplayedContent(label, label.getSize(), new Point(0,0));
        
        Assert.assertEquals("icon arraylength", 13*13, val.length);
        Assert.assertEquals("icon first", "0xffff0000", 
                String.format("0x%8s", Integer.toHexString(val[0])).replace(' ', '0'));
        Assert.assertEquals("icon upper right", "0xffff0000", 
                String.format("0x%8s", Integer.toHexString(val[12])).replace(' ', '0'));
        Assert.assertEquals("icon lower left", "0xffff0000", 
                String.format("0x%8s", Integer.toHexString(val[13*13-1-12])).replace(' ', '0'));
        Assert.assertEquals("icon middle", "0x00000000", 
                String.format("0x%8s", Integer.toHexString(val[(int)Math.floor(label.getSize().height/2)*label.getSize().width+(int)Math.floor(label.getSize().width/2)-1])).replace(' ', '0'));
        Assert.assertEquals("icon last", "0xffff0000", 
                String.format("0x%8s", Integer.toHexString(val[13*13-1])).replace(' ', '0'));
        
        // now check that background shows through
        // Need to find the icon location first
        Point p = SwingUtilities.convertPoint(label,0,0,f.getContentPane());
        
        val = getDisplayedContent(f.getContentPane(), label.getSize(), p);

        Assert.assertEquals("frame arraylength", 13*13, val.length);
        Assert.assertEquals("frame first", "0xffff0000", 
                String.format("0x%8s", Integer.toHexString(val[0])).replace(' ', '0'));
        Assert.assertEquals("frame middle", "0xff0000ff", 
                String.format("0x%8s", Integer.toHexString(val[(int)Math.floor(label.getSize().height/2)*label.getSize().width+(int)Math.floor(label.getSize().width/2)-1])).replace(' ', '0'));
        Assert.assertEquals("frame last", "0xffff0000", 
                String.format("0x%8s", Integer.toHexString(val[13*13-1])).replace(' ', '0'));

        f.dispose();
    }
    
    public void testDisplayTransparent45degrees() {
    
        JFrame f = new JFrame();
        f.getContentPane().setBackground(java.awt.Color.blue);
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
        Assert.assertEquals("icon first", "0x00000000", // should be transparent corner
                String.format("0x%8s", Integer.toHexString(val[0])).replace(' ', '0'));
        Assert.assertEquals("icon top center", "0xffff0000", 
                String.format("0x%8s", Integer.toHexString(val[9])).replace(' ', '0'));
        Assert.assertEquals("icon middle", "0x00000000", 
                String.format("0x%8s", Integer.toHexString(val[(int)Math.floor(label.getSize().height/2)*label.getSize().width+(int)Math.floor(label.getSize().width/2)-1])).replace(' ', '0'));
        Assert.assertEquals("icon last", "0x00000000", // should be transparent corner
                String.format("0x%8s", Integer.toHexString(val[19*19-1])).replace(' ', '0'));
        
        // now check that background shows through
        // Need to find the icon location first
        Point p = SwingUtilities.convertPoint(label,0,0,f.getContentPane());
        
        val = getDisplayedContent(f.getContentPane(), label.getSize(), p);

        Assert.assertEquals("frame arraylength", 19*19, val.length);
        Assert.assertEquals("frame first", "0xff0000ff", // blue bkg
                String.format("0x%8s", Integer.toHexString(val[0])).replace(' ', '0'));
        Assert.assertEquals("icon top center", "0xffff0000", 
                String.format("0x%8s", Integer.toHexString(val[9])).replace(' ', '0'));
        Assert.assertEquals("frame middle", "0xff0000ff", 
                String.format("0x%8s", Integer.toHexString(val[(int)Math.floor(label.getSize().height/2)*label.getSize().width+(int)Math.floor(label.getSize().width/2)-1])).replace(' ', '0'));
        Assert.assertEquals("frame last", "0xff0000ff", // blue bkg
                String.format("0x%8s", Integer.toHexString(val[19*19-2])).replace(' ', '0'));

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
    }

    protected void tearDown() {
        // now close panel window
        java.awt.event.WindowListener[] listeners = panel.getTargetFrame().getWindowListeners();
        for (int i = 0; i < listeners.length; i++) {
            panel.getTargetFrame().removeWindowListener(listeners[i]);
        }
        junit.extensions.jfcunit.TestHelper.disposeWindow(panel.getTargetFrame(), this);
        apps.tests.Log4JFixture.tearDown();
    }

	// static private Logger log = LoggerFactory.getLogger(TurnoutIconTest.class.getName());
}
