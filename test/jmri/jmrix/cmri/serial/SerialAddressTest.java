// SerialAddressTest.java

package jmri.jmrix.cmri.serial;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the SerialAddress utility class.
 * @author	Dave Duchamp Copyright 2004
 * @version	$Revision: 1.1 $
 */
public class SerialAddressTest extends TestCase {

	public void testValidateSystemNameFormat() {
            Assert.assertTrue("valid format - CL2", SerialAddress.validSystemNameFormat("CL2",'L') );
            Assert.assertTrue("valid format - CL0B2", SerialAddress.validSystemNameFormat("CL0B2",'L') );
            Assert.assertTrue("invalid format - CL", !SerialAddress.validSystemNameFormat("CL",'L') );
            Assert.assertTrue("invalid format - CLB2", !SerialAddress.validSystemNameFormat("CLB2",'L') );
            Assert.assertTrue("valid format - CL2005", SerialAddress.validSystemNameFormat("CL2005",'L') );
            Assert.assertTrue("valid format - CL2B5", SerialAddress.validSystemNameFormat("CL2B5",'L') );
            Assert.assertTrue("valid format - CT2005", SerialAddress.validSystemNameFormat("CT2005",'T') );
            Assert.assertTrue("valid format - CT2B5", SerialAddress.validSystemNameFormat("CT2B5",'T') );
            Assert.assertTrue("valid format - CS2005", SerialAddress.validSystemNameFormat("CS2005",'S') );
            Assert.assertTrue("valid format - CS2B5", SerialAddress.validSystemNameFormat("CS2B5",'S') );
            Assert.assertTrue("invalid format - CY2005", !SerialAddress.validSystemNameFormat("CY2005",'L') );
            Assert.assertTrue("invalid format - CY2B5", !SerialAddress.validSystemNameFormat("CY2B5",'L') );
            Assert.assertTrue("valid format - CL22001", SerialAddress.validSystemNameFormat("CL22001",'L') );
            Assert.assertTrue("valid format - CL22B1", SerialAddress.validSystemNameFormat("CL22B1",'L') );
            Assert.assertTrue("invalid format - CL22000", !SerialAddress.validSystemNameFormat("CL22000",'L') );
            Assert.assertTrue("invalid format - CL22B0", !SerialAddress.validSystemNameFormat("CL22B0",'L') );
            Assert.assertTrue("valid format - CL2999", SerialAddress.validSystemNameFormat("CL2999",'L') );
            Assert.assertTrue("valid format - CL2B2048", SerialAddress.validSystemNameFormat("CL2B2048",'L') );
            Assert.assertTrue("invalid format - CL2B2049", !SerialAddress.validSystemNameFormat("CL2B2049",'L') );
            Assert.assertTrue("valid format - CL127999", SerialAddress.validSystemNameFormat("CL127999",'L') );
            Assert.assertTrue("invalid format - CL128000", !SerialAddress.validSystemNameFormat("CL128000",'L') );
            Assert.assertTrue("valid format - CL127B7", SerialAddress.validSystemNameFormat("CL127B7",'L') );
            Assert.assertTrue("invalid format - CL128B7", !SerialAddress.validSystemNameFormat("CL128B7",'L') );
            Assert.assertTrue("invalid format - CL2oo5", !SerialAddress.validSystemNameFormat("CL2oo5",'L') );
            Assert.assertTrue("invalid format - CL2aB5", !SerialAddress.validSystemNameFormat("CL2aB5",'L') );
            Assert.assertTrue("invalid format - CL2B5x", !SerialAddress.validSystemNameFormat("CL2B5x",'L') );
	}

	public void testGetBitFromSystemName() {
            Assert.assertEquals("CL2", 2, SerialAddress.getBitFromSystemName("CL2") );
            Assert.assertEquals("CL2002", 2, SerialAddress.getBitFromSystemName("CL2002") );
            Assert.assertEquals("CL1", 1, SerialAddress.getBitFromSystemName("CL1") );
            Assert.assertEquals("CL2001", 1, SerialAddress.getBitFromSystemName("CL2001") );
            Assert.assertEquals("CL999", 999, SerialAddress.getBitFromSystemName("CL999") );
            Assert.assertEquals("CL2999", 999, SerialAddress.getBitFromSystemName("CL2999") );
            Assert.assertEquals("CL29O9", 0, SerialAddress.getBitFromSystemName("CL29O9") );
            Assert.assertEquals("CL0B7", 7, SerialAddress.getBitFromSystemName("CL0B7") );
            Assert.assertEquals("CL2B7", 7, SerialAddress.getBitFromSystemName("CL2B7") );
            Assert.assertEquals("CL0B1", 1, SerialAddress.getBitFromSystemName("CL0B1") );
            Assert.assertEquals("CL2B1", 1, SerialAddress.getBitFromSystemName("CL2B1") );
            Assert.assertEquals("CL0B2048", 2048, SerialAddress.getBitFromSystemName("CL0B2048") );
            Assert.assertEquals("CL11B2048", 2048, SerialAddress.getBitFromSystemName("CL11B2048") );
        }

        SerialNode d = new SerialNode(4,SerialNode.USIC_SUSIC);
        SerialNode c = new SerialNode(10,SerialNode.SMINI);
        SerialNode b = new SerialNode(127,SerialNode.SMINI);
        
	public void testGetNodeFromSystemName() {
            SerialNode d = new SerialNode(14,SerialNode.USIC_SUSIC);
            SerialNode c = new SerialNode(17,SerialNode.SMINI);
            SerialNode b = new SerialNode(127,SerialNode.SMINI);
            Assert.assertEquals("node of CL14007", d, SerialAddress.getNodeFromSystemName("CL14007") );
            Assert.assertEquals("node of CL14B7", d, SerialAddress.getNodeFromSystemName("CL14B7") );
            Assert.assertEquals("node of CL127007", b, SerialAddress.getNodeFromSystemName("CL127007") );
            Assert.assertEquals("node of CL127B7", b, SerialAddress.getNodeFromSystemName("CL127B7") );
            Assert.assertEquals("node of CL17007", c, SerialAddress.getNodeFromSystemName("CL17007") );
            Assert.assertEquals("node of CL17B7", c, SerialAddress.getNodeFromSystemName("CL17B7") );
            Assert.assertEquals("node of CL11007", null, SerialAddress.getNodeFromSystemName("CL11007") );
            Assert.assertEquals("node of CL11B7", null, SerialAddress.getNodeFromSystemName("CL11B7") );
        }

	public void testValidSystemNameConfig() {
            SerialNode d = new SerialNode(4,SerialNode.USIC_SUSIC);
            d.setNumBitsPerCard (32);
            d.setCardTypeByAddress (0,SerialNode.INPUT_CARD);
            d.setCardTypeByAddress (1,SerialNode.OUTPUT_CARD);
            d.setCardTypeByAddress (2,SerialNode.OUTPUT_CARD);
            d.setCardTypeByAddress (3,SerialNode.OUTPUT_CARD);
            d.setCardTypeByAddress (4,SerialNode.INPUT_CARD);
            d.setCardTypeByAddress (5,SerialNode.OUTPUT_CARD);
            SerialNode c = new SerialNode(10,SerialNode.SMINI);
            Assert.assertTrue("valid config CL4007", SerialAddress.validSystemNameConfig("CL4007",'L') );
            Assert.assertTrue("valid config CL4B7", SerialAddress.validSystemNameConfig("CL4B7",'L') );
            Assert.assertTrue("valid config CS10007", SerialAddress.validSystemNameConfig("CS10007",'S') );
            Assert.assertTrue("valid config CS10B7", SerialAddress.validSystemNameConfig("CS10B7",'S') );
            Assert.assertTrue("valid config CL10048", SerialAddress.validSystemNameConfig("CL10048",'L') );
            Assert.assertTrue("valid config CL10B48", SerialAddress.validSystemNameConfig("CL10B48",'L') );
            Assert.assertTrue("invalid config CL10049", !SerialAddress.validSystemNameConfig("CL10049",'L') );
            Assert.assertTrue("invalid config CL10B49", !SerialAddress.validSystemNameConfig("CL10B49",'L') );
            Assert.assertTrue("valid config CS10024", SerialAddress.validSystemNameConfig("CS10024",'S') );
            Assert.assertTrue("valid config CS10B24", SerialAddress.validSystemNameConfig("CS10B24",'S') );
            Assert.assertTrue("invalid config CS10025", !SerialAddress.validSystemNameConfig("CS10025",'S') );
            Assert.assertTrue("invalid config CS10B25", !SerialAddress.validSystemNameConfig("CS10B25",'S') );
            Assert.assertTrue("valid config CT4128", SerialAddress.validSystemNameConfig("CT4128",'T') );
            Assert.assertTrue("valid config CT4B128", SerialAddress.validSystemNameConfig("CT4B128",'T') );
            Assert.assertTrue("invalid config CT4129", !SerialAddress.validSystemNameConfig("CT4129",'T') );
            Assert.assertTrue("invalid config CT4129", !SerialAddress.validSystemNameConfig("CT4B129",'T') );
            Assert.assertTrue("valid config CS4064", SerialAddress.validSystemNameConfig("CS4064",'S') );
            Assert.assertTrue("valid config CS4B64", SerialAddress.validSystemNameConfig("CS4B64",'S') );
            Assert.assertTrue("invalid config CS4065", !SerialAddress.validSystemNameConfig("CS4065",'S') );
            Assert.assertTrue("invalid config CS4B65", !SerialAddress.validSystemNameConfig("CS4B65",'S') );
            Assert.assertTrue("invalid config CL11007", !SerialAddress.validSystemNameConfig("CL11007",'L') );
            Assert.assertTrue("invalid config CL11B7", !SerialAddress.validSystemNameConfig("CL11B7",'L') );
        }        
        
	public void testConvertSystemNameFormat() {
            Assert.assertEquals("convert CL14007", "CL14B7", SerialAddress.convertSystemNameToAlternate("CL14007") );
            Assert.assertEquals("convert CS7", "CS0B7", SerialAddress.convertSystemNameToAlternate("CS7") );
            Assert.assertEquals("convert CT4007", "CT4B7", SerialAddress.convertSystemNameToAlternate("CT4007") );
            Assert.assertEquals("convert CL14B7", "CL14007", SerialAddress.convertSystemNameToAlternate("CL14B7") );
            Assert.assertEquals("convert CL0B7", "CL7", SerialAddress.convertSystemNameToAlternate("CL0B7") );
            Assert.assertEquals("convert CS4B7", "CS4007", SerialAddress.convertSystemNameToAlternate("CS4B7") );
            Assert.assertEquals("convert CL14B8", "CL14008", SerialAddress.convertSystemNameToAlternate("CL14B8") );
            Assert.assertEquals("convert CL128B7", "", SerialAddress.convertSystemNameToAlternate("CL128B7") );
        }
        
	public void testNormalizeSystemName() {
            Assert.assertEquals("normalize CL14007", "CL14007", SerialAddress.normalizeSystemName("CL14007") );
            Assert.assertEquals("normalize CL007", "CL7", SerialAddress.normalizeSystemName("CL007") );
            Assert.assertEquals("normalize CL004007", "CL4007", SerialAddress.normalizeSystemName("CL004007") );
            Assert.assertEquals("normalize CL14B7", "CL14B7", SerialAddress.normalizeSystemName("CL14B7") );
            Assert.assertEquals("normalize CL0B7", "CL0B7", SerialAddress.normalizeSystemName("CL0B7") );
            Assert.assertEquals("normalize CL004B7", "CL4B7", SerialAddress.normalizeSystemName("CL004B7") );
            Assert.assertEquals("normalize CL014B0008", "CL14B8", SerialAddress.normalizeSystemName("CL014B0008") );
            Assert.assertEquals("normalize CL128B7", "", SerialAddress.normalizeSystemName("CL128B7") );
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

}
