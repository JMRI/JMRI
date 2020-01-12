package jmri.jmrit.sendpacket;

import java.awt.GraphicsEnvironment;
import org.junit.*;

/**
 * Tests for classes in the jmri.jmrit.sendpacket package
 *
 * @author	Bob Jacobsen Copyright 2003
 */
public class SendPacketTest {

    @Test
    public void testFrameCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SendPacketFrame t = new SendPacketFrame();
        Assert.assertNotNull(t);
    }

    @Test
    public void testPacketNull() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SendPacketFrame t = new SendPacketFrame();
        byte[] m = t.createPacket("");
        Assert.assertNull("null pointer", m);
    }

    @Test
    public void testPacketCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SendPacketFrame t = new SendPacketFrame();
        byte[] m = t.createPacket("12 34 AB 3 19 6 B B1");
        Assert.assertEquals("length", 8, m.length);
        Assert.assertEquals("0th byte", 0x12, m[0] & 0xFF);
        Assert.assertEquals("1st byte", 0x34, m[1] & 0xFF);
        Assert.assertEquals("2nd byte", 0xAB, m[2] & 0xFF);
        Assert.assertEquals("3rd byte", 0x03, m[3] & 0xFF);
        Assert.assertEquals("4th byte", 0x19, m[4] & 0xFF);
        Assert.assertEquals("5th byte", 0x06, m[5] & 0xFF);
        Assert.assertEquals("6th byte", 0x0B, m[6] & 0xFF);
        Assert.assertEquals("7th byte", 0xB1, m[7] & 0xFF);
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
