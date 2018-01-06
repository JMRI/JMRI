package jmri.jmrix.maple;

import jmri.Manager.NameValidity;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the SerialAddress utility class.
 *
 * @author	Dave Duchamp Copyright 2004
  */
public class SerialAddressTest {

    private InputBits ibits = null;
    private OutputBits obits = null;

    @Test
    public void testValidateSystemNameFormat() {
        Assert.assertTrue("valid format - KL2", NameValidity.VALID == SerialAddress.validSystemNameFormat("KL2", 'L', "K"));

        Assert.assertTrue("invalid format - KL", NameValidity.VALID != SerialAddress.validSystemNameFormat("KL", 'L', "K"));
        JUnitAppender.assertWarnMessage("missing numerical node address in system name: KL");

        Assert.assertTrue("valid format - KL2005", NameValidity.VALID == SerialAddress.validSystemNameFormat("KL2005", 'L', "K"));
        Assert.assertTrue("valid format - KT2005", NameValidity.VALID == SerialAddress.validSystemNameFormat("KT2005", 'T', "K"));
        Assert.assertTrue("valid format - KS205", NameValidity.VALID == SerialAddress.validSystemNameFormat("KS205", 'S', "K"));

        Assert.assertTrue("invalid format - KY2005", NameValidity.VALID != SerialAddress.validSystemNameFormat("KY2005", 'L', "K"));
        JUnitAppender.assertErrorMessage("invalid character in header field of system name: KY2005");

        Assert.assertTrue("valid format - KL1", NameValidity.VALID == SerialAddress.validSystemNameFormat("KL1", 'L', "K"));
        Assert.assertTrue("valid format - KL1000", NameValidity.VALID == SerialAddress.validSystemNameFormat("KL1000", 'L', "K"));

        // note: address format is invalid (out of range) as checked upon user input
        Assert.assertTrue("invalid format - KL0", NameValidity.VALID != SerialAddress.validSystemNameFormat("KL0", 'L', "K"));
        JUnitAppender.assertWarnMessage("node address field out of range in system name - KL0");

        Assert.assertTrue("valid format - KL2999", NameValidity.VALID == SerialAddress.validSystemNameFormat("KL2999", 'L', "K"));

        Assert.assertTrue("valid format - KL7999", NameValidity.VALID == SerialAddress.validSystemNameFormat("KL7999", 'L', "K"));

        Assert.assertTrue("invalid format - KL2oo5", NameValidity.VALID != SerialAddress.validSystemNameFormat("KL2oo5", 'L', "K"));
        JUnitAppender.assertWarnMessage("invalid character in number field of system name: KL2oo5");
        JUnitAppender.assertWarnMessage("node address field out of range in system name - KL2oo5");
    }

    @Test
    public void testGetBitFromSystemName() {
        Assert.assertEquals("KL2", 2, SerialAddress.getBitFromSystemName("KL2", "K"));
        Assert.assertEquals("KL2002", 2002, SerialAddress.getBitFromSystemName("KL2002", "K"));
        Assert.assertEquals("KL1", 1, SerialAddress.getBitFromSystemName("KL1", "K"));
        Assert.assertEquals("KL2001", 2001, SerialAddress.getBitFromSystemName("KL2001", "K"));
        Assert.assertEquals("KL999", 999, SerialAddress.getBitFromSystemName("KL999", "K"));
        Assert.assertEquals("KL2999", 2999, SerialAddress.getBitFromSystemName("KL2999", "K"));

        Assert.assertEquals("KL29O9", 0, SerialAddress.getBitFromSystemName("KL29O9", "K"));
        JUnitAppender.assertWarnMessage("invalid character in number field of system name: KL29O9");
    }

    @Test
    public void testValidSystemNameConfig() {
        InputBits.setNumInputBits(40);
        OutputBits.setNumOutputBits(201);
        Assert.assertTrue("valid config KL47", SerialAddress.validSystemNameConfig("KL47", 'L', memo));
        Assert.assertTrue("valid config KS17", SerialAddress.validSystemNameConfig("KS17", 'S', memo));
        Assert.assertTrue("valid config KL148", SerialAddress.validSystemNameConfig("KL148", 'L', memo));
        Assert.assertTrue("invalid config KL1049", !SerialAddress.validSystemNameConfig("KL1049", 'L', memo));
        JUnitAppender.assertWarnMessage("Maple hardware address out of range in system name: KL1049");
        Assert.assertTrue("valid config KS24", SerialAddress.validSystemNameConfig("KS24", 'S', memo));
        Assert.assertTrue("valid config KS40", SerialAddress.validSystemNameConfig("KS40", 'S', memo));
        Assert.assertTrue("invalid config KS41", !SerialAddress.validSystemNameConfig("KS41", 'S', memo));
        JUnitAppender.assertWarnMessage("Maple hardware address out of range in system name: KS41");
        Assert.assertTrue("invalid config KS0", !SerialAddress.validSystemNameConfig("KS0", 'S', memo));
        JUnitAppender.assertWarnMessage("node address field out of range in system name - KS0");
        Assert.assertTrue("valid config KT201", SerialAddress.validSystemNameConfig("KT201", 'T', memo));
        Assert.assertTrue("invalid config KT202", !SerialAddress.validSystemNameConfig("KT202", 'T', memo));
        JUnitAppender.assertWarnMessage("Maple hardware address out of range in system name: KT202");
        Assert.assertTrue("invalid config KT4129", !SerialAddress.validSystemNameConfig("KT4129", 'T', memo));
        JUnitAppender.assertWarnMessage("Maple hardware address out of range in system name: KT4129");
    }

    @Test
    public void testNormalizeSystemName() {
        Assert.assertEquals("normalize KL007", "KL7", SerialAddress.normalizeSystemName("KL007", "K"));
        Assert.assertEquals("normalize KL004007", "KL4007", SerialAddress.normalizeSystemName("KL004007", "K"));

        Assert.assertEquals("normalize KL12007", "", SerialAddress.normalizeSystemName("KL12007", "K"));
        JUnitAppender.assertWarnMessage("node address field out of range in system name - KL12007");
    }

    @Test
    public void testConstructSystemName() {
        Assert.assertEquals("make KL7", "KL7", SerialAddress.makeSystemName("L", 7, "K"));
        Assert.assertEquals("make KT7", "KT7", SerialAddress.makeSystemName("T", 7, "K"));
        Assert.assertEquals("make KS7", "KS7", SerialAddress.makeSystemName("S", 7, "K"));

        Assert.assertEquals("make illegal 1", "", SerialAddress.makeSystemName("L", 0, "K"));
        JUnitAppender.assertWarnMessage("illegal address range proposed for system name - 0");

        Assert.assertEquals("make illegal 2", "", SerialAddress.makeSystemName("L", 9990, "K"));
        JUnitAppender.assertWarnMessage("illegal address range proposed for system name - 9990");

        Assert.assertEquals("make illegal 3", "", SerialAddress.makeSystemName("R", 120, "K"));
        JUnitAppender.assertErrorMessage("illegal type character proposed for system name - R");

        Assert.assertEquals("make KS999", "KS999", SerialAddress.makeSystemName("S", 999, "K"));
        Assert.assertEquals("make KS1000", "KS1000", SerialAddress.makeSystemName("S", 1000, "K"));
    }

    @Test
    public void testIsOutputBitFree() {
        // create a new turnout
        jmri.TurnoutManager tMgr = memo.getTurnoutManager();
        jmri.Turnout t1 = tMgr.newTurnout("KT034", "userT34");
        // check that turnout was created correctly including normalizing system name
        Assert.assertEquals("create KT34 check 1", "KT34", t1.getSystemName());
        // create a new turnout
        jmri.Turnout t2 = tMgr.newTurnout("KT32", "userT32");
        // check that turnout was created correctly
        Assert.assertEquals("create KT32 check 1", "KT32", t2.getSystemName());
        // create two new lights  
        jmri.LightManager lMgr = memo.getLightManager();
        jmri.Light lgt1 = lMgr.newLight("KL36", "userL36");
        jmri.Light lgt2 = lMgr.newLight("KL037", "userL37");
        // check that the lights were created as expected
        Assert.assertEquals("create KL36 check", "KL36", lgt1.getSystemName());
        Assert.assertEquals("create KL37 check", "KL37", lgt2.getSystemName());
        // test
        Assert.assertEquals("test bit 30", "", SerialAddress.isOutputBitFree(30, "K"));
        Assert.assertEquals("test bit 34", "KT34", SerialAddress.isOutputBitFree(34, "K"));
        Assert.assertEquals("test bit 36", "KL36", SerialAddress.isOutputBitFree(36, "K"));
        Assert.assertEquals("test bit 37", "KL37", SerialAddress.isOutputBitFree(37, "K"));
        Assert.assertEquals("test bit 38", "", SerialAddress.isOutputBitFree(38, "K"));
        Assert.assertEquals("test bit 39", "", SerialAddress.isOutputBitFree(39, "K"));
        Assert.assertEquals("test bit 1000", "", SerialAddress.isOutputBitFree(1000, "K"));
    }

    @Test
    public void testIsInputBitFree() {
        jmri.SensorManager sMgr = memo.getSensorManager();
        // create 4 new sensors
        jmri.Sensor s1 = sMgr.newSensor("KS16", "userS16");
        jmri.Sensor s2 = sMgr.newSensor("KS014", "userS14");
        jmri.Sensor s3 = sMgr.newSensor("KS17", "userS17");
        jmri.Sensor s4 = sMgr.newSensor("KS12", "userS12");
        // check that the sensors were created as expected
        Assert.assertEquals("create KS16 check", "KS16", s1.getSystemName());
        Assert.assertEquals("create KS14 check", "KS14", s2.getSystemName());
        Assert.assertEquals("create KS17 check", "KS17", s3.getSystemName());
        Assert.assertEquals("create KS12 check", "KS12", s4.getSystemName());
        // test
        Assert.assertEquals("test bit 10", "", SerialAddress.isInputBitFree(10, "K"));
        Assert.assertEquals("test bit 11", "", SerialAddress.isInputBitFree(11, "K"));
        Assert.assertEquals("test bit 12", "KS12", SerialAddress.isInputBitFree(12, "K"));
        Assert.assertEquals("test bit 13", "", SerialAddress.isInputBitFree(13, "K"));
        Assert.assertEquals("test bit 14", "KS14", SerialAddress.isInputBitFree(14, "K"));
        Assert.assertEquals("test bit 15", "", SerialAddress.isInputBitFree(15, "K"));
        Assert.assertEquals("test bit 16", "KS16", SerialAddress.isInputBitFree(16, "K"));
        Assert.assertEquals("test bit 17", "KS17", SerialAddress.isInputBitFree(17, "K"));
        Assert.assertEquals("test bit 18", "", SerialAddress.isInputBitFree(18, "K"));
    }

    @Test
    public void testGetUserNameFromSystemName() {
        jmri.SensorManager sMgr = memo.getSensorManager();
        // create 4 new sensors
        sMgr.newSensor("KS16", "userS16");
        sMgr.newSensor("KS014", "userS14");
        sMgr.newSensor("KS17", "userS17");
        sMgr.newSensor("KS12", "userS12");

        jmri.LightManager lMgr = memo.getLightManager();
        lMgr.newLight("KL36", "userL36");
        lMgr.newLight("KL037", "userL37");

        jmri.TurnoutManager tMgr = memo.getTurnoutManager();
        tMgr.newTurnout("KT32", "userT32");
        tMgr.newTurnout("KT34", "userT34");

        Assert.assertEquals("test KS16", "userS16", SerialAddress.getUserNameFromSystemName("KS16", "K"));
        Assert.assertEquals("test KS12", "userS12", SerialAddress.getUserNameFromSystemName("KS12", "K"));
        Assert.assertEquals("test KS17", "userS17", SerialAddress.getUserNameFromSystemName("KS17", "K"));
        Assert.assertEquals("test undefined KS10", "", SerialAddress.getUserNameFromSystemName("KS10", "K"));
        Assert.assertEquals("test KL37", "userL37", SerialAddress.getUserNameFromSystemName("KL37", "K"));
        Assert.assertEquals("test KL36", "userL36", SerialAddress.getUserNameFromSystemName("KL36", "K"));
        Assert.assertEquals("test undefined KL30", "", SerialAddress.getUserNameFromSystemName("KL30", "K"));
        Assert.assertEquals("test KT32", "userT32", SerialAddress.getUserNameFromSystemName("KT32", "K"));
        Assert.assertEquals("test KT34", "userT34", SerialAddress.getUserNameFromSystemName("KT34", "K"));
        Assert.assertEquals("test undefined KT39", "", SerialAddress.getUserNameFromSystemName("KT39", "K"));
    }

    // from here down is testing infrastructure

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // create and register the manager objects
        SerialTrafficControlScaffold tc = new SerialTrafficControlScaffold();
        memo = new MapleSystemConnectionMemo("K", "Maple");
        memo.setTrafficController(tc);
        ibits = new InputBits(tc);
        obits = new OutputBits(tc);
        new SerialNode(4, 0,tc);
        new SerialNode(10, 0,tc);
        new SerialNode(99, 0,tc);
        new SerialNode(18, 0,tc);

        SerialTurnoutManager l = new SerialTurnoutManager(memo) {
            @Override
            public void notifyTurnoutCreationError(String conflict, int bitNum) {
            }
        };
        jmri.InstanceManager.setTurnoutManager(l);

        memo.setTurnoutManager(l);

        SerialLightManager lgt = new SerialLightManager(memo) {
            @Override
            public void notifyLightCreationError(String conflict, int bitNum) {
            }
        };
        jmri.InstanceManager.setLightManager(lgt);
        memo.setLightManager(lgt);

        SerialSensorManager s = new SerialSensorManager(memo);
        jmri.InstanceManager.setSensorManager(s);
        memo.setSensorManager(s);
    }

    private MapleSystemConnectionMemo memo = null;

    // The minimal setup for log4J
    @After
    public void tearDown() {
        memo = null;
        JUnitUtil.tearDown();
    }

}
