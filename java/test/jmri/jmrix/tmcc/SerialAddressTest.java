package jmri.jmrix.tmcc;

import jmri.Manager.NameValidity;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * JUnit tests for the SerialAddress utility class.
 *
 * @author Dave Duchamp Copyright 2004
 */
public class SerialAddressTest {

    @Test
    public void testValidateSystemNameFormat() {
        Assert.assertTrue("valid format - TL2", NameValidity.VALID == SerialAddress.validSystemNameFormat("TL2", 'L', "T"));

        Assert.assertTrue("valid format - TL0B2", NameValidity.VALID == SerialAddress.validSystemNameFormat("TL0B2", 'L', "T"));

        Assert.assertTrue("invalid format - TL", NameValidity.VALID != SerialAddress.validSystemNameFormat("TL", 'L', "T"));

        Assert.assertTrue("invalid format - TLB2", NameValidity.VALID != SerialAddress.validSystemNameFormat("TLB2", 'L', "T"));
        JUnitAppender.assertWarnMessage("invalid character in number field system name: TL");
        JUnitAppender.assertWarnMessage("no node address before 'B' in system name: TLB2");

        Assert.assertTrue("valid format - TL2005", NameValidity.VALID == SerialAddress.validSystemNameFormat("TL2005", 'L', "T"));

        Assert.assertTrue("valid format - TL2B5", NameValidity.VALID == SerialAddress.validSystemNameFormat("TL2B5", 'L', "T"));

        Assert.assertTrue("valid format - TT2005", NameValidity.VALID == SerialAddress.validSystemNameFormat("TT2005", 'T', "T"));

        Assert.assertTrue("valid format - TT2B5", NameValidity.VALID == SerialAddress.validSystemNameFormat("TT2B5", 'T', "T"));

        Assert.assertTrue("valid format - TS2005", NameValidity.VALID == SerialAddress.validSystemNameFormat("TS2005", 'S', "T"));

        Assert.assertTrue("valid format - TS2B5", NameValidity.VALID == SerialAddress.validSystemNameFormat("TS2B5", 'S', "T"));

        Assert.assertTrue("invalid format - TY2005", NameValidity.VALID != SerialAddress.validSystemNameFormat("TY2005", 'L', "T"));
        JUnitAppender.assertErrorMessage("invalid character in header field of system name: TY2005");

        Assert.assertTrue("invalid format - TY2B5", NameValidity.VALID != SerialAddress.validSystemNameFormat("TY2B5", 'L', "T"));
        JUnitAppender.assertErrorMessage("invalid character in header field of system name: TY2B5");

        Assert.assertTrue("valid format - TL22001", NameValidity.VALID == SerialAddress.validSystemNameFormat("TL22001", 'L', "T"));

        Assert.assertTrue("valid format - TL22B1", NameValidity.VALID == SerialAddress.validSystemNameFormat("TL22B1", 'L', "T"));

        Assert.assertTrue("invalid format - TL22000", NameValidity.VALID != SerialAddress.validSystemNameFormat("TL22000", 'L', "T"));
        JUnitAppender.assertWarnMessage("bit number not in range 1 - 999 in system name: TL22000");

        Assert.assertTrue("invalid format - TL22B0", NameValidity.VALID != SerialAddress.validSystemNameFormat("TL22B0", 'L', "T"));
        JUnitAppender.assertWarnMessage("bit number field out of range in system name: TL22B0");

        Assert.assertTrue("valid format - TL2999", NameValidity.VALID == SerialAddress.validSystemNameFormat("TL2999", 'L', "T"));

        Assert.assertTrue("valid format - TL2B2048", NameValidity.VALID == SerialAddress.validSystemNameFormat("TL2B2048", 'L', "T"));

        Assert.assertTrue("invalid format - TL2B2049", NameValidity.VALID != SerialAddress.validSystemNameFormat("TL2B2049", 'L', "T"));
        JUnitAppender.assertWarnMessage("bit number field out of range in system name: TL2B2049");

        Assert.assertTrue("valid format - TL127999", NameValidity.VALID == SerialAddress.validSystemNameFormat("TL127999", 'L', "T"));

        Assert.assertTrue("invalid format - TL128000", NameValidity.VALID != SerialAddress.validSystemNameFormat("TL128000", 'L', "T"));
        JUnitAppender.assertWarnMessage("number field out of range in system name: TL128000");

        Assert.assertTrue("valid format - TL127B7", NameValidity.VALID == SerialAddress.validSystemNameFormat("TL127B7", 'L', "T"));

        Assert.assertTrue("invalid format - TL128B7", NameValidity.VALID != SerialAddress.validSystemNameFormat("TL128B7", 'L', "T"));
        JUnitAppender.assertWarnMessage("node address field out of range in system name: TL128B7");

        Assert.assertTrue("invalid format - TL2oo5", NameValidity.VALID != SerialAddress.validSystemNameFormat("TL2oo5", 'L', "T"));
        JUnitAppender.assertWarnMessage("invalid character in number field system name: TL2oo5");

        Assert.assertTrue("invalid format - TL2aB5", NameValidity.VALID != SerialAddress.validSystemNameFormat("TL2aB5", 'L', "T"));
        JUnitAppender.assertWarnMessage("invalid character in node address field of system name: TL2aB5");

        Assert.assertTrue("invalid format - TL2B5x", NameValidity.VALID != SerialAddress.validSystemNameFormat("TL2B5x", 'L', "T"));
        JUnitAppender.assertWarnMessage("invalid character in bit number field of system name: TL2B5x");
    }

    @Test
    public void testGetBitFromSystemName() {
        Assert.assertEquals("TL2", 2, SerialAddress.getBitFromSystemName("TL2", "T"));
        Assert.assertEquals("TL2002", 2, SerialAddress.getBitFromSystemName("TL2002", "T"));
        Assert.assertEquals("TL1", 1, SerialAddress.getBitFromSystemName("TL1", "T"));
        Assert.assertEquals("TL2001", 1, SerialAddress.getBitFromSystemName("TL2001", "T"));
        Assert.assertEquals("TL999", 999, SerialAddress.getBitFromSystemName("TL999", "T"));
        Assert.assertEquals("TL2999", 999, SerialAddress.getBitFromSystemName("TL2999", "T"));
        Assert.assertEquals("TL29O9", 0, SerialAddress.getBitFromSystemName("TL29O9", "T"));
        JUnitAppender.assertWarnMessage("invalid character in number field of system name: TL29O9");
        Assert.assertEquals("TL0B7", 7, SerialAddress.getBitFromSystemName("TL0B7", "T"));
        Assert.assertEquals("TL2B7", 7, SerialAddress.getBitFromSystemName("TL2B7", "T"));
        Assert.assertEquals("TL0B1", 1, SerialAddress.getBitFromSystemName("TL0B1", "T"));
        Assert.assertEquals("TL2B1", 1, SerialAddress.getBitFromSystemName("TL2B1", "T"));
        Assert.assertEquals("TL0B2048", 2048, SerialAddress.getBitFromSystemName("TL0B2048", "T"));
        Assert.assertEquals("TL11B2048", 2048, SerialAddress.getBitFromSystemName("TL11B2048", "T"));
    }

    @Test
    public void testValidSystemNameConfig() {
        Assert.assertTrue("valid config TL4007", SerialAddress.validSystemNameConfig("TL4007", 'L', "T"));
        Assert.assertTrue("valid config TL4B7", SerialAddress.validSystemNameConfig("TL4B7", 'L', "T"));
        Assert.assertTrue("valid config TS10007", SerialAddress.validSystemNameConfig("TS10007", 'S', "T"));
        Assert.assertTrue("valid config TS10B7", SerialAddress.validSystemNameConfig("TS10B7", 'S', "T"));
        Assert.assertTrue("valid config TL10048", SerialAddress.validSystemNameConfig("TL10048", 'L', "T"));
        Assert.assertTrue("valid config TL10B48", SerialAddress.validSystemNameConfig("TL10B48", 'L', "T"));

        Assert.assertTrue("valid config TS10024", SerialAddress.validSystemNameConfig("TS10024", 'S', "T"));
        Assert.assertTrue("valid config TS10B24", SerialAddress.validSystemNameConfig("TS10B24", 'S', "T"));

        Assert.assertTrue("valid config TT4128", SerialAddress.validSystemNameConfig("TT4128", 'T', "T"));
        Assert.assertTrue("valid config TT4B128", SerialAddress.validSystemNameConfig("TT4B128", 'T', "T"));

        Assert.assertTrue("valid config TS4064", SerialAddress.validSystemNameConfig("TS4064", 'S', "T"));
        Assert.assertTrue("valid config TS4B64", SerialAddress.validSystemNameConfig("TS4B64", 'S', "T"));

        // Assert.assertTrue("invalid config TS4B65", !SerialAddress.validSystemNameConfig("TS4B65",'S', "T") );
        // Assert.assertTrue("invalid config TL11007", !SerialAddress.validSystemNameConfig("TL11007",'L', "T") );
        // Assert.assertTrue("invalid config TL11B7", !SerialAddress.validSystemNameConfig("TL11B7",'L', "T") );
    }

    @Test
    public void testConvertSystemNameFormat() {
        Assert.assertEquals("convert TL14007", "TL14B7", SerialAddress.convertSystemNameToAlternate("TL14007", "T"));
        Assert.assertEquals("convert TS7", "TS0B7", SerialAddress.convertSystemNameToAlternate("TS7", "T"));
        Assert.assertEquals("convert TT4007", "TT4B7", SerialAddress.convertSystemNameToAlternate("TT4007", "T"));
        Assert.assertEquals("convert TL14B7", "TL14007", SerialAddress.convertSystemNameToAlternate("TL14B7", "T"));
        Assert.assertEquals("convert TL0B7", "TL7", SerialAddress.convertSystemNameToAlternate("TL0B7", "T"));
        Assert.assertEquals("convert TS4B7", "TS4007", SerialAddress.convertSystemNameToAlternate("TS4B7", "T"));
        Assert.assertEquals("convert TL14B8", "TL14008", SerialAddress.convertSystemNameToAlternate("TL14B8", "T"));
        Assert.assertEquals("convert TL128B7", "", SerialAddress.convertSystemNameToAlternate("TL128B7", "T"));
        JUnitAppender.assertWarnMessage("node address field out of range in system name: TL128B7");
    }

    @Test
    public void testNormalizeSystemName() {
        Assert.assertEquals("normalize TL14007", "TL14007", SerialAddress.normalizeSystemName("TL14007", "T"));
        Assert.assertEquals("normalize TL007", "TL7", SerialAddress.normalizeSystemName("TL007", "T"));
        Assert.assertEquals("normalize TL004007", "TL4007", SerialAddress.normalizeSystemName("TL004007", "T"));
        Assert.assertEquals("normalize TL14B7", "TL14B7", SerialAddress.normalizeSystemName("TL14B7", "T"));
        Assert.assertEquals("normalize TL0B7", "TL0B7", SerialAddress.normalizeSystemName("TL0B7", "T"));
        Assert.assertEquals("normalize TL004B7", "TL4B7", SerialAddress.normalizeSystemName("TL004B7", "T"));
        Assert.assertEquals("normalize TL014B0008", "TL14B8", SerialAddress.normalizeSystemName("TL014B0008", "T"));
        Assert.assertEquals("normalize TL128B7", "", SerialAddress.normalizeSystemName("TL128B7", "T"));
        JUnitAppender.assertWarnMessage("node address field out of range in system name: TL128B7");
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
