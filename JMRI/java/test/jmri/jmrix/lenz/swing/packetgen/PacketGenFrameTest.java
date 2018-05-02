package jmri.jmrix.lenz.swing.packetgen;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.lenz.XNetMessage;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.lenz.packetgen.PacketGenFrame class
 *
 * @author	Bob Jacobsen Copyright (c) 2001, 2002
 */
public class PacketGenFrameTest {

    @Test
    public void testFrameCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PacketGenFrame packetGenFrame = new PacketGenFrame();
        Assert.assertNotNull(packetGenFrame);
    }

    @Test
    public void testPacketNull() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PacketGenFrame t = new PacketGenFrame();
        XNetMessage m = t.createPacket("");
        Assert.assertEquals("null pointer", null, m);
    }

    @Test
    public void testPacketCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PacketGenFrame t = new PacketGenFrame();
        AbstractMRMessage m = t.createPacket("12 34 AB 3 19 6 B B1");
        Assert.assertEquals("length", 8, m.getNumDataElements());
        Assert.assertEquals("0th byte", 0x12, m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x34, m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0xAB, m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", 0x03, m.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", 0x19, m.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", 0x06, m.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", 0x0B, m.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", 0xB1, m.getElement(7) & 0xFF);
    }
}
