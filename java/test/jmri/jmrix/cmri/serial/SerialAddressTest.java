package jmri.jmrix.cmri.serial;

import jmri.Manager.NameValidity;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the serial address functions in memo.
 * <p>
 * These used to be in a separate SerialAddress class, with its own test class.
 * This structure is a vestige of that.
 *
 * @author	Dave Duchamp Copyright 2004
 * @author Bob Jacobsen Copyright 2017
 */
public class SerialAddressTest {

    private jmri.jmrix.cmri.CMRISystemConnectionMemo memo = null;
    private SerialTrafficControlScaffold stcs = null;

    SerialNode n10;
    SerialNode n18;

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();

        // replace the SerialTrafficController
        stcs = new SerialTrafficControlScaffold();
        memo = new jmri.jmrix.cmri.CMRISystemConnectionMemo();
        memo.setTrafficController(stcs);

        n10 = new SerialNode(10, SerialNode.SMINI, stcs);
        n18 = new SerialNode(18, SerialNode.SMINI, stcs);

        // create and register the manager objects
        jmri.TurnoutManager l = new SerialTurnoutManager(memo) {
            @Override
            public void notifyTurnoutCreationError(String conflict, int bitNum) {
            }
        };
        jmri.InstanceManager.setTurnoutManager(l);

        jmri.LightManager lgt = new SerialLightManager(memo) {
            @Override
            public void notifyLightCreationError(String conflict, int bitNum) {
            }
        };
        jmri.InstanceManager.setLightManager(lgt);

        jmri.SensorManager s = new SerialSensorManager(memo);
        jmri.InstanceManager.setSensorManager(s);

    }

    @After
    public void tearDown() throws Exception {
        if (stcs != null) stcs.terminateThreads();
        stcs = null;
        memo = null;
        n10 = null;
        n18 = null;
	    JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    @Test
    public void testValidSystemNameFormat() {
        Assert.assertTrue("valid format - CL2", NameValidity.VALID == memo.validSystemNameFormat("CL2", 'L'));
        Assert.assertTrue("valid format - CL0B2", NameValidity.VALID == memo.validSystemNameFormat("CL0B2", 'L'));

        Assert.assertTrue("invalid format - CL", NameValidity.VALID != memo.validSystemNameFormat("CL", 'L'));
//        JUnitAppender.assertWarnMessage("invalid character in number field of CMRI system name: CL");

        Assert.assertTrue("invalid format - CLB2", NameValidity.VALID != memo.validSystemNameFormat("CLB2", 'L'));
//        JUnitAppender.assertWarnMessage("no node address before 'B' in CMRI system name: CLB2");

        Assert.assertTrue("valid format - CL2005", NameValidity.VALID == memo.validSystemNameFormat("CL2005", 'L'));
        Assert.assertTrue("valid format - CL2B5", NameValidity.VALID == memo.validSystemNameFormat("CL2B5", 'L'));
        Assert.assertTrue("valid format - CT2005", NameValidity.VALID == memo.validSystemNameFormat("CT2005", 'T'));
        Assert.assertTrue("valid format - CT2B5", NameValidity.VALID == memo.validSystemNameFormat("CT2B5", 'T'));
        Assert.assertTrue("valid format - CS2005", NameValidity.VALID == memo.validSystemNameFormat("CS2005", 'S'));
        Assert.assertTrue("valid format - CS2B5", NameValidity.VALID == memo.validSystemNameFormat("CS2B5", 'S'));

        Assert.assertTrue("invalid format - CY2005", NameValidity.VALID != memo.validSystemNameFormat("CY2005", 'L'));
//        JUnitAppender.assertErrorMessage("invalid type character in CMRI system name: CY2005");

        Assert.assertTrue("invalid format - CY2B5", NameValidity.VALID != memo.validSystemNameFormat("CY2B5", 'L'));
//        JUnitAppender.assertErrorMessage("invalid type character in CMRI system name: CY2B5");

        Assert.assertTrue("valid format - CL22001", NameValidity.VALID == memo.validSystemNameFormat("CL22001", 'L'));
        Assert.assertTrue("valid format - CL22B1", NameValidity.VALID == memo.validSystemNameFormat("CL22B1", 'L'));

        Assert.assertTrue("invalid format - CL22000", NameValidity.VALID != memo.validSystemNameFormat("CL22000", 'L'));
//        JUnitAppender.assertWarnMessage("bit number not in range 1 - 999 in CMRI system name: CL22000");

        Assert.assertTrue("invalid format - CL22B0", NameValidity.VALID != memo.validSystemNameFormat("CL22B0", 'L'));
//        JUnitAppender.assertWarnMessage("bit number field out of range in CMRI system name: CL22B0");

        Assert.assertTrue("valid format - CL2999", NameValidity.VALID == memo.validSystemNameFormat("CL2999", 'L'));
        Assert.assertTrue("valid format - CL2B2048", NameValidity.VALID == memo.validSystemNameFormat("CL2B2048", 'L'));

        Assert.assertTrue("invalid format - CL2B2049", NameValidity.VALID != memo.validSystemNameFormat("CL2B2049", 'L'));
//        JUnitAppender.assertWarnMessage("bit number field out of range in CMRI system name: CL2B2049");

        Assert.assertTrue("valid format - CL127999", NameValidity.VALID == memo.validSystemNameFormat("CL127999", 'L'));

        Assert.assertTrue("invalid format - CL128000", NameValidity.VALID != memo.validSystemNameFormat("CL128000", 'L'));
//        JUnitAppender.assertWarnMessage("number field out of range in CMRI system name: CL128000");

        Assert.assertTrue("valid format - CL127B7", NameValidity.VALID == memo.validSystemNameFormat("CL127B7", 'L'));

        Assert.assertTrue("invalid format - CL128B7", NameValidity.VALID != memo.validSystemNameFormat("CL128B7", 'L'));
//        JUnitAppender.assertWarnMessage("node address field out of range in CMRI system name: CL128B7");

        Assert.assertTrue("invalid format - CL2oo5", NameValidity.VALID != memo.validSystemNameFormat("CL2oo5", 'L'));
//        JUnitAppender.assertWarnMessage("invalid character in number field of CMRI system name: CL2oo5");

        Assert.assertTrue("invalid format - CL2aB5", NameValidity.VALID != memo.validSystemNameFormat("CL2aB5", 'L'));
//        JUnitAppender.assertWarnMessage("invalid character in node address field of CMRI system name: CL2aB5");

        Assert.assertTrue("invalid format - CL2B5x", NameValidity.VALID != memo.validSystemNameFormat("CL2B5x", 'L'));
//        JUnitAppender.assertWarnMessage("invalid character in bit number field of CMRI system name: CL2B5x");
    }

    @Test
    public void testGetBitFromSystemName() {
        Assert.assertEquals("CL2", 2, memo.getBitFromSystemName("CL2"));
        Assert.assertEquals("CL2002", 2, memo.getBitFromSystemName("CL2002"));
        Assert.assertEquals("CL1", 1, memo.getBitFromSystemName("CL1"));
        Assert.assertEquals("CL2001", 1, memo.getBitFromSystemName("CL2001"));
        Assert.assertEquals("CL999", 999, memo.getBitFromSystemName("CL999"));
        Assert.assertEquals("CL2999", 999, memo.getBitFromSystemName("CL2999"));

        Assert.assertEquals("CL29O9", 0, memo.getBitFromSystemName("CL29O9"));
//        JUnitAppender.assertWarnMessage("invalid character in number field of system name: CL29O9");

        Assert.assertEquals("CL0B7", 7, memo.getBitFromSystemName("CL0B7"));
        Assert.assertEquals("CL2B7", 7, memo.getBitFromSystemName("CL2B7"));
        Assert.assertEquals("CL0B1", 1, memo.getBitFromSystemName("CL0B1"));
        Assert.assertEquals("CL2B1", 1, memo.getBitFromSystemName("CL2B1"));
        Assert.assertEquals("CL0B2048", 2048, memo.getBitFromSystemName("CL0B2048"));
        Assert.assertEquals("CL11B2048", 2048, memo.getBitFromSystemName("CL11B2048"));
    }

    @Test
    public void testGetNodeFromSystemName() {
        SerialNode d = new SerialNode(14, SerialNode.USIC_SUSIC, stcs);
        SerialNode c = new SerialNode(17, SerialNode.SMINI, stcs);
        SerialNode b = new SerialNode(127, SerialNode.SMINI, stcs);
        Assert.assertEquals("node of CL14007", d, memo.getNodeFromSystemName("CL14007", stcs));
        Assert.assertEquals("node of CL14B7", d, memo.getNodeFromSystemName("CL14B7", stcs));
        Assert.assertEquals("node of CL127007", b, memo.getNodeFromSystemName("CL127007", stcs));
        Assert.assertEquals("node of CL127B7", b, memo.getNodeFromSystemName("CL127B7", stcs));
        Assert.assertEquals("node of CL17007", c, memo.getNodeFromSystemName("CL17007", stcs));
        Assert.assertEquals("node of CL17B7", c, memo.getNodeFromSystemName("CL17B7", stcs));
        Assert.assertEquals("node of CL11007", null, memo.getNodeFromSystemName("CL11007", stcs));
        Assert.assertEquals("node of CL11B7", null, memo.getNodeFromSystemName("CL11B7", stcs));
    }

    @Test
    public void testGetNodeAddressFromSystemName() {
        Assert.assertEquals("CL14007", 14, memo.getNodeAddressFromSystemName("CL14007"));
        Assert.assertEquals("CL14B7", 14, memo.getNodeAddressFromSystemName("CL14B7"));
        Assert.assertEquals("CL127007", 127, memo.getNodeAddressFromSystemName("CL127007"));
        Assert.assertEquals("CL127B7", 127, memo.getNodeAddressFromSystemName("CL127B7"));
        Assert.assertEquals("CL0B7", 0, memo.getNodeAddressFromSystemName("CL0B7"));
        Assert.assertEquals("CL7", 0, memo.getNodeAddressFromSystemName("CL7"));

        Assert.assertEquals("CLB7", -1, memo.getNodeAddressFromSystemName("CLB7"));
        JUnitAppender.assertWarnMessage("no node address before 'B' in CMRI system name: CLB7");

        Assert.assertEquals("CR7", -1, memo.getNodeAddressFromSystemName("CR7"));
        JUnitAppender.assertErrorMessage("invalid character in header field of system name: CR7");
    }

    @Test
    public void testValidSystemNameConfig() {
        SerialNode d = new SerialNode(4, SerialNode.USIC_SUSIC, stcs);
        d.setNumBitsPerCard(32);
        d.setCardTypeByAddress(0, SerialNode.INPUT_CARD);
        d.setCardTypeByAddress(1, SerialNode.OUTPUT_CARD);
        d.setCardTypeByAddress(2, SerialNode.OUTPUT_CARD);
        d.setCardTypeByAddress(3, SerialNode.OUTPUT_CARD);
        d.setCardTypeByAddress(4, SerialNode.INPUT_CARD);
        d.setCardTypeByAddress(5, SerialNode.OUTPUT_CARD);

        SerialNode c = new SerialNode(10, SerialNode.SMINI, stcs);
        Assert.assertNotNull("exists", c);
        Assert.assertTrue("valid config CL4007", memo.validSystemNameConfig("CL4007", 'L', stcs));
        Assert.assertTrue("valid config CL4B7", memo.validSystemNameConfig("CL4B7", 'L', stcs));
        Assert.assertTrue("valid config CS10007", memo.validSystemNameConfig("CS10007", 'S', stcs));
        Assert.assertTrue("valid config CS10B7", memo.validSystemNameConfig("CS10B7", 'S', stcs));
        Assert.assertTrue("valid config CL10048", memo.validSystemNameConfig("CL10048", 'L', stcs));
        Assert.assertTrue("valid config CL10B48", memo.validSystemNameConfig("CL10B48", 'L', stcs));
        Assert.assertTrue("invalid config CL10049", !memo.validSystemNameConfig("CL10049", 'L', stcs));
        Assert.assertTrue("invalid config CL10B49", !memo.validSystemNameConfig("CL10B49", 'L', stcs));
        Assert.assertTrue("valid config CS10024", memo.validSystemNameConfig("CS10024", 'S', stcs));
        Assert.assertTrue("valid config CS10B24", memo.validSystemNameConfig("CS10B24", 'S', stcs));
        Assert.assertTrue("invalid config CS10025", !memo.validSystemNameConfig("CS10025", 'S', stcs));
        Assert.assertTrue("invalid config CS10B25", !memo.validSystemNameConfig("CS10B25", 'S', stcs));
        Assert.assertTrue("valid config CT4128", memo.validSystemNameConfig("CT4128", 'T', stcs));
        Assert.assertTrue("valid config CT4B128", memo.validSystemNameConfig("CT4B128", 'T', stcs));
        Assert.assertTrue("invalid config CT4129", !memo.validSystemNameConfig("CT4129", 'T', stcs));
        Assert.assertTrue("invalid config CT4129", !memo.validSystemNameConfig("CT4B129", 'T', stcs));
        Assert.assertTrue("valid config CS4064", memo.validSystemNameConfig("CS4064", 'S', stcs));
        Assert.assertTrue("valid config CS4B64", memo.validSystemNameConfig("CS4B64", 'S', stcs));
        Assert.assertTrue("invalid config CS4065", !memo.validSystemNameConfig("CS4065", 'S', stcs));
        Assert.assertTrue("invalid config CS4B65", !memo.validSystemNameConfig("CS4B65", 'S', stcs));
        Assert.assertTrue("invalid config CL11007", !memo.validSystemNameConfig("CL11007", 'L', stcs));
        Assert.assertTrue("invalid config CL11B7", !memo.validSystemNameConfig("CL11B7", 'L', stcs));
    }

    @Test
    public void testConvertSystemNameFormat() {
        Assert.assertEquals("convert CL14007", "CL14B7", memo.convertSystemNameToAlternate("CL14007"));
        Assert.assertEquals("convert CS7", "CS0B7", memo.convertSystemNameToAlternate("CS7"));
        Assert.assertEquals("convert CT4007", "CT4B7", memo.convertSystemNameToAlternate("CT4007"));
        Assert.assertEquals("convert CL14B7", "CL14007", memo.convertSystemNameToAlternate("CL14B7"));
        Assert.assertEquals("convert CL0B7", "CL7", memo.convertSystemNameToAlternate("CL0B7"));
        Assert.assertEquals("convert CS4B7", "CS4007", memo.convertSystemNameToAlternate("CS4B7"));
        Assert.assertEquals("convert CL14B8", "CL14008", memo.convertSystemNameToAlternate("CL14B8"));

        Assert.assertEquals("convert CL128B7", "", memo.convertSystemNameToAlternate("CL128B7"));
//        JUnitAppender.assertWarnMessage("node address field out of range in CMRI system name: CL128B7");
    }

    @Test
    public void testNormalizeSystemName() {
        Assert.assertEquals("normalize CL14007", "CL14007", memo.normalizeSystemName("CL14007"));
        Assert.assertEquals("normalize CL007", "CL7", memo.normalizeSystemName("CL007"));
        Assert.assertEquals("normalize CL004007", "CL4007", memo.normalizeSystemName("CL004007"));
        Assert.assertEquals("normalize CL14B7", "CL14B7", memo.normalizeSystemName("CL14B7"));
        Assert.assertEquals("normalize CL0B7", "CL0B7", memo.normalizeSystemName("CL0B7"));
        Assert.assertEquals("normalize CL004B7", "CL4B7", memo.normalizeSystemName("CL004B7"));
        Assert.assertEquals("normalize CL014B0008", "CL14B8", memo.normalizeSystemName("CL014B0008"));

        Assert.assertEquals("normalize CL128B7", "", memo.normalizeSystemName("CL128B7"));
//        JUnitAppender.assertWarnMessage("node address field out of range in CMRI system name: CL128B7");
    }

    @Test
    public void testConstructSystemName() {
        Assert.assertEquals("make CL14007", "CL14007", memo.makeSystemName("L", 14, 7));
        Assert.assertEquals("make CT7", "CT7", memo.makeSystemName("T", 0, 7));

        Assert.assertEquals("make invalid 1", "", memo.makeSystemName("L", 0, 0));
        JUnitAppender.assertWarnMessage("invalid bit number proposed for system name");

        Assert.assertEquals("make invalid 2", "", memo.makeSystemName("L", 128, 7));
        JUnitAppender.assertWarnMessage("invalid node address proposed for system name");

        Assert.assertEquals("make invalid 3", "", memo.makeSystemName("R", 120, 7));
        JUnitAppender.assertErrorMessage("invalid type character proposed for system name");

        Assert.assertEquals("make CL0B1770", "CL0B1770", memo.makeSystemName("L", 0, 1770));
        Assert.assertEquals("make CS127999", "CS127999", memo.makeSystemName("S", 127, 999));
        Assert.assertEquals("make CS14B1000", "CS14B1000", memo.makeSystemName("S", 14, 1000));
    }

    @Test
    public void testIsOutputBitFree() {
        // create a new turnout, controlled by two output bits
        jmri.TurnoutManager tMgr = jmri.InstanceManager.turnoutManagerInstance();
        jmri.Turnout t1 = tMgr.newTurnout("CT18034", "userT34");
        t1.setNumberOutputBits(2);
        // check that turnout was created correctly
        Assert.assertEquals("create CT18034 check 1", "CT18034", t1.getSystemName());
        Assert.assertEquals("create CT18034 check 2", 2, t1.getNumberOutputBits());
        // create a new turnout, controlled by one output bit
        jmri.Turnout t2 = tMgr.newTurnout("CT18032", "userT32");
        // check that turnout was created correctly
        Assert.assertEquals("create CT18032 check 1", "CT18032", t2.getSystemName());
        Assert.assertEquals("create CT18032 check 2", 1, t2.getNumberOutputBits());
        // create two new lights
        jmri.LightManager lMgr = jmri.InstanceManager.lightManagerInstance();
        jmri.Light lgt1 = lMgr.newLight("CL18036", "userL36");
        jmri.Light lgt2 = lMgr.newLight("CL18037", "userL37");
        // check that the lights were created as expected
        Assert.assertEquals("create CL18036 check", "CL18036", lgt1.getSystemName());
        Assert.assertEquals("create CL18037 check", "CL18037", lgt2.getSystemName());
        // test
        Assert.assertEquals("test bit 30", "", memo.isOutputBitFree(18, 30));
        Assert.assertEquals("test bit 34", "CT18034", memo.isOutputBitFree(18, 34));
        Assert.assertEquals("test bit 33", "", memo.isOutputBitFree(18, 33));
        Assert.assertEquals("test bit 35", "CT18034", memo.isOutputBitFree(18, 35));
        Assert.assertEquals("test bit 36", "CL18036", memo.isOutputBitFree(18, 36));
        Assert.assertEquals("test bit 37", "CL18037", memo.isOutputBitFree(18, 37));
        Assert.assertEquals("test bit 38", "", memo.isOutputBitFree(18, 38));
        Assert.assertEquals("test bit 39", "", memo.isOutputBitFree(18, 39));
        Assert.assertEquals("test bit 2000", "", memo.isOutputBitFree(18, 2000));

        Assert.assertEquals("test bit bad bit", "", memo.isOutputBitFree(18, 0));
        JUnitAppender.assertWarnMessage("invalid bit number in free bit test");

        Assert.assertEquals("test bit bad node address", "", memo.isOutputBitFree(129, 34));
        JUnitAppender.assertWarnMessage("invalid node address in free bit test");
    }

    @Test
    public void testIsInputBitFree() {
        jmri.SensorManager sMgr = jmri.InstanceManager.sensorManagerInstance();
        // create 4 new sensors
        jmri.Sensor s1 = sMgr.newSensor("CS18016", "userS16");
        jmri.Sensor s2 = sMgr.newSensor("CS18014", "userS14");
        jmri.Sensor s3 = sMgr.newSensor("CS18017", "userS17");
        jmri.Sensor s4 = sMgr.newSensor("CS18012", "userS12");
        // check that the sensors were created as expected
        Assert.assertEquals("create CS18016 check", "CS18016", s1.getSystemName());
        Assert.assertEquals("create CS18014 check", "CS18014", s2.getSystemName());
        Assert.assertEquals("create CS18017 check", "CS18017", s3.getSystemName());
        Assert.assertEquals("create CS18012 check", "CS18012", s4.getSystemName());
        // test
        Assert.assertEquals("test bit 10", "", memo.isInputBitFree(18, 10));
        Assert.assertEquals("test bit 11", "", memo.isInputBitFree(18, 11));
        Assert.assertEquals("test bit 12", "CS18012", memo.isInputBitFree(18, 12));
        Assert.assertEquals("test bit 13", "", memo.isInputBitFree(18, 13));
        Assert.assertEquals("test bit 14", "CS18014", memo.isInputBitFree(18, 14));
        Assert.assertEquals("test bit 15", "", memo.isInputBitFree(18, 15));
        Assert.assertEquals("test bit 16", "CS18016", memo.isInputBitFree(18, 16));
        Assert.assertEquals("test bit 17", "CS18017", memo.isInputBitFree(18, 17));
        Assert.assertEquals("test bit 18", "", memo.isInputBitFree(18, 18));

        Assert.assertEquals("test bit bad bit", "", memo.isInputBitFree(18, 0));
        JUnitAppender.assertWarnMessage("invalid bit number in free bit test");

        Assert.assertEquals("test bit bad node address", "", memo.isInputBitFree(129, 34));
        JUnitAppender.assertWarnMessage("invalid node address in free bit test");
    }

    @Test
    public void testGetUserNameFromSystemName() {
        jmri.SensorManager sMgr = jmri.InstanceManager.sensorManagerInstance();
        // create 4 new sensors
        sMgr.newSensor("CS18016", "userS16");
        sMgr.newSensor("CS18014", "userS14");
        sMgr.newSensor("CS18017", "userS17");
        sMgr.newSensor("CS18012", "userS12");

        jmri.LightManager lMgr = jmri.InstanceManager.lightManagerInstance();
        lMgr.newLight("CL18036", "userL36");
        lMgr.newLight("CL18037", "userL37");

        jmri.TurnoutManager tMgr = jmri.InstanceManager.turnoutManagerInstance();
        tMgr.newTurnout("CT18032", "userT32");
        tMgr.newTurnout("CT18034", "userT34");

        Assert.assertEquals("test CS18016", "userS16", memo.getUserNameFromSystemName("CS18016"));
        Assert.assertEquals("test CS18012", "userS12", memo.getUserNameFromSystemName("CS18012"));
        Assert.assertEquals("test CS18017", "userS17", memo.getUserNameFromSystemName("CS18017"));
        Assert.assertEquals("test undefined CS18010", "", memo.getUserNameFromSystemName("CS18010"));
        Assert.assertEquals("test CL18037", "userL37", memo.getUserNameFromSystemName("CL18037"));
        Assert.assertEquals("test CL18036", "userL36", memo.getUserNameFromSystemName("CL18036"));
        Assert.assertEquals("test undefined CL18030", "", memo.getUserNameFromSystemName("CL18030"));
        Assert.assertEquals("test CT18032", "userT32", memo.getUserNameFromSystemName("CT18032"));
        Assert.assertEquals("test CT18034", "userT34", memo.getUserNameFromSystemName("CT18034"));
        Assert.assertEquals("test undefined CT18039", "", memo.getUserNameFromSystemName("CT18039"));
    }

}
