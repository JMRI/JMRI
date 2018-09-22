package jmri.jmrix.pricom.pockettester;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * JUnit tests for the MonitorFrame class
 *
 * @author	Bob Jacobsen Copyright 2005
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
        f.asciiFormattedMessage(PackageTest.speed012A);
        f.asciiFormattedMessage(PackageTest.speed0123A);
        f.asciiFormattedMessage(PackageTest.speed012A);
        f.asciiFormattedMessage(PackageTest.acc0222A);

        // close frame
        f.dispose();
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
