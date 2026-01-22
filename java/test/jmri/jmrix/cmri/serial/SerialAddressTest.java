package jmri.jmrix.cmri.serial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.Manager.NameValidity;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the serial address functions in memo.
 * <p>
 * These used to be in a separate SerialAddress class, with its own test class.
 * This structure is a vestige of that.
 *
 * @author Dave Duchamp Copyright 2004
 * @author Bob Jacobsen Copyright 2017
 */
public class SerialAddressTest {

    private jmri.jmrix.cmri.CMRISystemConnectionMemo memo = null;
    private SerialTrafficControlScaffold stcs = null;

    private SerialNode n10;
    private SerialNode n18;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        // replace the SerialTrafficController
        stcs = new SerialTrafficControlScaffold();
        memo = new jmri.jmrix.cmri.CMRISystemConnectionMemo();
        memo.setTrafficController(stcs);

        n10 = new SerialNode(10, SerialNode.SMINI, stcs);
        n18 = new SerialNode(18, SerialNode.SMINI, stcs);
        Assertions.assertNotNull(n10);
        Assertions.assertNotNull(n18);

        // create and register the manager objects
        jmri.TurnoutManager l = new SerialTurnoutManager(memo);
        jmri.InstanceManager.setTurnoutManager(l);

        jmri.LightManager lgt = new SerialLightManager(memo);
        jmri.InstanceManager.setLightManager(lgt);

        jmri.SensorManager s = new SerialSensorManager(memo);
        jmri.InstanceManager.setSensorManager(s);

    }

    @AfterEach
    public void tearDown() {
        if (stcs != null) {
            stcs.terminateThreads();
        }
        stcs = null;
        memo = null;
        n10 = null;
        n18 = null;
        // JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    @Test
    public void testValidSystemNameFormat() {
        assertEquals( NameValidity.VALID, memo.validSystemNameFormat("CL2", 'L'), "valid format - CL2");
        assertEquals( NameValidity.VALID, memo.validSystemNameFormat("CL0B2", 'L'), "valid format - CL0B2");

        assertNotEquals( NameValidity.VALID, memo.validSystemNameFormat("CL", 'L'), "invalid format - CL");
//        JUnitAppender.assertWarnMessage("invalid character in number field of CMRI system name: CL");

        assertNotEquals( NameValidity.VALID, memo.validSystemNameFormat("CLB2", 'L'), "invalid format - CLB2");
//        JUnitAppender.assertWarnMessage("no node address before 'B' in CMRI system name: CLB2");

        assertEquals( NameValidity.VALID, memo.validSystemNameFormat("CL2005", 'L'), "valid format - CL2005");
        assertEquals( NameValidity.VALID, memo.validSystemNameFormat("CL2B5", 'L'), "valid format - CL2B5");
        assertEquals( NameValidity.VALID, memo.validSystemNameFormat("CT2005", 'T'), "valid format - CT2005");
        assertEquals( NameValidity.VALID, memo.validSystemNameFormat("CT2B5", 'T'), "valid format - CT2B5");
        assertEquals( NameValidity.VALID, memo.validSystemNameFormat("CS2005", 'S'), "valid format - CS2005");
        assertEquals( NameValidity.VALID, memo.validSystemNameFormat("CS2B5", 'S'), "valid format - CS2B5");

        assertNotEquals( NameValidity.VALID, memo.validSystemNameFormat("CY2005", 'L'), "invalid format - CY2005");
//        JUnitAppender.assertErrorMessage("invalid type character in CMRI system name: CY2005");

        assertNotEquals( NameValidity.VALID, memo.validSystemNameFormat("CY2B5", 'L'), "invalid format - CY2B5");
//        JUnitAppender.assertErrorMessage("invalid type character in CMRI system name: CY2B5");

        assertEquals( NameValidity.VALID, memo.validSystemNameFormat("CL22001", 'L'), "valid format - CL22001");
        assertEquals( NameValidity.VALID, memo.validSystemNameFormat("CL22B1", 'L'), "valid format - CL22B1");

        assertNotEquals( NameValidity.VALID, memo.validSystemNameFormat("CL22000", 'L'), "invalid format - CL22000");
//        JUnitAppender.assertWarnMessage("bit number not in range 1 - 999 in CMRI system name: CL22000");

        assertNotEquals( NameValidity.VALID, memo.validSystemNameFormat("CL22B0", 'L'), "invalid format - CL22B0");
//        JUnitAppender.assertWarnMessage("bit number field out of range in CMRI system name: CL22B0");

        assertEquals( NameValidity.VALID, memo.validSystemNameFormat("CL2999", 'L'), "valid format - CL2999");
        assertEquals( NameValidity.VALID, memo.validSystemNameFormat("CL2B2048", 'L'), "valid format - CL2B2048");

        assertNotEquals( NameValidity.VALID, memo.validSystemNameFormat("CL2B2049", 'L'), "invalid format - CL2B2049");
//        JUnitAppender.assertWarnMessage("bit number field out of range in CMRI system name: CL2B2049");

        assertEquals( NameValidity.VALID, memo.validSystemNameFormat("CL127999", 'L'), "valid format - CL127999");

        assertNotEquals( NameValidity.VALID, memo.validSystemNameFormat("CL128000", 'L'), "invalid format - CL128000");
//        JUnitAppender.assertWarnMessage("number field out of range in CMRI system name: CL128000");

        assertEquals( NameValidity.VALID, memo.validSystemNameFormat("CL127B7", 'L'), "valid format - CL127B7");

        assertNotEquals( NameValidity.VALID, memo.validSystemNameFormat("CL128B7", 'L'), "invalid format - CL128B7");
//        JUnitAppender.assertWarnMessage("node address field out of range in CMRI system name: CL128B7");

        assertNotEquals( NameValidity.VALID, memo.validSystemNameFormat("CL2oo5", 'L'), "invalid format - CL2oo5");
//        JUnitAppender.assertWarnMessage("invalid character in number field of CMRI system name: CL2oo5");

        assertNotEquals( NameValidity.VALID, memo.validSystemNameFormat("CL2aB5", 'L'), "invalid format - CL2aB5");
//        JUnitAppender.assertWarnMessage("invalid character in node address field of CMRI system name: CL2aB5");

        assertNotEquals( NameValidity.VALID, memo.validSystemNameFormat("CL2B5x", 'L'), "invalid format - CL2B5x");
//        JUnitAppender.assertWarnMessage("invalid character in bit number field of CMRI system name: CL2B5x");
    }

    @Test
    public void testGetBitFromSystemName() {
        assertEquals( 2, memo.getBitFromSystemName("CL2"), "CL2");
        assertEquals( 2, memo.getBitFromSystemName("CL2002"), "CL2002");
        assertEquals( 1, memo.getBitFromSystemName("CL1"), "CL1");
        assertEquals( 1, memo.getBitFromSystemName("CL2001"), "CL2001");
        assertEquals( 999, memo.getBitFromSystemName("CL999"), "CL999");
        assertEquals( 999, memo.getBitFromSystemName("CL2999"), "CL2999");

        assertEquals( 0, memo.getBitFromSystemName("CL29O9"), "CL29O9");
//        JUnitAppender.assertWarnMessage("invalid character in number field of system name: CL29O9");

        assertEquals( 7, memo.getBitFromSystemName("CL0B7"), "CL0B7");
        assertEquals( 7, memo.getBitFromSystemName("CL2B7"), "CL2B7");
        assertEquals( 1, memo.getBitFromSystemName("CL0B1"), "CL0B1");
        assertEquals( 1, memo.getBitFromSystemName("CL2B1"), "CL2B1");
        assertEquals( 2048, memo.getBitFromSystemName("CL0B2048"), "CL0B2048");
        assertEquals( 2048, memo.getBitFromSystemName("CL11B2048"), "CL11B2048");
    }

    @Test
    public void testGetNodeFromSystemName() {
        SerialNode d = new SerialNode(14, SerialNode.USIC_SUSIC, stcs);
        SerialNode c = new SerialNode(17, SerialNode.SMINI, stcs);
        SerialNode b = new SerialNode(127, SerialNode.SMINI, stcs);
        assertEquals( d, memo.getNodeFromSystemName("CL14007", stcs), "node of CL14007");
        assertEquals( d, memo.getNodeFromSystemName("CL14B7", stcs), "node of CL14B7");
        assertEquals( b, memo.getNodeFromSystemName("CL127007", stcs), "node of CL127007");
        assertEquals( b, memo.getNodeFromSystemName("CL127B7", stcs), "node of CL127B7");
        assertEquals( c, memo.getNodeFromSystemName("CL17007", stcs), "node of CL17007");
        assertEquals( c, memo.getNodeFromSystemName("CL17B7", stcs), "node of CL17B7");
        assertNull( memo.getNodeFromSystemName("CL11007", stcs), "node of CL11007");
        assertNull( memo.getNodeFromSystemName("CL11B7", stcs), "node of CL11B7");
    }

    @Test
    public void testGetNodeAddressFromSystemName() {
        assertEquals( 14, memo.getNodeAddressFromSystemName("CL14007"), "CL14007");
        assertEquals( 14, memo.getNodeAddressFromSystemName("CL14B7"), "CL14B7");
        assertEquals( 127, memo.getNodeAddressFromSystemName("CL127007"), "CL127007");
        assertEquals( 127, memo.getNodeAddressFromSystemName("CL127B7"), "CL127B7");
        assertEquals( 0, memo.getNodeAddressFromSystemName("CL0B7"), "CL0B7");
        assertEquals( 0, memo.getNodeAddressFromSystemName("CL7"), "CL7");

        assertEquals( -1, memo.getNodeAddressFromSystemName("CLB7"), "CLB7");
        JUnitAppender.assertWarnMessage("no node address before 'B' in CMRI system name: CLB7");

        assertEquals( -1, memo.getNodeAddressFromSystemName("CR7"), "CR7");
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
        assertNotNull( c, "exists");
        assertTrue( memo.validSystemNameConfig("CL4007", 'L', stcs), "valid config CL4007");
        assertTrue( memo.validSystemNameConfig("CL4B7", 'L', stcs), "valid config CL4B7");
        assertTrue( memo.validSystemNameConfig("CS10007", 'S', stcs), "valid config CS10007");
        assertTrue( memo.validSystemNameConfig("CS10B7", 'S', stcs), "valid config CS10B7");
        assertTrue( memo.validSystemNameConfig("CL10048", 'L', stcs), "valid config CL10048");
        assertTrue( memo.validSystemNameConfig("CL10B48", 'L', stcs), "valid config CL10B48");
        assertFalse( memo.validSystemNameConfig("CL10049", 'L', stcs), "invalid config CL10049");
        assertFalse( memo.validSystemNameConfig("CL10B49", 'L', stcs), "invalid config CL10B49");
        assertTrue( memo.validSystemNameConfig("CS10024", 'S', stcs), "valid config CS10024");
        assertTrue( memo.validSystemNameConfig("CS10B24", 'S', stcs), "valid config CS10B24");
        assertFalse( memo.validSystemNameConfig("CS10025", 'S', stcs), "invalid config CS10025");
        assertFalse( memo.validSystemNameConfig("CS10B25", 'S', stcs), "invalid config CS10B25");
        assertTrue( memo.validSystemNameConfig("CT4128", 'T', stcs), "valid config CT4128");
        assertTrue( memo.validSystemNameConfig("CT4B128", 'T', stcs), "valid config CT4B128");
        assertFalse( memo.validSystemNameConfig("CT4129", 'T', stcs), "invalid config CT4129");
        assertFalse( memo.validSystemNameConfig("CT4B129", 'T', stcs), "invalid config CT4B129");
        assertTrue( memo.validSystemNameConfig("CS4064", 'S', stcs), "valid config CS4064");
        assertTrue( memo.validSystemNameConfig("CS4B64", 'S', stcs), "valid config CS4B64");
        assertFalse( memo.validSystemNameConfig("CS4065", 'S', stcs), "invalid config CS4065");
        assertFalse( memo.validSystemNameConfig("CS4B65", 'S', stcs), "invalid config CS4B65");
        assertFalse( memo.validSystemNameConfig("CL11007", 'L', stcs), "invalid config CL11007");
        assertFalse( memo.validSystemNameConfig("CL11B7", 'L', stcs), "invalid config CL11B7");
    }

    @Test
    public void testConvertSystemNameFormat() {
        assertEquals( "CL14B7", memo.convertSystemNameToAlternate("CL14007"), "convert CL14007");
        assertEquals( "CS0B7", memo.convertSystemNameToAlternate("CS7"), "convert CS7");
        assertEquals( "CT4B7", memo.convertSystemNameToAlternate("CT4007"), "convert CT4007");
        assertEquals( "CL14007", memo.convertSystemNameToAlternate("CL14B7"), "convert CL14B7");
        assertEquals( "CL7", memo.convertSystemNameToAlternate("CL0B7"), "convert CL0B7");
        assertEquals( "CS4007", memo.convertSystemNameToAlternate("CS4B7"), "convert CS4B7");
        assertEquals( "CL14008", memo.convertSystemNameToAlternate("CL14B8"), "convert CL14B8");

        assertEquals( "", memo.convertSystemNameToAlternate("CL128B7"), "convert CL128B7");
//        JUnitAppender.assertWarnMessage("node address field out of range in CMRI system name: CL128B7");
    }

    @Test
    public void testNormalizeSystemName() {
        assertEquals( "CL14007", memo.normalizeSystemName("CL14007"), "normalize CL14007");
        assertEquals( "CL7", memo.normalizeSystemName("CL007"), "normalize CL007");
        assertEquals( "CL4007", memo.normalizeSystemName("CL004007"), "normalize CL004007");
        assertEquals( "CL14B7", memo.normalizeSystemName("CL14B7"), "normalize CL14B7");
        assertEquals( "CL0B7", memo.normalizeSystemName("CL0B7"), "normalize CL0B7");
        assertEquals( "CL4B7", memo.normalizeSystemName("CL004B7"), "normalize CL1004B7");
        assertEquals( "CL14B8", memo.normalizeSystemName("CL014B0008"), "normalize CL014B0008");

        assertEquals( "", memo.normalizeSystemName("CL128B7"), "normalize CL128B7");
//        JUnitAppender.assertWarnMessage("node address field out of range in CMRI system name: CL128B7");
    }

    @Test
    public void testConstructSystemName() {
        assertEquals( "CL14007", memo.makeSystemName("L", 14, 7), "make CL14007");
        assertEquals( "CT7", memo.makeSystemName("T", 0, 7), "make CT7");

        assertEquals( "", memo.makeSystemName("L", 0, 0), "make invalid 1");
        JUnitAppender.assertWarnMessage("invalid bit number proposed for system name");

        assertEquals( "", memo.makeSystemName("L", 128, 7), "make invalid 2");
        JUnitAppender.assertWarnMessage("invalid node address proposed for system name");

        assertEquals( "", memo.makeSystemName("R", 120, 7), "make invalid 3");
        JUnitAppender.assertErrorMessage("invalid type character proposed for system name");

        assertEquals( "CL0B1770", memo.makeSystemName("L", 0, 1770), "make CL0B1770");
        assertEquals( "CS127999", memo.makeSystemName("S", 127, 999), "make CS127999");
        assertEquals( "CS14B1000", memo.makeSystemName("S", 14, 1000), "make CS14B1000");
    }

    @Test
    public void testIsOutputBitFree() {
        // create a new turnout, controlled by two output bits
        jmri.TurnoutManager tMgr = jmri.InstanceManager.turnoutManagerInstance();
        jmri.Turnout t1 = tMgr.newTurnout("CT18034", "userT34");
        t1.setNumberControlBits(2);
        // check that turnout was created correctly
        assertEquals( "CT18034", t1.getSystemName(), "create CT18034 check 1");
        assertEquals( 2, t1.getNumberControlBits(), "create CT18034 check 2");
        // create a new turnout, controlled by one output bit
        jmri.Turnout t2 = tMgr.newTurnout("CT18032", "userT32");
        // check that turnout was created correctly
        assertEquals( "CT18032", t2.getSystemName(), "create CT18032 check 1");
        assertEquals( 1, t2.getNumberControlBits(), "create CT18032 check 2");
        // create two new lights
        jmri.LightManager lMgr = jmri.InstanceManager.lightManagerInstance();
        jmri.Light lgt1 = lMgr.newLight("CL18036", "userL36");
        jmri.Light lgt2 = lMgr.newLight("CL18037", "userL37");
        // check that the lights were created as expected
        assertEquals( "CL18036", lgt1.getSystemName(), "create CL18036 check");
        assertEquals( "CL18037", lgt2.getSystemName(), "create CL18037 check");
        // test
        assertEquals( "", memo.isOutputBitFree(18, 30), "test bit 30");
        assertEquals( "CT18034", memo.isOutputBitFree(18, 34), "test bit 34");
        assertEquals( "", memo.isOutputBitFree(18, 33), "test bit 33");
        assertEquals( "CT18034", memo.isOutputBitFree(18, 35), "test bit 35");
        assertEquals( "CL18036", memo.isOutputBitFree(18, 36), "test bit 36");
        assertEquals( "CL18037", memo.isOutputBitFree(18, 37), "test bit 37");
        assertEquals( "", memo.isOutputBitFree(18, 38), "test bit 38");
        assertEquals( "", memo.isOutputBitFree(18, 39), "test bit 39");
        assertEquals( "", memo.isOutputBitFree(18, 2000), "test bit 2000");

        assertEquals( "", memo.isOutputBitFree(18, 0), "test bit bad bit");
        JUnitAppender.assertWarnMessage("invalid bit number in free bit test");

        assertEquals( "", memo.isOutputBitFree(129, 34), "test bit bad node address");
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
        assertEquals( "CS18016", s1.getSystemName(), "create CS18016 check");
        assertEquals( "CS18014", s2.getSystemName(), "create CS18014 check");
        assertEquals( "CS18017", s3.getSystemName(), "create CS18017 check");
        assertEquals( "CS18012", s4.getSystemName(), "create CS18012 check");
        // test
        assertEquals( "", memo.isInputBitFree(18, 10), "test bit 10");
        assertEquals( "", memo.isInputBitFree(18, 11), "test bit 11");
        assertEquals( "CS18012", memo.isInputBitFree(18, 12), "test bit 12");
        assertEquals( "", memo.isInputBitFree(18, 13), "test bit 13");
        assertEquals( "CS18014", memo.isInputBitFree(18, 14), "test bit 14");
        assertEquals( "", memo.isInputBitFree(18, 15), "test bit 15");
        assertEquals( "CS18016", memo.isInputBitFree(18, 16), "test bit 16");
        assertEquals( "CS18017", memo.isInputBitFree(18, 17), "test bit 17");
        assertEquals( "", memo.isInputBitFree(18, 18), "test bit 18");

        assertEquals( "", memo.isInputBitFree(18, 0), "test bit bad bit");
        JUnitAppender.assertWarnMessage("invalid bit number in free bit test");

        assertEquals( "", memo.isInputBitFree(129, 34));
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

        assertEquals( "userS16", memo.getUserNameFromSystemName("CS18016"), "test CS18016");
        assertEquals( "userS12", memo.getUserNameFromSystemName("CS18012"), "test CS18012");
        assertEquals( "userS17", memo.getUserNameFromSystemName("CS18017"), "test CS18017");
        assertEquals( "", memo.getUserNameFromSystemName("CS18010"), "test undefined CS18010");
        assertEquals( "userL37", memo.getUserNameFromSystemName("CL18037"), "test CL18037");
        assertEquals( "userL36", memo.getUserNameFromSystemName("CL18036"), "test CL18036");
        assertEquals( "", memo.getUserNameFromSystemName("CL18030"), "test undefined CL18030");
        assertEquals( "userT32", memo.getUserNameFromSystemName("CT18032"), "test CT18032");
        assertEquals( "userT34", memo.getUserNameFromSystemName("CT18034"), "test CT18034");
        assertEquals( "", memo.getUserNameFromSystemName("CT18039"), "test undefined CT18039");
    }

}
