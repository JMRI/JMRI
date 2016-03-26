package jmri.jmrix.loconet.locostats;

import javax.swing.JFrame;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.util.JmriJFrame;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the LocoStatsFrame class
 *
 * @author	Bob Jacobsen Copyright (C) 2006, 2008, 2010
 */
public class LocoStatsFrameTest extends TestCase {

    LocoStatsPanel getFrame(String title, int offset) throws Exception {
        JmriJFrame f = new JmriJFrame();
        LocoStatsPanel p = new LocoStatsPanel() {
            public void requestUpdate() {  // replace actual transmit
                updatePending = true;
            }

            void report(String m) {
            }  // suppress messages
        };
        p.initComponents();
        f.getContentPane().add(p);
        f.setTitle(title);
        f.setLocation(0, offset);
        f.pack();
        f.setVisible(true);
        return p;
    }

    public void testDefaultFormat() throws Exception {
        getFrame("Default LocoStats Window", 0);
        JFrame f = jmri.util.JmriJFrame.getFrame("Default LocoStats Window");
        Assert.assertTrue("found frame", f != null);
        f.dispose();
    }

    public void testLocoBufferFormat() throws Exception {
        LocoStatsPanel p = getFrame("LocoBuffer Stats Window", 150);
        p.requestUpdate();
        p.message(new LocoNetMessage(
                new int[]{LnConstants.OPC_PEER_XFER, 0x10, 0x50, 0x50, 0x01, 0x0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        ));
        JFrame f = jmri.util.JmriJFrame.getFrame("LocoBuffer Stats Window");
        Assert.assertTrue("found frame", f != null);
        f.dispose();
    }

    public void testPR2Format() throws Exception {
        LocoStatsPanel p = getFrame("PR2 Stats Window", 300);
        p.requestUpdate();
        p.message(new LocoNetMessage(
                new int[]{LnConstants.OPC_PEER_XFER, 0x10, 0x22, 0x22, 0x01,
                    0x00, 1, 2, 0, 4,
                    0x00, 5, 6, 0, 0,
                    0}
        ));
        JFrame f = jmri.util.JmriJFrame.getFrame("PR2 Stats Window");
        Assert.assertTrue("found frame", f != null);
        f.dispose();
    }

    public void testMS100Format() throws Exception {
        LocoStatsPanel p = getFrame("MS100 Stats Window", 450);
        p.requestUpdate();
        p.message(new LocoNetMessage(
                new int[]{LnConstants.OPC_PEER_XFER, 0x10, 0x22, 0x22, 0x01,
                    0x00, 1, 2, 0x20, 4,
                    0x00, 5, 6, 0, 0,
                    0}
        ));
        JFrame f = jmri.util.JmriJFrame.getFrame("MS100 Stats Window");
        Assert.assertTrue("found frame", f != null);
        f.dispose();
    }

    // from here down is testing infrastructure
    public LocoStatsFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LocoStatsFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LocoStatsFrameTest.class);
        return suite;
    }
}
