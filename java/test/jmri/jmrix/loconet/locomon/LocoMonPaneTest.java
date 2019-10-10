
package jmri.jmrix.loconet.locomon;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.AbstractMonPaneScaffold;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test of LocoMonPane
 * 
 * Initially written to test filtering
 *
 * @author	Bob Jacobsen   Copyright 2015
 */
public class LocoMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {

    @Test
    public void testInput() throws Exception {
        pane.initComponents();
        LocoNetMessage m = new LocoNetMessage(new int[]{0xA0, 0x07, 0x00, 0x58});
        ((LocoMonPane)pane).message(m);
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertEquals("shows message", "Set speed of loco in slot 7 to 0.\n", ((LocoMonPane)pane).getFrameText());
    }

    @Test
    public void testFilterNot() throws Exception {
        pane.initComponents();
        // filter not match
        pane.setFilterText("A1");
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertEquals("filter set", "A1", ((LocoMonPane)pane).getFilterText());
        
        LocoNetMessage m = new LocoNetMessage(new int[]{0xA0, 0x07, 0x00, 0x58});
        ((LocoMonPane)pane).message(m);
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertEquals("shows message", "Set speed of loco in slot 7 to 0.\n", ((LocoMonPane)pane).getFrameText());
    }

    @Test
    public void testFilterSimple() throws Exception {
        pane.initComponents();
        // filter A0
        pane.setFilterText("A0");
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertEquals("filter set", "A0", pane.getFilterText());
        
        LocoNetMessage m = new LocoNetMessage(new int[]{0xA0, 0x07, 0x00, 0x58});
        ((LocoMonPane)pane).message(m);
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertEquals("shows message", "", ((LocoMonPane)pane).getFrameText());
    }

    @Test
    public void testFilterMultiple() throws Exception {
        pane.initComponents();
        // filter A0
        pane.setFilterText("B1 A0");
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertEquals("filter set", "B1 A0", pane.getFilterText());
        
        LocoNetMessage m = new LocoNetMessage(new int[]{0xA0, 0x07, 0x00, 0x58});
        ((LocoMonPane)pane).message(m);
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertEquals("shows message", "", pane.getFrameText());
    }

    // Test checking the AutoScroll checkbox.
    // for some reason the LocoMonPane has the checkbox value reversed on
    // startup compared to other AbstractMonPane derivatives.
    @Override
    @Test
    public void checkAutoScrollCheckBox(){
         Assume.assumeFalse(GraphicsEnvironment.isHeadless());
         AbstractMonPaneScaffold s = new AbstractMonPaneScaffold(pane);

         // for Jemmy to work, we need the pane inside of a frame
         JmriJFrame f = new JmriJFrame();
         try{
            pane.initComponents();
         } catch(Exception ex) {
           Assert.fail("Could not load pane: " + ex);
         }
         f.add(pane);
         // set title if available
         if (pane.getTitle() != null) {
             f.setTitle(pane.getTitle());
         }
         f.pack();
         f.setVisible(true);
         Assert.assertTrue(s.getAutoScrollCheckBoxValue());
         s.checkAutoScrollCheckBox();
         Assert.assertFalse(s.getAutoScrollCheckBoxValue());
         f.setVisible(false);
         f.dispose();
    }

    jmri.TurnoutManager l;
    jmri.SensorManager s;
    jmri.ReporterManager r;

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDefaultUserMessagePreferences();

        // prepare an interface, register
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo("L", "LocoNet");
        jmri.jmrix.loconet.LocoNetInterfaceScaffold lnis = new jmri.jmrix.loconet.LocoNetInterfaceScaffold(memo);
        // create and register the manager object
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        l = new jmri.jmrix.loconet.LnTurnoutManager(memo, lnis, false);
        jmri.InstanceManager.setTurnoutManager(l);

        jmri.util.JUnitUtil.initInternalSensorManager();
        s = new jmri.jmrix.loconet.LnSensorManager(memo);
        jmri.InstanceManager.setSensorManager(s);

        jmri.util.JUnitUtil.initReporterManager();
        r = new jmri.jmrix.loconet.LnReporterManager(memo);
        jmri.InstanceManager.setReporterManager(r);

        // pane for AbstractMonFrameTestBase, panel for JmriPanelTest
        panel = pane = new LocoMonPane();
        helpTarget = "package.jmri.jmrix.loconet.locomon.LocoMonFrame";
        title = Bundle.getMessage("MenuItemLocoNetMonitor"); 
    }

    @Override
    @After
    public void tearDown() {
        pane.dispose();
        
        l.dispose();
        s.dispose();
        r.dispose();

        jmri.util.JUnitUtil.tearDown();
    }

}
