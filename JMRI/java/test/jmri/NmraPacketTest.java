/**
 * NmraPacketTest.java
 *
 * Description:
 *
 * @author	Bob Jacobsen
 */
package jmri;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NmraPacketTest {

    // output values for some of these tests were provided by Bob Scheffler
    // create an accessory decoder packet
    @Test
    public void testAccDecoderPacket1() {
        // test fixed bits
        byte[] ba = NmraPacket.accDecoderPkt(1, 0, 0);
        Assert.assertEquals("first byte ", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xF0, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x71, ba[2] & 0xFF);
    }

    @Test
    public void testAccDecoderPacket2() {
        // test C bit
        byte[] ba = NmraPacket.accDecoderPkt(1, 1, 0);
        Assert.assertEquals("first byte ", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xF8, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x79, ba[2] & 0xFF);
    }

    @Test
    public void testAccDecoderPacket3() {
        // test D bits
        byte[] ba = NmraPacket.accDecoderPkt(1, 0, 7);
        Assert.assertEquals("first byte ", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xF7, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x76, ba[2] & 0xFF);
    }

    @Test
    public void testAccDecoderPacket4() {
        // test short part of address
        byte[] ba = NmraPacket.accDecoderPkt(3, 0, 0);
        Assert.assertEquals("first byte ", 0x83, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xF0, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x73, ba[2] & 0xFF);
    }

    @Test
    public void testAccDecoderPacket5() {
        // test top part of address
        byte[] ba = NmraPacket.accDecoderPkt(128, 0, 0);
        Assert.assertEquals("first byte ", 0x80, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xD0, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x50, ba[2] & 0xFF);
    }

    @Test
    public void testAccDecoderPacket6() {
        // "typical packet" test
        byte[] ba = NmraPacket.accDecoderPkt(33, 1, 5);
        Assert.assertEquals("first byte ", 0xA1, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xFD, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x5C, ba[2] & 0xFF);
    }

    @Test
    public void testAccDecoderPacket7() {
        // address 256
        byte[] ba = NmraPacket.accDecoderPkt(256, true);
        Assert.assertEquals("first byte ", 0x80, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xEF, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x6F, ba[2] & 0xFF);
    }

    @Test
    public void testAccDecoderPacket8() {
        // address 257
        byte[] ba = NmraPacket.accDecoderPkt(257, true);
        Assert.assertEquals("first byte ", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xE9, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x68, ba[2] & 0xFF);
    }

    @Test
    public void testAccDecoderPacket9() {
        // address 512
        byte[] ba = NmraPacket.accDecoderPkt(512, true);
        Assert.assertEquals("first byte ", 0x80, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xDF, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x5F, ba[2] & 0xFF);
    }

    @Test
    public void testAccDecoderPacket10() {
        // address 513
        byte[] ba = NmraPacket.accDecoderPkt(513, true);
        Assert.assertEquals("first byte ", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xD9, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x58, ba[2] & 0xFF);
    }

    @Test
    public void testAccDecoderPacket11() {
        // address 1024
        byte[] ba = NmraPacket.accDecoderPkt(1024, true);
        Assert.assertEquals("first byte ", 0x80, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xBF, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x3F, ba[2] & 0xFF);
    }

    @Test
    public void testAccDecoderPacket12() {
        // address 1025
        byte[] ba = NmraPacket.accDecoderPkt(1025, true);
        Assert.assertEquals("first byte ", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xB9, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x38, ba[2] & 0xFF);
    }

    @Test
    public void testAccDecoderPacket13() {
        // invalid address (0)
        int addr = 0;
        // expect this to throw exception
        boolean threw = false;
        try {
            NmraPacket.accDecoderPkt(addr, 0, 0);
            Assert.fail("Expected IllegalArgumentException not thrown");
        } catch (IllegalArgumentException ex) {
            threw = true;
        } finally {
            jmri.util.JUnitAppender.assertErrorMessage("invalid address " + addr);
        }
        Assert.assertTrue("Expected exception", threw);
    }

    @Test
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

    @Test
    public void testOpsModeShort() {
        // "typical packet" test
        byte[] ba = NmraPacket.opsCvWriteByte(65, false, 21, 75);
        Assert.assertEquals("first byte ", 0x41, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xEC, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x14, ba[2] & 0xFF);
        Assert.assertEquals("fourth byte ", 0x4B, ba[3] & 0xFF);
        Assert.assertEquals("fifth byte ", 0xF2, ba[4] & 0xFF);
    }

    @Test
    public void testAnalog1() {
        // "typical packet" test
        byte[] ba = NmraPacket.analogControl(60, false, 1, 00);
        Assert.assertEquals("first byte ", 0x3C, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0x3D, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x01, ba[2] & 0xFF);
        Assert.assertEquals("fourth byte ", 0x00, ba[3] & 0xFF);
        Assert.assertEquals("fifth byte ", 0x3C ^ 0x3D ^ 0x01, ba[4] & 0xFF);
    }

    @Test
    public void testAnalog2() {
        // "typical packet" test
        byte[] ba = NmraPacket.analogControl(60, false, 1, 12);
        Assert.assertEquals("first byte ", 0x3C, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0x3D, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x01, ba[2] & 0xFF);
        Assert.assertEquals("fourth byte ", 0x0C, ba[3] & 0xFF);
        Assert.assertEquals("fifth byte ", 0x3C ^ 0x3D ^ 0x01 ^ 0x0C, ba[4] & 0xFF);
    }

    @Test
    public void testF13F20A() {
        // "typical packet" test, short address
        byte[] ba = NmraPacket.function13Through20Packet(60, false, true, false, true, false, true, false, true, false);
        Assert.assertEquals("first byte ", 0x3C, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xDE, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x55, ba[2] & 0xFF);
        Assert.assertEquals("fourth byte ", 0x3C ^ 0xDE ^ 0x55, ba[3] & 0xFF);
    }

    @Test
    public void testF13F20B() {
        // "typical packet" test, long address
        byte[] ba = NmraPacket.function13Through20Packet(2065, true, true, false, true, false, true, false, true, false);
        Assert.assertEquals("first byte ", 0xC8, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0x11, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0xDE, ba[2] & 0xFF);
        Assert.assertEquals("fourth byte ", 0x55, ba[3] & 0xFF);
        Assert.assertEquals("fifth byte ", 0xC8 ^ 0x11 ^ 0xDE ^ 0x55, ba[4] & 0xFF);
    }

    @Test
    public void testF21F28A() {
        // "typical packet" test, short address
        byte[] ba = NmraPacket.function21Through28Packet(60, false, true, false, true, false, true, false, true, false);
        Assert.assertEquals("first byte ", 0x3C, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0xDF, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x55, ba[2] & 0xFF);
        Assert.assertEquals("fourth byte ", 0x3C ^ 0xDF ^ 0x55, ba[3] & 0xFF);
    }

    @Test
    public void testF21F28B() {
        // "typical packet" test, long address
        byte[] ba = NmraPacket.function21Through28Packet(2065, true, true, false, true, false, true, false, true, false);
        Assert.assertEquals("first byte ", 0xC8, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0x11, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0xDF, ba[2] & 0xFF);
        Assert.assertEquals("fourth byte ", 0x55, ba[3] & 0xFF);
        Assert.assertEquals("fifth byte ", 0xC8 ^ 0x11 ^ 0xDF ^ 0x55, ba[4] & 0xFF);
    }

    @Test
    public void testConsist1() {
        // "typical packet" test
        byte[] ba = NmraPacket.consistControl(60, false, 1, true);
        Assert.assertEquals("first byte ", 0x3C, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0x12, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x01, ba[2] & 0xFF);
        Assert.assertEquals("fourth byte ", 0x3C ^ 0x12 ^ 0x01, ba[3] & 0xFF);
    }

    @Test
    public void testConsist2() {
        // "typical packet" test
        byte[] ba = NmraPacket.consistControl(2065, true, 12, false);
        Assert.assertEquals("first byte ", 0xC8, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0x11, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x13, ba[2] & 0xFF);
        Assert.assertEquals("fourth byte ", 0x0C, ba[3] & 0xFF);
        Assert.assertEquals("fifth byte ", 0xC8 ^ 0x11 ^ 0x13 ^ 0x0C, ba[4] & 0xFF);
    }

    @Test
    public void testConsist3() {
        // "typical packet" test
        byte[] ba = NmraPacket.consistControl(2065, true, 0, false);
        Assert.assertEquals("first byte ", 0xC8, ba[0] & 0xFF);
        Assert.assertEquals("second byte ", 0x11, ba[1] & 0xFF);
        Assert.assertEquals("third byte ", 0x13, ba[2] & 0xFF);
        Assert.assertEquals("fourth byte ", 0x00, ba[3] & 0xFF);
        Assert.assertEquals("fifth byte ", 0xC8 ^ 0x11 ^ 0x13 ^ 0x00, ba[4] & 0xFF);
    }

    @Test
    public void testIsAccSignalDecoderPktOK() {
        byte[] ba = NmraPacket.accSignalDecoderPkt(123, 12);
        Assert.assertTrue(NmraPacket.isAccSignalDecoderPkt(ba));
    }

    @Test
    public void testIsAccSignalDecoderPktFalseConsist() {
        byte[] ba = NmraPacket.consistControl(2065, true, 0, false);
        Assert.assertFalse(NmraPacket.isAccSignalDecoderPkt(ba));
    }

    @Test
    public void testIsAccSignalDecoderPktFalseFunction() {
        byte[] ba = NmraPacket.function21Through28Packet(2065, true, true, false, true, false, true, false, true, false);
        Assert.assertFalse(NmraPacket.isAccSignalDecoderPkt(ba));
    }

    @Test
    public void testIsAccSignalDecoderPktFalseAnalog() {
        byte[] ba = NmraPacket.analogControl(60, false, 1, 12);
        Assert.assertFalse(NmraPacket.isAccSignalDecoderPkt(ba));
    }

    @Test
    public void testIsAccSignalDecoderPktFalseOpsWrite() {
        byte[] ba = NmraPacket.opsCvWriteByte(65, false, 21, 75);
        Assert.assertFalse(NmraPacket.isAccSignalDecoderPkt(ba));
    }

    @Test
    public void testIsAccSignalDecoderPktFalseAccDecoder() {
        byte[] ba = NmraPacket.accDecoderPkt(257, true);
        Assert.assertFalse(NmraPacket.isAccSignalDecoderPkt(ba));
    }

    @Test
    public void testIsAccSignalDecoderPktFalseAccDecoderPktOpsMode() {
        byte[] ba = NmraPacket.accDecoderPktOpsMode(257, 33, 5);
        Assert.assertFalse(NmraPacket.isAccSignalDecoderPkt(ba));
    }

    @Test
    public void testIsAccSignalDecoderPktFalseAccDecoderPktOpsModeLegacy() {
        byte[] ba = NmraPacket.accDecoderPktOpsModeLegacy(1843, 384, 255);
        Assert.assertFalse(NmraPacket.isAccSignalDecoderPkt(ba));
    }

    @Test
    public void testIsAccDecoderPktOpsModeFalseSignalDecoderPkt() {
        byte[] ba = NmraPacket.accSignalDecoderPkt(123, 12);
        Assert.assertFalse(NmraPacket.isAccDecoderPktOpsMode(ba));
    }

    @Test
    public void testIsAccDecoderPktOpsModeFalseConsist() {
        byte[] ba = NmraPacket.consistControl(2065, true, 0, false);
        Assert.assertFalse(NmraPacket.isAccDecoderPktOpsMode(ba));
    }

    @Test
    public void testIsAccDecoderPktOpsModeFalseFunction() {
        byte[] ba = NmraPacket.function21Through28Packet(2065, true, true, false, true, false, true, false, true, false);
        Assert.assertFalse(NmraPacket.isAccDecoderPktOpsMode(ba));
    }

    @Test
    public void testIsAccDecoderPktOpsModeFalseAnalog() {
        byte[] ba = NmraPacket.analogControl(60, false, 1, 12);
        Assert.assertFalse(NmraPacket.isAccDecoderPktOpsMode(ba));
    }

    @Test
    public void testIsAccDecoderPktOpsModeFalseOpsWrite() {
        byte[] ba = NmraPacket.opsCvWriteByte(65, false, 21, 75);
        Assert.assertFalse(NmraPacket.isAccDecoderPktOpsMode(ba));
    }

    @Test
    public void testIsAccDecoderPktOpsModeFalseAccDecoder() {
        byte[] ba = NmraPacket.accDecoderPkt(257, true);
        Assert.assertFalse(NmraPacket.isAccDecoderPktOpsMode(ba));
    }

    @Test
    public void testIsAccDecoderPktOpsModeOK() {
        byte[] ba = NmraPacket.accDecoderPktOpsMode(257, 33, 5);
        Assert.assertTrue(NmraPacket.isAccDecoderPktOpsMode(ba));
    }

    @Test
    public void testIsAccDecoderPktOpsModeFalseAccDecoderPktOpsModeLegacy() {
        byte[] ba = NmraPacket.accDecoderPktOpsModeLegacy(1843, 384, 255);
        Assert.assertFalse(NmraPacket.isAccDecoderPktOpsMode(ba));
    }

    @Test
    public void testIsAccDecoderPktOpsModeLegacyFalseSignalDecoderPkt() {
        byte[] ba = NmraPacket.accSignalDecoderPkt(123, 12);
        Assert.assertFalse(NmraPacket.isAccDecoderPktOpsModeLegacy(ba));
    }

    @Test
    public void testIsAccDecoderPktOpsModeLegacyFalseConsist() {
        byte[] ba = NmraPacket.consistControl(2065, true, 0, false);
        Assert.assertFalse(NmraPacket.isAccDecoderPktOpsModeLegacy(ba));
    }

    @Test
    public void testIsAccDecoderPktOpsModeLegacyFalseFunction() {
        byte[] ba = NmraPacket.function21Through28Packet(2065, true, true, false, true, false, true, false, true, false);
        Assert.assertFalse(NmraPacket.isAccDecoderPktOpsModeLegacy(ba));
    }

    @Test
    public void testIsAccDecoderPktOpsModeLegacyFalseAnalog() {
        byte[] ba = NmraPacket.analogControl(60, false, 1, 12);
        Assert.assertFalse(NmraPacket.isAccDecoderPktOpsModeLegacy(ba));
    }

    @Test
    public void testIsAccDecoderPktOpsModeLegacyFalseOpsWrite() {
        byte[] ba = NmraPacket.opsCvWriteByte(65, false, 21, 75);
        Assert.assertFalse(NmraPacket.isAccDecoderPktOpsModeLegacy(ba));
    }

    @Test
    public void testIsAccDecoderPktOpsModeLegacyFalseAccDecoder() {
        byte[] ba = NmraPacket.accDecoderPkt(257, true);
        Assert.assertFalse(NmraPacket.isAccDecoderPktOpsModeLegacy(ba));
    }

    @Test
    public void testIsAccDecoderPktOpsModeLegacyFalseAccDecoderPktOpsMode() {
        byte[] ba = NmraPacket.accDecoderPktOpsMode(257, 33, 5);
        Assert.assertFalse(NmraPacket.isAccDecoderPktOpsModeLegacy(ba));
    }

    @Test
    public void testIsAccDecoderPktOpsModeLegacyOK() {
        byte[] ba = NmraPacket.accDecoderPktOpsModeLegacy(1843, 384, 255);
        Assert.assertTrue(NmraPacket.isAccDecoderPktOpsModeLegacy(ba));
    }

    @Test
    public void testGetAccSignalDecoderPktAddr1() {
        int addr = 1;
        byte[] ba = NmraPacket.accSignalDecoderPkt(addr, 12);
        Assert.assertEquals(addr, NmraPacket.getAccSignalDecoderPktAddress(ba));
    }

    @Test
    public void testGetAccSignalDecoderPktAddr2() {
        int addr = 2;
        byte[] ba = NmraPacket.accSignalDecoderPkt(addr, 12);
        Assert.assertEquals(addr, NmraPacket.getAccSignalDecoderPktAddress(ba));
    }

    @Test
    public void testGetAccSignalDecoderPktAddr4() {
        int addr = 4;
        byte[] ba = NmraPacket.accSignalDecoderPkt(addr, 12);
        Assert.assertEquals(addr, NmraPacket.getAccSignalDecoderPktAddress(ba));
    }

    @Test
    public void testGetAccSignalDecoderPktAddr8() {
        int addr = 8;
        byte[] ba = NmraPacket.accSignalDecoderPkt(addr, 12);
        Assert.assertEquals(addr, NmraPacket.getAccSignalDecoderPktAddress(ba));
    }

    @Test
    public void testGetAccSignalDecoderPktAddr16() {
        int addr = 16;
        byte[] ba = NmraPacket.accSignalDecoderPkt(addr, 12);
        Assert.assertEquals(addr, NmraPacket.getAccSignalDecoderPktAddress(ba));
    }

    @Test
    public void testGetAccSignalDecoderPktAddr32() {
        int addr = 32;
        byte[] ba = NmraPacket.accSignalDecoderPkt(addr, 12);
        Assert.assertEquals(addr, NmraPacket.getAccSignalDecoderPktAddress(ba));
    }

    @Test
    public void testGetAccSignalDecoderPktAddr64() {
        int addr = 64;
        byte[] ba = NmraPacket.accSignalDecoderPkt(addr, 12);
        Assert.assertEquals(addr, NmraPacket.getAccSignalDecoderPktAddress(ba));
    }

    @Test
    public void testGetAccSignalDecoderPktAddr128() {
        int addr = 128;
        byte[] ba = NmraPacket.accSignalDecoderPkt(addr, 12);
        Assert.assertEquals(addr, NmraPacket.getAccSignalDecoderPktAddress(ba));
    }

    @Test
    public void testGetAccSignalDecoderPktAddr256() {
        int addr = 256;
        byte[] ba = NmraPacket.accSignalDecoderPkt(addr, 12);
        Assert.assertEquals(addr, NmraPacket.getAccSignalDecoderPktAddress(ba));
    }

    @Test
    public void testGetAccSignalDecoderPktAddr512() {
        int addr = 512;
        byte[] ba = NmraPacket.accSignalDecoderPkt(addr, 12);
        Assert.assertEquals(addr, NmraPacket.getAccSignalDecoderPktAddress(ba));
    }

    @Test
    public void testGetAccSignalDecoderPktAddr1024() {
        int addr = 1024;
        byte[] ba = NmraPacket.accSignalDecoderPkt(addr, 12);
        Assert.assertEquals(addr, NmraPacket.getAccSignalDecoderPktAddress(ba));
    }

    @Test
    public void testGetAccSignalDecoderPktAddr2044() { // max valid value
        int addr = 2044;
        byte[] ba = NmraPacket.accSignalDecoderPkt(addr, 12);
        Assert.assertEquals(addr, NmraPacket.getAccSignalDecoderPktAddress(ba));
    }

    @Test
    public void testAccDecPktOpsModeLegacy1() {
        int address = 1;
        int cv = 29;
        int data = 136;
        byte[] ba = NmraPacket.accDecPktOpsModeLegacy(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 5, ba.length);
        Assert.assertEquals("byte 0", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x7C, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0x1C, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x88, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0x69, ba[4] & 0xFF);

        // check packet type and reverse address lookup
        Assert.assertTrue("verify packet type", NmraPacket.isAccDecoderPktOpsModeLegacy(ba));
        Assert.assertEquals("reverse lookup of decoder address", address, NmraPacket.getAccDecPktOpsModeLegacyAddress(ba));
    }

    @Test
    public void testAccDecPktOpsMode1() {
        int address = 1;
        int cv = 29;
        int data = 136;
        byte[] ba = NmraPacket.accDecPktOpsMode(address, cv, data);

        // the following values based on NCE Power Pro output adjusted for CDDD=0000
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0xF0, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEC, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x1C, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0x88, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0x09, ba[5] & 0xFF);

        // check packet type and reverse address lookup
        Assert.assertTrue("verify packet type", NmraPacket.isAccDecoderPktOpsMode(ba));
        Assert.assertEquals("reverse lookup of decoder address", address, NmraPacket.getAccDecPktOpsModeAddress(ba));
    }

    @Test
    public void testAccDecPktOpsModeLegacy2() {
        int address = 2;
        int cv = 41;
        int data = 24;
        byte[] ba = NmraPacket.accDecPktOpsModeLegacy(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 5, ba.length);
        Assert.assertEquals("byte 0", 0x82, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x7C, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0x28, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x18, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0xCE, ba[4] & 0xFF);

        // check packet type and reverse address lookup
        Assert.assertTrue("verify packet type", NmraPacket.isAccDecoderPktOpsModeLegacy(ba));
        Assert.assertEquals("reverse lookup of decoder address", address, NmraPacket.getAccDecPktOpsModeLegacyAddress(ba));
    }

    @Test
    public void testAccDecPktOpsMode2() {
        int address = 2;
        int cv = 41;
        int data = 24;
        byte[] ba = NmraPacket.accDecPktOpsMode(address, cv, data);

        // the following values based on NCE Power Pro output adjusted for CDDD=0000
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0x82, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0xF0, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEC, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x28, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0x18, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0xAE, ba[5] & 0xFF);

        // check packet type and reverse address lookup
        Assert.assertTrue("verify packet type", NmraPacket.isAccDecoderPktOpsMode(ba));
        Assert.assertEquals("reverse lookup of decoder address", address, NmraPacket.getAccDecPktOpsModeAddress(ba));
    }

    @Test
    public void testAccDecPktOpsModeLegacy510() {
        int address = 510;
        int cv = 892;
        int data = 135;
        byte[] ba = NmraPacket.accDecPktOpsModeLegacy(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 5, ba.length);
        Assert.assertEquals("byte 0", 0xBE, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x0F, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0x7B, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x87, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0x4D, ba[4] & 0xFF);
    }

    @Test
    public void testAccDecPktOpsMode510() {
        int address = 510;
        int cv = 892;
        int data = 135;
        byte[] ba = NmraPacket.accDecPktOpsMode(address, cv, data);

        // the following values based on NCE Power Pro output adjusted for CDDD=0000
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0xBE, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x80, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEF, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x7B, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0x87, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0x2D, ba[5] & 0xFF);

        // check packet type and reverse address lookup
        Assert.assertTrue("verify packet type", NmraPacket.isAccDecoderPktOpsMode(ba));
        Assert.assertEquals("reverse lookup of decoder address", address, NmraPacket.getAccDecPktOpsModeAddress(ba));
    }

    @Test
    public void testAccDecPktOpsModeLegacy511() {
        int address = 511;
        int cv = 275;
        int data = 198;
        byte[] ba = NmraPacket.accDecPktOpsModeLegacy(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 5, ba.length);
        Assert.assertEquals("byte 0", 0xBF, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x0D, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0x12, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0xC6, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0x66, ba[4] & 0xFF);

        // check packet type and reverse address lookup
        Assert.assertTrue("verify packet type", NmraPacket.isAccDecoderPktOpsModeLegacy(ba));
        Assert.assertEquals("reverse lookup of decoder address", address, NmraPacket.getAccDecPktOpsModeLegacyAddress(ba));
    }

    @Test
    public void testAccDecPktOpsMode511() {
        int address = 511;
        int cv = 275;
        int data = 198;
        byte[] ba = NmraPacket.accDecPktOpsMode(address, cv, data);

        // the following values based on NCE Power Pro output adjusted for CDDD=0000
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0xBF, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x80, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xED, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x12, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0xC6, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0x6, ba[5] & 0xFF);

        // check packet type and reverse address lookup
        Assert.assertTrue("verify packet type", NmraPacket.isAccDecoderPktOpsMode(ba));
        Assert.assertEquals("reverse lookup of decoder address", address, NmraPacket.getAccDecPktOpsModeAddress(ba));
    }

    @Test
    public void testAccDecoderPktOpsModeLegacy1() {
        int address = 1;
        int cv = 384;
        int data = 255;
        byte[] ba = NmraPacket.accDecoderPktOpsModeLegacy(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 5, ba.length);
        Assert.assertEquals("byte 0", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x7D, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0x7F, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0xFF, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0x7C, ba[4] & 0xFF);

        // check packet type and reverse address lookup
        Assert.assertTrue("verify packet type", NmraPacket.isAccDecoderPktOpsModeLegacy(ba));
        Assert.assertEquals("reverse lookup of decoder address", decAddr(address), NmraPacket.getAccDecoderPktOpsModeLegacyAddress(ba));
    }

    @Test
    public void testAccDecoderPktOpsMode1() {
        int address = 1;
        int cv = 384;
        int data = 255;
        byte[] ba = NmraPacket.accDecoderPktOpsMode(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0xF9, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xED, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x7F, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0xFF, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0x15, ba[5] & 0xFF);

        // check packet type and reverse address lookup
        Assert.assertTrue("verify packet type", NmraPacket.isAccDecoderPktOpsMode(ba));
        Assert.assertEquals("reverse lookup of address", address, NmraPacket.getAccDecoderPktOpsModeAddress(ba));
    }

    @Test
    public void testAccDecoderPktOpsModeLegacy4() {
        int address = 4;
        int cv = 56;
        int data = 0;
        byte[] ba = NmraPacket.accDecoderPktOpsModeLegacy(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 5, ba.length);
        Assert.assertEquals("byte 0", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x7C, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0x37, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x00, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0xCA, ba[4] & 0xFF);

        // check packet type and reverse address lookup
        Assert.assertTrue("verify packet type", NmraPacket.isAccDecoderPktOpsModeLegacy(ba));
        Assert.assertEquals("reverse lookup of decoder address", decAddr(address), NmraPacket.getAccDecoderPktOpsModeLegacyAddress(ba));
    }

    @Test
    public void testAccDecoderPktOpsMode4() {
        int address = 4;
        int cv = 56;
        int data = 0;
        byte[] ba = NmraPacket.accDecoderPktOpsMode(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0xFF, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEC, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x37, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0x00, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0xA5, ba[5] & 0xFF);

        // check packet type and reverse address lookup
        Assert.assertTrue("verify packet type", NmraPacket.isAccDecoderPktOpsMode(ba));
        Assert.assertEquals("reverse lookup of address", address, NmraPacket.getAccDecoderPktOpsModeAddress(ba));
    }

    @Test
    public void testAccDecoderPktOpsModeLegacy5() {
        int address = 5;
        int cv = 1;
        int data = 30;
        byte[] ba = NmraPacket.accDecoderPktOpsModeLegacy(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 5, ba.length);
        Assert.assertEquals("byte 0", 0x82, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x7C, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0x00, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x1E, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0xE0, ba[4] & 0xFF);

        // check packet type and reverse address lookup
        Assert.assertTrue("verify packet type", NmraPacket.isAccDecoderPktOpsModeLegacy(ba));
        Assert.assertEquals("reverse lookup of decoder address", decAddr(address), NmraPacket.getAccDecoderPktOpsModeLegacyAddress(ba));
    }

    @Test
    public void testAccDecoderPktOpsMode5() {
        int address = 5;
        int cv = 1;
        int data = 30;
        byte[] ba = NmraPacket.accDecoderPktOpsMode(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0x82, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0xF9, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEC, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x00, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0x1E, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0x89, ba[5] & 0xFF);

        // check packet type and reverse address lookup
        Assert.assertTrue("verify packet type", NmraPacket.isAccDecoderPktOpsMode(ba));
        Assert.assertEquals("reverse lookup of address", address, NmraPacket.getAccDecoderPktOpsModeAddress(ba));
    }

    @Test
    public void testAccDecoderPktOpsMode252() {
        int address = 252;
        int cv = 999;
        int data = 179;
        byte[] ba = NmraPacket.accDecoderPktOpsMode(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0xBF, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0xFF, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEF, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0xE6, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0xB3, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0xFA, ba[5] & 0xFF);

        // check packet type and reverse address lookup
        Assert.assertTrue("verify packet type", NmraPacket.isAccDecoderPktOpsMode(ba));
        Assert.assertEquals("reverse lookup of address", address, NmraPacket.getAccDecoderPktOpsModeAddress(ba));
    }

    @Test
    public void testAccDecoderPktOpsMode253() {
        int address = 253;
        int cv = 1;
        int data = 241;
        byte[] ba = NmraPacket.accDecoderPktOpsMode(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0x80, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0xE9, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEC, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x00, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0xF1, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0x74, ba[5] & 0xFF);

        // check packet type and reverse address lookup
        Assert.assertTrue("verify packet type", NmraPacket.isAccDecoderPktOpsMode(ba));
        Assert.assertEquals("reverse lookup of address", address, NmraPacket.getAccDecoderPktOpsModeAddress(ba));
    }

    @Test
    public void testAccDecoderPktOpsMode256() {
        int address = 256;
        int cv = 55;
        int data = 127;
        byte[] ba = NmraPacket.accDecoderPktOpsMode(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0x80, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0xEF, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEC, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x36, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0x7F, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0xCA, ba[5] & 0xFF);

        // check packet type and reverse address lookup
        Assert.assertTrue("verify packet type", NmraPacket.isAccDecoderPktOpsMode(ba));
        Assert.assertEquals("reverse lookup of address", address, NmraPacket.getAccDecoderPktOpsModeAddress(ba));
    }

    @Test
    public void testAccDecoderPktOpsMode257() {
        int address = 257;
        int cv = 55;
        int data = 99;
        byte[] ba = NmraPacket.accDecoderPktOpsMode(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0xE9, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEC, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x36, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0x63, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0xD1, ba[5] & 0xFF);

        // check packet type and reverse address lookup
        Assert.assertTrue("verify packet type", NmraPacket.isAccDecoderPktOpsMode(ba));
        Assert.assertEquals("reverse lookup of address", address, NmraPacket.getAccDecoderPktOpsModeAddress(ba));
    }

    @Test
    public void testAccDecoderPktOpsModeLegacy2037() {
        int address = 2037;
        int cv = 556;
        int data = 175;
        byte[] ba = NmraPacket.accDecoderPktOpsModeLegacy(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 5, ba.length);
        Assert.assertEquals("byte 0", 0xBE, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x0E, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0x2B, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0xAF, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0x34, ba[4] & 0xFF);

        // check packet type and reverse address lookup
        Assert.assertTrue("verify packet type", NmraPacket.isAccDecoderPktOpsModeLegacy(ba));
        Assert.assertEquals("reverse lookup of decoder address", decAddr(address), NmraPacket.getAccDecoderPktOpsModeLegacyAddress(ba));
    }

    @Test
    public void testAccDecoderPktOpsMode2037() {
        int address = 2037;
        int cv = 556;
        int data = 175;
        byte[] ba = NmraPacket.accDecoderPktOpsMode(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0xBE, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x89, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEE, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x2B, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0xAF, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0x5D, ba[5] & 0xFF);

        // check packet type and reverse address lookup
        Assert.assertTrue("verify packet type", NmraPacket.isAccDecoderPktOpsMode(ba));
        Assert.assertEquals("reverse lookup of address", address, NmraPacket.getAccDecoderPktOpsModeAddress(ba));
    }

    @Test
    public void testAccDecoderPktOpsModeLegacy2040() {
        int address = 2040;
        int cv = 771;
        int data = 102;
        byte[] ba = NmraPacket.accDecoderPktOpsModeLegacy(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 5, ba.length);
        Assert.assertEquals("byte 0", 0xBE, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x0F, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0x02, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x66, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0xD5, ba[4] & 0xFF);

        // check packet type and reverse address lookup
        Assert.assertTrue("verify packet type", NmraPacket.isAccDecoderPktOpsModeLegacy(ba));
        Assert.assertEquals("reverse lookup of decoder address", decAddr(address), NmraPacket.getAccDecoderPktOpsModeLegacyAddress(ba));
    }

    @Test
    public void testAccDecoderPktOpsMode2040() {
        int address = 2040;
        int cv = 771;
        int data = 102;
        byte[] ba = NmraPacket.accDecoderPktOpsMode(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0xBE, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x8F, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEF, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x02, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0x66, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0xBA, ba[5] & 0xFF);

        // check packet type and reverse address lookup
        Assert.assertTrue("verify packet type", NmraPacket.isAccDecoderPktOpsMode(ba));
        Assert.assertEquals("reverse lookup of address", address, NmraPacket.getAccDecoderPktOpsModeAddress(ba));
    }

    @Test
    public void testAccDecoderPktOpsModeLegacy2044() {
        int address = 2044;
        int cv = 1024;
        int data = 151;
        byte[] ba = NmraPacket.accDecoderPktOpsModeLegacy(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 5, ba.length);
        Assert.assertEquals("byte 0", 0xBF, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x0F, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xFF, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x97, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0xD8, ba[4] & 0xFF);

        // check packet type and reverse address lookup
        Assert.assertTrue("verify packet type", NmraPacket.isAccDecoderPktOpsModeLegacy(ba));
        Assert.assertEquals("reverse lookup of decoder address", decAddr(address), NmraPacket.getAccDecoderPktOpsModeLegacyAddress(ba));
    }

    @Test
    public void testAccDecoderPktOpsMode2044() {
        int address = 2044;
        int cv = 1024;
        int data = 151;
        byte[] ba = NmraPacket.accDecoderPktOpsMode(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0xBF, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x8F, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEF, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0xFF, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0x97, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0xB7, ba[5] & 0xFF);

        // check packet type and reverse address lookup
        Assert.assertTrue("verify packet type", NmraPacket.isAccDecoderPktOpsMode(ba));
        Assert.assertEquals("reverse lookup of address", address, NmraPacket.getAccDecoderPktOpsModeAddress(ba));
    }

    @Test
    public void testAccSignalDecoderPktOpsMode1() {
        int address = 1;
        int cv = 384;
        int data = 255;
        byte[] ba = NmraPacket.accSignalDecoderPktOpsMode(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x71, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xED, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x7F, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0xFF, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0x9D, ba[5] & 0xFF);
    }

    @Test
    public void testAccSignalDecoderPktOpsMode4() {
        int address = 4;
        int cv = 56;
        int data = 0;
        byte[] ba = NmraPacket.accSignalDecoderPktOpsMode(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x77, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEC, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x37, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0x00, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0x2D, ba[5] & 0xFF);
    }

    @Test
    public void testAccSignalDecoderPktOpsMode5() {
        int address = 5;
        int cv = 1;
        int data = 30;
        byte[] ba = NmraPacket.accSignalDecoderPktOpsMode(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0x82, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x71, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEC, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x00, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0x1E, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0x01, ba[5] & 0xFF);
    }

    @Test
    public void testAccSignalDecoderPktOpsMode252() {
        int address = 252;
        int cv = 999;
        int data = 179;
        byte[] ba = NmraPacket.accSignalDecoderPktOpsMode(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0xBF, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x77, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEF, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0xE6, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0xB3, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0x72, ba[5] & 0xFF);
    }

    @Test
    public void testAccSignalDecoderPktOpsMode253() {
        int address = 253;
        int cv = 1;
        int data = 241;
        byte[] ba = NmraPacket.accSignalDecoderPktOpsMode(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0x80, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x61, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEC, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x00, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0xF1, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0xFC, ba[5] & 0xFF);
    }

    @Test
    public void testAccSignalDecoderPktOpsMode256() {
        int address = 256;
        int cv = 55;
        int data = 127;
        byte[] ba = NmraPacket.accSignalDecoderPktOpsMode(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0x80, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x67, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEC, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x36, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0x7F, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0x42, ba[5] & 0xFF);
    }

    @Test
    public void testAccSignalDecoderPktOpsMode257() {
        int address = 257;
        int cv = 55;
        int data = 99;
        byte[] ba = NmraPacket.accSignalDecoderPktOpsMode(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x61, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEC, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x36, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0x63, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0x59, ba[5] & 0xFF);
    }

    @Test
    public void testAccSignalDecoderPktOpsMode2037() {
        int address = 2037;
        int cv = 556;
        int data = 175;
        byte[] ba = NmraPacket.accSignalDecoderPktOpsMode(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0xBE, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x01, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEE, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x2B, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0xAF, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0xD5, ba[5] & 0xFF);
    }

    @Test
    public void testAccSignalDecoderPktOpsMode2040() {
        int address = 2040;
        int cv = 771;
        int data = 102;
        byte[] ba = NmraPacket.accSignalDecoderPktOpsMode(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0xBE, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x07, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEF, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x02, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0x66, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0x32, ba[5] & 0xFF);
    }

    @Test
    public void testAccSignalDecoderPktOpsMode2044() {
        int address = 2044;
        int cv = 1024;
        int data = 151;
        byte[] ba = NmraPacket.accSignalDecoderPktOpsMode(address, cv, data);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0xBF, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x07, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEF, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0xFF, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0x97, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0x3F, ba[5] & 0xFF);
    }

    @Test
    public void testAccSignalDecoderPkt1Aspect23() {
        int address = 1;
        int aspect = 23;
        byte[] ba = NmraPacket.accSignalDecoderPkt(address, aspect);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 4, ba.length);
        Assert.assertEquals("byte 0", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x71, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0x17, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0xE7, ba[3] & 0xFF);
    }

    @Test
    public void testAccSignalDecoderPkt2Aspect5() {
        int address = 2;
        int aspect = 5;
        byte[] ba = NmraPacket.accSignalDecoderPkt(address, aspect);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 4, ba.length);
        Assert.assertEquals("byte 0", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x73, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0x05, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0xF7, ba[3] & 0xFF);
    }

    @Test
    public void testAccSignalDecoderPkt3Aspect9() {
        int address = 3;
        int aspect = 9;
        byte[] ba = NmraPacket.accSignalDecoderPkt(address, aspect);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 4, ba.length);
        Assert.assertEquals("byte 0", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x75, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0x09, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0xFD, ba[3] & 0xFF);
    }

    @Test
    public void testAccSignalDecoderPkt4Aspect11() {
        int address = 4;
        int aspect = 11;
        byte[] ba = NmraPacket.accSignalDecoderPkt(address, aspect);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 4, ba.length);
        Assert.assertEquals("byte 0", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x77, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0x0B, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0xFD, ba[3] & 0xFF);
    }

    @Test
    public void testAccSignalDecoderPkt5Aspect15() {
        int address = 5;
        int aspect = 15;
        byte[] ba = NmraPacket.accSignalDecoderPkt(address, aspect);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 4, ba.length);
        Assert.assertEquals("byte 0", 0x82, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x71, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0x0F, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0xFC, ba[3] & 0xFF);
    }

    @Test
    public void testAccSignalDecoderPkt6Aspect28() {
        int address = 6;
        int aspect = 28;
        byte[] ba = NmraPacket.accSignalDecoderPkt(address, aspect);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 4, ba.length);
        Assert.assertEquals("byte 0", 0x82, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x73, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0x1C, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0xED, ba[3] & 0xFF);
    }

    @Test
    public void testAccSignalDecoderPkt7Aspect10() {
        int address = 7;
        int aspect = 10;
        byte[] ba = NmraPacket.accSignalDecoderPkt(address, aspect);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 4, ba.length);
        Assert.assertEquals("byte 0", 0x82, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x75, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0x0A, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0xFD, ba[3] & 0xFF);
    }

    @Test
    public void testAccSignalDecoderPkt8Aspect11() {
        int address = 8;
        int aspect = 11;
        byte[] ba = NmraPacket.accSignalDecoderPkt(address, aspect);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 4, ba.length);
        Assert.assertEquals("byte 0", 0x82, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x77, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0x0B, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0xFE, ba[3] & 0xFF);
    }

    @Test
    public void testAccSignalDecoderPkt252Aspect13() {
        int address = 252;
        int aspect = 13;
        byte[] ba = NmraPacket.accSignalDecoderPkt(address, aspect);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 4, ba.length);
        Assert.assertEquals("byte 0", 0xBF, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x77, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0x0D, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0xC5, ba[3] & 0xFF);
    }

    @Test
    public void testAccSignalDecoderPkt253Aspect19() {
        int address = 253;
        int aspect = 19;
        byte[] ba = NmraPacket.accSignalDecoderPkt(address, aspect);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 4, ba.length);
        Assert.assertEquals("byte 0", 0x80, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x61, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0x13, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0xF2, ba[3] & 0xFF);
    }

    @Test
    public void testAccSignalDecoderPkt254Aspect2() {
        int address = 254;
        int aspect = 2;
        byte[] ba = NmraPacket.accSignalDecoderPkt(address, aspect);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 4, ba.length);
        Assert.assertEquals("byte 0", 0x80, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x63, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0x02, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0xE1, ba[3] & 0xFF);
    }

    @Test
    public void testAccSignalDecoderPkt255Aspect3() {
        int address = 255;
        int aspect = 3;
        byte[] ba = NmraPacket.accSignalDecoderPkt(address, aspect);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 4, ba.length);
        Assert.assertEquals("byte 0", 0x80, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x65, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0x3, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0xE6, ba[3] & 0xFF);
    }

    @Test
    public void testAccSignalDecoderPkt256Aspect7() {
        int address = 256;
        int aspect = 7;
        byte[] ba = NmraPacket.accSignalDecoderPkt(address, aspect);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 4, ba.length);
        Assert.assertEquals("byte 0", 0x80, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x67, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0x07, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0xE0, ba[3] & 0xFF);
    }

    @Test
    public void testAccSignalDecoderPkt2044Aspect0() {
        int address = 2044;
        int aspect = 0;
        byte[] ba = NmraPacket.accSignalDecoderPkt(address, aspect);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 4, ba.length);
        Assert.assertEquals("byte 0", 0xBF, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x07, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0x00, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0xB8, ba[3] & 0xFF);
    }

    @Test
    public void testAccSignalDecoderPkt2044Aspect31() {
        int address = 2044;
        int aspect = 31;
        byte[] ba = NmraPacket.accSignalDecoderPkt(address, aspect);

        // the following values validated against NCE Power Pro output
        Assert.assertEquals("length", 4, ba.length);
        Assert.assertEquals("byte 0", 0xBF, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x07, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0x1F, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0xA7, ba[3] & 0xFF);
    }

    @Test
    public void testAltAccSignalDecoderPktOpsMode1() {
        int address = 1;
        int cv = 384;
        int data = 255;
        byte[] ba = NmraPacket.altAccSignalDecoderPktOpsMode(address, cv, data);

        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0x80, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x71, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xED, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x7F, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0xFF, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0x9C, ba[5] & 0xFF);
    }

    @Test
    public void testAltAccSignalDecoderPktOpsMode4() {
        int address = 4;
        int cv = 384;
        int data = 255;
        byte[] ba = NmraPacket.altAccSignalDecoderPktOpsMode(address, cv, data);

        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 1", 0x77, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xED, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x7F, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0xFF, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0x9A, ba[5] & 0xFF);
    }

    @Test
    public void testAltAccSignalDecoderPktOpsMode5() {
        int address = 5;
        int cv = 384;
        int data = 255;
        byte[] ba = NmraPacket.altAccSignalDecoderPktOpsMode(address, cv, data);

        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x71, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xED, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x7F, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0xFF, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0x9D, ba[5] & 0xFF);
    }

    @Test
    public void testAltAccSignalDecoderPktOpsMode8() {
        int address = 8;
        int cv = 56;
        int data = 0;
        byte[] ba = NmraPacket.altAccSignalDecoderPktOpsMode(address, cv, data);

        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x77, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEC, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x37, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0x00, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0x2D, ba[5] & 0xFF);
    }

    @Test
    public void testAltAccSignalDecoderPktOpsMode9() {
        int address = 9;
        int cv = 1;
        int data = 30;
        byte[] ba = NmraPacket.altAccSignalDecoderPktOpsMode(address, cv, data);

        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0x82, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x71, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEC, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x00, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0x1E, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0x01, ba[5] & 0xFF);
    }

    @Test
    public void testAltAccSignalDecoderPktOpsMode256() {
        int address = 256;
        int cv = 999;
        int data = 179;
        byte[] ba = NmraPacket.altAccSignalDecoderPktOpsMode(address, cv, data);

        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0xBF, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x77, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEF, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0xE6, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0xB3, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0x72, ba[5] & 0xFF);
    }

    @Test
    public void testAltAccSignalDecoderPktOpsMode257() {
        int address = 257;
        int cv = 1;
        int data = 241;
        byte[] ba = NmraPacket.altAccSignalDecoderPktOpsMode(address, cv, data);

        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0x80, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x61, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEC, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x00, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0xF1, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0xFC, ba[5] & 0xFF);
    }

    @Test
    public void testAltAccSignalDecoderPktOpsMode260() {
        int address = 260;
        int cv = 55;
        int data = 127;
        byte[] ba = NmraPacket.altAccSignalDecoderPktOpsMode(address, cv, data);

        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0x80, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x67, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEC, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x36, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0x7F, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0x42, ba[5] & 0xFF);
    }

    @Test
    public void testAltAccSignalDecoderPktOpsMode261() {
        int address = 261;
        int cv = 55;
        int data = 99;
        byte[] ba = NmraPacket.altAccSignalDecoderPktOpsMode(address, cv, data);

        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0x81, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x61, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEC, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x36, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0x63, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0x59, ba[5] & 0xFF);
    }

    @Test
    public void testAltAccSignalDecoderPktOpsMode2041() {
        int address = 2041;
        int cv = 556;
        int data = 175;
        byte[] ba = NmraPacket.altAccSignalDecoderPktOpsMode(address, cv, data);

        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0xBE, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x01, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEE, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x2B, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0xAF, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0xD5, ba[5] & 0xFF);
    }

    @Test
    public void testAltAccSignalDecoderPktOpsMode2044() {
        int address = 2044;
        int cv = 771;
        int data = 102;
        byte[] ba = NmraPacket.altAccSignalDecoderPktOpsMode(address, cv, data);

        Assert.assertEquals("length", 6, ba.length);
        Assert.assertEquals("byte 0", 0xBE, ba[0] & 0xFF);
        Assert.assertEquals("byte 1", 0x07, ba[1] & 0xFF);
        Assert.assertEquals("byte 2", 0xEF, ba[2] & 0xFF);
        Assert.assertEquals("byte 3", 0x02, ba[3] & 0xFF);
        Assert.assertEquals("byte 4", 0x66, ba[4] & 0xFF);
        Assert.assertEquals("byte 5", 0x32, ba[5] & 0xFF);
    }


    @Test
    public void testExtractAddressTypeAcc() {
        byte[] ba = NmraPacket.accSignalDecoderPkt(123, 12);
        Assert.assertEquals("Accessory", NmraPacket.DccAddressType.ACCESSORY_ADDRESS, NmraPacket.extractAddressType(ba));
    }

    @Test
    public void testExtractAddressTypeShort() {
        byte[] bs = NmraPacket.function13Through20Packet(60, false, true, false, true, false, true, false, true, false);
        Assert.assertEquals("Short Loco", NmraPacket.DccAddressType.LOCO_SHORT_ADDRESS, NmraPacket.extractAddressType(bs));
    }

    @Test
    public void testExtractAddressTypeLong() {
        byte[] bl = NmraPacket.function13Through20Packet(2060, true, true, false, true, false, true, false, true, false);
        Assert.assertEquals("Long Loco", NmraPacket.DccAddressType.LOCO_LONG_ADDRESS, NmraPacket.extractAddressType(bl));
    }

    @Test
    public void testExtractAddressNumberAccSignal1() {
        byte[] ba = NmraPacket.accSignalDecoderPkt(1, 12);
        NmraPacket.extractAddressNumber(ba);
        Assert.assertEquals(1, NmraPacket.extractAddressNumber(ba));
    }

    @Test
    public void testExtractAddressNumberAccSignal2() {
        byte[] ba = NmraPacket.accSignalDecoderPkt(2, 12);
        NmraPacket.extractAddressNumber(ba);
        Assert.assertEquals(2, NmraPacket.extractAddressNumber(ba));
    }

    @Test
    public void testExtractAddressNumberAccSignal4() {
        byte[] ba = NmraPacket.accSignalDecoderPkt(4, 12);
        NmraPacket.extractAddressNumber(ba);
        Assert.assertEquals(4, NmraPacket.extractAddressNumber(ba));
    }

    @Test
    public void testExtractAddressNumberAccSignal8() {
        byte[] ba = NmraPacket.accSignalDecoderPkt(8, 12);
        NmraPacket.extractAddressNumber(ba);
        Assert.assertEquals(8, NmraPacket.extractAddressNumber(ba));
    }

    @Test
    public void testExtractAddressNumberAccSignal16() {
        byte[] ba = NmraPacket.accSignalDecoderPkt(16, 12);
        NmraPacket.extractAddressNumber(ba);
        Assert.assertEquals(16, NmraPacket.extractAddressNumber(ba));
    }

    @Test
    public void testExtractAddressNumberAccSignal32() {
        byte[] ba = NmraPacket.accSignalDecoderPkt(32, 12);
        NmraPacket.extractAddressNumber(ba);
        Assert.assertEquals(32, NmraPacket.extractAddressNumber(ba));
    }

    @Test
    public void testExtractAddressNumberAccSignal64() {
        byte[] ba = NmraPacket.accSignalDecoderPkt(64, 12);
        NmraPacket.extractAddressNumber(ba);
        Assert.assertEquals(64, NmraPacket.extractAddressNumber(ba));
    }

    @Test
    public void testExtractAddressNumberAccSignal128() {
        byte[] ba = NmraPacket.accSignalDecoderPkt(128, 12);
        NmraPacket.extractAddressNumber(ba);
        Assert.assertEquals(128, NmraPacket.extractAddressNumber(ba));
    }

    @Test
    public void testExtractAddressNumberAccSignal256() {
        byte[] ba = NmraPacket.accSignalDecoderPkt(256, 12);
        NmraPacket.extractAddressNumber(ba);
        Assert.assertEquals(256, NmraPacket.extractAddressNumber(ba));
    }

    @Test
    public void testExtractAddressNumberAcc1() {
        byte[] ba = NmraPacket.accDecoderPkt(1, true);
        NmraPacket.extractAddressNumber(ba);
        Assert.assertEquals(1, NmraPacket.extractAddressNumber(ba));
    }

    @Test
    public void testExtractAddressNumberAcc2() {
        byte[] ba = NmraPacket.accDecoderPkt(2, true);
        NmraPacket.extractAddressNumber(ba);
        Assert.assertEquals(2, NmraPacket.extractAddressNumber(ba));
    }

    @Test
    public void testExtractAddressNumberAcc4() {
        byte[] ba = NmraPacket.accDecoderPkt(4, true);
        NmraPacket.extractAddressNumber(ba);
        Assert.assertEquals(4, NmraPacket.extractAddressNumber(ba));
    }

    @Test
    public void testExtractAddressNumberAcc8() {
        byte[] ba = NmraPacket.accDecoderPkt(8, true);
        NmraPacket.extractAddressNumber(ba);
        Assert.assertEquals(8, NmraPacket.extractAddressNumber(ba));
    }

    @Test
    public void testExtractAddressNumberAcc16() {
        byte[] ba = NmraPacket.accDecoderPkt(16, true);
        NmraPacket.extractAddressNumber(ba);
        Assert.assertEquals(16, NmraPacket.extractAddressNumber(ba));
    }

    @Test
    public void testExtractAddressNumberAcc32() {
        byte[] ba = NmraPacket.accDecoderPkt(32, true);
        NmraPacket.extractAddressNumber(ba);
        Assert.assertEquals(32, NmraPacket.extractAddressNumber(ba));
    }

    @Test
    public void testExtractAddressNumberAcc64() {
        byte[] ba = NmraPacket.accDecoderPkt(64, true);
        NmraPacket.extractAddressNumber(ba);
        Assert.assertEquals(64, NmraPacket.extractAddressNumber(ba));
    }

    @Test
    public void testExtractAddressNumberAcc128() {
        byte[] ba = NmraPacket.accDecoderPkt(128, true);
        NmraPacket.extractAddressNumber(ba);
        Assert.assertEquals(128, NmraPacket.extractAddressNumber(ba));
    }

    @Test
    public void testExtractAddressNumberAcc256() {
        byte[] ba = NmraPacket.accDecoderPkt(256, true);
        NmraPacket.extractAddressNumber(ba);
        Assert.assertEquals(256, NmraPacket.extractAddressNumber(ba));
    }

    @Test
    public void testExtractAddressNumberAcc512() {
        byte[] ba = NmraPacket.accDecoderPkt(512, true);
        NmraPacket.extractAddressNumber(ba);
        Assert.assertEquals(512, NmraPacket.extractAddressNumber(ba));
    }

    @Test
    public void testExtractAddressNumberShort() {
        byte[] bs = NmraPacket.function13Through20Packet(60, false, true, false, true, false, true, false, true, false);
        Assert.assertEquals("Short Loco", 60, NmraPacket.extractAddressNumber(bs));
    }

    @Test
    public void testExtractAddressNumberLong() {
        byte[] bl = NmraPacket.function13Through20Packet(2060, true, true, false, true, false, true, false, true, false);
        Assert.assertEquals("Long Loco", 2060, NmraPacket.extractAddressNumber(bl));
    }

    /**
     * Test the 28 speed step forward throttle Note that this has not been
     * independently verified
     */
    @Test
    public void testSpeedStep28PacketOld() {
        int address = 100;
        // results for speed steps 0-28 when forward
        byte[][] speeds = {
            {-64, 100, 96, -60}, //   0
            {-64, 100, 113, -43}, //  1
            {-64, 100, 98, -58}, //   2
            {-64, 100, 114, -42}, //  3
            {-64, 100, 99, -57}, //   4
            {-64, 100, 115, -41}, //  5
            {-64, 100, 100, -64}, //  6
            {-64, 100, 116, -48}, //  7
            {-64, 100, 101, -63}, //  8
            {-64, 100, 117, -47}, //  9
            {-64, 100, 102, -62}, // 10
            {-64, 100, 118, -46}, // 11
            {-64, 100, 103, -61}, // 12
            {-64, 100, 119, -45}, // 13
            {-64, 100, 104, -52}, // 14
            {-64, 100, 120, -36}, // 15
            {-64, 100, 105, -51}, // 16
            {-64, 100, 121, -35}, // 17
            {-64, 100, 106, -50}, // 18
            {-64, 100, 122, -34}, // 19
            {-64, 100, 107, -49}, // 20
            {-64, 100, 123, -33}, // 21
            {-64, 100, 108, -56}, // 22
            {-64, 100, 124, -40}, // 23
            {-64, 100, 109, -55}, // 24
            {-64, 100, 125, -39}, // 25
            {-64, 100, 110, -54}, // 26
            {-64, 100, 126, -38}, // 27
            {-64, 100, 111, -53} //  28
        };
        for (int speed = 0; speed < 29; speed++) {
            byte[] packet = NmraPacket.speedStep28Packet(address, true, speed, true);
            Assert.assertNotNull(packet);
            Assert.assertArrayEquals("Speed step " + speed, speeds[speed], packet);
        }
        // invalid inputs should result in null output
        Assert.assertNull("Speed step -1", NmraPacket.speedStep28Packet(address, true, -1, true));
        JUnitAppender.assertErrorMessage("invalid speed -1");
        Assert.assertNull("Speed step 29", NmraPacket.speedStep28Packet(address, true, 29, true));
        JUnitAppender.assertErrorMessage("invalid speed 29");
    }

    /**
     * Test the 28 speed step forward throttle Note that this has not been
     * independently verified
     */
    @Test
    public void testSpeedStep28Packet() {
        int address = 100;
        // results for speed steps 0-31 when forward
        byte[][] forward = {
            {-64, 100, 96, -60}, //   0
            {-64, 100, 112, -44}, //  1
            {-64, 100, 97, -59}, //   2
            {-64, 100, 113, -43}, //  3
            {-64, 100, 98, -58}, //   4
            {-64, 100, 114, -42}, //  5
            {-64, 100, 99, -57}, //   6
            {-64, 100, 115, -41}, //  7
            {-64, 100, 100, -64}, //  8
            {-64, 100, 116, -48}, //  9
            {-64, 100, 101, -63}, // 10
            {-64, 100, 117, -47}, // 11
            {-64, 100, 102, -62}, // 12
            {-64, 100, 118, -46}, // 13
            {-64, 100, 103, -61}, // 14
            {-64, 100, 119, -45}, // 15
            {-64, 100, 104, -52}, // 16
            {-64, 100, 120, -36}, // 17
            {-64, 100, 105, -51}, // 18
            {-64, 100, 121, -35}, // 19
            {-64, 100, 106, -50}, // 20
            {-64, 100, 122, -34}, // 21
            {-64, 100, 107, -49}, // 22
            {-64, 100, 123, -33}, // 23
            {-64, 100, 108, -56}, // 24
            {-64, 100, 124, -40}, // 25
            {-64, 100, 109, -55}, // 26
            {-64, 100, 125, -39}, // 27
            {-64, 100, 110, -54}, // 28
            {-64, 100, 126, -38}, // 29
            {-64, 100, 111, -53}, // 30
            {-64, 100, 127, -37} //  31
        };
        for (int speed = 0; speed < 32; speed++) {
            byte[] packet = NmraPacket.speedStep28Packet(true, address, true, speed, true);
            Assert.assertNotNull(packet);
            Assert.assertArrayEquals("Speed step " + speed, forward[speed], packet);
        }
        // results for speed steps 0-31 when reversed
        byte[][] reverse = {
            {-64, 100, 64, -28}, //  0
            {-64, 100, 80, -12}, //  1
            {-64, 100, 65, -27}, //  2
            {-64, 100, 81, -11}, //  3
            {-64, 100, 66, -26}, //  4
            {-64, 100, 82, -10}, //  5
            {-64, 100, 67, -25}, //  6
            {-64, 100, 83, -9}, //   7
            {-64, 100, 68, -32}, //  8
            {-64, 100, 84, -16}, //  9
            {-64, 100, 69, -31}, // 10
            {-64, 100, 85, -15}, // 11
            {-64, 100, 70, -30}, // 12
            {-64, 100, 86, -14}, // 13
            {-64, 100, 71, -29}, // 14
            {-64, 100, 87, -13}, // 15
            {-64, 100, 72, -20}, // 16
            {-64, 100, 88, -4}, //  17
            {-64, 100, 73, -19}, // 18
            {-64, 100, 89, -3}, //  19
            {-64, 100, 74, -18}, // 20
            {-64, 100, 90, -2}, //  21
            {-64, 100, 75, -17}, // 22
            {-64, 100, 91, -1}, //  23
            {-64, 100, 76, -24}, // 24
            {-64, 100, 92, -8}, //  25
            {-64, 100, 77, -23}, // 26
            {-64, 100, 93, -7}, //  27
            {-64, 100, 78, -22}, // 28
            {-64, 100, 94, -6}, //  29
            {-64, 100, 79, -21}, // 30
            {-64, 100, 95, -5} //   31
        };
        for (int speed = 0; speed < 32; speed++) {
            byte[] packet = NmraPacket.speedStep28Packet(true, address, true, speed, false);
            Assert.assertNotNull(packet);
            Assert.assertArrayEquals("Speed step " + speed, reverse[speed], packet);
        }
        // invalid inputs should result in null output
        Assert.assertNull("Speed step -1", NmraPacket.speedStep28Packet(true, address, true, -1, true));
        JUnitAppender.assertErrorMessage("invalid speed -1");
        Assert.assertNull("Speed step 32", NmraPacket.speedStep28Packet(true, address, true, 32, true));
        JUnitAppender.assertErrorMessage("invalid speed 32");
    }

    @Test
    public void testToStringNoPacket() {
        boolean thrown = false;
        try {
            NmraPacket.toString(new byte[]{});
        } catch (IllegalArgumentException e) {
            thrown = true;
        }

        Assert.assertTrue(thrown);
    }

    @Test
    public void testToStringShortLocoPacket() {
        // short address function set
        String display = NmraPacket.toString(new byte[]{(byte) 0x3C, (byte) 0xDE, (byte) 0x55, (byte) 00});
        Assert.assertEquals("LOCO_SHORT_ADDRESS type: 222 to addr 60", display);
    }

    @Test
    public void testToStringAccessoryPacket1() {
        String display = NmraPacket.toString(NmraPacket.accDecoderPkt(1, true));
        Assert.assertEquals("ACCESSORY_ADDRESS type: 120 to addr 1", display);
    }

    @Test
    public void testToStringAccessoryPacket257() {
        String display = NmraPacket.toString(NmraPacket.accDecoderPkt(257, true));
        Assert.assertEquals("ACCESSORY_ADDRESS type: 104 to addr 257", display);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private static int decAddr(int accyAddr) {
        return (((accyAddr - 1) >> 2) << 2) + 1;
    }

}
