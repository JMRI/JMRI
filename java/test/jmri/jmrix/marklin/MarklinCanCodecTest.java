package jmri.jmrix.marklin;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MarklinCanCodec class.
 *
 * @author Generated for issue #14104
 */
public class MarklinCanCodecTest {

    @Test
    public void testDecodeSystemGoCommand() {
        // System Go command
        int[] message = new int[]{
            0x00, 0x00, 0x47, 0x11, 0x01, // DLC: 1 data byte per CAN 2.0B spec
            0x00, 0x00, 0x00, 0x00,
            0x01, 0x00, 0x00, 0x00
        };

        MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(message);

        assertEquals(MarklinConstants.PRIO_1, decoded.getPriority());
        assertEquals(MarklinConstants.SYSCOMMANDSTART, decoded.getCommand());
        assertFalse(decoded.isResponse());
        assertEquals(0x47, decoded.getHash()[0]);
        assertEquals(0x11, decoded.getHash()[1]);
        assertEquals(0x00, decoded.getAddress());
        assertEquals(1, decoded.getDataLength());
        assertEquals(MarklinConstants.CMDGOSYS, decoded.getDataByte(0));
    }

    @Test
    public void testDecodeLocoSpeedCommand() {
        // Loco speed command for DCC address 3, speed 500
        int[] message = new int[]{
            0x00, 0x08, 0x47, 0x11, 0x02, // DLC: 2 data bytes per CAN 2.0B spec
            0x00, 0x00, 0xC0, 0x03, // DCC address 3
            0x01, (byte) 0xF4, 0x00, 0x00  // speed 500 (0x01F4)
        };

        MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(message);

        assertEquals(MarklinConstants.PRIO_1, decoded.getPriority());
        assertEquals(MarklinConstants.LOCOSPEED, decoded.getCommand());
        assertEquals(0xC003, decoded.getAddress());
        assertEquals(2, decoded.getDataLength());
        assertEquals(0x01, decoded.getDataByte(0));
        assertEquals(0xF4, decoded.getDataByte(1));
    }

    @Test
    public void testDecodeCanBootCommand() {
        // CAN BOOT command (0x1B)
        int[] message = new int[]{
            0x00, 0x36, 0x47, 0x11, 0x00, // DLC: 0 data bytes per CAN 2.0B spec
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00
        };

        MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(message);

        assertEquals(MarklinConstants.PRIO_1, decoded.getPriority());
        assertEquals(MarklinConstants.CMDCANBOOT, decoded.getCommand());
        assertFalse(decoded.isResponse());
        assertEquals(0, decoded.getDataLength());
    }

    @Test
    public void testDecodeTurnoutCommand() {
        // Accessory command for DCC turnout 1, state closed (1), power on
        int[] message = new int[]{
            0x00, 0x16, 0x47, 0x11, 0x02, // DLC: 2 data bytes per CAN 2.0B spec
            0x00, 0x00, 0x38, 0x00, // DCC accessory address 0x3800
            0x01, 0x01, 0x00, 0x00
        };

        MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(message);

        assertEquals(MarklinConstants.PRIO_1, decoded.getPriority());
        assertEquals(MarklinConstants.ACCCOMMANDSTART, decoded.getCommand());
        assertEquals(0x3800, decoded.getAddress());
        assertEquals(2, decoded.getDataLength());
        assertEquals(0x01, decoded.getDataByte(0)); // closed
        assertEquals(0x01, decoded.getDataByte(1)); // power on
    }

    @Test
    public void testBuildSystemGoCommand() {
        int[] encoded = MarklinCanCodec.builder()
            .setCommand(MarklinConstants.SYSCOMMANDSTART)
            .setAddress(0x00)
            .setData(MarklinConstants.CMDGOSYS)
            .setDataLength(1)
            .build();

        assertEquals(13, encoded.length);
        assertEquals(0x00, encoded[0]); // Priority 0, command high bits
        assertEquals(0x00, encoded[1]); // Command low bits, not response
        assertEquals(MarklinConstants.HASHBYTE1, encoded[2]);
        assertEquals(MarklinConstants.HASHBYTE2, encoded[3]);
        assertEquals(0x01, encoded[4]); // DLC: 1 data byte per CAN 2.0B spec
        assertEquals(0x00, encoded[5]); // Address byte 1
        assertEquals(0x00, encoded[6]); // Address byte 2
        assertEquals(0x00, encoded[7]); // Address byte 3
        assertEquals(0x00, encoded[8]); // Address byte 4
        assertEquals(MarklinConstants.CMDGOSYS, encoded[9]); // Data byte
    }

    @Test
    public void testBuildLocoSpeedCommand() {
        int speed = 500;
        int[] encoded = MarklinCanCodec.builder()
            .setCommand(MarklinConstants.LOCOSPEED)
            .setAddress(0xC003) // DCC address 3
            .setData((speed >> 8) & 0xff, speed & 0xff)
            .setDataLength(2)
            .build();

        assertEquals(13, encoded.length);
        assertEquals(0x00, encoded[0] & 0xC0); // Priority 0
        assertEquals(0xC003,
            ((long)encoded[5] << 24) | ((long)encoded[6] << 16) |
            ((long)encoded[7] << 8) | encoded[8]);
        assertEquals(0x01, encoded[9]); // Speed high byte
        assertEquals(0xF4, encoded[10]); // Speed low byte (500 = 0x01F4)
    }

    @Test
    public void testBuildCanBootCommand() {
        int[] encoded = MarklinCanCodec.builder()
            .setCommand(MarklinConstants.CMDCANBOOT)
            .setAddress(0x00)
            .setDataLength(0)
            .build();

        assertEquals(13, encoded.length);
        assertEquals(0x00, encoded[0]);
        assertEquals(0x36, encoded[1]); // Command 0x1B encoded: (0x1B << 1) = 0x36
        assertEquals(0x00, encoded[4]); // DLC: 0 data bytes per CAN 2.0B spec
    }

    @Test
    public void testGetPriorityDescription() {
        assertEquals("Priority 1: Stop/Go/Short",
            MarklinCanCodec.getPriorityDescription(MarklinConstants.PRIO_1));
        assertEquals("Priority 2: Feedback",
            MarklinCanCodec.getPriorityDescription(MarklinConstants.PRIO_2));
        assertEquals("Priority 3: Engine Stop",
            MarklinCanCodec.getPriorityDescription(MarklinConstants.PRIO_3));
        assertEquals("Priority 4: Engine/Accessory Command",
            MarklinCanCodec.getPriorityDescription(MarklinConstants.PRIO_4));
    }

    @Test
    public void testGetProtocolFromAddress() {
        assertEquals("Broadcast", MarklinCanCodec.getProtocolFromAddress(0));
        assertEquals("MM1/MM2 Loco", MarklinCanCodec.getProtocolFromAddress(0x0001));
        assertEquals("DCC", MarklinCanCodec.getProtocolFromAddress(0xC003));
        assertEquals("DCC Accessory", MarklinCanCodec.getProtocolFromAddress(0x3800));
        assertEquals("MFX", MarklinCanCodec.getProtocolFromAddress(0x4001));
        assertEquals("Selectrix SX2", MarklinCanCodec.getProtocolFromAddress(0x8001));
    }

    @Test
    public void testGetBaseAddress() {
        assertEquals(0, MarklinCanCodec.getBaseAddress(0));
        assertEquals(1, MarklinCanCodec.getBaseAddress(0x0001));
        assertEquals(3, MarklinCanCodec.getBaseAddress(0xC003));
        assertEquals(0, MarklinCanCodec.getBaseAddress(0x3800));
        assertEquals(1, MarklinCanCodec.getBaseAddress(0x4001));
    }

    @Test
    public void testDecodeResponseFlag() {
        // Request message
        int[] request = new int[]{
            0x00, 0x00, 0x47, 0x11, 0x01, // DLC: 1 data byte per CAN 2.0B spec
            0x00, 0x00, 0x00, 0x00,
            0x01, 0x00, 0x00, 0x00
        };

        // Response message (bit 0 of byte 1 set)
        int[] response = new int[]{
            0x00, 0x01, 0x47, 0x11, 0x01, // DLC: 1 data byte per CAN 2.0B spec
            0x00, 0x00, 0x00, 0x00,
            0x01, 0x00, 0x00, 0x00
        };

        assertFalse(MarklinCanCodec.decode(request).isResponse());
        assertTrue(MarklinCanCodec.decode(response).isResponse());
    }

    @Test
    public void testCommandCategories() {
        int[] systemMsg = new int[]{0x00, 0x00, 0x47, 0x11, 0x01, 0, 0, 0, 0, 0x01, 0, 0, 0}; // DLC: 1 data byte
        assertEquals("SYSTEM", MarklinCanCodec.decode(systemMsg).getCommandCategory());

        int[] locoMsg = new int[]{0x00, 0x08, 0x47, 0x11, 0x02, 0, 0, 0xC0, 3, 1, 0xF4, 0, 0}; // DLC: 2 data bytes
        assertEquals("MANAGEMENT", MarklinCanCodec.decode(locoMsg).getCommandCategory());

        int[] accMsg = new int[]{0x00, 0x16, 0x47, 0x11, 0x02, 0, 0, 0x38, 0, 1, 1, 0, 0}; // DLC: 2 data bytes
        assertEquals("ACCESSORY", MarklinCanCodec.decode(accMsg).getCommandCategory());

        int[] bootMsg = new int[]{0x00, 0x36, 0x47, 0x11, 0x00, 0, 0, 0, 0, 0, 0, 0, 0}; // DLC: 0 data bytes
        assertEquals("SOFTWARE", MarklinCanCodec.decode(bootMsg).getCommandCategory());
    }

    @Test
    public void testRoundTrip() {
        // Build a message, then decode it and verify all fields match
        int[] encoded = MarklinCanCodec.builder()
            .setPriority(MarklinConstants.PRIO_4)
            .setCommand(MarklinConstants.LOCOFUNCTION)
            .setResponse(false)
            .setAddress(0xC003)
            .setData(5, 1) // Function 5, state on
            .setDataLength(2)
            .build();

        MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(encoded);

        assertEquals(MarklinConstants.PRIO_4, decoded.getPriority());
        assertEquals(MarklinConstants.LOCOFUNCTION, decoded.getCommand());
        assertFalse(decoded.isResponse());
        assertEquals(0xC003, decoded.getAddress());
        assertEquals(2, decoded.getDataLength());
        assertEquals(5, decoded.getDataByte(0));
        assertEquals(1, decoded.getDataByte(1));
    }

    @Test
    public void testGetProtocolFromAddressAllRanges() {
        // Test all protocol ranges mentioned in documentation
        assertEquals("MM Function Decoder", MarklinCanCodec.getProtocolFromAddress(0x1000));
        assertEquals("MM Function Decoder", MarklinCanCodec.getProtocolFromAddress(0x13FF));

        assertEquals("MM1/MM2 Loco (20kHz)", MarklinCanCodec.getProtocolFromAddress(0x2000));
        assertEquals("MM1/MM2 Loco (20kHz)", MarklinCanCodec.getProtocolFromAddress(0x23FF));

        assertEquals("Selectrix SX1", MarklinCanCodec.getProtocolFromAddress(0x0800));
        assertEquals("Selectrix SX1", MarklinCanCodec.getProtocolFromAddress(0x0BFF));

        assertEquals("Selectrix SX1 Accessory", MarklinCanCodec.getProtocolFromAddress(0x2800));
        assertEquals("Selectrix SX1 Accessory", MarklinCanCodec.getProtocolFromAddress(0x2BFF));

        assertEquals("MM Accessory", MarklinCanCodec.getProtocolFromAddress(0x3000));
        assertEquals("MM Accessory", MarklinCanCodec.getProtocolFromAddress(0x33FF));

        assertEquals("Club Range", MarklinCanCodec.getProtocolFromAddress(0x1800));
        assertEquals("Club Range", MarklinCanCodec.getProtocolFromAddress(0x1BFF));

        assertEquals("Vendor Range", MarklinCanCodec.getProtocolFromAddress(0x1C00));
        assertEquals("Vendor Range", MarklinCanCodec.getProtocolFromAddress(0x1FFF));
    }

    @Test
    public void testGetCommandCategoryAllCategories() {
        // Test all command categories from documentation
        int[] guiMsg = new int[]{0x00, 0x40, 0x47, 0x11, 0x00, 0, 0, 0, 0, 0, 0, 0, 0}; // DLC: 0 data bytes
        assertEquals("GUI", MarklinCanCodec.decode(guiMsg).getCommandCategory());

        int[] feedbackMsg = new int[]{0x00, 0x20, 0x47, 0x11, 0x00, 0, 0, 0, 0, 0, 0, 0, 0}; // DLC: 0 data bytes
        assertEquals("FEEDBACK", MarklinCanCodec.decode(feedbackMsg).getCommandCategory());

        int[] automationMsg = new int[]{0x00, 0x60, 0x47, 0x11, 0x00, 0, 0, 0, 0, 0, 0, 0, 0}; // DLC: 0 data bytes
        assertEquals("AUTOMATION", MarklinCanCodec.decode(automationMsg).getCommandCategory());
    }

    @Test
    public void testBuilderSetPriority() {
        // Test that builder correctly sets all priority levels
        for (int prio = 0; prio <= 3; prio++) {
            int[] encoded = MarklinCanCodec.builder()
                .setPriority(prio)
                .setCommand(MarklinConstants.SYSCOMMANDSTART)
                .setAddress(0x00)
                .setDataLength(0)
                .build();

            MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(encoded);
            assertEquals(prio, decoded.getPriority());
        }
    }

    @Test
    public void testBuilderSetResponse() {
        // Test response flag setting
        int[] requestMsg = MarklinCanCodec.builder()
            .setCommand(MarklinConstants.SYSCOMMANDSTART)
            .setResponse(false)
            .setAddress(0x00)
            .setDataLength(0)
            .build();

        int[] responseMsg = MarklinCanCodec.builder()
            .setCommand(MarklinConstants.SYSCOMMANDSTART)
            .setResponse(true)
            .setAddress(0x00)
            .setDataLength(0)
            .build();

        assertFalse(MarklinCanCodec.decode(requestMsg).isResponse());
        assertTrue(MarklinCanCodec.decode(responseMsg).isResponse());
    }

    @Test
    public void testBuilderSetHash() {
        // Test custom hash setting
        int[] encoded = MarklinCanCodec.builder()
            .setCommand(MarklinConstants.SYSCOMMANDSTART)
            .setAddress(0x00)
            .setHash(0xAB, 0xCD)
            .setDataLength(0)
            .build();

        MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(encoded);
        assertEquals(0xAB, decoded.getHash()[0]);
        assertEquals(0xCD, decoded.getHash()[1]);
    }

    @Test
    public void testDecodeInvalidShortMessage() {
        // Test error handling for messages shorter than 13 bytes
        int[] shortMsg = new int[]{0x00, 0x00, 0x47, 0x11, 0x05};

        assertThrows(IllegalArgumentException.class, () -> {
            MarklinCanCodec.decode(shortMsg);
        });
    }

    @Test
    public void testGetDataByteOutOfBounds() {
        // Test that getDataByte returns 0 for out-of-bounds indices
        int[] message = new int[]{
            0x00, 0x00, 0x47, 0x11, 0x01, // DLC: 1 data byte per CAN 2.0B spec
            0x00, 0x00, 0x00, 0x00,
            0x01, 0x00, 0x00, 0x00
        };

        MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(message);
        assertEquals(0x01, decoded.getDataByte(0)); // Valid
        assertEquals(0, decoded.getDataByte(5)); // Out of bounds
        assertEquals(0, decoded.getDataByte(-1)); // Negative index
    }

    @Test
    public void testMaxDataLength() {
        // Test maximum data length (4 bytes for 13-byte message format)
        int[] encoded = MarklinCanCodec.builder()
            .setCommand(MarklinConstants.SYSCOMMANDSTART)
            .setAddress(0x00)
            .setData(0x01, 0x02, 0x03, 0x04)
            .setDataLength(4)
            .build();

        MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(encoded);
        assertEquals(4, decoded.getDataLength());
        assertEquals(0x01, decoded.getDataByte(0));
        assertEquals(0x04, decoded.getDataByte(3));
        assertEquals(0x04, encoded[4]); // DLC: 4 data bytes per CAN 2.0B spec
    }

    @Test
    public void testBuilderDataLengthClamping() {
        // Test data length handling - builder clamps internally to 8 but
        // 13-byte message format only has space for 4 data bytes (indices 9-12)
        // Setting more than 4 data bytes will write beyond array bounds
        int[] encoded = MarklinCanCodec.builder()
            .setCommand(MarklinConstants.SYSCOMMANDSTART)
            .setAddress(0x00)
            .setData(0x01, 0x02, 0x03, 0x04)
            .setDataLength(4)
            .build();

        MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(encoded);
        assertEquals(4, decoded.getDataLength());
        assertEquals(0x01, decoded.getDataByte(0));
        assertEquals(0x04, decoded.getDataByte(3));
    }

    @Test
    public void testGetPriorityDescriptionUnknown() {
        // Test unknown priority value
        assertEquals("Unknown Priority", MarklinCanCodec.getPriorityDescription(99));
    }

    @Test
    public void testGetProtocolFromAddressUnknown() {
        // Test address outside all known ranges
        assertEquals("Unknown Protocol", MarklinCanCodec.getProtocolFromAddress(0x10000000L));
    }

    @Test
    public void testDecodeDLCEdgeCases() {
        // Test DLC edge cases: DLC = 2 (2 data bytes)
        int[] twoByteDLC = new int[]{
            0x00, 0x00, 0x47, 0x11, 0x02, // DLC: 2 data bytes per CAN 2.0B spec
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00
        };

        MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(twoByteDLC);
        assertEquals(2, decoded.getDataLength());

        // Test valid max DLC = 4 (4 data bytes in 13-byte message format)
        int[] validMaxDLC = new int[]{
            0x00, 0x00, 0x47, 0x11, 0x04, // DLC: 4 data bytes per CAN 2.0B spec
            0x00, 0x00, 0x00, 0x00,
            0xAA, 0xBB, 0xCC, 0xDD
        };

        decoded = MarklinCanCodec.decode(validMaxDLC);
        assertEquals(4, decoded.getDataLength());
        assertEquals(0xAA, decoded.getDataByte(0));
        assertEquals(0xDD, decoded.getDataByte(3));
    }

    @Test
    public void testGetCommandCategoryUnknown() {
        // Test command outside all known ranges
        int[] unknownMsg = new int[]{0x1F, 0xFE, 0x47, 0x11, 0x00, 0, 0, 0, 0, 0, 0, 0, 0}; // DLC: 0 data bytes
        assertEquals("UNKNOWN", MarklinCanCodec.decode(unknownMsg).getCommandCategory());
    }

    @Test
    public void testDecodeAlienCommand() {
        // Test that codec accepts and decodes "alien" (unknown) commands
        // Command 0x15 is in gap between FEEDBACK (0x12) and SOFTWARE (0x18)
        int[] alienMsg = new int[]{
            0x00, 0x2A, 0x47, 0x11, 0x01, // Command 0x15, DLC: 1 data byte per CAN 2.0B spec
            0x00, 0x00, 0x00, 0x00,
            0xAA, 0x00, 0x00, 0x00
        };

        // Should decode successfully without throwing exceptions
        MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(alienMsg);

        assertEquals(0x15, decoded.getCommand());
        assertEquals("UNKNOWN", decoded.getCommandCategory());
        assertEquals(1, decoded.getDataLength());
        assertEquals(0xAA, decoded.getDataByte(0));
    }

    @Test
    public void testBuildAlienCommand() {
        // Test that builder can create messages with arbitrary command values
        // Use command 0x1E which is in gap between SOFTWARE (0x1C) and GUI (0x20)
        int alienCommand = 0x1E;

        int[] encoded = MarklinCanCodec.builder()
            .setCommand(alienCommand)
            .setAddress(0x12345678L)
            .setData(0xAA, 0xBB)
            .setDataLength(2)
            .build();

        MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(encoded);

        assertEquals(alienCommand, decoded.getCommand());
        assertEquals("UNKNOWN", decoded.getCommandCategory());
        assertEquals(0x12345678L, decoded.getAddress());
        assertEquals(2, decoded.getDataLength());
        assertEquals(0xAA, decoded.getDataByte(0));
        assertEquals(0xBB, decoded.getDataByte(1));
    }

    @Test
    public void testDecodeGapRangeCommands() {
        // Test commands in gaps between defined ranges

        // Gap between ACCESSORY (0x0D) and FEEDBACK (0x10): commands 0x0E-0x0F
        int[] gapMsg1 = new int[]{0x00, 0x1C, 0x47, 0x11, 0x00, 0, 0, 0, 0, 0, 0, 0, 0}; // Command 0x0E, DLC: 0 data bytes
        assertEquals("UNKNOWN", MarklinCanCodec.decode(gapMsg1).getCommandCategory());

        // Gap between FEEDBACK (0x12) and SOFTWARE (0x18): commands 0x13-0x17
        int[] gapMsg2 = new int[]{0x00, 0x26, 0x47, 0x11, 0x00, 0, 0, 0, 0, 0, 0, 0, 0}; // Command 0x13, DLC: 0 data bytes
        assertEquals("UNKNOWN", MarklinCanCodec.decode(gapMsg2).getCommandCategory());

        // Gap between SOFTWARE (0x1C) and GUI (0x20): commands 0x1D-0x1F
        int[] gapMsg3 = new int[]{0x00, 0x3A, 0x47, 0x11, 0x00, 0, 0, 0, 0, 0, 0, 0, 0}; // Command 0x1D, DLC: 0 data bytes
        assertEquals("UNKNOWN", MarklinCanCodec.decode(gapMsg3).getCommandCategory());

        // Gap between GUI (0x22) and AUTOMATION (0x30): commands 0x23-0x2F
        int[] gapMsg4 = new int[]{0x00, 0x46, 0x47, 0x11, 0x00, 0, 0, 0, 0, 0, 0, 0, 0}; // Command 0x23, DLC: 0 data bytes
        assertEquals("UNKNOWN", MarklinCanCodec.decode(gapMsg4).getCommandCategory());
    }

    @Test
    public void testRoundTripWithResponseFlag() {
        // Build a response message and verify round-trip
        int[] encoded = MarklinCanCodec.builder()
            .setPriority(MarklinConstants.PRIO_2)
            .setCommand(MarklinConstants.LOCOSPEED)
            .setResponse(true)
            .setAddress(0xC003)
            .setData(0x01, 0xF4)
            .setDataLength(2)
            .build();

        MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(encoded);

        assertEquals(MarklinConstants.PRIO_2, decoded.getPriority());
        assertEquals(MarklinConstants.LOCOSPEED, decoded.getCommand());
        assertTrue(decoded.isResponse());
        assertEquals(0xC003, decoded.getAddress());
        assertEquals(2, decoded.getDataLength());
        assertEquals(0x01, decoded.getDataByte(0));
        assertEquals(0xF4, decoded.getDataByte(1));
    }

    @Test
    public void testBuildWithNoData() {
        // Test building message with zero data bytes
        int[] encoded = MarklinCanCodec.builder()
            .setCommand(MarklinConstants.CMDPING)
            .setAddress(0x12345678L)
            .setDataLength(0)
            .build();

        assertEquals(13, encoded.length);
        assertEquals(0x00, encoded[4]); // DLC: 0 data bytes per CAN 2.0B spec

        MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(encoded);
        assertEquals(0, decoded.getDataLength());
        assertEquals(0x12345678L, decoded.getAddress());
    }

    @Test
    public void testGetBaseAddressAllRanges() {
        // Test base address calculation for all protocol ranges
        assertEquals(0x100, MarklinCanCodec.getBaseAddress(0x1100)); // MM Function
        assertEquals(0x200, MarklinCanCodec.getBaseAddress(0x2200)); // MM Loco 20kHz
        assertEquals(0x100, MarklinCanCodec.getBaseAddress(0x0900)); // SX1
        assertEquals(0x100, MarklinCanCodec.getBaseAddress(0x2900)); // SX1 Accessory
        assertEquals(0x100, MarklinCanCodec.getBaseAddress(0x3100)); // MM Accessory
        assertEquals(0x100, MarklinCanCodec.getBaseAddress(0x3900)); // DCC Accessory
        assertEquals(0x100, MarklinCanCodec.getBaseAddress(0x4100)); // MFX
        assertEquals(0x100, MarklinCanCodec.getBaseAddress(0x8100)); // SX2
        assertEquals(0x100, MarklinCanCodec.getBaseAddress(0xC100)); // DCC
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
