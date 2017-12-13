package jmri.jmrit.display;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.ComponentChooser;

/**
 * MemoryIconTest.java
 *
 * Description:
 *
 * @author	Bob Jacobsen Copyright 2007, 2015
 */
public class MemoryIconTest extends PositionableTestBase {

    private jmri.jmrit.display.panelEditor.PanelEditor panel = null;
    private MemoryIcon to = null;

    @Test
    public void testShowContent() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame jf = new JmriJFrame();
        jf.setTitle("Expect \"Data Data\" as text");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());
        jf.getContentPane().setBackground(Color.white);

        MemoryIcon to = new MemoryIcon("MemoryTest1", panel);
        jf.getContentPane().add(to);
        to.getPopupUtility().setBackgroundColor(Color.white);

        jf.getContentPane().add(new javax.swing.JLabel("| Expect \"Data Data\" text"));

        jmri.InstanceManager.memoryManagerInstance().provideMemory("IM1").setValue("Data Data");
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);

        jf.pack();
        jf.setVisible(true);
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);

        int[] colors = getColor("Expect \"Data Data\" as text", "| Expect \"Data Data\" as text", 0, 6, 10);
        int r = ((colors[1] >> 16) & 0xFF) + ((colors[2] >> 16) & 0xFF) + ((colors[3] >> 16) & 0xFF) + ((colors[4] >> 16) & 0xFF);
        int g = ((colors[1] >> 8) & 0xFF) + ((colors[2] >> 8) & 0xFF) + ((colors[3] >> 8) & 0xFF) + ((colors[4] >> 8) & 0xFF);
        int b = ((colors[1]) & 0xFF) + ((colors[2]) & 0xFF) + ((colors[3]) & 0xFF) + ((colors[4]) & 0xFF);
        Assert.assertTrue("Expect gray/black text", r == g & g == b); // gray pixels
        Assert.assertTrue("Expect blacker than grey", r < 4 * 0xee); // gray pixels

        if (System.getProperty("jmri.demo", "false").equals("false")) {
            jf.setVisible(false);
            JUnitUtil.dispose(jf);
        }
    }

    @Test
    public void testShowBlank() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        log.debug("testShowBlank");
        JFrame jf = new JmriJFrame();
        jf.setTitle("Expect blank");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());
        jf.getContentPane().setBackground(Color.white);

        jf.getContentPane().add(to);
        to.getPopupUtility().setBackgroundColor(Color.white);

        jf.getContentPane().add(new javax.swing.JLabel("| Expect blank: "));

        jmri.InstanceManager.memoryManagerInstance().provideMemory("IM2").setValue("");

        to.setMemory("IM2");

        jf.pack();
        jf.setVisible(true);
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);

        int[] colors = getColor("Expect blank", "| Expect blank", 0, 6, 10);
        //for (int i=0; i< 10; i++) System.out.println("   "+String.format("0x%8s", Integer.toHexString(colors[i])).replace(' ', '0'));
        boolean white = (colors[3] == 0xffffffff) && (colors[4] == 0xffffffff);
        Assert.assertTrue("Expect white pixels", white);

        if (System.getProperty("jmri.demo", "false").equals("false")) {
            jf.setVisible(false);
            JUnitUtil.dispose(jf);
        }

    }

    @Test
    public void testShowEmpty() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame jf = new JmriJFrame();
        jf.setTitle("Expect empty");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());
        jf.getContentPane().setBackground(Color.white);

        jf.getContentPane().add(to);
        to.getPopupUtility().setBackgroundColor(Color.white);

        jf.getContentPane().add(new javax.swing.JLabel("| Expect red X default icon: "));

        jmri.InstanceManager.memoryManagerInstance().provideMemory("IM3");
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);

        to.setMemory("IM3");

        jf.pack();
        jf.setVisible(true);
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);

        int colors[] = getColor("Expect empty", "| Expect empty", 0, 6, 10);
        Assert.assertTrue("Expect red X", (colors[3] == 0xff800000) || (colors[4] == 0xff800000) || (colors[5] == 0xff800000));

        if (System.getProperty("jmri.demo", "false").equals("false")) {
            jf.setVisible(false);
            JUnitUtil.dispose(jf);
        }

    }

    int[] getColor(String frameName, String label, int x, int y, int n) {
        // Find window by name
        JmriJFrame frame = JmriJFrame.getFrame(frameName);

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

        // find a point in mid-center of memory icon - location choosen by
        // looking at v4.0.1 on Mac
        Point p = SwingUtilities.convertPoint(jl,x, y, frame);

        // check pixel color (from http://stackoverflow.com/questions/13307962/how-to-get-the-color-of-a-point-in-a-jpanel )
        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = image.createGraphics();
        frame.paint(g2);

        // display a sweep of color
        int[] colors = new int[n];
        for (int i = 0; i < n; i++) {
            int color = image.getRGB(p.x + i, p.y);
            //System.err.println(" "+i+" "+String.format("0x%8s", Integer.toHexString(color)).replace(' ', '0'));
            colors[i] = color;
        }

        g2.dispose();
        return colors;
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
        if (!GraphicsEnvironment.isHeadless()) {
            panel = new jmri.jmrit.display.panelEditor.PanelEditor("Test MemoryIcon Panel");
            p = to = new MemoryIcon("MemoryTest1", panel);
            to.setMemory("IM1");
        }
    }

    @After
    public void tearDown() {
        // now close panel window
        if (panel != null) {
            java.awt.event.WindowListener[] listeners = panel.getTargetFrame().getWindowListeners();
            for (WindowListener listener : listeners) {
                panel.getTargetFrame().removeWindowListener(listener);
            }
            JFrameOperator jfo = new JFrameOperator(panel.getTargetFrame());
            jfo.requestClose();
        }
        JUnitUtil.resetWindows(false, false);  // don't log here.  should be from this class.
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(TurnoutIconTest.class);
}
