// SerialAddressTest.java

package jmri.jmrix.cmri.serial;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.util.JUnitAppender;

/**
 * JUnit tests for the SerialAddress utility class.
 * @author	Dave Duchamp Copyright 2004
 * @version	$Revision$
 */
public class SerialAddressTest extends TestCase {


	public void setUp() {
		// log4j
		apps.tests.Log4JFixture.setUp(); 
		// create and register the manager objects
		jmri.TurnoutManager l = new SerialTurnoutManager() {
			public void notifyTurnoutCreationError(String conflict,int bitNum) {}
		};	
		jmri.InstanceManager.setTurnoutManager(l);
		
		jmri.LightManager lgt = new SerialLightManager() {
			public void notifyLightCreationError(String conflict,int bitNum) {}
		};	
		jmri.InstanceManager.setLightManager(lgt);
		
		jmri.SensorManager s = new SerialSensorManager();
		jmri.InstanceManager.setSensorManager(s);

	}

    // The minimal setup for log4J
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

	public void testValidateSystemNameFormat() {
            Assert.assertTrue("valid format - CL2", SerialAddress.validSystemNameFormat("CL2",'L') );
            Assert.assertTrue("valid format - CL0B2", SerialAddress.validSystemNameFormat("CL0B2",'L') );

            Assert.assertTrue("invalid format - CL", !SerialAddress.validSystemNameFormat("CL",'L') );
            JUnitAppender.assertErrorMessage("illegal character in number field of CMRI system name: CL");

            Assert.assertTrue("invalid format - CLB2", !SerialAddress.validSystemNameFormat("CLB2",'L') );
            JUnitAppender.assertErrorMessage("no node address before 'B' in CMRI system name: CLB2");

            Assert.assertTrue("valid format - CL2005", SerialAddress.validSystemNameFormat("CL2005",'L') );
            Assert.assertTrue("valid format - CL2B5", SerialAddress.validSystemNameFormat("CL2B5",'L') );
            Assert.assertTrue("valid format - CT2005", SerialAddress.validSystemNameFormat("CT2005",'T') );
            Assert.assertTrue("valid format - CT2B5", SerialAddress.validSystemNameFormat("CT2B5",'T') );
            Assert.assertTrue("valid format - CS2005", SerialAddress.validSystemNameFormat("CS2005",'S') );
            Assert.assertTrue("valid format - CS2B5", SerialAddress.validSystemNameFormat("CS2B5",'S') );

            Assert.assertTrue("invalid format - CY2005", !SerialAddress.validSystemNameFormat("CY2005",'L') );
            JUnitAppender.assertErrorMessage("illegal character in header field of CMRI system name: CY2005");

            Assert.assertTrue("invalid format - CY2B5", !SerialAddress.validSystemNameFormat("CY2B5",'L') );
            JUnitAppender.assertErrorMessage("illegal character in header field of CMRI system name: CY2B5");

            Assert.assertTrue("valid format - CL22001", SerialAddress.validSystemNameFormat("CL22001",'L') );
            Assert.assertTrue("valid format - CL22B1", SerialAddress.validSystemNameFormat("CL22B1",'L') );

            Assert.assertTrue("invalid format - CL22000", !SerialAddress.validSystemNameFormat("CL22000",'L') );
            JUnitAppender.assertErrorMessage("bit number not in range 1 - 999 in CMRI system name: CL22000");

            Assert.assertTrue("invalid format - CL22B0", !SerialAddress.validSystemNameFormat("CL22B0",'L') );
            JUnitAppender.assertErrorMessage("bit number field out of range in CMRI system name: CL22B0");

            Assert.assertTrue("valid format - CL2999", SerialAddress.validSystemNameFormat("CL2999",'L') );
            Assert.assertTrue("valid format - CL2B2048", SerialAddress.validSystemNameFormat("CL2B2048",'L') );

            Assert.assertTrue("invalid format - CL2B2049", !SerialAddress.validSystemNameFormat("CL2B2049",'L') );
            JUnitAppender.assertErrorMessage("bit number field out of range in CMRI system name: CL2B2049");

            Assert.assertTrue("valid format - CL127999", SerialAddress.validSystemNameFormat("CL127999",'L') );

            Assert.assertTrue("invalid format - CL128000", !SerialAddress.validSystemNameFormat("CL128000",'L') );
            JUnitAppender.assertErrorMessage("number field out of range in CMRI system name: CL128000");
 
            Assert.assertTrue("valid format - CL127B7", SerialAddress.validSystemNameFormat("CL127B7",'L') );

            Assert.assertTrue("invalid format - CL128B7", !SerialAddress.validSystemNameFormat("CL128B7",'L') );
            JUnitAppender.assertErrorMessage("node address field out of range in CMRI system name: CL128B7");

            Assert.assertTrue("invalid format - CL2oo5", !SerialAddress.validSystemNameFormat("CL2oo5",'L') );
            JUnitAppender.assertErrorMessage("illegal character in number field of CMRI system name: CL2oo5");
 
            Assert.assertTrue("invalid format - CL2aB5", !SerialAddress.validSystemNameFormat("CL2aB5",'L') );
            JUnitAppender.assertErrorMessage("illegal character in node address field of CMRI system name: CL2aB5");

            Assert.assertTrue("invalid format - CL2B5x", !SerialAddress.validSystemNameFormat("CL2B5x",'L') );
            JUnitAppender.assertErrorMessage("illegal character in bit number field of CMRI system name: CL2B5x");
	}

	public void testGetBitFromSystemName() {
            Assert.assertEquals("CL2", 2, SerialAddress.getBitFromSystemName("CL2") );
            Assert.assertEquals("CL2002", 2, SerialAddress.getBitFromSystemName("CL2002") );
            Assert.assertEquals("CL1", 1, SerialAddress.getBitFromSystemName("CL1") );
            Assert.assertEquals("CL2001", 1, SerialAddress.getBitFromSystemName("CL2001") );
            Assert.assertEquals("CL999", 999, SerialAddress.getBitFromSystemName("CL999") );
            Assert.assertEquals("CL2999", 999, SerialAddress.getBitFromSystemName("CL2999") );

            Assert.assertEquals("CL29O9", 0, SerialAddress.getBitFromSystemName("CL29O9") );
            JUnitAppender.assertErrorMessage("illegal character in number field of system name: CL29O9");

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

	public void testGetNodeAddressFromSystemName() {
			Assert.assertEquals("CL14007", 14, SerialAddress.getNodeAddressFromSystemName("CL14007") );
			Assert.assertEquals("CL14B7", 14, SerialAddress.getNodeAddressFromSystemName("CL14B7") );
			Assert.assertEquals("CL127007", 127, SerialAddress.getNodeAddressFromSystemName("CL127007") );
			Assert.assertEquals("CL127B7", 127, SerialAddress.getNodeAddressFromSystemName("CL127B7") );
			Assert.assertEquals("CL0B7", 0, SerialAddress.getNodeAddressFromSystemName("CL0B7") );
			Assert.assertEquals("CL7", 0, SerialAddress.getNodeAddressFromSystemName("CL7") );

			Assert.assertEquals("CLB7", -1, SerialAddress.getNodeAddressFromSystemName("CLB7") );
            JUnitAppender.assertErrorMessage("no node address before 'B' in CMRI system name: CLB7");

			Assert.assertEquals("CR7", -1, SerialAddress.getNodeAddressFromSystemName("CR7") );
            JUnitAppender.assertErrorMessage("illegal character in header field of system name: CR7");
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
            Assert.assertNotNull("exists", c );
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
            JUnitAppender.assertErrorMessage("node address field out of range in CMRI system name: CL128B7");
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
            JUnitAppender.assertErrorMessage("node address field out of range in CMRI system name: CL128B7");
        }
		
	public void testConstructSystemName() {
            Assert.assertEquals("make CL14007", "CL14007", SerialAddress.makeSystemName("L",14,7) );
            Assert.assertEquals("make CT7", "CT7", SerialAddress.makeSystemName("T",0,7) );

            Assert.assertEquals("make illegal 1", "", SerialAddress.makeSystemName("L",0,0) );
            JUnitAppender.assertErrorMessage("illegal bit number proposed for system name");

            Assert.assertEquals("make illegal 2", "", SerialAddress.makeSystemName("L",128,7) );
            JUnitAppender.assertErrorMessage("illegal node adddress proposed for system name");

            Assert.assertEquals("make illegal 3", "", SerialAddress.makeSystemName("R",120,7) );
            JUnitAppender.assertErrorMessage("illegal type character proposed for system name");

            Assert.assertEquals("make CL0B1770", "CL0B1770", SerialAddress.makeSystemName("L",0,1770) );
            Assert.assertEquals("make CS127999", "CS127999", SerialAddress.makeSystemName("S",127,999) );
            Assert.assertEquals("make CS14B1000", "CS14B1000", SerialAddress.makeSystemName("S",14,1000) );
		}
		
	SerialNode n = new SerialNode(18,SerialNode.SMINI);

	public void testIsOutputBitFree() {
			// create a new turnout, controlled by two output bits
			jmri.TurnoutManager tMgr = jmri.InstanceManager.turnoutManagerInstance();
			jmri.Turnout t1 = tMgr.newTurnout("CT18034","userT34");
			t1.setNumberOutputBits(2);
			// check that turnout was created correctly
            Assert.assertEquals("create CT18034 check 1", "CT18034", t1.getSystemName() );
            Assert.assertEquals("create CT18034 check 2", 2, t1.getNumberOutputBits() );			
			// create a new turnout, controlled by one output bit
			jmri.Turnout t2 = tMgr.newTurnout("CT18032","userT32");
			// check that turnout was created correctly
            Assert.assertEquals("create CT18032 check 1", "CT18032", t2.getSystemName() );
            Assert.assertEquals("create CT18032 check 2", 1, t2.getNumberOutputBits() );			
			// create two new lights  
			jmri.LightManager lMgr = jmri.InstanceManager.lightManagerInstance();
			jmri.Light lgt1 = lMgr.newLight("CL18036","userL36");
			jmri.Light lgt2 = lMgr.newLight("CL18037","userL37");
			// check that the lights were created as expected
            Assert.assertEquals("create CL18036 check", "CL18036", lgt1.getSystemName() );
            Assert.assertEquals("create CL18037 check", "CL18037", lgt2.getSystemName() );
			// test
            Assert.assertEquals("test bit 30", "", SerialAddress.isOutputBitFree(18,30) );
            Assert.assertEquals("test bit 34", "CT18034", SerialAddress.isOutputBitFree(18,34) );			
            Assert.assertEquals("test bit 33", "", SerialAddress.isOutputBitFree(18,33) );
            Assert.assertEquals("test bit 35", "CT18034", SerialAddress.isOutputBitFree(18,35) );			
            Assert.assertEquals("test bit 36", "CL18036", SerialAddress.isOutputBitFree(18,36) );
            Assert.assertEquals("test bit 37", "CL18037", SerialAddress.isOutputBitFree(18,37) );			
            Assert.assertEquals("test bit 38", "", SerialAddress.isOutputBitFree(18,38) );
            Assert.assertEquals("test bit 39", "", SerialAddress.isOutputBitFree(18,39) );			
            Assert.assertEquals("test bit 2000", "", SerialAddress.isOutputBitFree(18,2000) );			

            Assert.assertEquals("test bit bad bit", "", SerialAddress.isOutputBitFree(18,0) );
            JUnitAppender.assertWarnMessage("Turnout 'CT18034' refers to an undefined Serial Node.");
            JUnitAppender.assertWarnMessage("Turnout 'CT18032' refers to an undefined Serial Node.");
            JUnitAppender.assertWarnMessage("Light system Name does not refer to configured hardware: CL18036");
            JUnitAppender.assertWarnMessage("Light system Name does not refer to configured hardware: CL18037");
            JUnitAppender.assertErrorMessage("illegal bit number in free bit test");

            Assert.assertEquals("test bit bad node address", "", SerialAddress.isOutputBitFree(129,34) );			
            JUnitAppender.assertErrorMessage("illegal node adddress in free bit test");
		}

	public void testIsInputBitFree() {
			jmri.SensorManager sMgr = jmri.InstanceManager.sensorManagerInstance();
			// create 4 new sensors
			jmri.Sensor s1 = sMgr.newSensor("CS18016","userS16");
			jmri.Sensor s2 = sMgr.newSensor("CS18014","userS14");
			jmri.Sensor s3 = sMgr.newSensor("CS18017","userS17");
			jmri.Sensor s4 = sMgr.newSensor("CS18012","userS12");
			// check that the sensors were created as expected
            Assert.assertEquals("create CS18016 check", "CS18016", s1.getSystemName() );
            Assert.assertEquals("create CS18014 check", "CS18014", s2.getSystemName() );
            Assert.assertEquals("create CS18017 check", "CS18017", s3.getSystemName() );
            Assert.assertEquals("create CS18012 check", "CS18012", s4.getSystemName() );
			// test
            Assert.assertEquals("test bit 10", "", SerialAddress.isInputBitFree(18,10) );
            Assert.assertEquals("test bit 11", "", SerialAddress.isInputBitFree(18,11) );
            Assert.assertEquals("test bit 12", "CS18012", SerialAddress.isInputBitFree(18,12) );
            Assert.assertEquals("test bit 13", "", SerialAddress.isInputBitFree(18,13) );
            Assert.assertEquals("test bit 14", "CS18014", SerialAddress.isInputBitFree(18,14) );
            Assert.assertEquals("test bit 15", "", SerialAddress.isInputBitFree(18,15) );
            Assert.assertEquals("test bit 16", "CS18016", SerialAddress.isInputBitFree(18,16) );
            Assert.assertEquals("test bit 17", "CS18017", SerialAddress.isInputBitFree(18,17) );
            Assert.assertEquals("test bit 18", "", SerialAddress.isInputBitFree(18,18) );

            Assert.assertEquals("test bit bad bit", "", SerialAddress.isInputBitFree(18,0) );
            JUnitAppender.assertWarnMessage("Sensor CS18016 refers to an undefined Serial Node.");
            JUnitAppender.assertWarnMessage("Sensor CS18014 refers to an undefined Serial Node.");
            JUnitAppender.assertWarnMessage("Sensor CS18017 refers to an undefined Serial Node.");
            JUnitAppender.assertWarnMessage("Sensor CS18012 refers to an undefined Serial Node.");
            JUnitAppender.assertErrorMessage("illegal bit number in free bit test");

            Assert.assertEquals("test bit bad node address", "", SerialAddress.isInputBitFree(129,34) );			
            JUnitAppender.assertErrorMessage("illegal node adddress in free bit test");
		}

	public void testGetUserNameFromSystemName() {
			jmri.SensorManager sMgr = jmri.InstanceManager.sensorManagerInstance();
			// create 4 new sensors
			jmri.Sensor s1 = sMgr.newSensor("CS18016","userS16");
			jmri.Sensor s2 = sMgr.newSensor("CS18014","userS14");
			jmri.Sensor s3 = sMgr.newSensor("CS18017","userS17");
			jmri.Sensor s4 = sMgr.newSensor("CS18012","userS12");

			jmri.LightManager lMgr = jmri.InstanceManager.lightManagerInstance();
			jmri.Light lgt1 = lMgr.newLight("CL18036","userL36");
			jmri.Light lgt2 = lMgr.newLight("CL18037","userL37");

			jmri.TurnoutManager tMgr = jmri.InstanceManager.turnoutManagerInstance();
			jmri.Turnout t2 = tMgr.newTurnout("CT18032","userT32");
			jmri.Turnout t1 = tMgr.newTurnout("CT18034","userT34");

            Assert.assertEquals("test CS18016", "userS16", SerialAddress.getUserNameFromSystemName("CS18016") );
            Assert.assertEquals("test CS18012", "userS12", SerialAddress.getUserNameFromSystemName("CS18012") );
            Assert.assertEquals("test CS18017", "userS17", SerialAddress.getUserNameFromSystemName("CS18017") );
            Assert.assertEquals("test undefined CS18010", "", SerialAddress.getUserNameFromSystemName("CS18010") );
            Assert.assertEquals("test CL18037", "userL37", SerialAddress.getUserNameFromSystemName("CL18037") );
            Assert.assertEquals("test CL18036", "userL36", SerialAddress.getUserNameFromSystemName("CL18036") );
            Assert.assertEquals("test undefined CL18030", "", SerialAddress.getUserNameFromSystemName("CL18030") );
            Assert.assertEquals("test CT18032", "userT32", SerialAddress.getUserNameFromSystemName("CT18032") );
            Assert.assertEquals("test CT18034", "userT34", SerialAddress.getUserNameFromSystemName("CT18034") );
            Assert.assertEquals("test undefined CT18039", "", SerialAddress.getUserNameFromSystemName("CT18039") );
	}	
		
	// from here down is testing infrastructure

	public SerialAddressTest(String s) {
            super(s);
	}

	// Main entry point
	static public void main(String[] args) {
            String[] testCaseName = {"-noloading", SerialAddressTest.class.getName()};
            junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
            TestSuite suite = new TestSuite(SerialAddressTest.class);
            return suite;
	}

}
