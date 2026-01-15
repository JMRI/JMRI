package jmri.jmrix.pricom.pockettester;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the MonitorFrame class
 *
 * @author Bob Jacobsen Copyright 2005
 */
public class PacketTableFrameTest {

    @Test
    @DisabledIfHeadless
    public void testCreate() {

        PacketTableFrame packetTableFrame = new PacketTableFrame();
        Assertions.assertNotNull(packetTableFrame);
    }

    // create and show, with some data present
    @Test
    @DisabledIfHeadless
    public void testShow() {

        PacketTableFrame f = new PacketTableFrame();
        f.initComponents();
        f.setVisible(true);
        f.asciiFormattedMessage(TestConstants.speed012A);
        f.asciiFormattedMessage(TestConstants.speed0123A);
        f.asciiFormattedMessage(TestConstants.speed012A);
        f.asciiFormattedMessage(TestConstants.acc0222A);

        // close frame
        f.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
