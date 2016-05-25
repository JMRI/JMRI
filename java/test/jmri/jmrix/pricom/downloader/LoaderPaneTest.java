package jmri.jmrix.pricom.downloader;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the LoaderPane class
 *
 * @author	Bob Jacobsen Copyright 2005
 * @version	$Revision$
 */
public class LoaderPaneTest extends TestCase {

    public void testCreate() {
        new LoaderPane();
    }

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

    public void testIsUploadReady() {
        LoaderPane p = new LoaderPane();

        byte[] bytes = jmri.util.StringUtil.bytesFromHexString("1F 20 63 00 2C 00 00");
        Assert.assertEquals("1st message", true, p.isUploadReady(bytes));

        bytes = jmri.util.StringUtil.bytesFromHexString("1F 20 1E 42 6F 6F 74 65 72 20 56 31 2E 30 BC CE");
        Assert.assertEquals("2nd message", false, p.isUploadReady(bytes));

    }

    public void testLength() {
        LoaderPane p = new LoaderPane();

        byte[] bytes = jmri.util.StringUtil.bytesFromHexString("1F 20 63 00 2C 00 00");
        Assert.assertEquals("length", 64, p.getDataSize(bytes));

        bytes = jmri.util.StringUtil.bytesFromHexString("1F 20 63 00 2D 00 00");
        Assert.assertEquals("length", 128, p.getDataSize(bytes));

    }

    // from here down is testing infrastructure
    public LoaderPaneTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LoaderPaneTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LoaderPaneTest.class);
        return suite;
    }

}
