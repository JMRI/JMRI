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

    public void testShow() {
        JFrame jf = new JFrame();
        JPanel p = new JPanel();
        jf.getContentPane().add(p);
        p.setPreferredSize(new Dimension(200, 200));
        p.setLayout(null);

        // test button in upper left
        JButton whereButton = new JButton("where");
        whereButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                whereButtonPushed();
            }
        });
        whereButton.setBounds(0, 0, 70, 40);
        p.add(whereButton);

        to = new PositionableLabel("here", panel);
        to.setBounds(80, 80, 40, 40);
        panel.putItem(to);
        to.setDisplayLevel(jmri.jmrit.display.Editor.LABELS);
        assertEquals("Display Level ", to.getDisplayLevel(), jmri.jmrit.display.Editor.LABELS);

        p.add(to);
        panel.addLabel("There");

        jf.pack();
        jf.setVisible(true);
    }

    // animate the visible frame
    public void whereButtonPushed() {
    }
    
    // Load labels with backgrounds and make sure they have right color
    // The file used was written with 4.0.1, and behaves as expected from panel names
    public void testBackgroundColors() throws Exception {
        if (!System.getProperty("jmri.headlesstest","false").equals("false")) {
            return;
        }
        // make four windows
        InstanceManager.configureManagerInstance()
                .load(new java.io.File("java/test/jmri/jmrit/display/configurexml/verify/backgrounds.xml"));

        // Find color in label by frame name
        int color1 = getColor("F Bkg none, label Bkg none");

        int color2 = getColor("F Bkg blue, label Bkg none");

        int color3 = getColor("F Bkg none, label Bkg yellow");

        int color4 = getColor("F Bkg blue, label Bkg yellow");

//        Assert.assertEquals("F Bkg none, label Bkg none color", "0xffffffff",   // white background pre Java 1.8
//                            String.format("0x%8s", Integer.toHexString(color1)).replace(' ', '0'));
        Assert.assertEquals("F Bkg none, label Bkg none color", "0xffeeeeee",   // light grey background
                String.format("0x%8s", Integer.toHexString(color1)).replace(' ', '0'));
        Assert.assertEquals("F Bkg blue, label Bkg none color", "0xff0000ff",   // blue background
                            String.format("0x%8s", Integer.toHexString(color2)).replace(' ', '0')); // no blue, looking at transparent label
        Assert.assertEquals("F Bkg none, label Bkg yellow color", "0xffffff00", // yellow
                            String.format("0x%8s", Integer.toHexString(color3)).replace(' ', '0'));
        Assert.assertEquals("F Bkg blue, label Bkg yellow color", "0xffffff00", // yellow
                            String.format("0x%8s", Integer.toHexString(color4)).replace(' ', '0'));

    }
    
    int getColor(String name) {
        // Find window by name
        JmriJFrame ft = JmriJFrame.getFrame(name);
        Assert.assertNotNull("frame: "+name, ft);
        
        // find label within that
        JLabelFinder finder = new JLabelFinder("....");
        java.util.List list = finder.findAll(ft);
        Assert.assertNotNull("list: "+name, list);
        Assert.assertTrue("length: "+name+": "+list.size(), list.size()>0);
        
        // find a point in upper left of label
        Point p = SwingUtilities.convertPoint(((JComponent)list.get(0)),1,1,ft);
        
        // check pixel color (from http://stackoverflow.com/questions/13307962/how-to-get-the-color-of-a-point-in-a-jpanel )
        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = image.createGraphics();
        ft.paint(g2);
        
        int color = image.getRGB(p.x,p.y);

        g2.dispose();
        // Ask to close table window
        ft.setVisible(false);
        //TestHelper.disposeWindow(ft, this);
        return color;
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
