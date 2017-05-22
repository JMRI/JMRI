package jmri.jmrix.cmri.serial;

import jmri.util.JUnitAppender;
import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the serial address functions in SerialTrafficController.
 * 
 * These used to be in a separate SerialAddress class, with its own test class.
 * This structure is a vestige of that.
 *
 * @author	Dave Duchamp   Copyright 2004
 * @author Bob Jacobsen    Copyright 2017
 */
public class SerialAddressTwoSystemTest extends TestCase {

    private jmri.jmrix.cmri.CMRISystemConnectionMemo memo1 = null;
    private SerialTrafficControlScaffold stcs1 = null;

    SerialNode c10;
    SerialNode c18;

    private jmri.jmrix.cmri.CMRISystemConnectionMemo memo2 = null;
    private SerialTrafficControlScaffold stcs2 = null;

    SerialNode k10;
    SerialNode k20;

    @Override
    public void setUp() throws Exception {
        // log4j
        apps.tests.Log4JFixture.setUp();
        super.setUp();

        jmri.util.JUnitUtil.resetInstanceManager();

        // replace the 1st SerialTrafficController
        stcs1 = new SerialTrafficControlScaffold();
        memo1 = new jmri.jmrix.cmri.CMRISystemConnectionMemo();
        memo1.setTrafficController(stcs1);
        // create 1st nodes
        c10 = new SerialNode(10, SerialNode.SMINI,stcs1);
        c18 = new SerialNode(18, SerialNode.SMINI,stcs1);
        // create and register the 1st manager objects
        jmri.TurnoutManager l1 = new SerialTurnoutManager(memo1) {
            @Override
            public void notifyTurnoutCreationError(String conflict, int bitNum) {}
        };
        jmri.InstanceManager.setTurnoutManager(l1);
        jmri.LightManager lgt1 = new SerialLightManager(memo1) {
            @Override
            public void notifyLightCreationError(String conflict, int bitNum) {}
        };
        jmri.InstanceManager.setLightManager(lgt1);
        jmri.SensorManager s1 = new SerialSensorManager(memo1);
        jmri.InstanceManager.setSensorManager(s1);

        // replace the 2nd SerialTrafficController
        stcs2 = new SerialTrafficControlScaffold();
        memo2 = new jmri.jmrix.cmri.CMRISystemConnectionMemo("K", "CMRI2");
        memo2.setTrafficController(stcs1);
        // create 2nd nodes
        k10 = new SerialNode(10, SerialNode.SMINI,stcs2);
        k20 = new SerialNode(20, SerialNode.SMINI,stcs2);
        // create and register the 1st manager objects
        jmri.TurnoutManager l2 = new SerialTurnoutManager(memo2) {
            @Override
            public void notifyTurnoutCreationError(String conflict, int bitNum) {}
        };
        jmri.InstanceManager.setTurnoutManager(l2);
        jmri.LightManager lgt2 = new SerialLightManager(memo2) {
            @Override
            public void notifyLightCreationError(String conflict, int bitNum) {}
        };
        jmri.InstanceManager.setLightManager(lgt2);
        jmri.SensorManager s2 = new SerialSensorManager(memo2);
        jmri.InstanceManager.setSensorManager(s2);

    }

    // The minimal setup for log4J
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
        jmri.util.JUnitUtil.resetInstanceManager();
        stcs1 = null;
        memo1 = null;
    }


    public void testValidateSystemNameFormat() {
        Assert.assertTrue("valid format - CL2", SerialTrafficController.validSystemNameFormat("CL2", 'L'));
        Assert.assertTrue("valid format - CL0B2", SerialTrafficController.validSystemNameFormat("CL0B2", 'L'));

        Assert.assertTrue("invalid format - CL", !SerialTrafficController.validSystemNameFormat("CL", 'L'));
        JUnitAppender.assertErrorMessage("illegal character in number field of CMRI system name: CL");

        Assert.assertTrue("invalid format - CLB2", !SerialTrafficController.validSystemNameFormat("CLB2", 'L'));
        JUnitAppender.assertErrorMessage("no node address before 'B' in CMRI system name: CLB2");

        Assert.assertTrue("valid format - CL2005", SerialTrafficController.validSystemNameFormat("CL2005", 'L'));
        Assert.assertTrue("valid format - CL2B5", SerialTrafficController.validSystemNameFormat("CL2B5", 'L'));
        Assert.assertTrue("valid format - CT2005", SerialTrafficController.validSystemNameFormat("CT2005", 'T'));
        Assert.assertTrue("valid format - CT2B5", SerialTrafficController.validSystemNameFormat("CT2B5", 'T'));
        Assert.assertTrue("valid format - CS2005", SerialTrafficController.validSystemNameFormat("CS2005", 'S'));
        Assert.assertTrue("valid format - CS2B5", SerialTrafficController.validSystemNameFormat("CS2B5", 'S'));

        Assert.assertTrue("invalid format - CY2005", !SerialTrafficController.validSystemNameFormat("CY2005", 'L'));
        JUnitAppender.assertErrorMessage("illegal type character in CMRI system name: CY2005");

        Assert.assertTrue("invalid format - CY2B5", !SerialTrafficController.validSystemNameFormat("CY2B5", 'L'));
        JUnitAppender.assertErrorMessage("illegal type character in CMRI system name: CY2B5");

        Assert.assertTrue("valid format - CL22001", SerialTrafficController.validSystemNameFormat("CL22001", 'L'));
        Assert.assertTrue("valid format - CL22B1", SerialTrafficController.validSystemNameFormat("CL22B1", 'L'));

        Assert.assertTrue("invalid format - CL22000", !SerialTrafficController.validSystemNameFormat("CL22000", 'L'));
        JUnitAppender.assertErrorMessage("bit number not in range 1 - 999 in CMRI system name: CL22000");

        Assert.assertTrue("invalid format - CL22B0", !SerialTrafficController.validSystemNameFormat("CL22B0", 'L'));
        JUnitAppender.assertErrorMessage("bit number field out of range in CMRI system name: CL22B0");

        Assert.assertTrue("valid format - CL2999", SerialTrafficController.validSystemNameFormat("CL2999", 'L'));
        Assert.assertTrue("valid format - CL2B2048", SerialTrafficController.validSystemNameFormat("CL2B2048", 'L'));

        Assert.assertTrue("invalid format - CL2B2049", !SerialTrafficController.validSystemNameFormat("CL2B2049", 'L'));
        JUnitAppender.assertErrorMessage("bit number field out of range in CMRI system name: CL2B2049");

        Assert.assertTrue("valid format - CL127999", SerialTrafficController.validSystemNameFormat("CL127999", 'L'));

        Assert.assertTrue("invalid format - CL128000", !SerialTrafficController.validSystemNameFormat("CL128000", 'L'));
        JUnitAppender.assertErrorMessage("number field out of range in CMRI system name: CL128000");

        Assert.assertTrue("valid format - CL127B7", SerialTrafficController.validSystemNameFormat("CL127B7", 'L'));

        Assert.assertTrue("invalid format - CL128B7", !SerialTrafficController.validSystemNameFormat("CL128B7", 'L'));
        JUnitAppender.assertErrorMessage("node address field out of range in CMRI system name: CL128B7");

        Assert.assertTrue("invalid format - CL2oo5", !SerialTrafficController.validSystemNameFormat("CL2oo5", 'L'));
        JUnitAppender.assertErrorMessage("illegal character in number field of CMRI system name: CL2oo5");

        Assert.assertTrue("invalid format - CL2aB5", !SerialTrafficController.validSystemNameFormat("CL2aB5", 'L'));
        JUnitAppender.assertErrorMessage("illegal character in node address field of CMRI system name: CL2aB5");

        Assert.assertTrue("invalid format - CL2B5x", !SerialTrafficController.validSystemNameFormat("CL2B5x", 'L'));
        JUnitAppender.assertErrorMessage("illegal character in bit number field of CMRI system name: CL2B5x");
    }

    public void testGetBitFromSystemName() {
        Assert.assertEquals("CL2", 2, SerialTrafficController.getBitFromSystemName("CL2"));
        Assert.assertEquals("CL2002", 2, SerialTrafficController.getBitFromSystemName("CL2002"));
        Assert.assertEquals("CL1", 1, SerialTrafficController.getBitFromSystemName("CL1"));
        Assert.assertEquals("CL2001", 1, SerialTrafficController.getBitFromSystemName("CL2001"));
        Assert.assertEquals("CL999", 999, SerialTrafficController.getBitFromSystemName("CL999"));
        Assert.assertEquals("CL2999", 999, SerialTrafficController.getBitFromSystemName("CL2999"));

        Assert.assertEquals("CL29O9", 0, SerialTrafficController.getBitFromSystemName("CL29O9"));
        JUnitAppender.assertErrorMessage("illegal character in number field of system name: CL29O9");

        Assert.assertEquals("CL0B7", 7, SerialTrafficController.getBitFromSystemName("CL0B7"));
        Assert.assertEquals("CL2B7", 7, SerialTrafficController.getBitFromSystemName("CL2B7"));
        Assert.assertEquals("CL0B1", 1, SerialTrafficController.getBitFromSystemName("CL0B1"));
        Assert.assertEquals("CL2B1", 1, SerialTrafficController.getBitFromSystemName("CL2B1"));
        Assert.assertEquals("CL0B2048", 2048, SerialTrafficController.getBitFromSystemName("CL0B2048"));
        Assert.assertEquals("CL11B2048", 2048, SerialTrafficController.getBitFromSystemName("CL11B2048"));
    }


    public void testGetNodeFromSystemName() {
        SerialNode d = new SerialNode(14, SerialNode.USIC_SUSIC,stcs1);
        SerialNode c = new SerialNode(17, SerialNode.SMINI,stcs1);
        SerialNode b = new SerialNode(127, SerialNode.SMINI,stcs1);
        Assert.assertEquals("node of CL14007", d, SerialTrafficController.getNodeFromSystemName("CL14007",stcs1));
        Assert.assertEquals("node of CL14B7", d, SerialTrafficController.getNodeFromSystemName("CL14B7",stcs1));
        Assert.assertEquals("node of CL127007", b, SerialTrafficController.getNodeFromSystemName("CL127007",stcs1));
        Assert.assertEquals("node of CL127B7", b, SerialTrafficController.getNodeFromSystemName("CL127B7",stcs1));
        Assert.assertEquals("node of CL17007", c, SerialTrafficController.getNodeFromSystemName("CL17007",stcs1));
        Assert.assertEquals("node of CL17B7", c, SerialTrafficController.getNodeFromSystemName("CL17B7",stcs1));
        Assert.assertEquals("node of CL11007", null, SerialTrafficController.getNodeFromSystemName("CL11007",stcs1));
        Assert.assertEquals("node of CL11B7", null, SerialTrafficController.getNodeFromSystemName("CL11B7",stcs1));
    }

    public void testGetNodeFromSystemName2() {
        SerialNode d = new SerialNode(14, SerialNode.USIC_SUSIC,stcs2);
        SerialNode c = new SerialNode(17, SerialNode.SMINI,stcs2);
        SerialNode b = new SerialNode(127, SerialNode.SMINI,stcs2);
        Assert.assertEquals("node of KL14007", d, SerialTrafficController.getNodeFromSystemName("KL14007",stcs1));
        Assert.assertEquals("node of KL14B7", d, SerialTrafficController.getNodeFromSystemName("KL14B7",stcs1));
        Assert.assertEquals("node of KL127007", b, SerialTrafficController.getNodeFromSystemName("KL127007",stcs1));
        Assert.assertEquals("node of KL127B7", b, SerialTrafficController.getNodeFromSystemName("KL127B7",stcs1));
        Assert.assertEquals("node of KL17007", c, SerialTrafficController.getNodeFromSystemName("KL17007",stcs1));
        Assert.assertEquals("node of KL17B7", c, SerialTrafficController.getNodeFromSystemName("KL17B7",stcs1));
        Assert.assertEquals("node of KL11007", null, SerialTrafficController.getNodeFromSystemName("KL11007",stcs1));
        Assert.assertEquals("node of KL11B7", null, SerialTrafficController.getNodeFromSystemName("KL11B7",stcs1));
    }

    public void testGetNodeAddressFromSystemName() {
        Assert.assertEquals("CL14007", 14, SerialTrafficController.getNodeAddressFromSystemName("CL14007"));
        Assert.assertEquals("CL14B7", 14, SerialTrafficController.getNodeAddressFromSystemName("CL14B7"));
        Assert.assertEquals("CL127007", 127, SerialTrafficController.getNodeAddressFromSystemName("CL127007"));
        Assert.assertEquals("CL127B7", 127, SerialTrafficController.getNodeAddressFromSystemName("CL127B7"));
        Assert.assertEquals("CL0B7", 0, SerialTrafficController.getNodeAddressFromSystemName("CL0B7"));
        Assert.assertEquals("CL7", 0, SerialTrafficController.getNodeAddressFromSystemName("CL7"));

        Assert.assertEquals("CLB7", -1, SerialTrafficController.getNodeAddressFromSystemName("CLB7"));
        JUnitAppender.assertErrorMessage("no node address before 'B' in CMRI system name: CLB7");

        Assert.assertEquals("CR7", -1, SerialTrafficController.getNodeAddressFromSystemName("CR7"));
        JUnitAppender.assertErrorMessage("illegal character in header field of system name: CR7");
    }

    public void testValidSystemNameConfig() {
        SerialNode d = new SerialNode(4, SerialNode.USIC_SUSIC,stcs1);
        d.setNumBitsPerCard(32);
        d.setCardTypeByAddress(0, SerialNode.INPUT_CARD);
        d.setCardTypeByAddress(1, SerialNode.OUTPUT_CARD);
        d.setCardTypeByAddress(2, SerialNode.OUTPUT_CARD);
        d.setCardTypeByAddress(3, SerialNode.OUTPUT_CARD);
        d.setCardTypeByAddress(4, SerialNode.INPUT_CARD);
        d.setCardTypeByAddress(5, SerialNode.OUTPUT_CARD);
        
        SerialNode c = new SerialNode(10, SerialNode.SMINI,stcs1);
        Assert.assertNotNull("exists", c);
        Assert.assertTrue("valid config CL4007", SerialTrafficController.validSystemNameConfig("CL4007", 'L',stcs1));
        Assert.assertTrue("valid config CL4B7", SerialTrafficController.validSystemNameConfig("CL4B7", 'L',stcs1));
        Assert.assertTrue("valid config CS10007", SerialTrafficController.validSystemNameConfig("CS10007", 'S',stcs1));
        Assert.assertTrue("valid config CS10B7", SerialTrafficController.validSystemNameConfig("CS10B7", 'S',stcs1));
        Assert.assertTrue("valid config CL10048", SerialTrafficController.validSystemNameConfig("CL10048", 'L',stcs1));
        Assert.assertTrue("valid config CL10B48", SerialTrafficController.validSystemNameConfig("CL10B48", 'L',stcs1));
        Assert.assertTrue("invalid config CL10049", !SerialTrafficController.validSystemNameConfig("CL10049", 'L',stcs1));
        Assert.assertTrue("invalid config CL10B49", !SerialTrafficController.validSystemNameConfig("CL10B49", 'L',stcs1));
        Assert.assertTrue("valid config CS10024", SerialTrafficController.validSystemNameConfig("CS10024", 'S',stcs1));
        Assert.assertTrue("valid config CS10B24", SerialTrafficController.validSystemNameConfig("CS10B24", 'S',stcs1));
        Assert.assertTrue("invalid config CS10025", !SerialTrafficController.validSystemNameConfig("CS10025", 'S',stcs1));
        Assert.assertTrue("invalid config CS10B25", !SerialTrafficController.validSystemNameConfig("CS10B25", 'S',stcs1));
        Assert.assertTrue("valid config CT4128", SerialTrafficController.validSystemNameConfig("CT4128", 'T',stcs1));
        Assert.assertTrue("valid config CT4B128", SerialTrafficController.validSystemNameConfig("CT4B128", 'T',stcs1));
        Assert.assertTrue("invalid config CT4129", !SerialTrafficController.validSystemNameConfig("CT4129", 'T',stcs1));
        Assert.assertTrue("invalid config CT4129", !SerialTrafficController.validSystemNameConfig("CT4B129", 'T',stcs1));
        Assert.assertTrue("valid config CS4064", SerialTrafficController.validSystemNameConfig("CS4064", 'S',stcs1));
        Assert.assertTrue("valid config CS4B64", SerialTrafficController.validSystemNameConfig("CS4B64", 'S',stcs1));
        Assert.assertTrue("invalid config CS4065", !SerialTrafficController.validSystemNameConfig("CS4065", 'S',stcs1));
        Assert.assertTrue("invalid config CS4B65", !SerialTrafficController.validSystemNameConfig("CS4B65", 'S',stcs1));
        Assert.assertTrue("invalid config CL11007", !SerialTrafficController.validSystemNameConfig("CL11007", 'L',stcs1));
        Assert.assertTrue("invalid config CL11B7", !SerialTrafficController.validSystemNameConfig("CL11B7", 'L',stcs1));
    }

    public void testConvertSystemNameFormat() {
        Assert.assertEquals("convert CL14007", "CL14B7", SerialTrafficController.convertSystemNameToAlternate("CL14007"));
        Assert.assertEquals("convert CS7", "CS0B7", SerialTrafficController.convertSystemNameToAlternate("CS7"));
        Assert.assertEquals("convert CT4007", "CT4B7", SerialTrafficController.convertSystemNameToAlternate("CT4007"));
        Assert.assertEquals("convert CL14B7", "CL14007", SerialTrafficController.convertSystemNameToAlternate("CL14B7"));
        Assert.assertEquals("convert CL0B7", "CL7", SerialTrafficController.convertSystemNameToAlternate("CL0B7"));
        Assert.assertEquals("convert CS4B7", "CS4007", SerialTrafficController.convertSystemNameToAlternate("CS4B7"));
        Assert.assertEquals("convert CL14B8", "CL14008", SerialTrafficController.convertSystemNameToAlternate("CL14B8"));

        Assert.assertEquals("convert CL128B7", "", SerialTrafficController.convertSystemNameToAlternate("CL128B7"));
        JUnitAppender.assertErrorMessage("node address field out of range in CMRI system name: CL128B7");
    }

    public void testNormalizeSystemName() {
        Assert.assertEquals("normalize CL14007", "CL14007", SerialTrafficController.normalizeSystemName("CL14007"));
        Assert.assertEquals("normalize CL007", "CL7", SerialTrafficController.normalizeSystemName("CL007"));
        Assert.assertEquals("normalize CL004007", "CL4007", SerialTrafficController.normalizeSystemName("CL004007"));
        Assert.assertEquals("normalize CL14B7", "CL14B7", SerialTrafficController.normalizeSystemName("CL14B7"));
        Assert.assertEquals("normalize CL0B7", "CL0B7", SerialTrafficController.normalizeSystemName("CL0B7"));
        Assert.assertEquals("normalize CL004B7", "CL4B7", SerialTrafficController.normalizeSystemName("CL004B7"));
        Assert.assertEquals("normalize CL014B0008", "CL14B8", SerialTrafficController.normalizeSystemName("CL014B0008"));

        Assert.assertEquals("normalize CL128B7", "", SerialTrafficController.normalizeSystemName("CL128B7"));
        JUnitAppender.assertErrorMessage("node address field out of range in CMRI system name: CL128B7");
    }

    public void testConstructSystemName() {
        Assert.assertEquals("make CL14007", "CL14007", SerialTrafficController.makeSystemName("L", 14, 7));
        Assert.assertEquals("make CT7", "CT7", SerialTrafficController.makeSystemName("T", 0, 7));

        Assert.assertEquals("make illegal 1", "", SerialTrafficController.makeSystemName("L", 0, 0));
        JUnitAppender.assertErrorMessage("illegal bit number proposed for system name");

        Assert.assertEquals("make illegal 2", "", SerialTrafficController.makeSystemName("L", 128, 7));
        JUnitAppender.assertErrorMessage("illegal node adddress proposed for system name");

        Assert.assertEquals("make illegal 3", "", SerialTrafficController.makeSystemName("R", 120, 7));
        JUnitAppender.assertErrorMessage("illegal type character proposed for system name");

        Assert.assertEquals("make CL0B1770", "CL0B1770", SerialTrafficController.makeSystemName("L", 0, 1770));
        Assert.assertEquals("make CS127999", "CS127999", SerialTrafficController.makeSystemName("S", 127, 999));
        Assert.assertEquals("make CS14B1000", "CS14B1000", SerialTrafficController.makeSystemName("S", 14, 1000));
    }

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
        Assert.assertEquals("test bit 30", "", SerialTrafficController.isOutputBitFree(18, 30));
        Assert.assertEquals("test bit 34", "CT18034", SerialTrafficController.isOutputBitFree(18, 34));
        Assert.assertEquals("test bit 33", "", SerialTrafficController.isOutputBitFree(18, 33));
        Assert.assertEquals("test bit 35", "CT18034", SerialTrafficController.isOutputBitFree(18, 35));
        Assert.assertEquals("test bit 36", "CL18036", SerialTrafficController.isOutputBitFree(18, 36));
        Assert.assertEquals("test bit 37", "CL18037", SerialTrafficController.isOutputBitFree(18, 37));
        Assert.assertEquals("test bit 38", "", SerialTrafficController.isOutputBitFree(18, 38));
        Assert.assertEquals("test bit 39", "", SerialTrafficController.isOutputBitFree(18, 39));
        Assert.assertEquals("test bit 2000", "", SerialTrafficController.isOutputBitFree(18, 2000));

        Assert.assertEquals("test bit bad bit", "", SerialTrafficController.isOutputBitFree(18, 0));
        JUnitAppender.assertErrorMessage("illegal bit number in free bit test");

        Assert.assertEquals("test bit bad node address", "", SerialTrafficController.isOutputBitFree(129, 34));
        JUnitAppender.assertErrorMessage("illegal node adddress in free bit test");
    }

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
        Assert.assertEquals("test bit 10", "", SerialTrafficController.isInputBitFree(18, 10));
        Assert.assertEquals("test bit 11", "", SerialTrafficController.isInputBitFree(18, 11));
        Assert.assertEquals("test bit 12", "CS18012", SerialTrafficController.isInputBitFree(18, 12));
        Assert.assertEquals("test bit 13", "", SerialTrafficController.isInputBitFree(18, 13));
        Assert.assertEquals("test bit 14", "CS18014", SerialTrafficController.isInputBitFree(18, 14));
        Assert.assertEquals("test bit 15", "", SerialTrafficController.isInputBitFree(18, 15));
        Assert.assertEquals("test bit 16", "CS18016", SerialTrafficController.isInputBitFree(18, 16));
        Assert.assertEquals("test bit 17", "CS18017", SerialTrafficController.isInputBitFree(18, 17));
        Assert.assertEquals("test bit 18", "", SerialTrafficController.isInputBitFree(18, 18));

        Assert.assertEquals("test bit bad bit", "", SerialTrafficController.isInputBitFree(18, 0));
        JUnitAppender.assertErrorMessage("illegal bit number in free bit test");

        Assert.assertEquals("test bit bad node address", "", SerialTrafficController.isInputBitFree(129, 34));
        JUnitAppender.assertErrorMessage("illegal node adddress in free bit test");
    }

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

        Assert.assertEquals("test CS18016", "userS16", SerialTrafficController.getUserNameFromSystemName("CS18016"));
        Assert.assertEquals("test CS18012", "userS12", SerialTrafficController.getUserNameFromSystemName("CS18012"));
        Assert.assertEquals("test CS18017", "userS17", SerialTrafficController.getUserNameFromSystemName("CS18017"));
        Assert.assertEquals("test undefined CS18010", "", SerialTrafficController.getUserNameFromSystemName("CS18010"));
        Assert.assertEquals("test CL18037", "userL37", SerialTrafficController.getUserNameFromSystemName("CL18037"));
        Assert.assertEquals("test CL18036", "userL36", SerialTrafficController.getUserNameFromSystemName("CL18036"));
        Assert.assertEquals("test undefined CL18030", "", SerialTrafficController.getUserNameFromSystemName("CL18030"));
        Assert.assertEquals("test CT18032", "userT32", SerialTrafficController.getUserNameFromSystemName("CT18032"));
        Assert.assertEquals("test CT18034", "userT34", SerialTrafficController.getUserNameFromSystemName("CT18034"));
        Assert.assertEquals("test undefined CT18039", "", SerialTrafficController.getUserNameFromSystemName("CT18039"));
    }

    // from here down is testing infrastructure
    public SerialAddressTwoSystemTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SerialAddressTwoSystemTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(SerialAddressTwoSystemTest.class);
        return suite;
    }

}
