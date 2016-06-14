package jmri.jmrix.oaktree;

import jmri.util.JUnitAppender;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the SerialAddress utility class.
 *
 * @author	Dave Duchamp Copyright 2004
 * @author Bob Jacobsen Copyright 2007
 * @version	$Revision$
 */
public class SerialAddressTest extends TestCase {

    public void testValidateSystemNameFormat() {
        Assert.assertTrue("valid format - OL2", SerialAddress.validSystemNameFormat("OL2", 'L'));
        Assert.assertTrue("valid format - OL0B2", SerialAddress.validSystemNameFormat("OL0B2", 'L'));
        Assert.assertTrue("invalid format - OL", !SerialAddress.validSystemNameFormat("OL", 'L'));

        Assert.assertTrue("invalid format - OLB2", !SerialAddress.validSystemNameFormat("OLB2", 'L'));
        JUnitAppender.assertErrorMessage("illegal character in number field system name: OL");
        JUnitAppender.assertErrorMessage("no node address before 'B' in system name: OLB2");

        Assert.assertTrue("valid format - OL2005", SerialAddress.validSystemNameFormat("OL2005", 'L'));
        Assert.assertTrue("valid format - OL2B5", SerialAddress.validSystemNameFormat("OL2B5", 'L'));
        Assert.assertTrue("valid format - OT2005", SerialAddress.validSystemNameFormat("OT2005", 'T'));
        Assert.assertTrue("valid format - OT2B5", SerialAddress.validSystemNameFormat("OT2B5", 'T'));
        Assert.assertTrue("valid format - OS2005", SerialAddress.validSystemNameFormat("OS2005", 'S'));
        Assert.assertTrue("valid format - OS2B5", SerialAddress.validSystemNameFormat("OS2B5", 'S'));

        Assert.assertTrue("invalid format - OY2005", !SerialAddress.validSystemNameFormat("OY2005", 'L'));
        JUnitAppender.assertErrorMessage("illegal character in header field system name: OY2005");

        Assert.assertTrue("invalid format - OY2B5", !SerialAddress.validSystemNameFormat("OY2B5", 'L'));
        JUnitAppender.assertErrorMessage("illegal character in header field system name: OY2B5");

        Assert.assertTrue("valid format - OL22001", SerialAddress.validSystemNameFormat("OL22001", 'L'));
        Assert.assertTrue("valid format - OL22B1", SerialAddress.validSystemNameFormat("OL22B1", 'L'));

        Assert.assertTrue("invalid format - OL22000", !SerialAddress.validSystemNameFormat("OL22000", 'L'));
        JUnitAppender.assertErrorMessage("bit number not in range 1 - 999 in system name: OL22000");

        Assert.assertTrue("invalid format - OL22B0", !SerialAddress.validSystemNameFormat("OL22B0", 'L'));
        JUnitAppender.assertErrorMessage("bit number field out of range in system name: OL22B0");

        Assert.assertTrue("valid format - OL2999", SerialAddress.validSystemNameFormat("OL2999", 'L'));
        Assert.assertTrue("valid format - OL2B2048", SerialAddress.validSystemNameFormat("OL2B2048", 'L'));

        Assert.assertTrue("invalid format - OL2B2049", !SerialAddress.validSystemNameFormat("OL2B2049", 'L'));
        JUnitAppender.assertErrorMessage("bit number field out of range in system name: OL2B2049");

        Assert.assertTrue("valid format - OL127999", SerialAddress.validSystemNameFormat("OL127999", 'L'));

        Assert.assertTrue("invalid format - OL128000", !SerialAddress.validSystemNameFormat("OL128000", 'L'));
        JUnitAppender.assertErrorMessage("bit number not in range 1 - 999 in system name: OL128000");

        Assert.assertTrue("valid format - OL127B7", SerialAddress.validSystemNameFormat("OL127B7", 'L'));

        Assert.assertTrue("invalid format - OL128B7", !SerialAddress.validSystemNameFormat("OL128B7", 'L'));
        JUnitAppender.assertErrorMessage("node address field out of range in system name: OL128B7");

        Assert.assertTrue("invalid format - OL2oo5", !SerialAddress.validSystemNameFormat("OL2oo5", 'L'));
        JUnitAppender.assertErrorMessage("illegal character in number field system name: OL2oo5");

        Assert.assertTrue("invalid format - OL2aB5", !SerialAddress.validSystemNameFormat("OL2aB5", 'L'));
        JUnitAppender.assertErrorMessage("illegal character in node address field of system name: OL2aB5");

        Assert.assertTrue("invalid format - OL2B5x", !SerialAddress.validSystemNameFormat("OL2B5x", 'L'));
        JUnitAppender.assertErrorMessage("illegal character in bit number field of system name: OL2B5x");
    }

    public void testGetBitFromSystemName() {
        Assert.assertEquals("OL2", 2, SerialAddress.getBitFromSystemName("OL2"));
        Assert.assertEquals("OL2002", 2, SerialAddress.getBitFromSystemName("OL2002"));
        Assert.assertEquals("OL1", 1, SerialAddress.getBitFromSystemName("OL1"));
        Assert.assertEquals("OL2001", 1, SerialAddress.getBitFromSystemName("OL2001"));
        Assert.assertEquals("OL999", 999, SerialAddress.getBitFromSystemName("OL999"));
        Assert.assertEquals("OL2999", 999, SerialAddress.getBitFromSystemName("OL2999"));

        Assert.assertEquals("OL29O9", 0, SerialAddress.getBitFromSystemName("OL29O9"));
        JUnitAppender.assertErrorMessage("illegal character in number field of system name: OL29O9");

        Assert.assertEquals("OL0B7", 7, SerialAddress.getBitFromSystemName("OL0B7"));
        Assert.assertEquals("OL2B7", 7, SerialAddress.getBitFromSystemName("OL2B7"));
        Assert.assertEquals("OL0B1", 1, SerialAddress.getBitFromSystemName("OL0B1"));
        Assert.assertEquals("OL2B1", 1, SerialAddress.getBitFromSystemName("OL2B1"));
        Assert.assertEquals("OL0B2048", 2048, SerialAddress.getBitFromSystemName("OL0B2048"));
        Assert.assertEquals("OL11B2048", 2048, SerialAddress.getBitFromSystemName("OL11B2048"));
    }

    public void testGetNodeFromSystemName() {
        SerialNode d = new SerialNode(14, SerialNode.IO48);
        SerialNode c = new SerialNode(17, SerialNode.IO24);
        SerialNode b = new SerialNode(127, SerialNode.IO24);
        Assert.assertEquals("node of OL14007", d, SerialAddress.getNodeFromSystemName("OL14007"));
        Assert.assertEquals("node of OL14B7", d, SerialAddress.getNodeFromSystemName("OL14B7"));
        Assert.assertEquals("node of OL127007", b, SerialAddress.getNodeFromSystemName("OL127007"));
        Assert.assertEquals("node of OL127B7", b, SerialAddress.getNodeFromSystemName("OL127B7"));
        Assert.assertEquals("node of OL17007", c, SerialAddress.getNodeFromSystemName("OL17007"));
        Assert.assertEquals("node of OL17B7", c, SerialAddress.getNodeFromSystemName("OL17B7"));
        Assert.assertEquals("node of OL11007", null, SerialAddress.getNodeFromSystemName("OL11007"));
        Assert.assertEquals("node of OL11B7", null, SerialAddress.getNodeFromSystemName("OL11B7"));
    }

    public void testValidSystemNameConfig() {
        SerialNode d = new SerialNode(4, SerialNode.IO24);
        SerialNode c = new SerialNode(10, SerialNode.IO48);
        Assert.assertNotNull("exists", d);
        Assert.assertNotNull("exists", c);
        Assert.assertTrue("valid config OL4007", SerialAddress.validSystemNameConfig("OL4007", 'L'));
        Assert.assertTrue("valid config OL4B7", SerialAddress.validSystemNameConfig("OL4B7", 'L'));
        Assert.assertTrue("valid config OS10007", SerialAddress.validSystemNameConfig("OS10007", 'S'));
        Assert.assertTrue("valid config OS10B7", SerialAddress.validSystemNameConfig("OS10B7", 'S'));
        Assert.assertTrue("valid config OL10032", SerialAddress.validSystemNameConfig("OL10032", 'L'));
        Assert.assertTrue("valid config OL10B32", SerialAddress.validSystemNameConfig("OL10B32", 'L'));

        Assert.assertTrue("invalid config OL10033", !SerialAddress.validSystemNameConfig("OL10033", 'L'));
        JUnitAppender.assertWarnMessage("OL10033 invalid; bad bit number");

        Assert.assertTrue("invalid config OL10B33", !SerialAddress.validSystemNameConfig("OL10B33", 'L'));
        JUnitAppender.assertWarnMessage("OL10B33 invalid; bad bit number");

        Assert.assertTrue("valid config OS10016", SerialAddress.validSystemNameConfig("OS10016", 'S'));
        Assert.assertTrue("valid config OS10B16", SerialAddress.validSystemNameConfig("OS10B16", 'S'));

        Assert.assertTrue("invalid config OS10017", !SerialAddress.validSystemNameConfig("OS10017", 'S'));
        JUnitAppender.assertWarnMessage("OS10017 invalid; bad bit number");

        Assert.assertTrue("invalid config OS10B17", !SerialAddress.validSystemNameConfig("OS10B17", 'S'));
        JUnitAppender.assertWarnMessage("OS10B17 invalid; bad bit number");

        Assert.assertTrue("valid config OT4016", SerialAddress.validSystemNameConfig("OT4016", 'T'));
        Assert.assertTrue("valid config OT4B16", SerialAddress.validSystemNameConfig("OT4B16", 'T'));

        Assert.assertTrue("invalid config OT4017", !SerialAddress.validSystemNameConfig("OT4017", 'T'));
        JUnitAppender.assertWarnMessage("OT4017 invalid; bad bit number");

        Assert.assertTrue("invalid config OT4017", !SerialAddress.validSystemNameConfig("OT4B17", 'T'));
        JUnitAppender.assertWarnMessage("OT4B17 invalid; bad bit number");

        Assert.assertTrue("valid config OS4008", SerialAddress.validSystemNameConfig("OS4008", 'S'));
        Assert.assertTrue("valid config OS4B8", SerialAddress.validSystemNameConfig("OS4B8", 'S'));

        Assert.assertTrue("invalid config OS4009", !SerialAddress.validSystemNameConfig("OS4009", 'S'));
        JUnitAppender.assertWarnMessage("OS4009 invalid; bad bit number");

        Assert.assertTrue("invalid config OS4B9", !SerialAddress.validSystemNameConfig("OS4B9", 'S'));
        JUnitAppender.assertWarnMessage("OS4B9 invalid; bad bit number");

        Assert.assertTrue("invalid config OL11007", !SerialAddress.validSystemNameConfig("OL11007", 'L'));
        JUnitAppender.assertWarnMessage("OL11007 invalid; no such node");

        Assert.assertTrue("invalid config OL11B7", !SerialAddress.validSystemNameConfig("OL11B7", 'L'));
        JUnitAppender.assertWarnMessage("OL11B7 invalid; no such node");

    }

    public void testConvertSystemNameFormat() {
        Assert.assertEquals("convert OL14007", "OL14B7", SerialAddress.convertSystemNameToAlternate("OL14007"));
        Assert.assertEquals("convert OS7", "OS0B7", SerialAddress.convertSystemNameToAlternate("OS7"));
        Assert.assertEquals("convert OT4007", "OT4B7", SerialAddress.convertSystemNameToAlternate("OT4007"));
        Assert.assertEquals("convert OL14B7", "OL14007", SerialAddress.convertSystemNameToAlternate("OL14B7"));
        Assert.assertEquals("convert OL0B7", "OL7", SerialAddress.convertSystemNameToAlternate("OL0B7"));
        Assert.assertEquals("convert OS4B7", "OS4007", SerialAddress.convertSystemNameToAlternate("OS4B7"));
        Assert.assertEquals("convert OL14B8", "OL14008", SerialAddress.convertSystemNameToAlternate("OL14B8"));

        Assert.assertEquals("convert OL128B7", "", SerialAddress.convertSystemNameToAlternate("OL128B7"));
        JUnitAppender.assertErrorMessage("node address field out of range in system name: OL128B7");
    }

    public void testNormalizeSystemName() {
        Assert.assertEquals("normalize OL14007", "OL14007", SerialAddress.normalizeSystemName("OL14007"));
        Assert.assertEquals("normalize OL007", "OL7", SerialAddress.normalizeSystemName("OL007"));
        Assert.assertEquals("normalize OL004007", "OL4007", SerialAddress.normalizeSystemName("OL004007"));
        Assert.assertEquals("normalize OL14B7", "OL14B7", SerialAddress.normalizeSystemName("OL14B7"));
        Assert.assertEquals("normalize OL0B7", "OL0B7", SerialAddress.normalizeSystemName("OL0B7"));
        Assert.assertEquals("normalize OL004B7", "OL4B7", SerialAddress.normalizeSystemName("OL004B7"));
        Assert.assertEquals("normalize OL014B0008", "OL14B8", SerialAddress.normalizeSystemName("OL014B0008"));

        Assert.assertEquals("normalize OL128B7", "", SerialAddress.normalizeSystemName("OL128B7"));
        JUnitAppender.assertErrorMessage("node address field out of range in system name: OL128B7");
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
