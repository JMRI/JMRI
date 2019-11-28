package jmri.jmrix.pricom.downloader;

import org.junit.*;

/**
 * JUnit tests for the LoaderPane class.
 *
 * @author	Bob Jacobsen Copyright 2005
 */
public class LoaderPaneTest {

    @Test
    public void testCreate() {
        new LoaderPane();
    }

    @Test
    public void testCRC() {
        LoaderPane p = new LoaderPane();

        byte[] bytes = jmri.util.StringUtil.bytesFromHexString("1F 20 63 00 2C 00 00");

        p.CRC_block(bytes);
        Assert.assertEquals("1st CRC byte msg 1", 0x38, bytes[bytes.length - 2] & 0xFF);
        Assert.assertEquals("2nd CRC byte msg 1", 0x71, bytes[bytes.length - 1] & 0xFF);

        bytes = jmri.util.StringUtil.bytesFromHexString("1F 20 1E 42 6F 6F 74 65 72 20 56 31 2E 30 BC CE");
        p.CRC_block(bytes);
        Assert.assertEquals("1st CRC byte msg 2", 0xBC, bytes[bytes.length - 2] & 0xFF);
        Assert.assertEquals("2nd CRC byte msg 2", 0xCE, bytes[bytes.length - 1] & 0xFF);

        bytes = jmri.util.StringUtil.bytesFromHexString("1F 1F 1E 20 20 00 00");
        p.CRC_block(bytes);
        Assert.assertEquals("1st CRC byte msg 3", 0x0C, bytes[bytes.length - 2] & 0xFF);
        Assert.assertEquals("2nd CRC byte msg 3", 0xD4, bytes[bytes.length - 1] & 0xFF);

        bytes = jmri.util.StringUtil.bytesFromHexString("1F 20 1E 47 72 65 65 74 69 6E 67 73 20 66 72 6F 6D 20 74 68 65 20 50 52 49 43 4F 4D 20 44 43 43 20 54 65 73 74 65 72 00 00");
        p.CRC_block(bytes);
        Assert.assertEquals("1st CRC byte msg 4", 0x3E, bytes[bytes.length - 2] & 0xFF);
        Assert.assertEquals("2nd CRC byte msg 4", 0x32, bytes[bytes.length - 1] & 0xFF);

        bytes = jmri.util.StringUtil.bytesFromHexString("1F 1F 1E 56 65 72 73 69 6F 6E 20 31 2E 34 00 00");
        p.CRC_block(bytes);
        Assert.assertEquals("1st CRC byte msg 5", 0x58, bytes[bytes.length - 2] & 0xFF);
        Assert.assertEquals("2nd CRC byte msg 5", 0x93, bytes[bytes.length - 1] & 0xFF);
    }

    @Test
    public void testIsUploadReady() {
        LoaderPane p = new LoaderPane();

        byte[] bytes = jmri.util.StringUtil.bytesFromHexString("1F 20 63 00 2C 00 00");
        Assert.assertEquals("1st message", true, p.isUploadReady(bytes));

        bytes = jmri.util.StringUtil.bytesFromHexString("1F 20 1E 42 6F 6F 74 65 72 20 56 31 2E 30 BC CE");
        Assert.assertEquals("2nd message", false, p.isUploadReady(bytes));
    }

    @Test
    public void testLength() {
        LoaderPane p = new LoaderPane();

        byte[] bytes = jmri.util.StringUtil.bytesFromHexString("1F 20 63 00 2C 00 00");
        Assert.assertEquals("length", 64, p.getDataSize(bytes));

        bytes = jmri.util.StringUtil.bytesFromHexString("1F 20 63 00 2D 00 00");
        Assert.assertEquals("length", 128, p.getDataSize(bytes));
    }

    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}
