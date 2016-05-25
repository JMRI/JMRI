package jmri.jmrit.sendpacket;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for classes in the jmri.jmrit.sendpacket package
 *
 * @author	Bob Jacobsen Copyright 2003
 * @version	$Revision$
 */
public class SendPacketTest extends TestCase {

    public void testFrameCreate() {
        new SendPacketFrame();
    }

    public void testPacketNull() {
        SendPacketFrame t = new SendPacketFrame();
        byte[] m = t.createPacket("");
        Assert.assertEquals("null pointer", null, m);
    }

    public void testPacketCreate() {
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

    // from here down is testing infrastructure
    public SendPacketTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SendPacketTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SendPacketTest.class);
        return suite;
    }

}
