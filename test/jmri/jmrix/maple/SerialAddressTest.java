// SerialAddressTest.java

package jmri.jmrix.maple;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.util.JUnitAppender;

/**
 * JUnit tests for the SerialAddress utility class.
 * @author	Dave Duchamp Copyright 2004
 * @version	$Revision: 1.2 $
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
            Assert.assertTrue("valid format - KL2", SerialAddress.validSystemNameFormat("KL2",'L') );
            Assert.assertTrue("valid format - KL0B2", SerialAddress.validSystemNameFormat("KL0B2",'L') );

            Assert.assertTrue("invalid format - KL", !SerialAddress.validSystemNameFormat("KL",'L') );
            JUnitAppender.assertErrorMessage("illegal character in number field of system name: KL");

            Assert.assertTrue("invalid format - KLB2", !SerialAddress.validSystemNameFormat("KLB2",'L') );
            JUnitAppender.assertErrorMessage("no node address before 'B' in system name: KLB2");

            Assert.assertTrue("valid format - KL2005", SerialAddress.validSystemNameFormat("KL2005",'L') );
            Assert.assertTrue("valid format - KL2B5", SerialAddress.validSystemNameFormat("KL2B5",'L') );
            Assert.assertTrue("valid format - KT2005", SerialAddress.validSystemNameFormat("KT2005",'T') );
            Assert.assertTrue("valid format - KT2B5", SerialAddress.validSystemNameFormat("KT2B5",'T') );
            Assert.assertTrue("valid format - KS2005", SerialAddress.validSystemNameFormat("KS2005",'S') );
            Assert.assertTrue("valid format - KS2B5", SerialAddress.validSystemNameFormat("KS2B5",'S') );

            Assert.assertTrue("invalid format - KY2005", !SerialAddress.validSystemNameFormat("KY2005",'L') );
            JUnitAppender.assertErrorMessage("illegal character in header field of system name: KY2005");

            Assert.assertTrue("invalid format - KY2B5", !SerialAddress.validSystemNameFormat("KY2B5",'L') );
            JUnitAppender.assertErrorMessage("illegal character in header field of system name: KY2B5");

            Assert.assertTrue("valid format - KL22001", SerialAddress.validSystemNameFormat("KL22001",'L') );
            Assert.assertTrue("valid format - KL22B1", SerialAddress.validSystemNameFormat("KL22B1",'L') );

            Assert.assertTrue("invalid format - KL22000", !SerialAddress.validSystemNameFormat("KL22000",'L') );
            JUnitAppender.assertErrorMessage("bit number not in range 1 - 999 in system name: KL22000");

            Assert.assertTrue("invalid format - KL22B0", !SerialAddress.validSystemNameFormat("KL22B0",'L') );
            JUnitAppender.assertErrorMessage("bit number field out of range in system name: KL22B0");

            Assert.assertTrue("valid format - KL2999", SerialAddress.validSystemNameFormat("KL2999",'L') );
            Assert.assertTrue("valid format - KL2B2048", SerialAddress.validSystemNameFormat("KL2B2048",'L') );

            Assert.assertTrue("invalid format - KL2B2049", !SerialAddress.validSystemNameFormat("KL2B2049",'L') );
            JUnitAppender.assertErrorMessage("bit number field out of range in system name: KL2B2049");

            Assert.assertTrue("valid format - KL127999", SerialAddress.validSystemNameFormat("KL127999",'L') );

            Assert.assertTrue("invalid format - KL128000", !SerialAddress.validSystemNameFormat("KL128000",'L') );
            JUnitAppender.assertErrorMessage("number field out of range in system name: KL128000");
 
            Assert.assertTrue("valid format - KL127B7", SerialAddress.validSystemNameFormat("KL127B7",'L') );

            Assert.assertTrue("invalid format - KL128B7", !SerialAddress.validSystemNameFormat("KL128B7",'L') );
            JUnitAppender.assertErrorMessage("node address field out of range in system name: KL128B7");

            Assert.assertTrue("invalid format - KL2oo5", !SerialAddress.validSystemNameFormat("KL2oo5",'L') );
            JUnitAppender.assertErrorMessage("illegal character in number field of system name: KL2oo5");
 
            Assert.assertTrue("invalid format - KL2aB5", !SerialAddress.validSystemNameFormat("KL2aB5",'L') );
            JUnitAppender.assertErrorMessage("illegal character in node address field of system name: KL2aB5");

            Assert.assertTrue("invalid format - KL2B5x", !SerialAddress.validSystemNameFormat("KL2B5x",'L') );
            JUnitAppender.assertErrorMessage("illegal character in bit number field of system name: KL2B5x");
	}

	public void testGetBitFromSystemName() {
            Assert.assertEquals("KL2", 2, SerialAddress.getBitFromSystemName("KL2") );
            Assert.assertEquals("KL2002", 2, SerialAddress.getBitFromSystemName("KL2002") );
            Assert.assertEquals("KL1", 1, SerialAddress.getBitFromSystemName("KL1") );
            Assert.assertEquals("KL2001", 1, SerialAddress.getBitFromSystemName("KL2001") );
            Assert.assertEquals("KL999", 999, SerialAddress.getBitFromSystemName("KL999") );
            Assert.assertEquals("KL2999", 999, SerialAddress.getBitFromSystemName("KL2999") );

            Assert.assertEquals("KL29O9", 0, SerialAddress.getBitFromSystemName("KL29O9") );
            JUnitAppender.assertErrorMessage("illegal character in number field of system name: KL29O9");

            Assert.assertEquals("KL0B7", 7, SerialAddress.getBitFromSystemName("KL0B7") );
            Assert.assertEquals("KL2B7", 7, SerialAddress.getBitFromSystemName("KL2B7") );
            Assert.assertEquals("KL0B1", 1, SerialAddress.getBitFromSystemName("KL0B1") );
            Assert.assertEquals("KL2B1", 1, SerialAddress.getBitFromSystemName("KL2B1") );
            Assert.assertEquals("KL0B2048", 2048, SerialAddress.getBitFromSystemName("KL0B2048") );
            Assert.assertEquals("KL11B2048", 2048, SerialAddress.getBitFromSystemName("KL11B2048") );
        }

        SerialNode d = new SerialNode(4,SerialNode.USIC_SUSIC);
        SerialNode c = new SerialNode(10,SerialNode.SMINI);
        SerialNode b = new SerialNode(127,SerialNode.SMINI);
        
	public void testGetNodeFromSystemName() {
            SerialNode d = new SerialNode(14,SerialNode.USIC_SUSIC);
            SerialNode c = new SerialNode(17,SerialNode.SMINI);
            SerialNode b = new SerialNode(127,SerialNode.SMINI);
            Assert.assertEquals("node of KL14007", d, SerialAddress.getNodeFromSystemName("KL14007") );
            Assert.assertEquals("node of KL14B7", d, SerialAddress.getNodeFromSystemName("KL14B7") );
            Assert.assertEquals("node of KL127007", b, SerialAddress.getNodeFromSystemName("KL127007") );
            Assert.assertEquals("node of KL127B7", b, SerialAddress.getNodeFromSystemName("KL127B7") );
            Assert.assertEquals("node of KL17007", c, SerialAddress.getNodeFromSystemName("KL17007") );
            Assert.assertEquals("node of KL17B7", c, SerialAddress.getNodeFromSystemName("KL17B7") );
            Assert.assertEquals("node of KL11007", null, SerialAddress.getNodeFromSystemName("KL11007") );
            Assert.assertEquals("node of KL11B7", null, SerialAddress.getNodeFromSystemName("KL11B7") );
        }

	public void testGetNodeAddressFromSystemName() {
			Assert.assertEquals("KL14007", 14, SerialAddress.getNodeAddressFromSystemName("KL14007") );
			Assert.assertEquals("KL14B7", 14, SerialAddress.getNodeAddressFromSystemName("KL14B7") );
			Assert.assertEquals("KL127007", 127, SerialAddress.getNodeAddressFromSystemName("KL127007") );
			Assert.assertEquals("KL127B7", 127, SerialAddress.getNodeAddressFromSystemName("KL127B7") );
			Assert.assertEquals("KL0B7", 0, SerialAddress.getNodeAddressFromSystemName("KL0B7") );
			Assert.assertEquals("KL7", 0, SerialAddress.getNodeAddressFromSystemName("KL7") );

			Assert.assertEquals("KLB7", -1, SerialAddress.getNodeAddressFromSystemName("KLB7") );
            JUnitAppender.assertErrorMessage("no node address before 'B' in system name: KLB7");

			Assert.assertEquals("KR7", -1, SerialAddress.getNodeAddressFromSystemName("KR7") );
            JUnitAppender.assertErrorMessage("illegal character in header field of system name: KR7");
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
            Assert.assertTrue("valid config KL4007", SerialAddress.validSystemNameConfig("KL4007",'L') );
            Assert.assertTrue("valid config KL4B7", SerialAddress.validSystemNameConfig("KL4B7",'L') );
            Assert.assertTrue("valid config KS10007", SerialAddress.validSystemNameConfig("KS10007",'S') );
            Assert.assertTrue("valid config KS10B7", SerialAddress.validSystemNameConfig("KS10B7",'S') );
            Assert.assertTrue("valid config KL10048", SerialAddress.validSystemNameConfig("KL10048",'L') );
            Assert.assertTrue("valid config KL10B48", SerialAddress.validSystemNameConfig("KL10B48",'L') );
            Assert.assertTrue("invalid config KL10049", !SerialAddress.validSystemNameConfig("KL10049",'L') );
            Assert.assertTrue("invalid config KL10B49", !SerialAddress.validSystemNameConfig("KL10B49",'L') );
            Assert.assertTrue("valid config KS10024", SerialAddress.validSystemNameConfig("KS10024",'S') );
            Assert.assertTrue("valid config KS10B24", SerialAddress.validSystemNameConfig("KS10B24",'S') );
            Assert.assertTrue("invalid config KS10025", !SerialAddress.validSystemNameConfig("KS10025",'S') );
            Assert.assertTrue("invalid config KS10B25", !SerialAddress.validSystemNameConfig("KS10B25",'S') );
            Assert.assertTrue("valid config KT4128", SerialAddress.validSystemNameConfig("KT4128",'T') );
            Assert.assertTrue("valid config KT4B128", SerialAddress.validSystemNameConfig("KT4B128",'T') );
            Assert.assertTrue("invalid config KT4129", !SerialAddress.validSystemNameConfig("KT4129",'T') );
            Assert.assertTrue("invalid config KT4129", !SerialAddress.validSystemNameConfig("KT4B129",'T') );
            Assert.assertTrue("valid config KS4064", SerialAddress.validSystemNameConfig("KS4064",'S') );
            Assert.assertTrue("valid config KS4B64", SerialAddress.validSystemNameConfig("KS4B64",'S') );
            Assert.assertTrue("invalid config KS4065", !SerialAddress.validSystemNameConfig("KS4065",'S') );
            Assert.assertTrue("invalid config KS4B65", !SerialAddress.validSystemNameConfig("KS4B65",'S') );
            Assert.assertTrue("invalid config KL11007", !SerialAddress.validSystemNameConfig("KL11007",'L') );
            Assert.assertTrue("invalid config KL11B7", !SerialAddress.validSystemNameConfig("KL11B7",'L') );
        }        
        
	public void testConvertSystemNameFormat() {
            Assert.assertEquals("convert KL14007", "KL14B7", SerialAddress.convertSystemNameToAlternate("KL14007") );
            Assert.assertEquals("convert KS7", "KS0B7", SerialAddress.convertSystemNameToAlternate("KS7") );
            Assert.assertEquals("convert KT4007", "KT4B7", SerialAddress.convertSystemNameToAlternate("KT4007") );
            Assert.assertEquals("convert KL14B7", "KL14007", SerialAddress.convertSystemNameToAlternate("KL14B7") );
            Assert.assertEquals("convert KL0B7", "KL7", SerialAddress.convertSystemNameToAlternate("KL0B7") );
            Assert.assertEquals("convert KS4B7", "KS4007", SerialAddress.convertSystemNameToAlternate("KS4B7") );
            Assert.assertEquals("convert KL14B8", "KL14008", SerialAddress.convertSystemNameToAlternate("KL14B8") );

            Assert.assertEquals("convert KL128B7", "", SerialAddress.convertSystemNameToAlternate("KL128B7") );
            JUnitAppender.assertErrorMessage("node address field out of range in system name: KL128B7");
        }
        
	public void testNormalizeSystemName() {
            Assert.assertEquals("normalize KL14007", "KL14007", SerialAddress.normalizeSystemName("KL14007") );
            Assert.assertEquals("normalize KL007", "KL7", SerialAddress.normalizeSystemName("KL007") );
            Assert.assertEquals("normalize KL004007", "KL4007", SerialAddress.normalizeSystemName("KL004007") );
            Assert.assertEquals("normalize KL14B7", "KL14B7", SerialAddress.normalizeSystemName("KL14B7") );
            Assert.assertEquals("normalize KL0B7", "KL0B7", SerialAddress.normalizeSystemName("KL0B7") );
            Assert.assertEquals("normalize KL004B7", "KL4B7", SerialAddress.normalizeSystemName("KL004B7") );
            Assert.assertEquals("normalize KL014B0008", "KL14B8", SerialAddress.normalizeSystemName("KL014B0008") );

            Assert.assertEquals("normalize KL128B7", "", SerialAddress.normalizeSystemName("KL128B7") );
            JUnitAppender.assertErrorMessage("node address field out of range in system name: KL128B7");
        }
		
	public void testConstructSystemName() {
            Assert.assertEquals("make KL14007", "KL14007", SerialAddress.makeSystemName("L",14,7) );
            Assert.assertEquals("make KT7", "KT7", SerialAddress.makeSystemName("T",0,7) );

            Assert.assertEquals("make illegal 1", "", SerialAddress.makeSystemName("L",0,0) );
            JUnitAppender.assertErrorMessage("illegal bit number proposed for system name");

            Assert.assertEquals("make illegal 2", "", SerialAddress.makeSystemName("L",128,7) );
            JUnitAppender.assertErrorMessage("illegal node adddress proposed for system name");

            Assert.assertEquals("make illegal 3", "", SerialAddress.makeSystemName("R",120,7) );
            JUnitAppender.assertErrorMessage("illegal type character proposed for system name");

            Assert.assertEquals("make KL0B1770", "KL0B1770", SerialAddress.makeSystemName("L",0,1770) );
            Assert.assertEquals("make KS127999", "KS127999", SerialAddress.makeSystemName("S",127,999) );
            Assert.assertEquals("make KS14B1000", "KS14B1000", SerialAddress.makeSystemName("S",14,1000) );
		}
		
	SerialNode n = new SerialNode(18,SerialNode.SMINI);

	public void testIsOutputBitFree() {
			// create a new turnout, controlled by two output bits
			jmri.TurnoutManager tMgr = jmri.InstanceManager.turnoutManagerInstance();
			jmri.Turnout t1 = tMgr.newTurnout("KT18034","userT34");
			t1.setNumberOutputBits(2);
			// check that turnout was created correctly
            Assert.assertEquals("create KT18034 check 1", "KT18034", t1.getSystemName() );
            Assert.assertEquals("create KT18034 check 2", 2, t1.getNumberOutputBits() );			
			// create a new turnout, controlled by one output bit
			jmri.Turnout t2 = tMgr.newTurnout("KT18032","userT32");
			// check that turnout was created correctly
            Assert.assertEquals("create KT18032 check 1", "KT18032", t2.getSystemName() );
            Assert.assertEquals("create KT18032 check 2", 1, t2.getNumberOutputBits() );			
			// create two new lights  
			jmri.LightManager lMgr = jmri.InstanceManager.lightManagerInstance();
			jmri.Light lgt1 = lMgr.newLight("KL18036","userL36");
			jmri.Light lgt2 = lMgr.newLight("KL18037","userL37");
			// check that the lights were created as expected
            Assert.assertEquals("create KL18036 check", "KL18036", lgt1.getSystemName() );
            Assert.assertEquals("create KL18037 check", "KL18037", lgt2.getSystemName() );
			// test
            Assert.assertEquals("test bit 30", "", SerialAddress.isOutputBitFree(18,30) );
            Assert.assertEquals("test bit 34", "KT18034", SerialAddress.isOutputBitFree(18,34) );			
            Assert.assertEquals("test bit 33", "", SerialAddress.isOutputBitFree(18,33) );
            Assert.assertEquals("test bit 35", "KT18034", SerialAddress.isOutputBitFree(18,35) );			
            Assert.assertEquals("test bit 36", "KL18036", SerialAddress.isOutputBitFree(18,36) );
            Assert.assertEquals("test bit 37", "KL18037", SerialAddress.isOutputBitFree(18,37) );			
            Assert.assertEquals("test bit 38", "", SerialAddress.isOutputBitFree(18,38) );
            Assert.assertEquals("test bit 39", "", SerialAddress.isOutputBitFree(18,39) );			
            Assert.assertEquals("test bit 2000", "", SerialAddress.isOutputBitFree(18,2000) );			

            Assert.assertEquals("test bit bad bit", "", SerialAddress.isOutputBitFree(18,0) );
            JUnitAppender.assertWarnMessage("Turnout 'KT18034' refers to an undefined Serial Node.");
            JUnitAppender.assertWarnMessage("Turnout 'KT18032' refers to an undefined Serial Node.");
            JUnitAppender.assertWarnMessage("Light system Name does not refer to configured hardware: KL18036");
            JUnitAppender.assertWarnMessage("Light system Name does not refer to configured hardware: KL18037");
            JUnitAppender.assertErrorMessage("illegal bit number in free bit test");

            Assert.assertEquals("test bit bad node address", "", SerialAddress.isOutputBitFree(129,34) );			
            JUnitAppender.assertErrorMessage("illegal node adddress in free bit test");
		}

	public void testIsInputBitFree() {
			jmri.SensorManager sMgr = jmri.InstanceManager.sensorManagerInstance();
			// create 4 new sensors
			jmri.Sensor s1 = sMgr.newSensor("KS18016","userS16");
			jmri.Sensor s2 = sMgr.newSensor("KS18014","userS14");
			jmri.Sensor s3 = sMgr.newSensor("KS18017","userS17");
			jmri.Sensor s4 = sMgr.newSensor("KS18012","userS12");
			// check that the sensors were created as expected
            Assert.assertEquals("create KS18016 check", "KS18016", s1.getSystemName() );
            Assert.assertEquals("create KS18014 check", "KS18014", s2.getSystemName() );
            Assert.assertEquals("create KS18017 check", "KS18017", s3.getSystemName() );
            Assert.assertEquals("create KS18012 check", "KS18012", s4.getSystemName() );
			// test
            Assert.assertEquals("test bit 10", "", SerialAddress.isInputBitFree(18,10) );
            Assert.assertEquals("test bit 11", "", SerialAddress.isInputBitFree(18,11) );
            Assert.assertEquals("test bit 12", "KS18012", SerialAddress.isInputBitFree(18,12) );
            Assert.assertEquals("test bit 13", "", SerialAddress.isInputBitFree(18,13) );
            Assert.assertEquals("test bit 14", "KS18014", SerialAddress.isInputBitFree(18,14) );
            Assert.assertEquals("test bit 15", "", SerialAddress.isInputBitFree(18,15) );
            Assert.assertEquals("test bit 16", "KS18016", SerialAddress.isInputBitFree(18,16) );
            Assert.assertEquals("test bit 17", "KS18017", SerialAddress.isInputBitFree(18,17) );
            Assert.assertEquals("test bit 18", "", SerialAddress.isInputBitFree(18,18) );

            Assert.assertEquals("test bit bad bit", "", SerialAddress.isInputBitFree(18,0) );
            JUnitAppender.assertWarnMessage("Sensor KS18016 refers to an undefined Serial Node.");
            JUnitAppender.assertWarnMessage("Sensor KS18014 refers to an undefined Serial Node.");
            JUnitAppender.assertWarnMessage("Sensor KS18017 refers to an undefined Serial Node.");
            JUnitAppender.assertWarnMessage("Sensor KS18012 refers to an undefined Serial Node.");
            JUnitAppender.assertErrorMessage("illegal bit number in free bit test");

            Assert.assertEquals("test bit bad node address", "", SerialAddress.isInputBitFree(129,34) );			
            JUnitAppender.assertErrorMessage("illegal node adddress in free bit test");
		}

	public void testGetUserNameFromSystemName() {
            Assert.assertEquals("test KS18016", "userS16", SerialAddress.getUserNameFromSystemName("KS18016") );
            Assert.assertEquals("test KS18012", "userS12", SerialAddress.getUserNameFromSystemName("KS18012") );
            Assert.assertEquals("test KS18017", "userS17", SerialAddress.getUserNameFromSystemName("KS18017") );
            Assert.assertEquals("test undefined KS18010", "", SerialAddress.getUserNameFromSystemName("KS18010") );
            Assert.assertEquals("test KL18037", "userL37", SerialAddress.getUserNameFromSystemName("KL18037") );
            Assert.assertEquals("test KL18036", "userL36", SerialAddress.getUserNameFromSystemName("KL18036") );
            Assert.assertEquals("test undefined KL18030", "", SerialAddress.getUserNameFromSystemName("KL18030") );
            Assert.assertEquals("test KT18032", "userT32", SerialAddress.getUserNameFromSystemName("KT18032") );
            Assert.assertEquals("test KT18034", "userT34", SerialAddress.getUserNameFromSystemName("KT18034") );
            Assert.assertEquals("test undefined KT18039", "", SerialAddress.getUserNameFromSystemName("KT18039") );
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
