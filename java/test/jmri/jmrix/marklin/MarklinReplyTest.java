package jmri.jmrix.marklin;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MarklinReplyTest extends jmri.jmrix.AbstractMessageTestBase {

    private MarklinReply mr;

    @Test
    public void testDefaultMarklinReply() {
        Assertions.assertEquals(MarklinConstants.PRIO_1, mr.getPriority());
        Assertions.assertEquals(0, mr.getCommand());
    }

    @Test
    public void testSetGetCommand() {
        mr.setCommand( MarklinConstants.SYSCOMMANDSTART);
        Assertions.assertEquals(MarklinConstants.SYSCOMMANDSTART, mr.getCommand());

        mr.setCommand( MarklinConstants.AUTCOMMANDSTART);
        Assertions.assertEquals(MarklinConstants.AUTCOMMANDSTART, mr.getCommand());
    }

    @Test
    public void testSetGetAddress() {
        Assertions.assertEquals( 0, mr.getAddress());
        mr.setAddress( 0xFFFFFFFFL);
        Assertions.assertEquals(0xFFFFFFFFL, mr.getAddress());

        mr.setAddress( 0xA1B2C3D4L);
        Assertions.assertEquals(0xA1B2C3D4L, mr.getAddress());
    }

    @Test
    public void testCommandPreservesPriority() {
        // Test that setCommand preserves priority bits
        // Set initial priority to PRIO_4 (0x03) by setting byte 0 to 0x30
        mr.setElement(0, 0x30);
        mr.setElement(1, 0x00);

        // Verify initial priority
        assertEquals(MarklinConstants.PRIO_4, mr.getPriority());

        // Set a command
        mr.setCommand(MarklinConstants.LOCOSPEED); // 0x04

        // Priority should remain unchanged
        assertEquals(MarklinConstants.PRIO_4, mr.getPriority());
        assertEquals(MarklinConstants.LOCOSPEED, mr.getCommand());
    }

    @Test
    public void testCommandPreservesResponseFlag() {
        // Test that setCommand preserves response flag
        // Set response flag (bit 0 of byte 1)
        mr.setElement(0, 0x00);
        mr.setElement(1, 0x01); // Response flag set

        // Verify initial state
        assertTrue(mr.isResponse());

        // Set a command
        mr.setCommand(MarklinConstants.LOCOFUNCTION); // 0x06

        // Response flag should remain set
        assertTrue(mr.isResponse());
        assertEquals(MarklinConstants.LOCOFUNCTION, mr.getCommand());
    }

    @Test
    public void testGetCommandWithPrioritySet() {
        // Test getCommand extracts only command bits, not priority
        // Byte 0: 0x20 = 0010 0000 (priority 2=PRIO_3, command bits 10-7 = 0000)
        // Byte 1: 0x08 = 0000 1000 (command bits 6-0 = 0000100)
        // Command should be 0000 0000100 = 0x04
        mr.setElement(0, 0x20);
        mr.setElement(1, 0x08);

        assertEquals(MarklinConstants.PRIO_3, mr.getPriority());
        assertEquals(MarklinConstants.LOCOSPEED, mr.getCommand());
    }

    @Test
    public void testGetCommandWithLargeCommand() {
        // Test with a large command value (11 bits)
        // Command 0x7FF (all 11 bits set)
        // Byte 0 lower 4 bits: 0x0F (command bits 10-7)
        // Byte 1 upper 7 bits: 0xFE (command bits 6-0 shifted left)
        mr.setElement(0, 0x0F);
        mr.setElement(1, 0xFE);

        assertEquals(0x7FF, mr.getCommand());
    }

    @Test
    public void testSetCommandWithAllBitsSet() {
        // Test setting maximum command value
        mr.setCommand(0x7FF); // All 11 bits set

        assertEquals(0x7FF, mr.getCommand());
        // Verify the bit pattern
        assertEquals(0x0F, mr.getElement(0) & 0x0F); // Lower 4 bits of byte 0
        assertEquals(0xFE, mr.getElement(1) & 0xFE); // Upper 7 bits of byte 1
    }

    @Test
    public void testRoundTripCommandWithPriorityAndResponse() {
        // Complex test: set priority, response flag and command, verify all preserved
        mr.setElement(0, 0x10); // Priority 1=PRIO_2
        mr.setElement(1, 0x01); // Response flag set

        mr.setCommand(MarklinConstants.ACCCOMMANDSTART); // 0x0B

        assertEquals(MarklinConstants.PRIO_2, mr.getPriority());
        assertTrue(mr.isResponse());
        assertEquals(MarklinConstants.ACCCOMMANDSTART, mr.getCommand());
    }

    @Test
    public void testSetAddressDoesNotAffectOtherFields() {
        // Set initial state with command and priority
        mr.setElement(0, 0x00); // Priority 0=PRIO_1, command bits
        mr.setElement(1, 0x08); // Command 0x04
        mr.setElement(2, 0x47); // Hash byte 1
        mr.setElement(3, 0x11); // Hash byte 2

        // Set address
        mr.setAddress(0xC003L);

        // Verify other fields unchanged
        assertEquals(MarklinConstants.PRIO_1, mr.getPriority());
        assertEquals(0x47, mr.getElement(2));
        assertEquals(0x11, mr.getElement(3));

        // Verify address set correctly
        assertEquals(0xC003L, mr.getAddress());
    }

    @Test
    public void testGetCommand() {
        int[] message = new int[]{
            0x00, 0x08, 0x47, 0x11, 0x02,
            0x00, 0x00, 0xC0, 0x03,
            0x01, 0xF4, 0x00, 0x00
        };
        mr = new MarklinReply(message);
        // command encoded in bytes 0-1: (byte0 & 0x0F) << 7 | (byte1 & 0xFE) >> 1
        int expectedCommand = ((0x00 & 0x0F) << 7) | ((0x08 & 0xFE) >> 1);
        assertEquals(expectedCommand, mr.getCommand());
        assertEquals(0xC003L, mr.getAddress());
    }

    @Test
    public void testSetCommand() {
        mr = new MarklinReply();
        mr.setCommand(MarklinConstants.LOCOFUNCTION);
        assertEquals(MarklinConstants.LOCOFUNCTION, mr.getCommand());
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        mr = new MarklinReply();
        m = mr;
    }

    @Override
    @AfterEach
    public void tearDown() {
        m = null;
        mr = null;
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(MarklinReplyTest.class);
}
