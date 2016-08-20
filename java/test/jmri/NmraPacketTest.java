/**
 * NmraPacketTest.java
 *
 * Description:
 *
 * @author	Bob Jacobsen
 * @version
 */
package jmri;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class NmraPacketTest extends TestCase {

    // output values for some of these tests were provided by Bob Scheffler
    // create an accessory decoder packet
    public void testAccDecoderPacket1() {
        // test fixed bits
        byte[] ba = NmraPacket.accDecoderPkt(1, 0, 0);
        Assert.assertEquals("first byte ", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xF0, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x71, ba[2] & 0xFF);
    }

    public void testAccDecoderPacket2() {
        // test C bit
        byte[] ba = NmraPacket.accDecoderPkt(1, 1, 0);
        Assert.assertEquals("first byte ", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xF8, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x79, ba[2] & 0xFF);
    }

    public void testAccDecoderPacket3() {
        // test D bits
        byte[] ba = NmraPacket.accDecoderPkt(1, 0, 7);
        Assert.assertEquals("first byte ", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xF7, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x76, ba[2] & 0xFF);
    }

    public void testAccDecoderPacket4() {
        // test short part of address
        byte[] ba = NmraPacket.accDecoderPkt(3, 0, 0);
        Assert.assertEquals("first byte ", 0x83, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xF0, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x73, ba[2] & 0xFF);
    }

    public void testAccDecoderPacket5() {
        // test top part of address
        byte[] ba = NmraPacket.accDecoderPkt(128, 0, 0);
        Assert.assertEquals("first byte ", 0x80, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xD0, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x50, ba[2] & 0xFF);
    }

    public void testAccDecoderPacket6() {
        // "typical packet" test
        byte[] ba = NmraPacket.accDecoderPkt(33, 1, 5);
        Assert.assertEquals("first byte ", 0xA1, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xFD, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x5C, ba[2] & 0xFF);
    }

    public void testAccDecoderPacket7() {
        // address 256
        byte[] ba = NmraPacket.accDecoderPkt(256, true);
        Assert.assertEquals("first byte ", 0x80, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xEF, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x6F, ba[2] & 0xFF);
    }

    public void testAccDecoderPacket8() {
        // address 257
        byte[] ba = NmraPacket.accDecoderPkt(257, true);
        Assert.assertEquals("first byte ", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xE9, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x68, ba[2] & 0xFF);
    }

    public void testAccDecoderPacket9() {
        // address 512
        byte[] ba = NmraPacket.accDecoderPkt(512, true);
        Assert.assertEquals("first byte ", 0x80, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xDF, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x5F, ba[2] & 0xFF);
    }

    public void testAccDecoderPacket10() {
        // address 513
        byte[] ba = NmraPacket.accDecoderPkt(513, true);
        Assert.assertEquals("first byte ", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xD9, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x58, ba[2] & 0xFF);
    }

    public void testAccDecoderPacket11() {
        // address 1024
        byte[] ba = NmraPacket.accDecoderPkt(1024, true);
        Assert.assertEquals("first byte ", 0x80, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xBF, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x3F, ba[2] & 0xFF);
    }

    public void testAccDecoderPacket12() {
        // address 1025
        byte[] ba = NmraPacket.accDecoderPkt(1025, true);
        Assert.assertEquals("first byte ", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xB9, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x38, ba[2] & 0xFF);
    }

    public void testOpsModeLong() {
        // "typical packet" test
        byte[] ba = NmraPacket.opsCvWriteByte(2065, true, 21, 75);
        Assert.assertEquals("first byte ", 0xC8, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0x11, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0xEC, ba[2] & 0xFF);
        Assert.assertEquals("fourth byte ", 0x14, ba[3] & 0xFF);
        Assert.assertEquals("fifth byte ", 0x4B, ba[4] & 0xFF);
        Assert.assertEquals("sixth byte ", 0x6A, ba[5] & 0xFF);
    }

    public void testOpsModeShort() {
        // "typical packet" test
        byte[] ba = NmraPacket.opsCvWriteByte(65, false, 21, 75);
        Assert.assertEquals("first byte ", 0x41, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xEC, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x14, ba[2] & 0xFF);
        Assert.assertEquals("fourth byte ", 0x4B, ba[3] & 0xFF);
        Assert.assertEquals("fifth byte ", 0xF2, ba[4] & 0xFF);
    }

    public void testAnalog1() {
        // "typical packet" test
        byte[] ba = NmraPacket.analogControl(60, false, 1, 00);
        Assert.assertEquals("first byte ", 0x3C, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0x3D, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x01, ba[2] & 0xFF);
        Assert.assertEquals("fourth byte ", 0x00, ba[3] & 0xFF);
        Assert.assertEquals("fifth byte ", 0x3C ^ 0x3D ^ 0x01, ba[4] & 0xFF);
    }

    public void testAnalog2() {
        // "typical packet" test
        byte[] ba = NmraPacket.analogControl(60, false, 1, 12);
        Assert.assertEquals("first byte ", 0x3C, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0x3D, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x01, ba[2] & 0xFF);
        Assert.assertEquals("fourth byte ", 0x0C, ba[3] & 0xFF);
        Assert.assertEquals("fifth byte ", 0x3C ^ 0x3D ^ 0x01 ^ 0x0C, ba[4] & 0xFF);
    }

    public void testF13F20A() {
        // "typical packet" test, short address
        byte[] ba = NmraPacket.function13Through20Packet(60, false, true, false, true, false, true, false, true, false);
        Assert.assertEquals("first byte ", 0x3C, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xDE, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x55, ba[2] & 0xFF);
        Assert.assertEquals("fourth byte ", 0x3C ^ 0xDE ^ 0x55, ba[3] & 0xFF);
    }

    public void testF13F20B() {
        // "typical packet" test, long address
        byte[] ba = NmraPacket.function13Through20Packet(2065, true, true, false, true, false, true, false, true, false);
        Assert.assertEquals("first byte ", 0xC8, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0x11, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0xDE, ba[2] & 0xFF);
        Assert.assertEquals("fourth byte ", 0x55, ba[3] & 0xFF);
        Assert.assertEquals("fifth byte ", 0xC8 ^ 0x11 ^ 0xDE ^ 0x55, ba[4] & 0xFF);
    }

    public void testF21F28A() {
        // "typical packet" test, short address
        byte[] ba = NmraPacket.function21Through28Packet(60, false, true, false, true, false, true, false, true, false);
        Assert.assertEquals("first byte ", 0x3C, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xDF, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x55, ba[2] & 0xFF);
        Assert.assertEquals("fourth byte ", 0x3C ^ 0xDF ^ 0x55, ba[3] & 0xFF);
    }

    public void testF21F28B() {
        // "typical packet" test, long address
        byte[] ba = NmraPacket.function21Through28Packet(2065, true, true, false, true, false, true, false, true, false);
        Assert.assertEquals("first byte ", 0xC8, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0x11, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0xDF, ba[2] & 0xFF);
        Assert.assertEquals("fourth byte ", 0x55, ba[3] & 0xFF);
        Assert.assertEquals("fifth byte ", 0xC8 ^ 0x11 ^ 0xDF ^ 0x55, ba[4] & 0xFF);
    }

    public void testConsist1() {
        // "typical packet" test
        byte[] ba = NmraPacket.consistControl(60, false, 1, true);
        Assert.assertEquals("first byte ", 0x3C, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0x12, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x01, ba[2] & 0xFF);
        Assert.assertEquals("fourth byte ", 0x3C ^ 0x12 ^ 0x01, ba[3] & 0xFF);
    }

    public void testConsist2() {
        // "typical packet" test
        byte[] ba = NmraPacket.consistControl(2065, true, 12, false);
        Assert.assertEquals("first byte ", 0xC8, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0x11, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x13, ba[2] & 0xFF);
        Assert.assertEquals("fourth byte ", 0x0C, ba[3] & 0xFF);
        Assert.assertEquals("fifth byte ", 0xC8 ^ 0x11 ^ 0x13 ^ 0x0C, ba[4] & 0xFF);
    }

    public void testConsist3() {
        // "typical packet" test
        byte[] ba = NmraPacket.consistControl(2065, true, 0, false);
        Assert.assertEquals("first byte ", 0xC8, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0x11, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x13, ba[2] & 0xFF);
        Assert.assertEquals("fourth byte ", 0x00, ba[3] & 0xFF);
        Assert.assertEquals("fifth byte ", 0xC8 ^ 0x11 ^ 0x13 ^ 0x00, ba[4] & 0xFF);
    }

    public void testIsAccSignalDecoderPktOK() {
        byte[] ba = NmraPacket.accSignalDecoderPkt(123, 12);
        Assert.assertTrue(NmraPacket.isAccSignalDecoderPkt(ba));
    }

    public void testIsAccSignalDecoderPktFalseConsist() {
        byte[] ba = NmraPacket.consistControl(2065, true, 0, false);
        Assert.assertFalse(NmraPacket.isAccSignalDecoderPkt(ba));
    }

    public void testIsAccSignalDecoderPktFalseFunction() {
        byte[] ba = NmraPacket.function21Through28Packet(2065, true, true, false, true, false, true, false, true, false);
        Assert.assertFalse(NmraPacket.isAccSignalDecoderPkt(ba));
    }

    public void testIsAccSignalDecoderPktFalseAnalog() {
        byte[] ba = NmraPacket.analogControl(60, false, 1, 12);
        Assert.assertFalse(NmraPacket.isAccSignalDecoderPkt(ba));
    }

    public void testIsAccSignalDecoderPktFalseOpsWrite() {
        byte[] ba = NmraPacket.opsCvWriteByte(65, false, 21, 75);
        Assert.assertFalse(NmraPacket.isAccSignalDecoderPkt(ba));
    }

    public void testIsAccSignalDecoderPktFalseAccDecoder() {
        byte[] ba = NmraPacket.accDecoderPkt(257, true);
        Assert.assertFalse(NmraPacket.isAccSignalDecoderPkt(ba));
    }

    public void testGetAccSignalDecoderPktAddr1() {
        int addr = 1;
        byte[] ba = NmraPacket.accSignalDecoderPkt(addr, 12);
        Assert.assertEquals(addr, NmraPacket.getAccSignalDecoderPktAddress(ba));
    }

    public void testGetAccSignalDecoderPktAddr2() {
        int addr = 2;
        byte[] ba = NmraPacket.accSignalDecoderPkt(addr, 12);
        Assert.assertEquals(addr, NmraPacket.getAccSignalDecoderPktAddress(ba));
    }

    public void testGetAccSignalDecoderPktAddr4() {
        int addr = 4;
        byte[] ba = NmraPacket.accSignalDecoderPkt(addr, 12);
        Assert.assertEquals(addr, NmraPacket.getAccSignalDecoderPktAddress(ba));
    }

    public void testGetAccSignalDecoderPktAddr8() {
        int addr = 8;
        byte[] ba = NmraPacket.accSignalDecoderPkt(addr, 12);
        Assert.assertEquals(addr, NmraPacket.getAccSignalDecoderPktAddress(ba));
    }

    public void testGetAccSignalDecoderPktAddr16() {
        int addr = 16;
        byte[] ba = NmraPacket.accSignalDecoderPkt(addr, 12);
        Assert.assertEquals(addr, NmraPacket.getAccSignalDecoderPktAddress(ba));
    }

    public void testGetAccSignalDecoderPktAddr32() {
        int addr = 32;
        byte[] ba = NmraPacket.accSignalDecoderPkt(addr, 12);
        Assert.assertEquals(addr, NmraPacket.getAccSignalDecoderPktAddress(ba));
    }

    public void testGetAccSignalDecoderPktAddr64() {
        int addr = 64;
        byte[] ba = NmraPacket.accSignalDecoderPkt(addr, 12);
        Assert.assertEquals(addr, NmraPacket.getAccSignalDecoderPktAddress(ba));
    }

    public void testGetAccSignalDecoderPktAddr128() {
        int addr = 128;
        byte[] ba = NmraPacket.accSignalDecoderPkt(addr, 12);
        Assert.assertEquals(addr, NmraPacket.getAccSignalDecoderPktAddress(ba));
    }

    public void testGetAccSignalDecoderPktAddr256() {
        int addr = 256;
        byte[] ba = NmraPacket.accSignalDecoderPkt(addr, 12);
        Assert.assertEquals(addr, NmraPacket.getAccSignalDecoderPktAddress(ba));
    }

    public void testGetAccSignalDecoderPktAddr512() {
        int addr = 512;
        byte[] ba = NmraPacket.accSignalDecoderPkt(addr, 12);
        Assert.assertEquals(addr, NmraPacket.getAccSignalDecoderPktAddress(ba));
    }

    public void testGetAccSignalDecoderPktAddr1024() {
        int addr = 1024;
        byte[] ba = NmraPacket.accSignalDecoderPkt(addr, 12);
        Assert.assertEquals(addr, NmraPacket.getAccSignalDecoderPktAddress(ba));
    }

    public void testGetAccSignalDecoderPktAddr2044() { // max valid value
        int addr = 2044;
        byte[] ba = NmraPacket.accSignalDecoderPkt(addr, 12);
        Assert.assertEquals(addr, NmraPacket.getAccSignalDecoderPktAddress(ba));
    }

    public void testAccDecoderPktOpsModeLegacy1() {
        int address = 12;
        int cv = 556;
        int data = 34;
        byte[] ba = NmraPacket.accDecoderPktOpsMode(address, cv, data);

        // the following values have not been independently validated
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0x83, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0xFE, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEE, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x2B, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", data, ba[4] & 0xFF);
    }

    public void testAccDecoderPktOpsModeLegacy2() {
        int address = 13;
        int cv = 557;
        int data = 34;
        byte[] ba = NmraPacket.accDecoderPktOpsMode(address, cv, data);

        // the following values have not been independently validated
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0x84, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0xF8, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEE, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x2C, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", data, ba[4] & 0xFF);
    }

    public void testExtractAddressTypeAcc() {
        byte[] ba = NmraPacket.accSignalDecoderPkt(123, 12);
        Assert.assertEquals("Accessory", NmraPacket.DccAddressType.ACCESSORY_ADDRESS, NmraPacket.extractAddressType(ba));
    }

    public void testExtractAddressTypeShort() {
        byte[] bs = NmraPacket.function13Through20Packet(60, false, true, false, true, false, true, false, true, false);
        Assert.assertEquals("Short Loco", NmraPacket.DccAddressType.LOCO_SHORT_ADDRESS, NmraPacket.extractAddressType(bs));
    }

    public void testExtractAddressTypeLong() {
        byte[] bl = NmraPacket.function13Through20Packet(2060, true, true, false, true, false, true, false, true, false);
        Assert.assertEquals("Long Loco", NmraPacket.DccAddressType.LOCO_LONG_ADDRESS, NmraPacket.extractAddressType(bl));
    }

    public void testExtractAddressNumberAcc() {
        byte[] ba = NmraPacket.accSignalDecoderPkt(123, 12);
        NmraPacket.extractAddressNumber(ba);
        jmri.util.JUnitAppender.assertWarnMessage("extractAddressNumber can't handle ACCESSORY_ADDRESS in 9F 75 0C E6 ");
    }

    public void testExtractAddressNumberShort() {
        byte[] bs = NmraPacket.function13Through20Packet(60, false, true, false, true, false, true, false, true, false);
        Assert.assertEquals("Short Loco", 60, NmraPacket.extractAddressNumber(bs));
    }

    public void testExtractAddressNumberLong() {
        byte[] bl = NmraPacket.function13Through20Packet(2060, true, true, false, true, false, true, false, true, false);
        Assert.assertEquals("Long Loco", 2060, NmraPacket.extractAddressNumber(bl));
    }

    // from here down is testing infrastructure
    public NmraPacketTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NmraPacketTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(NmraPacketTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

}
