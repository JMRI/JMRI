package jmri.jmrix.tmcc;

import jmri.util.JUnitAppender;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the SerialAddress utility class.
 *
 * @author	Dave Duchamp Copyright 2004
 * @version	$Revision$
 */
public class SerialAddressTest extends TestCase {

    public void testValidateSystemNameFormat() {
        Assert.assertTrue("valid format - TL2", SerialAddress.validSystemNameFormat("TL2", 'L'));

        Assert.assertTrue("valid format - TL0B2", SerialAddress.validSystemNameFormat("TL0B2", 'L'));

        Assert.assertTrue("invalid format - TL", !SerialAddress.validSystemNameFormat("TL", 'L'));

        Assert.assertTrue("invalid format - TLB2", !SerialAddress.validSystemNameFormat("TLB2", 'L'));
        JUnitAppender.assertErrorMessage("illegal character in number field system name: TL");
        JUnitAppender.assertErrorMessage("no node address before 'B' in system name: TLB2");

        Assert.assertTrue("valid format - TL2005", SerialAddress.validSystemNameFormat("TL2005", 'L'));

        Assert.assertTrue("valid format - TL2B5", SerialAddress.validSystemNameFormat("TL2B5", 'L'));

        Assert.assertTrue("valid format - TT2005", SerialAddress.validSystemNameFormat("TT2005", 'T'));

        Assert.assertTrue("valid format - TT2B5", SerialAddress.validSystemNameFormat("TT2B5", 'T'));

        Assert.assertTrue("valid format - TS2005", SerialAddress.validSystemNameFormat("TS2005", 'S'));

        Assert.assertTrue("valid format - TS2B5", SerialAddress.validSystemNameFormat("TS2B5", 'S'));

        Assert.assertTrue("invalid format - TY2005", !SerialAddress.validSystemNameFormat("TY2005", 'L'));
        JUnitAppender.assertErrorMessage("illegal character in header field system name: TY2005");

        Assert.assertTrue("invalid format - TY2B5", !SerialAddress.validSystemNameFormat("TY2B5", 'L'));
        JUnitAppender.assertErrorMessage("illegal character in header field system name: TY2B5");

        Assert.assertTrue("valid format - TL22001", SerialAddress.validSystemNameFormat("TL22001", 'L'));

        Assert.assertTrue("valid format - TL22B1", SerialAddress.validSystemNameFormat("TL22B1", 'L'));

        Assert.assertTrue("invalid format - TL22000", !SerialAddress.validSystemNameFormat("TL22000", 'L'));
        JUnitAppender.assertErrorMessage("bit number not in range 1 - 999 in system name: TL22000");

        Assert.assertTrue("invalid format - TL22B0", !SerialAddress.validSystemNameFormat("TL22B0", 'L'));
        JUnitAppender.assertErrorMessage("bit number field out of range in system name: TL22B0");

        Assert.assertTrue("valid format - TL2999", SerialAddress.validSystemNameFormat("TL2999", 'L'));

        Assert.assertTrue("valid format - TL2B2048", SerialAddress.validSystemNameFormat("TL2B2048", 'L'));

        Assert.assertTrue("invalid format - TL2B2049", !SerialAddress.validSystemNameFormat("TL2B2049", 'L'));
        JUnitAppender.assertErrorMessage("bit number field out of range in system name: TL2B2049");

        Assert.assertTrue("valid format - TL127999", SerialAddress.validSystemNameFormat("TL127999", 'L'));

        Assert.assertTrue("invalid format - TL128000", !SerialAddress.validSystemNameFormat("TL128000", 'L'));
        JUnitAppender.assertErrorMessage("number field out of range in system name: TL128000");

        Assert.assertTrue("valid format - TL127B7", SerialAddress.validSystemNameFormat("TL127B7", 'L'));

        Assert.assertTrue("invalid format - TL128B7", !SerialAddress.validSystemNameFormat("TL128B7", 'L'));
        JUnitAppender.assertErrorMessage("node address field out of range in system name: TL128B7");

        Assert.assertTrue("invalid format - TL2oo5", !SerialAddress.validSystemNameFormat("TL2oo5", 'L'));
        JUnitAppender.assertErrorMessage("illegal character in number field system name: TL2oo5");

        Assert.assertTrue("invalid format - TL2aB5", !SerialAddress.validSystemNameFormat("TL2aB5", 'L'));
        JUnitAppender.assertErrorMessage("illegal character in node address field of system name: TL2aB5");

        Assert.assertTrue("invalid format - TL2B5x", !SerialAddress.validSystemNameFormat("TL2B5x", 'L'));
        JUnitAppender.assertErrorMessage("illegal character in bit number field of system name: TL2B5x");
    }

    public void testGetBitFromSystemName() {
        Assert.assertEquals("TL2", 2, SerialAddress.getBitFromSystemName("TL2"));
        Assert.assertEquals("TL2002", 2, SerialAddress.getBitFromSystemName("TL2002"));
        Assert.assertEquals("TL1", 1, SerialAddress.getBitFromSystemName("TL1"));
        Assert.assertEquals("TL2001", 1, SerialAddress.getBitFromSystemName("TL2001"));
        Assert.assertEquals("TL999", 999, SerialAddress.getBitFromSystemName("TL999"));
        Assert.assertEquals("TL2999", 999, SerialAddress.getBitFromSystemName("TL2999"));
        Assert.assertEquals("TL29O9", 0, SerialAddress.getBitFromSystemName("TL29O9"));
        JUnitAppender.assertErrorMessage("illegal character in number field of system name: TL29O9");
        Assert.assertEquals("TL0B7", 7, SerialAddress.getBitFromSystemName("TL0B7"));
        Assert.assertEquals("TL2B7", 7, SerialAddress.getBitFromSystemName("TL2B7"));
        Assert.assertEquals("TL0B1", 1, SerialAddress.getBitFromSystemName("TL0B1"));
        Assert.assertEquals("TL2B1", 1, SerialAddress.getBitFromSystemName("TL2B1"));
        Assert.assertEquals("TL0B2048", 2048, SerialAddress.getBitFromSystemName("TL0B2048"));
        Assert.assertEquals("TL11B2048", 2048, SerialAddress.getBitFromSystemName("TL11B2048"));
    }

    public void testValidSystemNameConfig() {
        Assert.assertTrue("valid config TL4007", SerialAddress.validSystemNameConfig("TL4007", 'L'));
        Assert.assertTrue("valid config TL4B7", SerialAddress.validSystemNameConfig("TL4B7", 'L'));
        Assert.assertTrue("valid config TS10007", SerialAddress.validSystemNameConfig("TS10007", 'S'));
        Assert.assertTrue("valid config TS10B7", SerialAddress.validSystemNameConfig("TS10B7", 'S'));
        Assert.assertTrue("valid config TL10048", SerialAddress.validSystemNameConfig("TL10048", 'L'));
        Assert.assertTrue("valid config TL10B48", SerialAddress.validSystemNameConfig("TL10B48", 'L'));

        Assert.assertTrue("valid config TS10024", SerialAddress.validSystemNameConfig("TS10024", 'S'));
        Assert.assertTrue("valid config TS10B24", SerialAddress.validSystemNameConfig("TS10B24", 'S'));

        Assert.assertTrue("valid config TT4128", SerialAddress.validSystemNameConfig("TT4128", 'T'));
        Assert.assertTrue("valid config TT4B128", SerialAddress.validSystemNameConfig("TT4B128", 'T'));

        Assert.assertTrue("valid config TS4064", SerialAddress.validSystemNameConfig("TS4064", 'S'));
        Assert.assertTrue("valid config TS4B64", SerialAddress.validSystemNameConfig("TS4B64", 'S'));

        // Assert.assertTrue("invalid config TS4B65", !SerialAddress.validSystemNameConfig("TS4B65",'S') );
        // Assert.assertTrue("invalid config TL11007", !SerialAddress.validSystemNameConfig("TL11007",'L') );
        // Assert.assertTrue("invalid config TL11B7", !SerialAddress.validSystemNameConfig("TL11B7",'L') );
    }

    public void testConvertSystemNameFormat() {
        Assert.assertEquals("convert TL14007", "TL14B7", SerialAddress.convertSystemNameToAlternate("TL14007"));
        Assert.assertEquals("convert TS7", "TS0B7", SerialAddress.convertSystemNameToAlternate("TS7"));
        Assert.assertEquals("convert TT4007", "TT4B7", SerialAddress.convertSystemNameToAlternate("TT4007"));
        Assert.assertEquals("convert TL14B7", "TL14007", SerialAddress.convertSystemNameToAlternate("TL14B7"));
        Assert.assertEquals("convert TL0B7", "TL7", SerialAddress.convertSystemNameToAlternate("TL0B7"));
        Assert.assertEquals("convert TS4B7", "TS4007", SerialAddress.convertSystemNameToAlternate("TS4B7"));
        Assert.assertEquals("convert TL14B8", "TL14008", SerialAddress.convertSystemNameToAlternate("TL14B8"));
        Assert.assertEquals("convert TL128B7", "", SerialAddress.convertSystemNameToAlternate("TL128B7"));
        JUnitAppender.assertErrorMessage("node address field out of range in system name: TL128B7");
    }

    public void testNormalizeSystemName() {
        Assert.assertEquals("normalize TL14007", "TL14007", SerialAddress.normalizeSystemName("TL14007"));
        Assert.assertEquals("normalize TL007", "TL7", SerialAddress.normalizeSystemName("TL007"));
        Assert.assertEquals("normalize TL004007", "TL4007", SerialAddress.normalizeSystemName("TL004007"));
        Assert.assertEquals("normalize TL14B7", "TL14B7", SerialAddress.normalizeSystemName("TL14B7"));
        Assert.assertEquals("normalize TL0B7", "TL0B7", SerialAddress.normalizeSystemName("TL0B7"));
        Assert.assertEquals("normalize TL004B7", "TL4B7", SerialAddress.normalizeSystemName("TL004B7"));
        Assert.assertEquals("normalize TL014B0008", "TL14B8", SerialAddress.normalizeSystemName("TL014B0008"));
        Assert.assertEquals("normalize TL128B7", "", SerialAddress.normalizeSystemName("TL128B7"));
        JUnitAppender.assertErrorMessage("node address field out of range in system name: TL128B7");
    }

    // from here down is testing infrastructure
    public SerialAddressTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SerialAddressTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SerialAddressTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
