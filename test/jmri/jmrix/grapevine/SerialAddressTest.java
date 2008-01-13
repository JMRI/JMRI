// SerialAddressTest.java

package jmri.jmrix.grapevine;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.util.JUnitAppender;

/**
 * JUnit tests for the SerialAddress utility class.
 * @author	Dave Duchamp Copyright 2004
 * @author  Bob Jacobsen Copyright 2007, 2008
 * @version	$Revision: 1.3 $
 */
public class SerialAddressTest extends TestCase {

	public void testValidateSystemNameFormat() {
            Assert.assertTrue("valid format - GL2", SerialAddress.validSystemNameFormat("GL2",'L') );
            Assert.assertTrue("valid format - GL0B2", SerialAddress.validSystemNameFormat("GL0B2",'L') );
            Assert.assertTrue("invalid format - GL", !SerialAddress.validSystemNameFormat("GL",'L') );

            Assert.assertTrue("invalid format - GLB2", !SerialAddress.validSystemNameFormat("GLB2",'L') );
            JUnitAppender.assertErrorMessage("illegal character in number field system name: GL");
            JUnitAppender.assertErrorMessage("no node address before 'B' in system name: GLB2");

            Assert.assertTrue("valid format - GL2005", SerialAddress.validSystemNameFormat("GL2005",'L') );
            Assert.assertTrue("valid format - GL2B5", SerialAddress.validSystemNameFormat("GL2B5",'L') );
            Assert.assertTrue("valid format - GT2005", SerialAddress.validSystemNameFormat("GT2005",'T') );
            Assert.assertTrue("valid format - GT2B5", SerialAddress.validSystemNameFormat("GT2B5",'T') );
            Assert.assertTrue("valid format - GS2005", SerialAddress.validSystemNameFormat("GS2005",'S') );
            Assert.assertTrue("valid format - GS2B5", SerialAddress.validSystemNameFormat("GS2B5",'S') );

            Assert.assertTrue("invalid format - GY2005", !SerialAddress.validSystemNameFormat("GY2005",'L') );
            JUnitAppender.assertErrorMessage("illegal character in header field system name: GY2005");

            Assert.assertTrue("invalid format - GY2B5", !SerialAddress.validSystemNameFormat("GY2B5",'L') );
            JUnitAppender.assertErrorMessage("illegal character in header field system name: GY2B5");

            Assert.assertTrue("valid format - GL22001", SerialAddress.validSystemNameFormat("GL22001",'L') );
            Assert.assertTrue("valid format - GL22B1", SerialAddress.validSystemNameFormat("GL22B1",'L') );

            Assert.assertTrue("invalid format - GL22000", !SerialAddress.validSystemNameFormat("GL22000",'L') );
            JUnitAppender.assertErrorMessage("bit number not in range 1 - 999 in system name: GL22000");

            Assert.assertTrue("invalid format - GL22B0", !SerialAddress.validSystemNameFormat("GL22B0",'L') );
            JUnitAppender.assertErrorMessage("bit number field out of range in system name: GL22B0");

            Assert.assertTrue("valid format - GL2999", SerialAddress.validSystemNameFormat("GL2999",'L') );
            Assert.assertTrue("valid format - GL2B2048", SerialAddress.validSystemNameFormat("GL2B2048",'L') );

            Assert.assertTrue("invalid format - GL2B2049", !SerialAddress.validSystemNameFormat("GL2B2049",'L') );
            JUnitAppender.assertErrorMessage("bit number field out of range in system name: GL2B2049");

            Assert.assertTrue("valid format - GL127999", SerialAddress.validSystemNameFormat("GL127999",'L') );

            Assert.assertTrue("invalid format - GL128000", !SerialAddress.validSystemNameFormat("GL128000",'L') );
            JUnitAppender.assertErrorMessage("bit number not in range 1 - 999 in system name: GL128000");

            Assert.assertTrue("valid format - GL127B7", SerialAddress.validSystemNameFormat("GL127B7",'L') );

            Assert.assertTrue("invalid format - GL128B7", !SerialAddress.validSystemNameFormat("GL128B7",'L') );
            JUnitAppender.assertErrorMessage("node address field out of range in system name: GL128B7");

            Assert.assertTrue("invalid format - GL2oo5", !SerialAddress.validSystemNameFormat("GL2oo5",'L') );
            JUnitAppender.assertErrorMessage("illegal character in number field system name: GL2oo5");

            Assert.assertTrue("invalid format - GL2aB5", !SerialAddress.validSystemNameFormat("GL2aB5",'L') );
            JUnitAppender.assertErrorMessage("illegal character in node address field of system name: GL2aB5");

            Assert.assertTrue("invalid format - GL2B5x", !SerialAddress.validSystemNameFormat("GL2B5x",'L') );
            JUnitAppender.assertErrorMessage("illegal character in bit number field of system name: GL2B5x");
	}

	public void testGetBitFromSystemName() {
            Assert.assertEquals("GL2", 2, SerialAddress.getBitFromSystemName("GL2") );
            Assert.assertEquals("GL2002", 2, SerialAddress.getBitFromSystemName("GL2002") );
            Assert.assertEquals("GL1", 1, SerialAddress.getBitFromSystemName("GL1") );
            Assert.assertEquals("GL2001", 1, SerialAddress.getBitFromSystemName("GL2001") );
            Assert.assertEquals("GL999", 999, SerialAddress.getBitFromSystemName("GL999") );
            Assert.assertEquals("GL2999", 999, SerialAddress.getBitFromSystemName("GL2999") );

            Assert.assertEquals("OL29O9", 0, SerialAddress.getBitFromSystemName("GL29O9") );
            JUnitAppender.assertErrorMessage("illegal character in number field of system name: GL29O9");

            Assert.assertEquals("GL0B7", 7, SerialAddress.getBitFromSystemName("GL0B7") );
            Assert.assertEquals("GL2B7", 7, SerialAddress.getBitFromSystemName("GL2B7") );
            Assert.assertEquals("GL0B1", 1, SerialAddress.getBitFromSystemName("GL0B1") );
            Assert.assertEquals("GL2B1", 1, SerialAddress.getBitFromSystemName("GL2B1") );
            Assert.assertEquals("GL0B2048", 2048, SerialAddress.getBitFromSystemName("GL0B2048") );
            Assert.assertEquals("GL11B2048", 2048, SerialAddress.getBitFromSystemName("GL11B2048") );
        }
        
	public void testGetNodeFromSystemName() {
            SerialNode d = new SerialNode(14,SerialNode.NODE2002V6);
            SerialNode c = new SerialNode(17,SerialNode.NODE2002V1);
            SerialNode b = new SerialNode(127,SerialNode.NODE2002V1);
            Assert.assertEquals("node of GL14007", d,  SerialAddress.getNodeFromSystemName("GL14007") );
            Assert.assertEquals("node of GL14B7", d,   SerialAddress.getNodeFromSystemName("GL14B7") );
            Assert.assertEquals("node of GL127007", b, SerialAddress.getNodeFromSystemName("GL127007") );
            Assert.assertEquals("node of GL127B7", b,  SerialAddress.getNodeFromSystemName("GL127B7") );
            Assert.assertEquals("node of GL17007", c,  SerialAddress.getNodeFromSystemName("GL17007") );
            Assert.assertEquals("node of GL17B7", c,   SerialAddress.getNodeFromSystemName("GL17B7") );
            Assert.assertEquals("node of GL11007", null, SerialAddress.getNodeFromSystemName("GL11007") );
            Assert.assertEquals("node of GL11B7", null,  SerialAddress.getNodeFromSystemName("GL11B7") );
        }

	public void testValidSystemNameConfig() {
            SerialNode d = new SerialNode(4,SerialNode.NODE2002V6);
            SerialNode c = new SerialNode(10,SerialNode.NODE2002V1);
            Assert.assertTrue("valid config GL4007",  SerialAddress.validSystemNameConfig("GL4007",'L') );
            Assert.assertTrue("valid config GL4B7",   SerialAddress.validSystemNameConfig("GL4B7",'L') );
            Assert.assertTrue("valid config GS10007", SerialAddress.validSystemNameConfig("GS10007",'S') );
            Assert.assertTrue("valid config GS10B7",  SerialAddress.validSystemNameConfig("GS10B7",'S') );
            Assert.assertTrue("valid config GL10011", SerialAddress.validSystemNameConfig("GL10011",'L') );
            Assert.assertTrue("valid config GL10B06", SerialAddress.validSystemNameConfig("GL10B06",'L') );

            Assert.assertTrue("invalid config GL10133", !SerialAddress.validSystemNameConfig("GL10133",'L') );
            JUnitAppender.assertWarnMessage("GL10133 invalid; bad bit number 133 > 96");

            Assert.assertTrue("invalid config GL10B133", !SerialAddress.validSystemNameConfig("GL10B133",'L') );
            JUnitAppender.assertWarnMessage("GL10B133 invalid; bad bit number 133 > 96");

            Assert.assertTrue("valid config GS10006", SerialAddress.validSystemNameConfig("GS10006",'S') );
            Assert.assertTrue("valid config GS10B06", SerialAddress.validSystemNameConfig("GS10B06",'S') );

            Assert.assertTrue("invalid config GS10517", !SerialAddress.validSystemNameConfig("GS10517",'S') );
            JUnitAppender.assertWarnMessage("GS10517 invalid; bad bit number 517 > 96");

            Assert.assertTrue("invalid config GS10B547", !SerialAddress.validSystemNameConfig("GS10B547",'S') );
            JUnitAppender.assertWarnMessage("GS10B547 invalid; bad bit number 547 > 96");

            Assert.assertTrue("valid config GT4006", SerialAddress.validSystemNameConfig("GT4006",'T') );
            Assert.assertTrue("valid config GT4B6", SerialAddress.validSystemNameConfig("GT4B6",'T') );

            Assert.assertTrue("invalid config GT4317", !SerialAddress.validSystemNameConfig("GT4317",'T') );
            JUnitAppender.assertWarnMessage("GT4317 invalid; bad bit number 317 > 96");

            Assert.assertTrue("invalid config GT4317", !SerialAddress.validSystemNameConfig("GT4B317",'T') );
            JUnitAppender.assertWarnMessage("GT4B317 invalid; bad bit number 317 > 96");

            Assert.assertTrue("valid config GS4008", SerialAddress.validSystemNameConfig("GS4008",'S') );
            Assert.assertTrue("valid config GS4B8", SerialAddress.validSystemNameConfig("GS4B8",'S') );

            Assert.assertTrue("invalid config GS4309", !SerialAddress.validSystemNameConfig("GS4309",'S') );
            JUnitAppender.assertWarnMessage("GS4309 invalid; bad bit number 309 > 96");

            Assert.assertTrue("invalid config GS4B309", !SerialAddress.validSystemNameConfig("GS4B309",'S') );
            JUnitAppender.assertWarnMessage("GS4B309 invalid; bad bit number 309 > 96");

            Assert.assertTrue("invalid config GL11007", !SerialAddress.validSystemNameConfig("GL11007",'L') );
            JUnitAppender.assertWarnMessage("GL11007 invalid; no such node");

            Assert.assertTrue("invalid config GL11B7", !SerialAddress.validSystemNameConfig("GL11B7",'L') );
            JUnitAppender.assertWarnMessage("GL11B7 invalid; no such node");

        }        
        
	public void testConvertSystemNameFormat() {
            Assert.assertEquals("convert GL14007",  "GL14B7", SerialAddress.convertSystemNameToAlternate("GL14007") );
            Assert.assertEquals("convert GS7",      "GS0B7", SerialAddress.convertSystemNameToAlternate("GS7") );
            Assert.assertEquals("convert GT4007",   "GT4B7", SerialAddress.convertSystemNameToAlternate("GT4007") );
            Assert.assertEquals("convert GL14B7",   "GL14007", SerialAddress.convertSystemNameToAlternate("GL14B7") );
            Assert.assertEquals("convert GL0B7",    "GL7", SerialAddress.convertSystemNameToAlternate("GL0B7") );
            Assert.assertEquals("convert GS4B7",    "GS4007", SerialAddress.convertSystemNameToAlternate("GS4B7") );
            Assert.assertEquals("convert GL14B8",   "GL14008", SerialAddress.convertSystemNameToAlternate("GL14B8") );

            Assert.assertEquals("convert GL128B7", "", SerialAddress.convertSystemNameToAlternate("GL128B7") );
            JUnitAppender.assertErrorMessage("node address field out of range in system name: GL128B7");
        }
        
	public void testNormalizeSystemName() {
            Assert.assertEquals("normalize GL14007",    "GL14007", SerialAddress.normalizeSystemName("GL14007") );
            Assert.assertEquals("normalize GL007",      "GL7", SerialAddress.normalizeSystemName("GL007") );
            Assert.assertEquals("normalize GL004007",   "GL4007", SerialAddress.normalizeSystemName("GL004007") );
            Assert.assertEquals("normalize GL14B7",     "GL14B7", SerialAddress.normalizeSystemName("GL14B7") );
            Assert.assertEquals("normalize GL0B7",      "GL0B7", SerialAddress.normalizeSystemName("GL0B7") );
            Assert.assertEquals("normalize GL004B7",    "GL4B7", SerialAddress.normalizeSystemName("GL004B7") );
            Assert.assertEquals("normalize GL014B0008", "GL14B8", SerialAddress.normalizeSystemName("GL014B0008") );

            Assert.assertEquals("normalize GL128B7", "", SerialAddress.normalizeSystemName("GL128B7") );
            JUnitAppender.assertErrorMessage("node address field out of range in system name: GL128B7");
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
