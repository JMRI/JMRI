package jmri.jmrix.pricom.pockettester;

import java.awt.GraphicsEnvironment;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

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
}
