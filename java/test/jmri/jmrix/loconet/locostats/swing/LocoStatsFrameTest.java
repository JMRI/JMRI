package jmri.jmrix.loconet.locostats.swing;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.swing.JFrame;

import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.util.JmriJFrame;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Tests for the LocoStatsFrame class
 *
 * @author Bob Jacobsen Copyright (C) 2006, 2008, 2010
 */
public class LocoStatsFrameTest {

    LocoStatsPanel getFrame(String title, int offset) {
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
    @DisabledIfHeadless
    public void testDefaultFormat() {

        getFrame("Default LocoStats Window", 0);
        JFrame f = JmriJFrame.getFrame("Default LocoStats Window");
        assertNotNull( f, "found frame");
        JUnitUtil.dispose(f);
    }

    @Test
    @DisabledIfHeadless
    public void testLocoBufferFormat() {

        LocoStatsPanel p = getFrame("LocoBuffer Stats Window", 150);
        p.requestUpdate();
        p.stats = new jmri.jmrix.loconet.locostats.LocoStatsFunc(null); // initialize with a null traffic controller
        p.stats.message(new LocoNetMessage(
                new int[]{LnConstants.OPC_PEER_XFER, 0x10, 0x50, 0x50, 0x01, 0x0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        ));
        JFrame f = JmriJFrame.getFrame("LocoBuffer Stats Window");
        assertNotNull( f, "found frame");
        JUnitUtil.dispose(f);
    }

    @Test
    @DisabledIfHeadless
    public void testPR2Format() {

        LocoStatsPanel p = getFrame("PR2 Stats Window", 300);
        p.requestUpdate();
        p.stats = new jmri.jmrix.loconet.locostats.LocoStatsFunc(null); // initialize with a null traffic controller
        p.stats.message(new LocoNetMessage(
                new int[]{LnConstants.OPC_PEER_XFER, 0x10, 0x22, 0x22, 0x01,
                    0x00, 1, 2, 0, 4,
                    0x00, 5, 6, 0, 0,
                    0}
        ));
        JFrame f = JmriJFrame.getFrame("PR2 Stats Window");
        assertNotNull( f, "found frame");
        JUnitUtil.dispose(f);
    }

    @Test
    @DisabledIfHeadless
    public void testMS100Format() {

        LocoStatsPanel p = getFrame("MS100 Stats Window", 450);
        p.requestUpdate();
        assertNotNull( p, "p isn't supposed to be null");
        p.stats = new jmri.jmrix.loconet.locostats.LocoStatsFunc(null); // initialize with a null traffic controller
        p.stats.message(new LocoNetMessage(
                new int[]{LnConstants.OPC_PEER_XFER, 0x10, 0x22, 0x22, 0x01,
                    0x00, 1, 2, 0x20, 4,
                    0x00, 5, 6, 0, 0,
                    0}
        ));
        JFrame f = JmriJFrame.getFrame("MS100 Stats Window");
        assertNotNull( f, "found frame");
        JUnitUtil.dispose(f);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
