// SerialAddressTest.java

package jmri.jmrix.powerline;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.util.JUnitAppender;

/**
 * JUnit tests for the SerialAddress utility class.
 * @author	Dave Duchamp Copyright 2004
 * @author  Bob Jacobsen Copyright 2007, 2008
 * @version	$Revision: 1.1 $
 */
public class SerialAddressTest extends TestCase {

	public void testValidateSystemNameFormat() {
            Assert.assertTrue("valid format - PL2", SerialAddress.validSystemNameFormat("PL2",'L') );
            Assert.assertTrue("valid format - PL0B2", SerialAddress.validSystemNameFormat("PL0B2",'L') );
            Assert.assertTrue("invalid format - PL", !SerialAddress.validSystemNameFormat("PL",'L') );

            Assert.assertTrue("invalid format - PLB2", !SerialAddress.validSystemNameFormat("PLB2",'L') );
            JUnitAppender.assertErrorMessage("illegal character in number field system name: PL");
            JUnitAppender.assertErrorMessage("no node address before 'B' in system name: PLB2");

            Assert.assertTrue("valid format - PL2005", SerialAddress.validSystemNameFormat("PL2005",'L') );
            Assert.assertTrue("valid format - PL2B5", SerialAddress.validSystemNameFormat("PL2B5",'L') );
            Assert.assertTrue("valid format - PT2005", SerialAddress.validSystemNameFormat("PT2005",'T') );
            Assert.assertTrue("valid format - PT2B5", SerialAddress.validSystemNameFormat("PT2B5",'T') );
            Assert.assertTrue("valid format - PS2005", SerialAddress.validSystemNameFormat("PS2005",'S') );
            Assert.assertTrue("valid format - PS2B5", SerialAddress.validSystemNameFormat("PS2B5",'S') );

            Assert.assertTrue("invalid format - PY2005", !SerialAddress.validSystemNameFormat("PY2005",'L') );
            JUnitAppender.assertErrorMessage("illegal character in header field system name: PY2005");

            Assert.assertTrue("invalid format - PY2B5", !SerialAddress.validSystemNameFormat("PY2B5",'L') );
            JUnitAppender.assertErrorMessage("illegal character in header field system name: PY2B5");

            Assert.assertTrue("valid format - PL22001", SerialAddress.validSystemNameFormat("PL22001",'L') );
            Assert.assertTrue("valid format - PL22B1", SerialAddress.validSystemNameFormat("PL22B1",'L') );

            Assert.assertTrue("invalid format - PL22000", !SerialAddress.validSystemNameFormat("PL22000",'L') );
            JUnitAppender.assertErrorMessage("bit number not in range 1 - 999 in system name: PL22000");

            Assert.assertTrue("invalid format - PL22B0", !SerialAddress.validSystemNameFormat("PL22B0",'L') );
            JUnitAppender.assertErrorMessage("bit number field out of range in system name: PL22B0");

            Assert.assertTrue("valid format - PL2999", SerialAddress.validSystemNameFormat("PL2999",'L') );
            Assert.assertTrue("invalid format - PL2B2048", !SerialAddress.validSystemNameFormat("PL2B2048",'L') );
            JUnitAppender.assertErrorMessage("bit number field out of range in system name: PL2B2048");

            Assert.assertTrue("invalid format - PL2B2049", !SerialAddress.validSystemNameFormat("PL2B2049",'L') );
            JUnitAppender.assertErrorMessage("bit number field out of range in system name: PL2B2049");

            Assert.assertTrue("invalid format - PL2B33", !SerialAddress.validSystemNameFormat("PL2B33",'L') );
            JUnitAppender.assertErrorMessage("bit number field out of range in system name: PL2B33");

            Assert.assertTrue("valid format - PL127032", SerialAddress.validSystemNameFormat("PL127032",'L') );

            Assert.assertTrue("valid format - PL127001", SerialAddress.validSystemNameFormat("PL127001",'L') );

            Assert.assertTrue("invalid format - PL127000", !SerialAddress.validSystemNameFormat("PL127000",'L') );
            JUnitAppender.assertErrorMessage("bit number not in range 1 - 999 in system name: PL127000");

            Assert.assertTrue("valid format - PL127B7", SerialAddress.validSystemNameFormat("PL127B7",'L') );

            Assert.assertTrue("invalid format -PL128B7", !SerialAddress.validSystemNameFormat("PL128B7",'L') );
            JUnitAppender.assertErrorMessage("node address field out of range in system name: PL128B7");

            Assert.assertTrue("invalid format - PL2oo5", !SerialAddress.validSystemNameFormat("PL2oo5",'L') );
            JUnitAppender.assertErrorMessage("illegal character in number field system name: PL2oo5");

            Assert.assertTrue("invalid format - PL2aB5", !SerialAddress.validSystemNameFormat("PL2aB5",'L') );
            JUnitAppender.assertErrorMessage("illegal character in node address field of system name: PL2aB5");

            Assert.assertTrue("invalid format - PL2B5x", !SerialAddress.validSystemNameFormat("PL2B5x",'L') );
            JUnitAppender.assertErrorMessage("illegal character in bit number field of system name: PL2B5x");
	}

	public void testGetBitFromSystemName() {
            Assert.assertEquals("PL2", 2, SerialAddress.getBitFromSystemName("PL2") );
            Assert.assertEquals("PL2002", 2, SerialAddress.getBitFromSystemName("PL2002") );
            Assert.assertEquals("PL1", 1, SerialAddress.getBitFromSystemName("PL1") );
            Assert.assertEquals("PL2001", 1, SerialAddress.getBitFromSystemName("PL2001") );
            Assert.assertEquals("PL999", 999, SerialAddress.getBitFromSystemName("PL999") );
            Assert.assertEquals("PL2999", 999, SerialAddress.getBitFromSystemName("PL2999") );

            Assert.assertEquals("PL29O9", 0, SerialAddress.getBitFromSystemName("PL29O9") );
            JUnitAppender.assertErrorMessage("illegal character in number field of system name: PL29O9");

            Assert.assertEquals("PL0B7", 7, SerialAddress.getBitFromSystemName("PL0B7") );
            Assert.assertEquals("PL2B7", 7, SerialAddress.getBitFromSystemName("PL2B7") );
            Assert.assertEquals("PL0B1", 1, SerialAddress.getBitFromSystemName("PL0B1") );
            Assert.assertEquals("PL2B1", 1, SerialAddress.getBitFromSystemName("PL2B1") );
            Assert.assertEquals("PL0B2048", 2048, SerialAddress.getBitFromSystemName("PL0B2048") );
            Assert.assertEquals("PL11B2048", 2048, SerialAddress.getBitFromSystemName("PL11B2048") );
        }
        
	public void testGetNodeFromSystemName() {
            SerialNode d = new SerialNode(14,SerialNode.DAUGHTER);
            SerialNode c = new SerialNode(17,SerialNode.DAUGHTER);
            SerialNode b = new SerialNode(127,SerialNode.DAUGHTER);
            Assert.assertEquals("node of PL14007", d,  SerialAddress.getNodeFromSystemName("PL14007") );
            Assert.assertEquals("node of PL14B7", d,   SerialAddress.getNodeFromSystemName("PL14B7") );
            Assert.assertEquals("node of PL127007", b, SerialAddress.getNodeFromSystemName("PL127007") );
            Assert.assertEquals("node of PL127B7", b,  SerialAddress.getNodeFromSystemName("PL127B7") );
            Assert.assertEquals("node of PL17007", c,  SerialAddress.getNodeFromSystemName("PL17007") );
            Assert.assertEquals("node of PL17B7", c,   SerialAddress.getNodeFromSystemName("PL17B7") );
            Assert.assertEquals("node of PL11007", null, SerialAddress.getNodeFromSystemName("PL11007") );
            Assert.assertEquals("node of PL11B7", null,  SerialAddress.getNodeFromSystemName("PL11B7") );
        }

	public void testValidSystemNameConfig() {
            SerialNode d = new SerialNode(4,SerialNode.DAUGHTER);
            SerialNode c = new SerialNode(10,SerialNode.DAUGHTER);
            Assert.assertTrue("valid config PL4007",  SerialAddress.validSystemNameConfig("PL4007",'L') );
            Assert.assertTrue("valid config PL4B7",   SerialAddress.validSystemNameConfig("PL4B7",'L') );
            Assert.assertTrue("valid config PS10007", SerialAddress.validSystemNameConfig("PS10007",'S') );
            Assert.assertTrue("valid config PS10B7",  SerialAddress.validSystemNameConfig("PS10B7",'S') );
            Assert.assertTrue("valid config PL10032", SerialAddress.validSystemNameConfig("PL10032",'L') );
            Assert.assertTrue("valid config PL10B32", SerialAddress.validSystemNameConfig("PL10B32",'L') );

            Assert.assertTrue("invalid config PL10033", !SerialAddress.validSystemNameConfig("PL10033",'L') );
            JUnitAppender.assertWarnMessage("PL10033 invalid; bad bit number");

            Assert.assertTrue("invalid config PL10B33", !SerialAddress.validSystemNameConfig("PL10B33",'L') );
            JUnitAppender.assertErrorMessage("bit number field out of range in system name: PL10B33");
            JUnitAppender.assertWarnMessage("PL10B33 invalid; bad format");

            Assert.assertTrue("valid config PS10016", SerialAddress.validSystemNameConfig("PS10016",'S') );
            Assert.assertTrue("valid config PS10B16", SerialAddress.validSystemNameConfig("PS10B16",'S') );

            Assert.assertTrue("invalid config PS10017", !SerialAddress.validSystemNameConfig("PS10017",'S') );
            JUnitAppender.assertWarnMessage("PS10017 invalid; bad bit number");

            Assert.assertTrue("invalid config PS10B17", !SerialAddress.validSystemNameConfig("PS10B17",'S') );
            JUnitAppender.assertWarnMessage("PS10B17 invalid; bad bit number");

            Assert.assertTrue("valid config PT4016", SerialAddress.validSystemNameConfig("PT4016",'T') );
            Assert.assertTrue("valid config PT4B16", SerialAddress.validSystemNameConfig("PT4B16",'T') );

            Assert.assertTrue("invalid config PT4117", !SerialAddress.validSystemNameConfig("PT4117",'T') );
            JUnitAppender.assertWarnMessage("PT4117 invalid; bad bit number");

            Assert.assertTrue("invalid config PT4B117", !SerialAddress.validSystemNameConfig("PT4B117",'T') );
            JUnitAppender.assertErrorMessage("bit number field out of range in system name: PT4B117");
            JUnitAppender.assertWarnMessage("PT4B117 invalid; bad format");

            Assert.assertTrue("valid config PS4008", SerialAddress.validSystemNameConfig("PS4008",'S') );
            Assert.assertTrue("valid config PS4B8", SerialAddress.validSystemNameConfig("PS4B8",'S') );

            Assert.assertTrue("invalid config PS4017", !SerialAddress.validSystemNameConfig("PS4017",'S') );
            JUnitAppender.assertWarnMessage("PS4017 invalid; bad bit number");

            Assert.assertTrue("invalid config PS4B19", !SerialAddress.validSystemNameConfig("PS4B19",'S') );
            JUnitAppender.assertWarnMessage("PS4B19 invalid; bad bit number");

            Assert.assertTrue("invalid config PL11007", !SerialAddress.validSystemNameConfig("PL11007",'L') );
            JUnitAppender.assertWarnMessage("PL11007 invalid; no such node");

            Assert.assertTrue("invalid config PL11B7", !SerialAddress.validSystemNameConfig("PL11B7",'L') );
            JUnitAppender.assertWarnMessage("PL11B7 invalid; no such node");

        }        
        
	public void testConvertSystemNameFormat() {
            Assert.assertEquals("convert PL14007",  "PL14B7", SerialAddress.convertSystemNameToAlternate("PL14007") );
            Assert.assertEquals("convert PS7",      "PS0B7", SerialAddress.convertSystemNameToAlternate("PS7") );
            Assert.assertEquals("convert PT4007",   "PT4B7", SerialAddress.convertSystemNameToAlternate("PT4007") );
            Assert.assertEquals("convert PL14B7",   "PL14007", SerialAddress.convertSystemNameToAlternate("PL14B7") );
            Assert.assertEquals("convert PL0B7",    "PL7", SerialAddress.convertSystemNameToAlternate("PL0B7") );
            Assert.assertEquals("convert PS4B7",    "PS4007", SerialAddress.convertSystemNameToAlternate("PS4B7") );
            Assert.assertEquals("convert PL14B8",   "PL14008", SerialAddress.convertSystemNameToAlternate("PL14B8") );

            Assert.assertEquals("convert PL128B7", "", SerialAddress.convertSystemNameToAlternate("PL128B7") );
            JUnitAppender.assertErrorMessage("node address field out of range in system name: PL128B7");
        }
        
	public void testNormalizeSystemName() {
            Assert.assertEquals("normalize PL14007",    "PL14007", SerialAddress.normalizeSystemName("PL14007") );
            Assert.assertEquals("normalize PL007",      "PL7", SerialAddress.normalizeSystemName("PL007") );
            Assert.assertEquals("normalize PL004007",   "PL4007", SerialAddress.normalizeSystemName("PL004007") );
            Assert.assertEquals("normalize PL14B7",     "PL14B7", SerialAddress.normalizeSystemName("PL14B7") );
            Assert.assertEquals("normalize PL0B7",      "PL0B7", SerialAddress.normalizeSystemName("PL0B7") );
            Assert.assertEquals("normalize PL004B7",    "PL4B7", SerialAddress.normalizeSystemName("PL004B7") );
            Assert.assertEquals("normalize PL014B0008", "PL14B8", SerialAddress.normalizeSystemName("PL014B0008") );

            Assert.assertEquals("normalize PL128B7", "", SerialAddress.normalizeSystemName("PL128B7") );
            JUnitAppender.assertErrorMessage("node address field out of range in system name: PL128B7");
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
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
