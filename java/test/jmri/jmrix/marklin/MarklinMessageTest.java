package jmri.jmrix.marklin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for MarklinMessage.
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MarklinMessageTest extends jmri.jmrix.AbstractMessageTestBase {


    @Test
    public void testGetCanBoot() {
        // Test the Gleisbox activation variant (DLC=5, data byte 0 = 0x11)
        MarklinMessage bootMessage = MarklinMessage.getCanBoot();
        assertNotNull(bootMessage, "CAN BOOT message created");

        // Command 0x1B: (0x1B >> 7) & 0xFF = 0x00, (0x1B << 1) & 0xFF = 0x36
        assertEquals(0x00, bootMessage.getElement(0), "Element 0 (command high bits)");
        assertEquals(0x36, bootMessage.getElement(1), "Element 1 (command low bits)");

        // Hash bytes
        assertEquals(0x47, bootMessage.getElement(2), "Element 2 (hash byte 1)");
        assertEquals(0x11, bootMessage.getElement(3), "Element 3 (hash byte 2)");

        // DLC should be 5
        assertEquals(0x05, bootMessage.getElement(4), "Element 4 (DLC)");

        // Address bytes (broadcast)
        for (int i = 5; i <= 8; i++) {
            assertEquals(0x00, bootMessage.getElement(i), "Element " + i + " (address byte)");
        }

        // Data byte 0: magic value 0x11
        assertEquals(0x11, bootMessage.getElement(9), "Element 9 (data byte 0 - magic value)");

        // Remaining data bytes
        for (int i = 10; i < bootMessage.getNumDataElements(); i++) {
            assertEquals(0x00, bootMessage.getElement(i), "Element " + i + " (data byte)");
        }
    }

    @Test
    public void testGetCanBootloaderMode() {
        // Test the bootloader invocation variant (DLC=0, no data bytes)
        MarklinMessage bootloaderMessage = MarklinMessage.getCanBootloaderMode();
        assertNotNull(bootloaderMessage, "CAN BOOT bootloader message created");

        // Command 0x1B: (0x1B >> 7) & 0xFF = 0x00, (0x1B << 1) & 0xFF = 0x36
        assertEquals(0x00, bootloaderMessage.getElement(0), "Element 0 (command high bits)");
        assertEquals(0x36, bootloaderMessage.getElement(1), "Element 1 (command low bits)");

        // Hash bytes
        assertEquals(0x47, bootloaderMessage.getElement(2), "Element 2 (hash byte 1)");
        assertEquals(0x11, bootloaderMessage.getElement(3), "Element 3 (hash byte 2)");

        // DLC should be 0 (no data bytes)
        assertEquals(0x00, bootloaderMessage.getElement(4), "Element 4 (DLC)");

        // All remaining elements should be 0
        for (int i = 5; i < bootloaderMessage.getNumDataElements(); i++) {
            assertEquals(0x00, bootloaderMessage.getElement(i), "Element " + i + " should be 0");
        }
    }

    @Test
    public void testCanBootVsBootloaderModeDifference() {
        // Verify the two variants are different
        MarklinMessage activation = MarklinMessage.getCanBoot();
        MarklinMessage bootloader = MarklinMessage.getCanBootloaderMode();

        // Command bytes should be identical (both 0x1B)
        assertEquals(activation.getElement(0), bootloader.getElement(0), "Commands should match");
        assertEquals(activation.getElement(1), bootloader.getElement(1), "Commands should match");

        // DLC should be different
        assertNotEquals(activation.getElement(4), bootloader.getElement(4), "DLC should differ");
        assertEquals(0x05, activation.getElement(4), "Activation DLC should be 5");
        assertEquals(0x00, bootloader.getElement(4), "Bootloader DLC should be 0");

        // Data byte 0 should be different
        assertEquals(0x11, activation.getElement(9), "Activation should have magic 0x11");
        assertEquals(0x00, bootloader.getElement(9), "Bootloader should have 0x00");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = new MarklinMessage();
    }

    @Override
    @AfterEach
    public void tearDown() {
        m = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MarklinMessageTest.class);
}
