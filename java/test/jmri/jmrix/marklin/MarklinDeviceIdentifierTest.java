package jmri.jmrix.marklin;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MarklinDeviceIdentifier class.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de) Copyright (C) 2026
 */
public class MarklinDeviceIdentifierTest {

    @Test
    public void testCalculateHash() {
        // Test hash calculation: Hash = (UID[31:16]) XOR (UID[15:0])

        // Example: UID = 0x12345678
        // Upper 16 bits: 0x1234
        // Lower 16 bits: 0x5678
        // Hash = 0x1234 XOR 0x5678 = 0x444C
        long uid1 = 0x12345678L;
        assertEquals(0x444C, MarklinDeviceIdentifier.calculateHash(uid1));

        // Example: UID = 0xFFFF0000
        // Hash = 0xFFFF XOR 0x0000 = 0xFFFF
        long uid2 = 0xFFFF0000L;
        assertEquals(0xFFFF, MarklinDeviceIdentifier.calculateHash(uid2));

        // Example: UID = 0x00000000 (Broadcast)
        // Hash = 0x0000 XOR 0x0000 = 0x0000
        assertEquals(0x0000, MarklinDeviceIdentifier.calculateHash(
            MarklinDeviceIdentifier.UID_BROADCAST));

        // Example: UID = 0xFFFFFFFF (Uninitialized)
        // Hash = 0xFFFF XOR 0xFFFF = 0x0000
        assertEquals(0x0000, MarklinDeviceIdentifier.calculateHash(
            MarklinDeviceIdentifier.UID_UNINITIALIZED));
    }

    @Test
    public void testFromPingResponseGleisbox() {
        // Simulated PING response from Gleisbox
        // UID: 0x00100001 (device type 0x0010 + serial 0x0001)
        // Software version: 0x0102 (v1.2)
        // Device type: 0x0010
        int[] message = new int[]{
            0x00, 0x31, // Command 0x18 (PING), response flag set
            0x11, 0x22, // Hash (calculated from UID)
            0x08,       // DLC: 8 data bytes per CAN 2.0B spec (4 UID + 2 version + 2 device type)
            0x00, 0x00, 0x00, 0x00, // Address (broadcast)
            0x00, 0x10, 0x00, 0x01, // UID bytes 0-3
            0x01, 0x02,             // Software version bytes 4-5
            0x00, 0x10              // Device type bytes 6-7
        };

        MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(message);
        MarklinDeviceIdentifier.DeviceInfo info = MarklinDeviceIdentifier.fromPingResponse(decoded);

        assertNotNull(info);
        assertEquals(0x00100001L, info.getUid());
        assertEquals(0x0010, info.getDeviceType());
        assertEquals(0x0102, info.getSoftwareVersion());
        assertEquals("Gleisbox", info.getDeviceTypeName());
        assertFalse(info.isBroadcast());
        assertFalse(info.isUninitialized());
    }

    @Test
    public void testFromPingResponseMS2() {
        // Simulated PING response from MS2
        int[] message = new int[]{
            0x00, 0x31, // Command 0x18 (PING), response flag set
            0x47, 0x11, // Hash
            0x08,       // DLC: 8 data bytes per CAN 2.0B spec
            0x00, 0x00, 0x00, 0x00, // Address
            0x00, 0x30, 0xAB, 0xCD, // UID (MS2 with serial ABCD)
            0x02, 0x05,             // Software version v2.5
            0x00, 0x30              // Device type MS2
        };

        MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(message);
        MarklinDeviceIdentifier.DeviceInfo info = MarklinDeviceIdentifier.fromPingResponse(decoded);

        assertNotNull(info);
        assertEquals(0x0030ABCDL, info.getUid());
        assertEquals(0x0030, info.getDeviceType());
        assertEquals(0x0205, info.getSoftwareVersion());
        assertEquals("MS2", info.getDeviceTypeName());
    }

    @Test
    public void testFromPingResponseCS2() {
        // Simulated PING response from CS2-GUI Master
        int[] message = new int[]{
            0x00, 0x31, // Command 0x18 (PING), response flag set
            0x12, 0x34, // Hash
            0x08,       // DLC: 8 data bytes per CAN 2.0B spec
            0x00, 0x00, 0x00, 0x00, // Address
            0xFF, 0xFF, 0x12, 0x34, // UID (CS2)
            0x03, 0x00,             // Software version v3.0
            0xFF, 0xFF              // Device type CS2-GUI
        };

        MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(message);
        MarklinDeviceIdentifier.DeviceInfo info = MarklinDeviceIdentifier.fromPingResponse(decoded);

        assertNotNull(info);
        assertEquals(0xFFFF1234L, info.getUid());
        assertEquals(0xFFFF, info.getDeviceType());
        assertEquals("CS2-GUI (Master)", info.getDeviceTypeName());
    }

    @Test
    public void testFromPingResponseNotPing() {
        // Message that's not a PING response
        int[] message = new int[]{
            0x00, 0x08, // Command 0x04 (LOCOSPEED)
            0x47, 0x11, // Hash
            0x02,       // DLC: 2 data bytes per CAN 2.0B spec
            0x00, 0x00, 0xC0, 0x03, // Address
            0x01, 0xF4, 0x00, 0x00  // Speed data
        };

        MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(message);
        MarklinDeviceIdentifier.DeviceInfo info = MarklinDeviceIdentifier.fromPingResponse(decoded);

        assertNull(info); // Not a PING response
    }

    @Test
    public void testFromPingResponseInvalidLength() {
        // PING response with wrong data length
        int[] message = new int[]{
            0x00, 0x31, // Command 0x18 (PING), response flag set
            0x47, 0x11, // Hash
            0x01,       // DLC: 1 data byte (wrong! should be 8 for PING response)
            0x00, 0x00, 0x00, 0x00, // Address
            0x00, 0x00, 0x00, 0x00  // Incomplete data
        };

        MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(message);
        MarklinDeviceIdentifier.DeviceInfo info = MarklinDeviceIdentifier.fromPingResponse(decoded);

        assertNull(info); // Invalid PING response
    }

    @Test
    public void testFromHash() {
        // Extract device info from hash in regular message
        int[] message = new int[]{
            0x00, 0x08, // Command 0x04 (LOCOSPEED)
            0x47, 0x11, // Hash
            0x02,       // DLC: 2 data bytes per CAN 2.0B spec
            0x00, 0x00, 0xC0, 0x03, // Address
            0x01, 0xF4, 0x00, 0x00  // Speed data
        };

        MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(message);
        MarklinDeviceIdentifier.DeviceInfo info = MarklinDeviceIdentifier.fromHash(decoded);

        assertNotNull(info);
        assertEquals(0x4711, info.getHash());
        assertTrue(info.isUninitialized()); // UID not determined from hash
        assertEquals(-1, info.getDeviceType()); // Not available from hash
        assertEquals(-1, info.getSoftwareVersion()); // Not available from hash
    }

    @Test
    public void testIsBroadcast() {
        MarklinDeviceIdentifier.DeviceInfo info = new MarklinDeviceIdentifier.DeviceInfo(
            MarklinDeviceIdentifier.UID_BROADCAST, 0, 0, 0);

        assertTrue(info.isBroadcast());
        assertFalse(info.isUninitialized());
    }

    @Test
    public void testIsUninitialized() {
        MarklinDeviceIdentifier.DeviceInfo info = new MarklinDeviceIdentifier.DeviceInfo(
            MarklinDeviceIdentifier.UID_UNINITIALIZED, 0, 0, 0);

        assertFalse(info.isBroadcast());
        assertTrue(info.isUninitialized());
    }

    @Test
    public void testIsHashValid() {
        // Create info with correct hash
        long uid = 0x12345678L;
        int correctHash = MarklinDeviceIdentifier.calculateHash(uid);
        MarklinDeviceIdentifier.DeviceInfo validInfo = new MarklinDeviceIdentifier.DeviceInfo(
            uid, 0x0010, 0x0100, correctHash);

        assertTrue(validInfo.isHashValid());

        // Create info with incorrect hash
        MarklinDeviceIdentifier.DeviceInfo invalidInfo = new MarklinDeviceIdentifier.DeviceInfo(
            uid, 0x0010, 0x0100, 0x9999);

        assertFalse(invalidInfo.isHashValid());
    }

    @Test
    public void testGetDeviceTypeName() {
        assertEquals("GFP (Gleis Format Processor)",
            MarklinDeviceIdentifier.getDeviceTypeName(MarklinDeviceIdentifier.DEVICE_TYPE_GFP));
        assertEquals("Gleisbox",
            MarklinDeviceIdentifier.getDeviceTypeName(MarklinDeviceIdentifier.DEVICE_TYPE_GLEISBOX));
        assertEquals("Connect 6021",
            MarklinDeviceIdentifier.getDeviceTypeName(MarklinDeviceIdentifier.DEVICE_TYPE_CONNECT_6021));
        assertEquals("MS2",
            MarklinDeviceIdentifier.getDeviceTypeName(MarklinDeviceIdentifier.DEVICE_TYPE_MS2));
        assertEquals("CC-Schnitte",
            MarklinDeviceIdentifier.getDeviceTypeName(MarklinDeviceIdentifier.DEVICE_TYPE_CC_SCHNITTE));
        assertEquals("Wireless Device",
            MarklinDeviceIdentifier.getDeviceTypeName(MarklinDeviceIdentifier.DEVICE_TYPE_WIRELESS));
        assertEquals("CS2-GUI (Master)",
            MarklinDeviceIdentifier.getDeviceTypeName(MarklinDeviceIdentifier.DEVICE_TYPE_CS2_GUI));
        assertEquals("MCAN Device (unknown type 0x0099)",
            MarklinDeviceIdentifier.getDeviceTypeName(0x0099));
    }

    @Test
    public void testFromPingResponseCcSchnitte() {
        // PING response from CC-Schnitte as described in the CAN forum:
        // SW=2.1 Geräte-UID=0x1300, device type code = 0x1300
        int[] message = new int[]{
            0x00, 0x31, // Command 0x18 (PING), response flag set
            0x47, 0x11, // Hash
            0x08,       // DLC: 8 data bytes
            0x00, 0x00, 0x00, 0x00, // Address
            0x00, 0x00, 0x13, 0x00, // UID bytes 0-3 (0x00001300)
            0x02, 0x01,             // Software version v2.1
            0x13, 0x00              // Device type 0x1300 (CC-Schnitte)
        };

        MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(message);
        MarklinDeviceIdentifier.DeviceInfo info = MarklinDeviceIdentifier.fromPingResponse(decoded);

        assertNotNull(info);
        assertEquals(0x00001300L, info.getUid());
        assertEquals(MarklinDeviceIdentifier.DEVICE_TYPE_CC_SCHNITTE, info.getDeviceType());
        assertEquals(0x0201, info.getSoftwareVersion());
        assertEquals("CC-Schnitte", info.getDeviceTypeName());
    }

    @Test
    public void testGetDeviceTypeNameVariants() {
        // Test undocumented MS2 variants
        assertTrue(MarklinDeviceIdentifier.getDeviceTypeName(0x0032).contains("MS2"));
        assertTrue(MarklinDeviceIdentifier.getDeviceTypeName(0x0033).contains("MS2"));

        // Test undocumented Gleisbox variants
        assertTrue(MarklinDeviceIdentifier.getDeviceTypeName(0x0011).contains("Gleisbox"));
        assertTrue(MarklinDeviceIdentifier.getDeviceTypeName(0x0012).contains("Gleisbox"));
    }

    @Test
    public void testIsPingRequest() {
        // Valid PING request (DLC=0, command=0x18, not response)
        int[] request = new int[]{
            0x00, 0x30, // Command 0x18 (PING), no response flag
            0x47, 0x11, // Hash
            0x00,       // DLC: 0 data bytes per CAN 2.0B spec
            0x00, 0x00, 0x00, 0x00, // Address
            0x00, 0x00, 0x00, 0x00
        };

        MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(request);
        assertTrue(MarklinDeviceIdentifier.isPingRequest(decoded));
        assertFalse(MarklinDeviceIdentifier.isPingResponse(decoded));
    }

    @Test
    public void testIsPingResponse() {
        // Valid PING response (DLC=8, command=0x18, response flag set)
        int[] response = new int[]{
            0x00, 0x31, // Command 0x18 (PING), response flag set
            0x47, 0x11, // Hash
            0x08,       // DLC: 8 data bytes per CAN 2.0B spec
            0x00, 0x00, 0x00, 0x00, // Address
            0x00, 0x10, 0x00, 0x01, // UID
            0x01, 0x02, 0x00, 0x10  // SW version + device type
        };

        MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(response);
        assertFalse(MarklinDeviceIdentifier.isPingRequest(decoded));
        assertTrue(MarklinDeviceIdentifier.isPingResponse(decoded));
    }

    @Test
    public void testFormatCompact() {
        // Test broadcast
        MarklinDeviceIdentifier.DeviceInfo broadcast = new MarklinDeviceIdentifier.DeviceInfo(
            MarklinDeviceIdentifier.UID_BROADCAST, 0, 0, 0);
        assertEquals("[Broadcast]", MarklinDeviceIdentifier.formatCompact(broadcast));

        // Test hash-only (from regular traffic)
        MarklinDeviceIdentifier.DeviceInfo hashOnly = new MarklinDeviceIdentifier.DeviceInfo(
            MarklinDeviceIdentifier.UID_UNINITIALIZED, -1, -1, 0x4711);
        assertEquals("[Hash:0x4711]", MarklinDeviceIdentifier.formatCompact(hashOnly));

        // Test full device info (from PING)
        MarklinDeviceIdentifier.DeviceInfo gleisbox = new MarklinDeviceIdentifier.DeviceInfo(
            0x00100001L, MarklinDeviceIdentifier.DEVICE_TYPE_GLEISBOX, 0x0102, 0x1234);
        assertEquals("[Gleisbox:0x00100001]", MarklinDeviceIdentifier.formatCompact(gleisbox));

        // Test null
        assertEquals("[Unknown]", MarklinDeviceIdentifier.formatCompact(null));
    }

    @Test
    public void testDeviceInfoToString() {
        // Test full device info with valid hash
        long uid = 0x00100001L;
        int hash = MarklinDeviceIdentifier.calculateHash(uid);
        MarklinDeviceIdentifier.DeviceInfo info = new MarklinDeviceIdentifier.DeviceInfo(
            uid, MarklinDeviceIdentifier.DEVICE_TYPE_GLEISBOX, 0x0203, hash);

        String str = info.toString();
        assertTrue(str.contains("Gleisbox"));
        assertTrue(str.contains("0x00100001"));
        assertTrue(str.contains("v2.3"));
        assertFalse(str.contains("MISMATCH")); // Hash is valid

        // Test with invalid hash
        MarklinDeviceIdentifier.DeviceInfo badHash = new MarklinDeviceIdentifier.DeviceInfo(
            uid, MarklinDeviceIdentifier.DEVICE_TYPE_GLEISBOX, 0x0203, 0x9999);

        String badStr = badHash.toString();
        assertTrue(badStr.contains("HASH MISMATCH"));
    }

    @Test
    public void testDeviceInfoToStringBroadcast() {
        MarklinDeviceIdentifier.DeviceInfo info = new MarklinDeviceIdentifier.DeviceInfo(
            MarklinDeviceIdentifier.UID_BROADCAST, 0, 0, 0);
        assertEquals("Broadcast", info.toString());
    }

    @Test
    public void testDeviceInfoToStringUninitialized() {
        MarklinDeviceIdentifier.DeviceInfo info = new MarklinDeviceIdentifier.DeviceInfo(
            MarklinDeviceIdentifier.UID_UNINITIALIZED, -1, -1, 0x4711);
        assertEquals("Uninitialized", info.toString());
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
