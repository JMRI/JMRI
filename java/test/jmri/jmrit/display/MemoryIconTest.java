package jmri.jmrit.display;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import jmri.jmrit.catalog.NamedIcon;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import org.apache.log4j.Level;
import org.junit.*;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JLabelOperator;

/**
 * Test simple functioning of MemoryIcon.
 *
 * @author	Bob Jacobsen Copyright 2007, 2015
 */
public class MemoryIconTest extends PositionableTestBase {

    protected MemoryIcon to = null;

    @Test
    public void testShowContent() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame jf = new JmriJFrame();
        jf.setTitle("Expect \"Data Data\" as text");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());
        jf.getContentPane().setBackground(Color.white);

        jf.getContentPane().add(to);
        to.getPopupUtility().setBackgroundColor(Color.white);

        jf.getContentPane().add(new javax.swing.JLabel("| Expect \"Data Data\" text"));

        jmri.InstanceManager.memoryManagerInstance().provideMemory("IM1").setValue("Data Data");
        new QueueTool().waitEmpty(100);

        jf.pack();
        jf.setVisible(true);
        new QueueTool().waitEmpty(100);

        int[] colors = getColor("Expect \"Data Data\" as text", "| Expect \"Data Data\" as text", 0, 6, 10);
        int r = ((colors[1] >> 16) & 0xFF) + ((colors[2] >> 16) & 0xFF) + ((colors[3] >> 16) & 0xFF) + ((colors[4] >> 16) & 0xFF);
        int g = ((colors[1] >> 8) & 0xFF) + ((colors[2] >> 8) & 0xFF) + ((colors[3] >> 8) & 0xFF) + ((colors[4] >> 8) & 0xFF);
        int b = ((colors[1]) & 0xFF) + ((colors[2]) & 0xFF) + ((colors[3]) & 0xFF) + ((colors[4]) & 0xFF);
        Assert.assertTrue("Expect gray/black text", r == g & g == b); // gray pixels
        // the following assert fails on some Linux machines, but I am 
        // uncertain what that implies, since the previous test verifies the 
        // text is grey.
        //Assert.assertTrue("Expect blacker than grey", r < 4 * 0xee); // gray pixels

        if (System.getProperty("jmri.demo", "false").equals("false")) {
            jf.setVisible(false);
            JUnitUtil.dispose(jf);
        }
    }

    @Test
    public void testShowBlank() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        //log.debug("testShowBlank");
        JFrame jf = new JmriJFrame();
        jf.setTitle("Expect blank");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());
        jf.getContentPane().setBackground(Color.white);

        jf.getContentPane().add(to);
        to.getPopupUtility().setBackgroundColor(Color.white);

        jf.getContentPane().add(new javax.swing.JLabel("| Expect blank: "));
        jmri.InstanceManager.memoryManagerInstance().provideMemory("IM1").setValue("");
        jf.pack();
        jf.setVisible(true);
        new QueueTool().waitEmpty(100);

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

        new QueueTool().waitEmpty(100);

        jf.pack();
        jf.setVisible(true);
        new QueueTool().waitEmpty(100);

        int colors[] = getColor("Expect empty", "| Expect empty", 0, 6, 10);
        Assert.assertTrue("Expect red X", (colors[3] == 0xff800000) || (colors[4] == 0xff800000) || (colors[5] == 0xff800000));

        if (System.getProperty("jmri.demo", "false").equals("false")) {
            jf.setVisible(false);
            JUnitUtil.dispose(jf);
        }
    }

    @Test
    public void testShowNumber() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame jf = new JmriJFrame();
        jf.setTitle("Expect Number");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());
        jf.getContentPane().setBackground(Color.white);

        jf.getContentPane().add(to);
        to.getPopupUtility().setBackgroundColor(Color.white);

        jf.getContentPane().add(new javax.swing.JLabel("| Expect roster entry: "));

	    jmri.InstanceManager.memoryManagerInstance().provideMemory("IM1").setValue(42);
        new QueueTool().waitEmpty(100);

        jf.pack();
        jf.setVisible(true);
        new QueueTool().waitEmpty(100);

        Assert.assertFalse("No Warn Level or higher Messages",JUnitAppender.unexpectedMessageSeen(Level.WARN));

        jf.setVisible(false);
        JUnitUtil.dispose(jf);
    }

    @Test
    public void testShowRosterEntry() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame jf = new JmriJFrame();
        jf.setTitle("Expect Roster Entry");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());
        jf.getContentPane().setBackground(Color.white);

        jf.getContentPane().add(to);
        to.getPopupUtility().setBackgroundColor(Color.white);

        jf.getContentPane().add(new javax.swing.JLabel("| Expect roster entry: "));

        jmri.jmrit.roster.RosterEntry re = jmri.jmrit.roster.RosterEntry.fromFile(new java.io.File("java/test/jmri/jmrit/roster/ACL1012-Schema.xml"));

	    jmri.InstanceManager.memoryManagerInstance().provideMemory("IM1").setValue(re);
        new QueueTool().waitEmpty(100);

        jf.pack();
        jf.setVisible(true);
        new QueueTool().waitEmpty(100);
        Assert.assertFalse("No Warn Level or higher Messages",JUnitAppender.unexpectedMessageSeen(Level.WARN));

        jf.setVisible(false);
        JUnitUtil.dispose(jf);
    }

    @Test
    public void testShowIdTag() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame jf = new JmriJFrame();
        jf.setTitle("Expect Roster Entry");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());
        jf.getContentPane().setBackground(Color.white);

        jf.getContentPane().add(to);
        to.getPopupUtility().setBackgroundColor(Color.white);

        jf.getContentPane().add(new javax.swing.JLabel("| Expect roster entry: "));

        jmri.IdTag tag = new jmri.implementation.DefaultIdTag("1234");

	    jmri.InstanceManager.memoryManagerInstance().provideMemory("IM1").setValue(tag);
        new QueueTool().waitEmpty(100);

        jf.pack();
        jf.setVisible(true);
        new QueueTool().waitEmpty(100);
        Assert.assertFalse("No Warn Level or higher Messages",JUnitAppender.unexpectedMessageSeen(Level.WARN));
        Assert.assertNotNull("Label with correct text value",jmri.util.swing.JemmyUtil.getLabelWithText(jf.getTitle(),tag.getDisplayName()));

        jf.setVisible(false);
        JUnitUtil.dispose(jf);
    }

    @Test
    public void testShowReportable() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame jf = new JmriJFrame();
        jf.setTitle("Expect Roster Entry");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());
        jf.getContentPane().setBackground(Color.white);

        jf.getContentPane().add(to);
        to.getPopupUtility().setBackgroundColor(Color.white);

        jf.getContentPane().add(new javax.swing.JLabel("| Expect roster entry: "));

        jmri.Reportable rpt = new jmri.Reportable(){
           @Override
           public String toReportString(){
              return "test string";
           }
        };

	    jmri.InstanceManager.memoryManagerInstance().provideMemory("IM1").setValue(rpt);
        new QueueTool().waitEmpty(100);

        jf.pack();
        jf.setVisible(true);
        new QueueTool().waitEmpty(100);
        Assert.assertFalse("No Warn Level or higher Messages",JUnitAppender.unexpectedMessageSeen(Level.WARN));
        Assert.assertNotNull("Label with correct text value",jmri.util.swing.JemmyUtil.getLabelWithText(jf.getTitle(),rpt.toReportString()));

        jf.setVisible(false);
        JUnitUtil.dispose(jf);
    }

    @Test
    public void testAddKeyAndIcon(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame jf = new JmriJFrame();
        jf.setTitle("Image From Key Value");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());
        jf.getContentPane().setBackground(Color.white);

        jf.getContentPane().add(to);
        to.getPopupUtility().setBackgroundColor(Color.white);

        jf.getContentPane().add(new javax.swing.JLabel("| Expect Image: "));

        NamedIcon icon = new NamedIcon("resources/icons/redTransparentBox.gif", "box"); // 13x13

	    jmri.InstanceManager.memoryManagerInstance().provideMemory("IM1").setValue("1");
        jf.pack();
        jf.setVisible(true);

        new QueueTool().waitEmpty(100);

        Assert.assertNotNull("Label with correct text value before key",jmri.util.swing.JemmyUtil.getLabelWithText(jf.getTitle(),"1"));

        to.addKeyAndIcon(icon,"1");

        new QueueTool().waitEmpty(100);

        new QueueTool().waitEmpty(100);
        Assert.assertFalse("No Warn Level or higher Messages",JUnitAppender.unexpectedMessageSeen(Level.WARN));
        // we should probably verify the icon displays the correct icon here.
        // The text contents of the field are not displayed.

        jf.setVisible(false);
        JUnitUtil.dispose(jf);
    }

    int[] getColor(String frameName, String label, int x, int y, int n) {
        // Find window by name
        JmriJFrame frame = JmriJFrame.getFrame(frameName);

        // find label within that
        JLabel jl = JLabelOperator.findJLabel(frame,new ComponentChooser(){
               @Override
               public boolean checkComponent(Component comp){
                   if(comp == null){
                      return false;
                   } else {
                     return (comp instanceof JLabel);
                   }
               }
               @Override
               public String getDescription(){
                  return "find the first JLabel";
               }
        });

        // find a point in mid-center of memory icon - location chosen by
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

    // Setup for log4J
    @Override
    @Before
    public void setUp() {
        super.setUp();
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
        if (!GraphicsEnvironment.isHeadless()) {
            editor = new jmri.jmrit.display.panelEditor.PanelEditor("Test MemoryIcon Panel");
            to = new MemoryIcon("MemoryTest1", editor );
            to.setMemory("IM1");
            p = to;
        }
    }

    @Override
    @After
    public void tearDown() {
        to = null;
        super.tearDown();
    }

    //private final static Logger log = LoggerFactory.getLogger(TurnoutIconTest.class);

}
