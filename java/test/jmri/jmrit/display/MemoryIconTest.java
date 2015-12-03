package jmri.jmrit.display;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import junit.extensions.jfcunit.finder.ComponentFinder;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * MemoryIconTest.java
 *
 * Description:
 *
 * @author	Bob Jacobsen Copyright 2007, 2015
 * @version	$Revision$
 */
public class MemoryIconTest extends jmri.util.SwingTestCase {

    MemoryIcon to = null;

    jmri.jmrit.display.panelEditor.PanelEditor panel
            = new jmri.jmrit.display.panelEditor.PanelEditor("Test MemoryIcon Panel");

    public void testShowContent() {
        JFrame jf = new JmriJFrame();
        jf.setTitle("Expect \"Data Data\" as text");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        to = new MemoryIcon("MemoryTest1", panel);
        jf.getContentPane().add(to);

        jf.getContentPane().add(new javax.swing.JLabel("| Expect \"Data Data\" text"));

        jmri.InstanceManager i = new jmri.InstanceManager() {
            protected void init() {
                super.init();
                root = this;
            }
        };
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
        Assert.assertNotNull("Instance exists", i);
        jmri.InstanceManager.memoryManagerInstance().provideMemory("IM1").setValue("Data Data");
        to.setMemory("IM1");

        jf.pack();
        jf.setVisible(true);
        flushAWT();

        int[] colors = getColor("Expect \"Data Data\" as text","| Expect \"Data Data\" as text",0,6,10);
        int r = ((colors[1]>>16)&0xFF)+((colors[2]>>16)&0xFF)+((colors[3]>>16)&0xFF)+((colors[4]>>16)&0xFF);
        int g = ((colors[1]>>8)&0xFF)+((colors[2]>>8)&0xFF)+((colors[3]>>8)&0xFF)+((colors[4]>>8)&0xFF);
        int b = ((colors[1])&0xFF)+((colors[2])&0xFF)+((colors[3])&0xFF)+((colors[4])&0xFF);
        Assert.assertTrue("Expect gray/black ", r==g & g==b); // gray pixels
        Assert.assertTrue("Expect blacker than grey", r<4*0xee); // gray pixels

        if (!System.getProperty("jmri.demo", "false").equals("false")) {
            jf.setVisible(false);
            jf.dispose();
        }
    }

    public void testShowBlank() {
        JFrame jf = new JmriJFrame();
        jf.setTitle("Expect blank");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        to = new MemoryIcon("MemoryTest2", panel);
        jf.getContentPane().add(to);

        jf.getContentPane().add(new javax.swing.JLabel("| Expect blank: "));

        jmri.InstanceManager i = new jmri.InstanceManager() {
            protected void init() {
                super.init();
                root = this;
            }
        };
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
        Assert.assertNotNull("Instance exists", i);
        jmri.InstanceManager.memoryManagerInstance().provideMemory("IM2").setValue("");
        to.setMemory("IM2");
                
        jf.pack();
        jf.setVisible(true);
        flushAWT();

        int[] colors = getColor("Expect blank","| Expect blank",0,6,10);
//        Assert.assertTrue("Expect gray pixels", (colors[3]==0xffeeeeee)&&(colors[4]==0xffeeeeee)); // gray pixels
        Assert.assertTrue("Expect white pixels", (colors[3]==0xffffffff)&&(colors[4]==0xffffffff)); // white pixels
        Assert.assertTrue("Expect gray pixels", (colors[7]==0xffeeeeee)&&(colors[8]==0xffeeeeee)); // gray pixels
        
        if (!System.getProperty("jmri.demo", "false").equals("false")) {
            jf.setVisible(false);
            jf.dispose();
        }

    }

    public void testShowEmpty() {
        JFrame jf = new JmriJFrame();
        jf.setTitle("Expect empty");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        to = new MemoryIcon("MemoryTest3", panel);
        jf.getContentPane().add(to);

        jf.getContentPane().add(new javax.swing.JLabel("| Expect red X default icon: "));

        jmri.InstanceManager i = new jmri.InstanceManager() {
            protected void init() {
                super.init();
                root = this;
            }
        };
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
        Assert.assertNotNull("Instance exists", i);
        jmri.InstanceManager.memoryManagerInstance().provideMemory("IM3");
        to.setMemory("IM3");

        jf.pack();
        jf.setVisible(true);
        flushAWT();

        int colors[] = getColor("Expect empty","| Expect empty",0,6,10);
        Assert.assertTrue("Expect red X", (colors[3]==0xff800000)||(colors[4]==0xff800000)||(colors[5]==0xff800000));

        if (!System.getProperty("jmri.demo", "false").equals("false")) {
            jf.setVisible(false);
            jf.dispose();
        }

    }

    int[] getColor(String frame, String label, int x, int y, int n) {
        // Find window by name
        JmriJFrame ft = JmriJFrame.getFrame(frame);
        Assert.assertNotNull("frame: "+frame, ft);
        
        // find label within that
        ComponentFinder finder = new ComponentFinder(MemoryIcon.class);
        java.util.List list = finder.findAll(ft);
        Assert.assertNotNull("list: "+frame, list);
        Assert.assertTrue("length: "+frame+": "+list.size(), list.size()>0);
        
        // find a point in mid-center of memory icon - location choosen by
        // looking at v4.0.1 on Mac
        Point p = SwingUtilities.convertPoint(((JComponent)list.get(0)),x,y,ft);
        
        // check pixel color (from http://stackoverflow.com/questions/13307962/how-to-get-the-color-of-a-point-in-a-jpanel )
        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = image.createGraphics();
        ft.paint(g2);
        
        // display a sweep of color
        int[] colors = new int[n];
        for (int i = 0; i<n; i++) {
            int color = image.getRGB(p.x+i,p.y);
            //System.err.println(" "+i+" "+String.format("0x%8s", Integer.toHexString(color)).replace(' ', '0'));
            colors[i] = color;
        }

        g2.dispose();
        return colors;
    }

    // from here down is testing infrastructure
    public MemoryIconTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", MemoryIconTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(MemoryIconTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
    }

    protected void tearDown() throws Exception {
        // now close panel window
        java.awt.event.WindowListener[] listeners = panel.getTargetFrame().getWindowListeners();
        for (int i = 0; i < listeners.length; i++) {
            panel.getTargetFrame().removeWindowListener(listeners[i]);
        }
        junit.extensions.jfcunit.TestHelper.disposeWindow(panel.getTargetFrame(), this);
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
        JUnitUtil.resetInstanceManager();
    }

	// static private Logger log = LoggerFactory.getLogger(TurnoutIconTest.class.getName());
}
