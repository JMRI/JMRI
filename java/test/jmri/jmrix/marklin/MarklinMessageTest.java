package jmri.jmrix.marklin;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Tests for MarklinMessage.
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MarklinMessageTest extends jmri.jmrix.AbstractMessageTestBase {

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

    @Test
    public void testGetCanBoot() {
        // Test the Gleisbox activation variant (DLC=5, data byte 0 = 0x11)
        MarklinMessage bootMessage = MarklinMessage.getCanBoot();
        Assert.assertNotNull("CAN BOOT message created", bootMessage);

        // Command 0x1B: (0x1B >> 7) & 0xFF = 0x00, (0x1B << 1) & 0xFF = 0x36
        Assert.assertEquals("Element 0 (command high bits)", 0x00, bootMessage.getElement(0));
        Assert.assertEquals("Element 1 (command low bits)", 0x36, bootMessage.getElement(1));

        // Hash bytes
        Assert.assertEquals("Element 2 (hash byte 1)", 0x47, bootMessage.getElement(2));
        Assert.assertEquals("Element 3 (hash byte 2)", 0x11, bootMessage.getElement(3));

        // DLC should be 5
        Assert.assertEquals("Element 4 (DLC)", 0x05, bootMessage.getElement(4));

        // Address bytes (broadcast)
        for (int i = 5; i <= 8; i++) {
            Assert.assertEquals("Element " + i + " (address byte)", 0x00, bootMessage.getElement(i));
        }

        // Data byte 0: magic value 0x11
        Assert.assertEquals("Element 9 (data byte 0 - magic value)", 0x11, bootMessage.getElement(9));

        // Remaining data bytes
        for (int i = 10; i < bootMessage.getNumDataElements(); i++) {
            Assert.assertEquals("Element " + i + " (data byte)", 0x00, bootMessage.getElement(i));
        }
    }

    @Test
    public void testGetCanBootloaderMode() {
        // Test the bootloader invocation variant (DLC=0, no data bytes)
        MarklinMessage bootloaderMessage = MarklinMessage.getCanBootloaderMode();
        Assert.assertNotNull("CAN BOOT bootloader message created", bootloaderMessage);

        // Command 0x1B: (0x1B >> 7) & 0xFF = 0x00, (0x1B << 1) & 0xFF = 0x36
        Assert.assertEquals("Element 0 (command high bits)", 0x00, bootloaderMessage.getElement(0));
        Assert.assertEquals("Element 1 (command low bits)", 0x36, bootloaderMessage.getElement(1));

        // Hash bytes
        Assert.assertEquals("Element 2 (hash byte 1)", 0x47, bootloaderMessage.getElement(2));
        Assert.assertEquals("Element 3 (hash byte 2)", 0x11, bootloaderMessage.getElement(3));

        // DLC should be 0 (no data bytes)
        Assert.assertEquals("Element 4 (DLC)", 0x00, bootloaderMessage.getElement(4));

        // All remaining elements should be 0
        for (int i = 5; i < bootloaderMessage.getNumDataElements(); i++) {
            Assert.assertEquals("Element " + i + " should be 0", 0x00, bootloaderMessage.getElement(i));
        }
    }

    @Test
    public void testCanBootVsBootloaderModeDifference() {
        // Verify the two variants are different
        MarklinMessage activation = MarklinMessage.getCanBoot();
        MarklinMessage bootloader = MarklinMessage.getCanBootloaderMode();

        // Command bytes should be identical (both 0x1B)
        Assert.assertEquals("Commands should match", activation.getElement(0), bootloader.getElement(0));
        Assert.assertEquals("Commands should match", activation.getElement(1), bootloader.getElement(1));

        // DLC should be different
        Assert.assertNotEquals("DLC should differ", activation.getElement(4), bootloader.getElement(4));
        Assert.assertEquals("Activation DLC should be 5", 0x05, activation.getElement(4));
        Assert.assertEquals("Bootloader DLC should be 0", 0x00, bootloader.getElement(4));

        // Data byte 0 should be different
        Assert.assertEquals("Activation should have magic 0x11", 0x11, activation.getElement(9));
        Assert.assertEquals("Bootloader should have 0x00", 0x00, bootloader.getElement(9));
    }

    // private final static Logger log = LoggerFactory.getLogger(MarklinMessageTest.class);
}
