package jmri.jmrix.loconet.locostats.swing;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.util.JmriJFrame;
import org.junit.*;

/**
 * Tests for the LocoStatsFrame class
 *
 * @author	Bob Jacobsen Copyright (C) 2006, 2008, 2010
 */
public class LocoStatsFrameTest {

    LocoStatsPanel getFrame(String title, int offset) throws Exception {
        JmriJFrame f = new JmriJFrame();
        LocoStatsPanel p = new LocoStatsPanel() {
            @Override
            public void requestUpdate() {  // replace actual transmit
                updateRequestPending = true;
            }
        };
        p.initComponents();
        f.getContentPane().add(p);
        f.setTitle(title);
        f.setLocation(0, offset);
        f.pack();
        f.setVisible(true);
        return p;
    }

    @Test
    public void testDefaultFormat() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        getFrame("Default LocoStats Window", 0);
        JFrame f = jmri.util.JmriJFrame.getFrame("Default LocoStats Window");
        Assert.assertTrue("found frame", f != null);
        f.dispose();
    }

    @Test
    public void testLocoBufferFormat() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LocoStatsPanel p = getFrame("LocoBuffer Stats Window", 150);
        p.requestUpdate();
        p.stats = new jmri.jmrix.loconet.locostats.LocoStatsFunc(null); // initialize with a null traffic controller
        p.stats.message(new LocoNetMessage(
                new int[]{LnConstants.OPC_PEER_XFER, 0x10, 0x50, 0x50, 0x01, 0x0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        ));
        JFrame f = jmri.util.JmriJFrame.getFrame("LocoBuffer Stats Window");
        Assert.assertTrue("found frame", f != null);
        f.dispose();
    }

    @Test
    public void testPR2Format() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LocoStatsPanel p = getFrame("PR2 Stats Window", 300);
        p.requestUpdate();
        p.stats = new jmri.jmrix.loconet.locostats.LocoStatsFunc(null); // initialize with a null traffic controller
        p.stats.message(new LocoNetMessage(
                new int[]{LnConstants.OPC_PEER_XFER, 0x10, 0x22, 0x22, 0x01,
                    0x00, 1, 2, 0, 4,
                    0x00, 5, 6, 0, 0,
                    0}
        ));
        JFrame f = jmri.util.JmriJFrame.getFrame("PR2 Stats Window");
        Assert.assertTrue("found frame", f != null);
        f.dispose();
    }

    @Test
    public void testMS100Format() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LocoStatsPanel p = getFrame("MS100 Stats Window", 450);
        p.requestUpdate();
        Assert.assertNotNull("p isn't supposed to be null",p);
        p.stats = new jmri.jmrix.loconet.locostats.LocoStatsFunc(null); // initialize with a null traffic controller
        p.stats.message(new LocoNetMessage(
                new int[]{LnConstants.OPC_PEER_XFER, 0x10, 0x22, 0x22, 0x01,
                    0x00, 1, 2, 0x20, 4,
                    0x00, 5, 6, 0, 0,
                    0}
        ));
        JFrame f = jmri.util.JmriJFrame.getFrame("MS100 Stats Window");
        Assert.assertTrue("found frame", f != null);
        f.dispose();
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}
