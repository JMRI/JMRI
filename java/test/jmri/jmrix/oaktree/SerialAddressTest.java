package jmri.jmrix.oaktree;

import jmri.Manager.NameValidity;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * JUnit tests for the SerialAddress utility class.
 *
 * @author	Dave Duchamp Copyright 2004
 * @author Bob Jacobsen Copyright 2007
  */
public class SerialAddressTest extends TestCase {

    public void testValidateSystemNameFormat() {
        Assert.assertTrue("valid format - OL2", NameValidity.VALID == SerialAddress.validSystemNameFormat("OL2", 'L', "O"));
        Assert.assertTrue("valid format - OL0B2", NameValidity.VALID == SerialAddress.validSystemNameFormat("OL0B2", 'L', "O"));
        Assert.assertTrue("invalid format - OL", NameValidity.VALID != SerialAddress.validSystemNameFormat("OL", 'L', "O"));

        Assert.assertTrue("invalid format - OLB2", NameValidity.VALID != SerialAddress.validSystemNameFormat("OLB2", 'L', "O"));
        JUnitAppender.assertWarnMessage("invalid character in number field system name: OL");
        JUnitAppender.assertWarnMessage("no node address before 'B' in system name: OLB2");

        Assert.assertTrue("valid format - OL2005", NameValidity.VALID == SerialAddress.validSystemNameFormat("OL2005", 'L', "O"));
        Assert.assertTrue("valid format - OL2B5", NameValidity.VALID == SerialAddress.validSystemNameFormat("OL2B5", 'L', "O"));
        Assert.assertTrue("valid format - OT2005", NameValidity.VALID == SerialAddress.validSystemNameFormat("OT2005", 'T', "O"));
        Assert.assertTrue("valid format - OT2B5", NameValidity.VALID == SerialAddress.validSystemNameFormat("OT2B5", 'T', "O"));
        Assert.assertTrue("valid format - OS2005", NameValidity.VALID == SerialAddress.validSystemNameFormat("OS2005", 'S', "O"));
        Assert.assertTrue("valid format - OS2B5", NameValidity.VALID == SerialAddress.validSystemNameFormat("OS2B5", 'S', "O"));

        Assert.assertTrue("invalid format - OY2005", NameValidity.VALID != SerialAddress.validSystemNameFormat("OY2005", 'L', "O"));
        JUnitAppender.assertErrorMessage("invalid character in header field system name: OY2005");

        Assert.assertTrue("invalid format - OY2B5", NameValidity.VALID != SerialAddress.validSystemNameFormat("OY2B5", 'L', "O"));
        JUnitAppender.assertErrorMessage("invalid character in header field system name: OY2B5");

        Assert.assertTrue("valid format - OL22001", NameValidity.VALID == SerialAddress.validSystemNameFormat("OL22001", 'L', "O"));
        Assert.assertTrue("valid format - OL22B1", NameValidity.VALID == SerialAddress.validSystemNameFormat("OL22B1", 'L', "O"));

        Assert.assertTrue("invalid format - OL22000", NameValidity.VALID != SerialAddress.validSystemNameFormat("OL22000", 'L', "O"));
        JUnitAppender.assertWarnMessage("bit number not in range 1 - 999 in system name: OL22000");

        Assert.assertTrue("invalid format - OL22B0", NameValidity.VALID != SerialAddress.validSystemNameFormat("OL22B0", 'L', "O"));
        JUnitAppender.assertWarnMessage("bit number field out of range in system name: OL22B0");

        Assert.assertTrue("valid format - OL2999", NameValidity.VALID == SerialAddress.validSystemNameFormat("OL2999", 'L', "O"));
        Assert.assertTrue("valid format - OL2B2048", NameValidity.VALID == SerialAddress.validSystemNameFormat("OL2B2048", 'L', "O"));

        Assert.assertTrue("invalid format - OL2B2049", NameValidity.VALID != SerialAddress.validSystemNameFormat("OL2B2049", 'L', "O"));
        JUnitAppender.assertWarnMessage("bit number field out of range in system name: OL2B2049");

        Assert.assertTrue("valid format - OL127999", NameValidity.VALID == SerialAddress.validSystemNameFormat("OL127999", 'L', "O"));

        Assert.assertTrue("invalid format - OL128000", NameValidity.VALID != SerialAddress.validSystemNameFormat("OL128000", 'L', "O"));
        JUnitAppender.assertWarnMessage("bit number not in range 1 - 999 in system name: OL128000");

        Assert.assertTrue("valid format - OL127B7", NameValidity.VALID == SerialAddress.validSystemNameFormat("OL127B7", 'L', "O"));

        Assert.assertTrue("invalid format - OL128B7", NameValidity.VALID != SerialAddress.validSystemNameFormat("OL128B7", 'L', "O"));
        JUnitAppender.assertWarnMessage("node address field out of range in system name: OL128B7");

        Assert.assertTrue("invalid format - OL2oo5", NameValidity.VALID != SerialAddress.validSystemNameFormat("OL2oo5", 'L', "O"));
        JUnitAppender.assertWarnMessage("invalid character in number field system name: OL2oo5");

        Assert.assertTrue("invalid format - OL2aB5", NameValidity.VALID != SerialAddress.validSystemNameFormat("OL2aB5", 'L', "O"));
        JUnitAppender.assertWarnMessage("invalid character in node address field of system name: OL2aB5");

        Assert.assertTrue("invalid format - OL2B5x", NameValidity.VALID != SerialAddress.validSystemNameFormat("OL2B5x", 'L', "O"));
        JUnitAppender.assertWarnMessage("invalid character in bit number field of system name: OL2B5x");
    }

    public void testGetBitFromSystemName() {
        Assert.assertEquals("OL2", 2, SerialAddress.getBitFromSystemName("OL2", "O"));
        Assert.assertEquals("OL2002", 2, SerialAddress.getBitFromSystemName("OL2002", "O"));
        Assert.assertEquals("OL1", 1, SerialAddress.getBitFromSystemName("OL1", "O"));
        Assert.assertEquals("OL2001", 1, SerialAddress.getBitFromSystemName("OL2001", "O"));
        Assert.assertEquals("OL999", 999, SerialAddress.getBitFromSystemName("OL999", "O"));
        Assert.assertEquals("OL2999", 999, SerialAddress.getBitFromSystemName("OL2999", "O"));

        Assert.assertEquals("OL29O9", 0, SerialAddress.getBitFromSystemName("OL29O9", "O"));
        JUnitAppender.assertErrorMessage("illegal character in number field of system name: OL29O9");

        Assert.assertEquals("OL0B7", 7, SerialAddress.getBitFromSystemName("OL0B7", "O"));
        Assert.assertEquals("OL2B7", 7, SerialAddress.getBitFromSystemName("OL2B7", "O"));
        Assert.assertEquals("OL0B1", 1, SerialAddress.getBitFromSystemName("OL0B1", "O"));
        Assert.assertEquals("OL2B1", 1, SerialAddress.getBitFromSystemName("OL2B1", "O"));
        Assert.assertEquals("OL0B2048", 2048, SerialAddress.getBitFromSystemName("OL0B2048", "O"));
        Assert.assertEquals("OL11B2048", 2048, SerialAddress.getBitFromSystemName("OL11B2048", "O"));
    }

    public void testGetNodeFromSystemName() {
        SerialNode d = new SerialNode(14, SerialNode.IO48,memo);
        SerialNode c = new SerialNode(17, SerialNode.IO24,memo);
        SerialNode b = new SerialNode(127, SerialNode.IO24,memo);
        Assert.assertEquals("node of OL14007", d, SerialAddress.getNodeFromSystemName("OL14007", "O",memo.getTrafficController()));
        Assert.assertEquals("node of OL14B7", d, SerialAddress.getNodeFromSystemName("OL14B7", "O",memo.getTrafficController()));
        Assert.assertEquals("node of OL127007", b, SerialAddress.getNodeFromSystemName("OL127007", "O",memo.getTrafficController()));
        Assert.assertEquals("node of OL127B7", b, SerialAddress.getNodeFromSystemName("OL127B7", "O",memo.getTrafficController()));
        Assert.assertEquals("node of OL17007", c, SerialAddress.getNodeFromSystemName("OL17007", "O",memo.getTrafficController()));
        Assert.assertEquals("node of OL17B7", c, SerialAddress.getNodeFromSystemName("OL17B7", "O",memo.getTrafficController()));
        Assert.assertEquals("node of OL11007", null, SerialAddress.getNodeFromSystemName("OL11007", "O",memo.getTrafficController()));
        Assert.assertEquals("node of OL11B7", null, SerialAddress.getNodeFromSystemName("OL11B7", "O",memo.getTrafficController()));
    }

    public void testValidSystemNameConfig() {
        SerialNode d = new SerialNode(4, SerialNode.IO24,memo);
        SerialNode c = new SerialNode(10, SerialNode.IO48,memo);
        Assert.assertNotNull("exists", d);
        Assert.assertNotNull("exists", c);
        Assert.assertTrue("valid config OL4007", SerialAddress.validSystemNameConfig("OL4007", 'L', memo));
        Assert.assertTrue("valid config OL4B7", SerialAddress.validSystemNameConfig("OL4B7", 'L', memo));
        Assert.assertTrue("valid config OS10007", SerialAddress.validSystemNameConfig("OS10007", 'S', memo));
        Assert.assertTrue("valid config OS10B7", SerialAddress.validSystemNameConfig("OS10B7", 'S', memo));
        Assert.assertTrue("valid config OL10032", SerialAddress.validSystemNameConfig("OL10032", 'L', memo));
        Assert.assertTrue("valid config OL10B32", SerialAddress.validSystemNameConfig("OL10B32", 'L', memo));

        Assert.assertTrue("invalid config OL10033", !SerialAddress.validSystemNameConfig("OL10033", 'L', memo));
        JUnitAppender.assertWarnMessage("invalid system name OL10033; bad bit number");

        Assert.assertTrue("invalid config OL10B33", !SerialAddress.validSystemNameConfig("OL10B33", 'L', memo));
        JUnitAppender.assertWarnMessage("invalid system name OL10B33; bad bit number");

        Assert.assertTrue("valid config OS10016", SerialAddress.validSystemNameConfig("OS10016", 'S', memo));
        Assert.assertTrue("valid config OS10B16", SerialAddress.validSystemNameConfig("OS10B16", 'S', memo));

        Assert.assertTrue("invalid config OS10017", !SerialAddress.validSystemNameConfig("OS10017", 'S', memo));
        JUnitAppender.assertWarnMessage("invalid system name OS10017; bad bit number");

        Assert.assertTrue("invalid config OS10B17", !SerialAddress.validSystemNameConfig("OS10B17", 'S', memo));
        JUnitAppender.assertWarnMessage("invalid system name OS10B17; bad bit number");

        Assert.assertTrue("valid config OT4016", SerialAddress.validSystemNameConfig("OT4016", 'T', memo));
        Assert.assertTrue("valid config OT4B16", SerialAddress.validSystemNameConfig("OT4B16", 'T', memo));

        Assert.assertTrue("invalid config OT4017", !SerialAddress.validSystemNameConfig("OT4017", 'T', memo));
        JUnitAppender.assertWarnMessage("invalid system name OT4017; bad bit number");

        Assert.assertTrue("invalid config OT4017", !SerialAddress.validSystemNameConfig("OT4B17", 'T', memo));
        JUnitAppender.assertWarnMessage("invalid system name OT4B17; bad bit number");

        Assert.assertTrue("valid config OS4008", SerialAddress.validSystemNameConfig("OS4008", 'S', memo));
        Assert.assertTrue("valid config OS4B8", SerialAddress.validSystemNameConfig("OS4B8", 'S', memo));

        Assert.assertTrue("invalid config OS4009", !SerialAddress.validSystemNameConfig("OS4009", 'S', memo));
        JUnitAppender.assertWarnMessage("invalid system name OS4009; bad bit number");

        Assert.assertTrue("invalid config OS4B9", !SerialAddress.validSystemNameConfig("OS4B9", 'S', memo));
        JUnitAppender.assertWarnMessage("invalid system name OS4B9; bad bit number");

        Assert.assertTrue("invalid config OL11007", !SerialAddress.validSystemNameConfig("OL11007", 'L', memo));
        JUnitAppender.assertWarnMessage("invalid system name OL11007; no such node");

        Assert.assertTrue("invalid config OL11B7", !SerialAddress.validSystemNameConfig("OL11B7", 'L', memo));
        JUnitAppender.assertWarnMessage("invalid system name OL11B7; no such node");

    }

    public void testConvertSystemNameFormat() {
        Assert.assertEquals("convert OL14007", "OL14B7", SerialAddress.convertSystemNameToAlternate("OL14007", "O"));
        Assert.assertEquals("convert OS7", "OS0B7", SerialAddress.convertSystemNameToAlternate("OS7", "O"));
        Assert.assertEquals("convert OT4007", "OT4B7", SerialAddress.convertSystemNameToAlternate("OT4007", "O"));
        Assert.assertEquals("convert OL14B7", "OL14007", SerialAddress.convertSystemNameToAlternate("OL14B7", "O"));
        Assert.assertEquals("convert OL0B7", "OL7", SerialAddress.convertSystemNameToAlternate("OL0B7", "O"));
        Assert.assertEquals("convert OS4B7", "OS4007", SerialAddress.convertSystemNameToAlternate("OS4B7", "O"));
        Assert.assertEquals("convert OL14B8", "OL14008", SerialAddress.convertSystemNameToAlternate("OL14B8", "O"));

        Assert.assertEquals("convert OL128B7", "", SerialAddress.convertSystemNameToAlternate("OL128B7", "O"));
        JUnitAppender.assertWarnMessage("node address field out of range in system name: OL128B7");
    }

    public void testNormalizeSystemName() {
        Assert.assertEquals("normalize OL14007", "OL14007", SerialAddress.normalizeSystemName("OL14007", "O"));
        Assert.assertEquals("normalize OL007", "OL7", SerialAddress.normalizeSystemName("OL007", "O"));
        Assert.assertEquals("normalize OL004007", "OL4007", SerialAddress.normalizeSystemName("OL004007", "O"));
        Assert.assertEquals("normalize OL14B7", "OL14B7", SerialAddress.normalizeSystemName("OL14B7", "O"));
        Assert.assertEquals("normalize OL0B7", "OL0B7", SerialAddress.normalizeSystemName("OL0B7", "O"));
        Assert.assertEquals("normalize OL004B7", "OL4B7", SerialAddress.normalizeSystemName("OL004B7", "O"));
        Assert.assertEquals("normalize OL014B0008", "OL14B8", SerialAddress.normalizeSystemName("OL014B0008", "O"));

        Assert.assertEquals("normalize OL128B7", "", SerialAddress.normalizeSystemName("OL128B7", "O"));
        JUnitAppender.assertWarnMessage("node address field out of range in system name: OL128B7");
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

    private OakTreeSystemConnectionMemo memo = null;

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        memo = new OakTreeSystemConnectionMemo();
        memo.setTrafficController(new SerialTrafficControlScaffold());
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
