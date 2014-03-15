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
            Assert.assertTrue("valid format - KL2", SerialAddress.validSystemNameFormat("KL2",'L') );

            Assert.assertTrue("invalid format - KL", !SerialAddress.validSystemNameFormat("KL",'L') );
            JUnitAppender.assertErrorMessage("illegal character in number field of system name: KL");

            Assert.assertTrue("valid format - KL2005", SerialAddress.validSystemNameFormat("KL2005",'L') );
            Assert.assertTrue("valid format - KT2005", SerialAddress.validSystemNameFormat("KT2005",'T') );
            Assert.assertTrue("valid format - KS205", SerialAddress.validSystemNameFormat("KS205",'S') );

            Assert.assertTrue("invalid format - KY2005", !SerialAddress.validSystemNameFormat("KY2005",'L') );
            JUnitAppender.assertErrorMessage("illegal character in header field of system name: KY2005");

            Assert.assertTrue("valid format - KL1", SerialAddress.validSystemNameFormat("KL1",'L') );
            Assert.assertTrue("valid format - KL1000", SerialAddress.validSystemNameFormat("KL1000",'L') );

			// note: address is invalid, but format is valid
            Assert.assertTrue("valid format - KL0", SerialAddress.validSystemNameFormat("KL0",'L') );

            Assert.assertTrue("valid format - KL2999", SerialAddress.validSystemNameFormat("KL2999",'L') );

            Assert.assertTrue("valid format - KL7999", SerialAddress.validSystemNameFormat("KL7999",'L') );

            Assert.assertTrue("invalid format - KL2oo5", !SerialAddress.validSystemNameFormat("KL2oo5",'L') );
            JUnitAppender.assertErrorMessage("illegal character in number field of system name: KL2oo5");
	}

	public void testGetBitFromSystemName() {
            Assert.assertEquals("KL2", 2, SerialAddress.getBitFromSystemName("KL2") );
            Assert.assertEquals("KL2002", 2002, SerialAddress.getBitFromSystemName("KL2002") );
            Assert.assertEquals("KL1", 1, SerialAddress.getBitFromSystemName("KL1") );
            Assert.assertEquals("KL2001", 2001, SerialAddress.getBitFromSystemName("KL2001") );
            Assert.assertEquals("KL999", 999, SerialAddress.getBitFromSystemName("KL999") );
            Assert.assertEquals("KL2999", 2999, SerialAddress.getBitFromSystemName("KL2999") );

            Assert.assertEquals("KL29O9", 0, SerialAddress.getBitFromSystemName("KL29O9") );
            JUnitAppender.assertErrorMessage("illegal character in number field of system name: KL29O9");
        }

        SerialNode d = new SerialNode(4,0);
        SerialNode c = new SerialNode(10,0);
        SerialNode b = new SerialNode(99,0);

	public void testValidSystemNameConfig() {
			//InputBits ibit = new InputBits();
			//OutputBits obit = new OutputBits();
			InputBits.setNumInputBits(40);
			OutputBits.setNumOutputBits(201);
            Assert.assertTrue("valid config KL47", SerialAddress.validSystemNameConfig("KL47",'L') );
            Assert.assertTrue("valid config KS17", SerialAddress.validSystemNameConfig("KS17",'S') );
            Assert.assertTrue("valid config KL148", SerialAddress.validSystemNameConfig("KL148",'L') );
            Assert.assertTrue("invalid config KL1049", !SerialAddress.validSystemNameConfig("KL1049",'L') );
            Assert.assertTrue("valid config KS24", SerialAddress.validSystemNameConfig("KS24",'S') );
            Assert.assertTrue("valid config KS40", SerialAddress.validSystemNameConfig("KS40",'S') );
            Assert.assertTrue("invalid config KS41", !SerialAddress.validSystemNameConfig("KS41",'S') );
            Assert.assertTrue("invalid config KS0", !SerialAddress.validSystemNameConfig("KS0",'S') );
            JUnitAppender.assertErrorMessage("invalid system name: KS0");
            Assert.assertTrue("valid config KT201", SerialAddress.validSystemNameConfig("KT201",'T') );
            Assert.assertTrue("invalid config KT202", !SerialAddress.validSystemNameConfig("KT202",'T') );
            Assert.assertTrue("invalid config KT4129", !SerialAddress.validSystemNameConfig("KT4129",'T') );
        }        
        
	public void testNormalizeSystemName() {
            Assert.assertEquals("normalize KL007", "KL7", SerialAddress.normalizeSystemName("KL007") );
            Assert.assertEquals("normalize KL004007", "KL4007", SerialAddress.normalizeSystemName("KL004007") );

            Assert.assertEquals("normalize KL12007", "", SerialAddress.normalizeSystemName("KL12007") );
            JUnitAppender.assertErrorMessage("node address field out of range in system name - KL12007");
        }
		
	public void testConstructSystemName() {
            Assert.assertEquals("make KL7", "KL7", SerialAddress.makeSystemName("L",7) );
            Assert.assertEquals("make KT7", "KT7", SerialAddress.makeSystemName("T",7) );
			Assert.assertEquals("make KS7", "KS7", SerialAddress.makeSystemName("S",7) );

            Assert.assertEquals("make illegal 1", "", SerialAddress.makeSystemName("L",0) );
            JUnitAppender.assertErrorMessage("illegal address range proposed for system name - 0");

            Assert.assertEquals("make illegal 2", "", SerialAddress.makeSystemName("L",9990) );
            JUnitAppender.assertErrorMessage("illegal address range proposed for system name - 9990");

            Assert.assertEquals("make illegal 3", "", SerialAddress.makeSystemName("R",120) );
            JUnitAppender.assertErrorMessage("illegal type character proposed for system name - R");

            Assert.assertEquals("make KS999", "KS999", SerialAddress.makeSystemName("S",999) );
            Assert.assertEquals("make KS1000", "KS1000", SerialAddress.makeSystemName("S",1000) );
		}
		
	SerialNode n = new SerialNode(18,0);

	public void testIsOutputBitFree() {
			// create a new turnout
			jmri.TurnoutManager tMgr = jmri.InstanceManager.turnoutManagerInstance();
			jmri.Turnout t1 = tMgr.newTurnout("KT034","userT34");
			// check that turnout was created correctly including normalizing system name
            Assert.assertEquals("create KT34 check 1", "KT34", t1.getSystemName() );
			// create a new turnout
			jmri.Turnout t2 = tMgr.newTurnout("KT32","userT32");
			// check that turnout was created correctly
            Assert.assertEquals("create KT32 check 1", "KT32", t2.getSystemName() );
			// create two new lights  
			jmri.LightManager lMgr = jmri.InstanceManager.lightManagerInstance();
			jmri.Light lgt1 = lMgr.newLight("KL36","userL36");
			jmri.Light lgt2 = lMgr.newLight("KL037","userL37");
			// check that the lights were created as expected
            Assert.assertEquals("create KL36 check", "KL36", lgt1.getSystemName() );
            Assert.assertEquals("create KL37 check", "KL37", lgt2.getSystemName() );
			// test
            Assert.assertEquals("test bit 30", "", SerialAddress.isOutputBitFree(30) );
            Assert.assertEquals("test bit 34", "KT34", SerialAddress.isOutputBitFree(34) );			
            Assert.assertEquals("test bit 36", "KL36", SerialAddress.isOutputBitFree(36) );
            Assert.assertEquals("test bit 37", "KL37", SerialAddress.isOutputBitFree(37) );			
            Assert.assertEquals("test bit 38", "", SerialAddress.isOutputBitFree(38) );
            Assert.assertEquals("test bit 39", "", SerialAddress.isOutputBitFree(39) );			
            Assert.assertEquals("test bit 1000", "", SerialAddress.isOutputBitFree(1000) );			
		}

	public void testIsInputBitFree() {
			jmri.SensorManager sMgr = jmri.InstanceManager.sensorManagerInstance();
			// create 4 new sensors
			jmri.Sensor s1 = sMgr.newSensor("KS16","userS16");
			jmri.Sensor s2 = sMgr.newSensor("KS014","userS14");
			jmri.Sensor s3 = sMgr.newSensor("KS17","userS17");
			jmri.Sensor s4 = sMgr.newSensor("KS12","userS12");
			// check that the sensors were created as expected
            Assert.assertEquals("create KS16 check", "KS16", s1.getSystemName() );
            Assert.assertEquals("create KS14 check", "KS14", s2.getSystemName() );
            Assert.assertEquals("create KS17 check", "KS17", s3.getSystemName() );
            Assert.assertEquals("create KS12 check", "KS12", s4.getSystemName() );
			// test
            Assert.assertEquals("test bit 10", "", SerialAddress.isInputBitFree(10) );
            Assert.assertEquals("test bit 11", "", SerialAddress.isInputBitFree(11) );
            Assert.assertEquals("test bit 12", "KS12", SerialAddress.isInputBitFree(12) );
            Assert.assertEquals("test bit 13", "", SerialAddress.isInputBitFree(13) );
            Assert.assertEquals("test bit 14", "KS14", SerialAddress.isInputBitFree(14) );
            Assert.assertEquals("test bit 15", "", SerialAddress.isInputBitFree(15) );
            Assert.assertEquals("test bit 16", "KS16", SerialAddress.isInputBitFree(16) );
            Assert.assertEquals("test bit 17", "KS17", SerialAddress.isInputBitFree(17) );
            Assert.assertEquals("test bit 18", "", SerialAddress.isInputBitFree(18) );
		}

	public void testGetUserNameFromSystemName() {
			jmri.SensorManager sMgr = jmri.InstanceManager.sensorManagerInstance();
			// create 4 new sensors
			jmri.Sensor s1 = sMgr.newSensor("KS16","userS16");
			jmri.Sensor s2 = sMgr.newSensor("KS014","userS14");
			jmri.Sensor s3 = sMgr.newSensor("KS17","userS17");
			jmri.Sensor s4 = sMgr.newSensor("KS12","userS12");

			jmri.LightManager lMgr = jmri.InstanceManager.lightManagerInstance();
			jmri.Light lgt1 = lMgr.newLight("KL36","userL36");
			jmri.Light lgt2 = lMgr.newLight("KL037","userL37");

			jmri.TurnoutManager tMgr = jmri.InstanceManager.turnoutManagerInstance();
			jmri.Turnout t2 = tMgr.newTurnout("KT32","userT32");
			jmri.Turnout t1 = tMgr.newTurnout("KT34","userT34");

            Assert.assertEquals("test KS16", "userS16", SerialAddress.getUserNameFromSystemName("KS16") );
            Assert.assertEquals("test KS12", "userS12", SerialAddress.getUserNameFromSystemName("KS12") );
            Assert.assertEquals("test KS17", "userS17", SerialAddress.getUserNameFromSystemName("KS17") );
            Assert.assertEquals("test undefined KS10", "", SerialAddress.getUserNameFromSystemName("KS10") );
            Assert.assertEquals("test KL37", "userL37", SerialAddress.getUserNameFromSystemName("KL37") );
            Assert.assertEquals("test KL36", "userL36", SerialAddress.getUserNameFromSystemName("KL36") );
            Assert.assertEquals("test undefined KL30", "", SerialAddress.getUserNameFromSystemName("KL30") );
            Assert.assertEquals("test KT32", "userT32", SerialAddress.getUserNameFromSystemName("KT32") );
            Assert.assertEquals("test KT34", "userT34", SerialAddress.getUserNameFromSystemName("KT34") );
            Assert.assertEquals("test undefined KT39", "", SerialAddress.getUserNameFromSystemName("KT39") );
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
