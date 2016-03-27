package jmri.jmrix.secsi;

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
        Assert.assertTrue("valid format - VL2", SerialAddress.validSystemNameFormat("VL2", 'L'));
        Assert.assertTrue("valid format - VL0B2", SerialAddress.validSystemNameFormat("VL0B2", 'L'));
        Assert.assertTrue("invalid format - VL", !SerialAddress.validSystemNameFormat("VL", 'L'));

        Assert.assertTrue("invalid format - VLB2", !SerialAddress.validSystemNameFormat("VLB2", 'L'));
        JUnitAppender.assertErrorMessage("illegal character in number field system name: VL");
        JUnitAppender.assertErrorMessage("no node address before 'B' in system name: VLB2");

        Assert.assertTrue("valid format - VL2005", SerialAddress.validSystemNameFormat("VL2005", 'L'));
        Assert.assertTrue("valid format - VL2B5", SerialAddress.validSystemNameFormat("VL2B5", 'L'));
        Assert.assertTrue("valid format - VT2005", SerialAddress.validSystemNameFormat("VT2005", 'T'));
        Assert.assertTrue("valid format - VT2B5", SerialAddress.validSystemNameFormat("VT2B5", 'T'));
        Assert.assertTrue("valid format - VS2005", SerialAddress.validSystemNameFormat("VS2005", 'S'));
        Assert.assertTrue("valid format - VS2B5", SerialAddress.validSystemNameFormat("VS2B5", 'S'));

        Assert.assertTrue("invalid format - VY2005", !SerialAddress.validSystemNameFormat("VY2005", 'L'));
        JUnitAppender.assertErrorMessage("illegal character in header field system name: VY2005");

        Assert.assertTrue("invalid format - VY2B5", !SerialAddress.validSystemNameFormat("VY2B5", 'L'));
        JUnitAppender.assertErrorMessage("illegal character in header field system name: VY2B5");

        Assert.assertTrue("valid format - VL22001", SerialAddress.validSystemNameFormat("VL22001", 'L'));
        Assert.assertTrue("valid format - VL22B1", SerialAddress.validSystemNameFormat("VL22B1", 'L'));

        Assert.assertTrue("invalid format - VL22000", !SerialAddress.validSystemNameFormat("VL22000", 'L'));
        JUnitAppender.assertErrorMessage("bit number not in range 1 - 999 in system name: VL22000");

        Assert.assertTrue("invalid format - VL22B0", !SerialAddress.validSystemNameFormat("VL22B0", 'L'));
        JUnitAppender.assertErrorMessage("bit number field out of range in system name: VL22B0");

        Assert.assertTrue("valid format - VL2999", SerialAddress.validSystemNameFormat("VL2999", 'L'));
        Assert.assertTrue("invalid format - VL2B2048", !SerialAddress.validSystemNameFormat("VL2B2048", 'L'));
        JUnitAppender.assertErrorMessage("bit number field out of range in system name: VL2B2048");

        Assert.assertTrue("invalid format - VL2B2049", !SerialAddress.validSystemNameFormat("VL2B2049", 'L'));
        JUnitAppender.assertErrorMessage("bit number field out of range in system name: VL2B2049");

        Assert.assertTrue("invalid format - VL2B33", !SerialAddress.validSystemNameFormat("VL2B33", 'L'));
        JUnitAppender.assertErrorMessage("bit number field out of range in system name: VL2B33");

        Assert.assertTrue("valid format - VL127032", SerialAddress.validSystemNameFormat("VL127032", 'L'));

        Assert.assertTrue("valid format - VL127001", SerialAddress.validSystemNameFormat("VL127001", 'L'));

        Assert.assertTrue("invalid format - VL127000", !SerialAddress.validSystemNameFormat("VL127000", 'L'));
        JUnitAppender.assertErrorMessage("bit number not in range 1 - 999 in system name: VL127000");

        Assert.assertTrue("valid format - VL127B7", SerialAddress.validSystemNameFormat("VL127B7", 'L'));

        Assert.assertTrue("invalid format -VL128B7", !SerialAddress.validSystemNameFormat("VL128B7", 'L'));
        JUnitAppender.assertErrorMessage("node address field out of range in system name: VL128B7");

        Assert.assertTrue("invalid format - VL2oo5", !SerialAddress.validSystemNameFormat("VL2oo5", 'L'));
        JUnitAppender.assertErrorMessage("illegal character in number field system name: VL2oo5");

        Assert.assertTrue("invalid format - VL2aB5", !SerialAddress.validSystemNameFormat("VL2aB5", 'L'));
        JUnitAppender.assertErrorMessage("illegal character in node address field of system name: VL2aB5");

        Assert.assertTrue("invalid format - VL2B5x", !SerialAddress.validSystemNameFormat("VL2B5x", 'L'));
        JUnitAppender.assertErrorMessage("illegal character in bit number field of system name: VL2B5x");
    }

    public void testGetBitFromSystemName() {
        Assert.assertEquals("VL2", 2, SerialAddress.getBitFromSystemName("VL2"));
        Assert.assertEquals("VL2002", 2, SerialAddress.getBitFromSystemName("VL2002"));
        Assert.assertEquals("VL1", 1, SerialAddress.getBitFromSystemName("VL1"));
        Assert.assertEquals("VL2001", 1, SerialAddress.getBitFromSystemName("VL2001"));
        Assert.assertEquals("VL999", 999, SerialAddress.getBitFromSystemName("VL999"));
        Assert.assertEquals("VL2999", 999, SerialAddress.getBitFromSystemName("VL2999"));

        Assert.assertEquals("VL29O9", 0, SerialAddress.getBitFromSystemName("VL29O9"));
        JUnitAppender.assertErrorMessage("illegal character in number field of system name: VL29O9");

        Assert.assertEquals("VL0B7", 7, SerialAddress.getBitFromSystemName("VL0B7"));
        Assert.assertEquals("VL2B7", 7, SerialAddress.getBitFromSystemName("VL2B7"));
        Assert.assertEquals("VL0B1", 1, SerialAddress.getBitFromSystemName("VL0B1"));
        Assert.assertEquals("VL2B1", 1, SerialAddress.getBitFromSystemName("VL2B1"));
        Assert.assertEquals("VL0B2048", 2048, SerialAddress.getBitFromSystemName("VL0B2048"));
        Assert.assertEquals("VL11B2048", 2048, SerialAddress.getBitFromSystemName("VL11B2048"));
    }

    public void testGetNodeFromSystemName() {
        SerialNode d = new SerialNode(14, SerialNode.DAUGHTER);
        SerialNode c = new SerialNode(17, SerialNode.DAUGHTER);
        SerialNode b = new SerialNode(127, SerialNode.DAUGHTER);
        Assert.assertEquals("node of VL14007", d, SerialAddress.getNodeFromSystemName("VL14007"));
        Assert.assertEquals("node of VL14B7", d, SerialAddress.getNodeFromSystemName("VL14B7"));
        Assert.assertEquals("node of VL127007", b, SerialAddress.getNodeFromSystemName("VL127007"));
        Assert.assertEquals("node of VL127B7", b, SerialAddress.getNodeFromSystemName("VL127B7"));
        Assert.assertEquals("node of VL17007", c, SerialAddress.getNodeFromSystemName("VL17007"));
        Assert.assertEquals("node of VL17B7", c, SerialAddress.getNodeFromSystemName("VL17B7"));
        Assert.assertEquals("node of VL11007", null, SerialAddress.getNodeFromSystemName("VL11007"));
        Assert.assertEquals("node of VL11B7", null, SerialAddress.getNodeFromSystemName("VL11B7"));
    }

    public void testValidSystemNameConfig() {
        SerialNode d = new SerialNode(4, SerialNode.DAUGHTER);
        SerialNode c = new SerialNode(10, SerialNode.DAUGHTER);
        Assert.assertNotNull("exists", d);
        Assert.assertNotNull("exists", c);
        Assert.assertTrue("valid config VL4007", SerialAddress.validSystemNameConfig("VL4007", 'L'));
        Assert.assertTrue("valid config VL4B7", SerialAddress.validSystemNameConfig("VL4B7", 'L'));
        Assert.assertTrue("valid config VS10007", SerialAddress.validSystemNameConfig("VS10007", 'S'));
        Assert.assertTrue("valid config VS10B7", SerialAddress.validSystemNameConfig("VS10B7", 'S'));
        Assert.assertTrue("valid config VL10032", SerialAddress.validSystemNameConfig("VL10032", 'L'));
        Assert.assertTrue("valid config VL10B32", SerialAddress.validSystemNameConfig("VL10B32", 'L'));

        Assert.assertTrue("invalid config VL10033", !SerialAddress.validSystemNameConfig("VL10033", 'L'));
        JUnitAppender.assertWarnMessage("VL10033 invalid; bad bit number");

        Assert.assertTrue("invalid config VL10B33", !SerialAddress.validSystemNameConfig("VL10B33", 'L'));
        JUnitAppender.assertErrorMessage("bit number field out of range in system name: VL10B33");
        JUnitAppender.assertWarnMessage("VL10B33 invalid; bad format");

        Assert.assertTrue("valid config VS10016", SerialAddress.validSystemNameConfig("VS10016", 'S'));
        Assert.assertTrue("valid config VS10B16", SerialAddress.validSystemNameConfig("VS10B16", 'S'));

        Assert.assertTrue("invalid config VS10017", !SerialAddress.validSystemNameConfig("VS10017", 'S'));
        JUnitAppender.assertWarnMessage("VS10017 invalid; bad bit number");

        Assert.assertTrue("invalid config VS10B17", !SerialAddress.validSystemNameConfig("VS10B17", 'S'));
        JUnitAppender.assertWarnMessage("VS10B17 invalid; bad bit number");

        Assert.assertTrue("valid config VT4016", SerialAddress.validSystemNameConfig("VT4016", 'T'));
        Assert.assertTrue("valid config VT4B16", SerialAddress.validSystemNameConfig("VT4B16", 'T'));

        Assert.assertTrue("invalid config VT4117", !SerialAddress.validSystemNameConfig("VT4117", 'T'));
        JUnitAppender.assertWarnMessage("VT4117 invalid; bad bit number");

        Assert.assertTrue("invalid config VT4B117", !SerialAddress.validSystemNameConfig("VT4B117", 'T'));
        JUnitAppender.assertErrorMessage("bit number field out of range in system name: VT4B117");
        JUnitAppender.assertWarnMessage("VT4B117 invalid; bad format");

        Assert.assertTrue("valid config VS4008", SerialAddress.validSystemNameConfig("VS4008", 'S'));
        Assert.assertTrue("valid config VS4B8", SerialAddress.validSystemNameConfig("VS4B8", 'S'));

        Assert.assertTrue("invalid config VS4017", !SerialAddress.validSystemNameConfig("VS4017", 'S'));
        JUnitAppender.assertWarnMessage("VS4017 invalid; bad bit number");

        Assert.assertTrue("invalid config VS4B19", !SerialAddress.validSystemNameConfig("VS4B19", 'S'));
        JUnitAppender.assertWarnMessage("VS4B19 invalid; bad bit number");

        Assert.assertTrue("invalid config VL11007", !SerialAddress.validSystemNameConfig("VL11007", 'L'));
        JUnitAppender.assertWarnMessage("VL11007 invalid; no such node");

        Assert.assertTrue("invalid config VL11B7", !SerialAddress.validSystemNameConfig("VL11B7", 'L'));
        JUnitAppender.assertWarnMessage("VL11B7 invalid; no such node");

    }

    public void testConvertSystemNameFormat() {
        Assert.assertEquals("convert VL14007", "VL14B7", SerialAddress.convertSystemNameToAlternate("VL14007"));
        Assert.assertEquals("convert VS7", "VS0B7", SerialAddress.convertSystemNameToAlternate("VS7"));
        Assert.assertEquals("convert VT4007", "VT4B7", SerialAddress.convertSystemNameToAlternate("VT4007"));
        Assert.assertEquals("convert VL14B7", "VL14007", SerialAddress.convertSystemNameToAlternate("VL14B7"));
        Assert.assertEquals("convert VL0B7", "VL7", SerialAddress.convertSystemNameToAlternate("VL0B7"));
        Assert.assertEquals("convert VS4B7", "VS4007", SerialAddress.convertSystemNameToAlternate("VS4B7"));
        Assert.assertEquals("convert VL14B8", "VL14008", SerialAddress.convertSystemNameToAlternate("VL14B8"));

        Assert.assertEquals("convert VL128B7", "", SerialAddress.convertSystemNameToAlternate("VL128B7"));
        JUnitAppender.assertErrorMessage("node address field out of range in system name: VL128B7");
    }

    public void testNormalizeSystemName() {
        Assert.assertEquals("normalize VL14007", "VL14007", SerialAddress.normalizeSystemName("VL14007"));
        Assert.assertEquals("normalize VL007", "VL7", SerialAddress.normalizeSystemName("VL007"));
        Assert.assertEquals("normalize VL004007", "VL4007", SerialAddress.normalizeSystemName("VL004007"));
        Assert.assertEquals("normalize VL14B7", "VL14B7", SerialAddress.normalizeSystemName("VL14B7"));
        Assert.assertEquals("normalize VL0B7", "VL0B7", SerialAddress.normalizeSystemName("VL0B7"));
        Assert.assertEquals("normalize VL004B7", "VL4B7", SerialAddress.normalizeSystemName("VL004B7"));
        Assert.assertEquals("normalize VL014B0008", "VL14B8", SerialAddress.normalizeSystemName("VL014B0008"));

        Assert.assertEquals("normalize VL128B7", "", SerialAddress.normalizeSystemName("VL128B7"));
        JUnitAppender.assertErrorMessage("node address field out of range in system name: VL128B7");
    }

    // from here down is testing infrastructure
    public SerialAddressTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SerialAddressTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
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
