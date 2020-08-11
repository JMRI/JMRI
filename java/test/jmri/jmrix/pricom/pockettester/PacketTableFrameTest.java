package jmri.jmrix.pricom.pockettester;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * JUnit tests for the MonitorFrame class
 *
 * @author Bob Jacobsen Copyright 2005
 */
public class PacketTableFrameTest {

    @Test
    public void testCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PacketTableFrame packetTableFrame = new PacketTableFrame();
        Assert.assertNotNull(packetTableFrame);
    }

    // create and show, with some data present
    @Test
    public void testShow() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
