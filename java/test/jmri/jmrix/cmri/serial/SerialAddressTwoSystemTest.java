package jmri.jmrix.cmri.serial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.Manager.NameValidity;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the serial address functions in memo1.
 * <p>
 * These used to be in a separate SerialAddress class, with its own test class.
 * This structure is a vestige of that.
 *
 * @author Dave Duchamp Copyright 2004
 * @author Bob Jacobsen Copyright 2017
 */
public class SerialAddressTwoSystemTest {

    private CMRISystemConnectionMemo memo1 = null;
    private SerialTrafficControlScaffold stcs1 = null;

    private SerialNode c10;
    private SerialNode c18;

    private CMRISystemConnectionMemo memo2 = null;
    private SerialTrafficControlScaffold stcs2 = null;

    private SerialNode k10;
    private SerialNode k20;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        // replace the 1st SerialTrafficController
        stcs1 = new SerialTrafficControlScaffold();
        memo1 = new CMRISystemConnectionMemo();
        memo1.setTrafficController(stcs1);
        // create 1st nodes
        c10 = new SerialNode(10, SerialNode.SMINI, stcs1);
        c18 = new SerialNode(18, SerialNode.SMINI, stcs1);
        assertNotNull(c10);
        assertNotNull(c18);
        // create and register the 1st manager objects
        jmri.TurnoutManager l1 = new SerialTurnoutManager(memo1);
        jmri.InstanceManager.setTurnoutManager(l1);
        jmri.LightManager lgt1 = new SerialLightManager(memo1);
        jmri.InstanceManager.setLightManager(lgt1);
        jmri.SensorManager s1 = new SerialSensorManager(memo1);
        jmri.InstanceManager.setSensorManager(s1);

        // replace the 2nd SerialTrafficController
        stcs2 = new SerialTrafficControlScaffold();
        memo2 = new jmri.jmrix.cmri.CMRISystemConnectionMemo("K2", "CMRI2");
        memo2.setTrafficController(stcs2);
        // create 2nd nodes
        k10 = new SerialNode(10, SerialNode.SMINI, stcs2);
        k20 = new SerialNode(20, SerialNode.SMINI, stcs2);
        assertNotNull(k10);
        assertNotNull(k20);
        // create and register the 1st manager objects
        jmri.TurnoutManager l2 = new SerialTurnoutManager(memo2);
        jmri.InstanceManager.setTurnoutManager(l2);
        jmri.LightManager lgt2 = new SerialLightManager(memo2);
        jmri.InstanceManager.setLightManager(lgt2);
        jmri.SensorManager s2 = new SerialSensorManager(memo2);
        jmri.InstanceManager.setSensorManager(s2);

    }

    @AfterEach
    public void tearDown() {
        if (stcs1 != null) {
            stcs1.terminateThreads();
        }
        stcs1 = null;
        memo1 = null;
        if (stcs2 != null) {
            stcs2.terminateThreads();
        }
        stcs2 = null;
        memo2 = null;

        JUnitUtil.tearDown();
    }

    @Test
    public void testValidSystemNameFormat() {
        assertEquals( NameValidity.VALID,  memo1.validSystemNameFormat("CL2", 'L'), "valid format - CL2");
        assertEquals( NameValidity.VALID, memo1.validSystemNameFormat("CL0B2", 'L'), "valid format - CL0B2");

        assertNotEquals( NameValidity.VALID, memo1.validSystemNameFormat("CL", 'L'), "invalid format - CL");
//        JUnitAppender.assertWarnMessage("invalid character in number field of CMRI system name: CL");

        assertNotEquals( NameValidity.VALID, memo1.validSystemNameFormat("CLB2", 'L'), "invalid format - CLB2");
//        JUnitAppender.assertWarnMessage("no node address before 'B' in CMRI system name: CLB2");

        assertEquals( NameValidity.VALID, memo1.validSystemNameFormat("CL2005", 'L'), "valid format - CL2005");
        assertEquals( NameValidity.VALID, memo1.validSystemNameFormat("CL2B5", 'L'), "valid format - CL2B5");
        assertEquals( NameValidity.VALID, memo1.validSystemNameFormat("CT2005", 'T'), "valid format - CT2005");
        assertEquals( NameValidity.VALID, memo1.validSystemNameFormat("CT2B5", 'T'), "valid format - CT2B5");
        assertEquals( NameValidity.VALID, memo1.validSystemNameFormat("CS2005", 'S'), "valid format - CS2005");
        assertEquals( NameValidity.VALID, memo1.validSystemNameFormat("CS2B5", 'S'), "valid format - CS2B5");

        assertNotEquals( NameValidity.VALID, memo1.validSystemNameFormat("CY2005", 'L'), "invalid format - CY2005");
//        JUnitAppender.assertErrorMessage("invalid type character in CMRI system name: CY2005");

        assertNotEquals( NameValidity.VALID, memo1.validSystemNameFormat("CY2B5", 'L'), "invalid format - CY2B5");
//        JUnitAppender.assertErrorMessage("invalid type character in CMRI system name: CY2B5");

        assertEquals( NameValidity.VALID, memo1.validSystemNameFormat("CL22001", 'L'), "valid format - CL22001");
        assertEquals( NameValidity.VALID, memo1.validSystemNameFormat("CL22B1", 'L'), "valid format - CL22B1");

        assertNotEquals( NameValidity.VALID, memo1.validSystemNameFormat("CL22000", 'L'), "invalid format - CL22000");
//        JUnitAppender.assertWarnMessage("bit number not in range 1 - 999 in CMRI system name: CL22000");

        assertNotEquals( NameValidity.VALID, memo1.validSystemNameFormat("CL22B0", 'L'), "invalid format - CL22B0");
//        JUnitAppender.assertWarnMessage("bit number field out of range in CMRI system name: CL22B0");

        assertEquals( NameValidity.VALID, memo1.validSystemNameFormat("CL2999", 'L'), "valid format - CL2999");
        assertEquals( NameValidity.VALID, memo1.validSystemNameFormat("CL2B2048", 'L'), "valid format - CL2B2048");

        assertNotEquals( NameValidity.VALID, memo1.validSystemNameFormat("CL2B2049", 'L'), "invalid format - CL2B2049");
//        JUnitAppender.assertWarnMessage("bit number field out of range in CMRI system name: CL2B2049");

        assertEquals( NameValidity.VALID, memo1.validSystemNameFormat("CL127999", 'L'), "valid format - CL127999");

        assertNotEquals( NameValidity.VALID, memo1.validSystemNameFormat("CL128000", 'L'), "invalid format - CL128000");
//        JUnitAppender.assertWarnMessage("number field out of range in CMRI system name: CL128000");

        assertEquals( NameValidity.VALID, memo1.validSystemNameFormat("CL127B7", 'L'), "valid format - CL127B7");

        assertNotEquals( NameValidity.VALID, memo1.validSystemNameFormat("CL128B7", 'L'), "invalid format - CL128B7");
//        JUnitAppender.assertWarnMessage("node address field out of range in CMRI system name: CL128B7");

        assertNotEquals( NameValidity.VALID, memo1.validSystemNameFormat("CL2oo5", 'L'), "invalid format - CL2oo5");
//        JUnitAppender.assertWarnMessage("invalid character in number field of CMRI system name: CL2oo5");

        assertNotEquals( NameValidity.VALID, memo1.validSystemNameFormat("CL2aB5", 'L'), "invalid format - CL2aB5");
//        JUnitAppender.assertWarnMessage("invalid character in node address field of CMRI system name: CL2aB5");

        assertNotEquals( NameValidity.VALID, memo1.validSystemNameFormat("CL2B5x", 'L'), "invalid format - CL2B5x");
//        JUnitAppender.assertWarnMessage("invalid character in bit number field of CMRI system name: CL2B5x");
    }

    @Test
    public void testGetBitFromSystemName() {
        assertEquals( 2, memo1.getBitFromSystemName("CL2"), "CL2");
        assertEquals( 2, memo1.getBitFromSystemName("CL2002"), "CL2002");
        assertEquals( 1, memo1.getBitFromSystemName("CL1"), "CL1");
        assertEquals( 1, memo1.getBitFromSystemName("CL2001"), "CL2001");
        assertEquals( 999, memo1.getBitFromSystemName("CL999"), "CL999");
        assertEquals( 999, memo1.getBitFromSystemName("CL2999"), "CL2999");

        assertEquals( 0, memo1.getBitFromSystemName("CL29O9"), "CL29O9");
//        JUnitAppender.assertWarnMessage("invalid character in number field of system name: CL29O9");

        assertEquals( 7, memo1.getBitFromSystemName("CL0B7"), "CL0B7");
        assertEquals( 7, memo1.getBitFromSystemName("CL2B7"), "CL2B7");
        assertEquals( 1, memo1.getBitFromSystemName("CL0B1"), "CL0B1");
        assertEquals( 1, memo1.getBitFromSystemName("CL2B1"), "CL2B1");
        assertEquals( 2048, memo1.getBitFromSystemName("CL0B2048"), "CL0B2048");
        assertEquals( 2048, memo1.getBitFromSystemName("CL11B2048"), "CL11B2048");
    }

    @Test
    public void testGetBitFromSystemName2() {
        assertEquals( 2, memo2.getBitFromSystemName("K2L2"), "K2L2");
        assertEquals( 2, memo2.getBitFromSystemName("K2L2002"), "K2L2002");
        assertEquals( 1, memo2.getBitFromSystemName("K2L1"), "K2L1");
        assertEquals( 1, memo2.getBitFromSystemName("K2L2001"), "K2L2001");
        assertEquals( 999, memo2.getBitFromSystemName("K2L999"), "K2L999");
        assertEquals( 999, memo2.getBitFromSystemName("K2L2999"), "K2L2999");

        assertEquals( 0, memo2.getBitFromSystemName("K2L29O9"), "K2L29O9");
//        JUnitAppender.assertWarnMessage("invalid character in number field of system name: K2L29O9");

        assertEquals( 7, memo2.getBitFromSystemName("K2L0B7"), "K2L0B7");
        assertEquals( 7, memo2.getBitFromSystemName("K2L2B7"), "K2L2B7");
        assertEquals( 1, memo2.getBitFromSystemName("K2L0B1"), "K2L0B1");
        assertEquals( 1, memo2.getBitFromSystemName("K2L2B1"), "K2L2B1");
        assertEquals( 2048, memo2.getBitFromSystemName("K2L0B2048"), "K2L0B2048");
        assertEquals( 2048, memo2.getBitFromSystemName("K2L11B2048"), "K2L11B2048");
    }

    @Test
    public void testGetNodeFromSystemName() {
        SerialNode d = new SerialNode(14, SerialNode.USIC_SUSIC, stcs1);
        SerialNode c = new SerialNode(17, SerialNode.SMINI, stcs1);
        SerialNode b = new SerialNode(127, SerialNode.SMINI, stcs1);
        assertEquals( d, memo1.getNodeFromSystemName("CL14007", stcs1), "node of CL14007" );
        assertEquals( d, memo1.getNodeFromSystemName("CL14B7", stcs1), "node of CL14B7"  );
        assertEquals( b, memo1.getNodeFromSystemName("CL127007", stcs1), "node of CL127007");
        assertEquals( b, memo1.getNodeFromSystemName("CL127B7", stcs1), "node of CL127B7" );
        assertEquals( c, memo1.getNodeFromSystemName("CL17007", stcs1), "node of CL17007" );
        assertEquals( c, memo1.getNodeFromSystemName("CL17B7", stcs1), "node of CL17B7"  );
        assertNull(  memo1.getNodeFromSystemName("CL11007", stcs1));
        assertNull(  memo1.getNodeFromSystemName("CL11B7", stcs1));
    }

    @Test
    public void testGetNodeFromSystemName2() {
        SerialNode d = new SerialNode(14, SerialNode.USIC_SUSIC, stcs2);
        SerialNode c = new SerialNode(17, SerialNode.SMINI, stcs2);
        SerialNode b = new SerialNode(127, SerialNode.SMINI, stcs2);
        assertEquals( d, memo2.getNodeFromSystemName("K2L14007", stcs2), "node of K2L14007");
        assertEquals( d, memo2.getNodeFromSystemName("K2L14B7", stcs2), "node of K2L14B7" );
        assertEquals( b, memo2.getNodeFromSystemName("K2L127007", stcs2), "node of K2L127007");
        assertEquals( b, memo2.getNodeFromSystemName("K2L127B7", stcs2), "node of K2L127B7");
        assertEquals( c, memo2.getNodeFromSystemName("K2L17007", stcs2), "node of K2L17007");
        assertEquals( c, memo2.getNodeFromSystemName("K2L17B7", stcs2), "node of K2L17B7" );
        assertNull( memo2.getNodeFromSystemName("K2L11007", stcs2), "node of K2L11007");
        assertNull( memo2.getNodeFromSystemName("K2L11B7", stcs2), "node of K2L11B7");
    }

    @Test
    public void testGetNodeAddressFromSystemName() {
        assertEquals( 14, memo1.getNodeAddressFromSystemName("CL14007"), "CL14007");
        assertEquals( 14, memo1.getNodeAddressFromSystemName("CL14B7"), "CL14B7");
        assertEquals( 127, memo1.getNodeAddressFromSystemName("CL127007"), "CL127007");
        assertEquals( 127, memo1.getNodeAddressFromSystemName("CL127B7"), "CL127B7");
        assertEquals( 0, memo1.getNodeAddressFromSystemName("CL0B7"), "CL0B7");
        assertEquals( 0, memo1.getNodeAddressFromSystemName("CL7"), "CL7");

        assertEquals( -1, memo1.getNodeAddressFromSystemName("CLB7"), "CLB7");
        JUnitAppender.assertWarnMessage("no node address before 'B' in CMRI system name: CLB7");

        assertEquals( -1, memo1.getNodeAddressFromSystemName("CR7"), "CR7");
        JUnitAppender.assertErrorMessage("invalid character in header field of system name: CR7");
    }

    @Test
    public void testGetNodeAddressFromSystemName2() {
        assertEquals( 14, memo2.getNodeAddressFromSystemName("K2L14007"), "K2L14007");
        assertEquals( 14, memo2.getNodeAddressFromSystemName("K2L14B7"), "K2L14B7");
        assertEquals( 127, memo2.getNodeAddressFromSystemName("K2L127007"), "K2L127007");
        assertEquals( 127, memo2.getNodeAddressFromSystemName("K2L127B7"), "K2L127B7");
        assertEquals( 0, memo2.getNodeAddressFromSystemName("K2L0B7"), "K2L0B7");
        assertEquals( 0, memo2.getNodeAddressFromSystemName("K2L7"), "K2L7");

        assertEquals( -1, memo2.getNodeAddressFromSystemName("K2LB7"), "K2LB7");
        JUnitAppender.assertWarnMessage("no node address before 'B' in CMRI system name: K2LB7");

        assertEquals( -1, memo2.getNodeAddressFromSystemName("K2R7"), "K2R7");
        JUnitAppender.assertErrorMessage("invalid character in header field of system name: K2R7");
    }

    @Test
    public void testValidSystemNameConfig() {
        SerialNode d = new SerialNode(4, SerialNode.USIC_SUSIC, stcs1);
        d.setNumBitsPerCard(32);
        d.setCardTypeByAddress(0, SerialNode.INPUT_CARD);
        d.setCardTypeByAddress(1, SerialNode.OUTPUT_CARD);
        d.setCardTypeByAddress(2, SerialNode.OUTPUT_CARD);
        d.setCardTypeByAddress(3, SerialNode.OUTPUT_CARD);
        d.setCardTypeByAddress(4, SerialNode.INPUT_CARD);
        d.setCardTypeByAddress(5, SerialNode.OUTPUT_CARD);

        SerialNode c = new SerialNode(10, SerialNode.SMINI, stcs1);
        assertNotNull( c, "exists");
        assertTrue( memo1.validSystemNameConfig("CL4007", 'L', stcs1), "valid config CL4007");
        assertTrue( memo1.validSystemNameConfig("CL4B7", 'L', stcs1), "valid config CL4B7");
        assertTrue( memo1.validSystemNameConfig("CS10007", 'S', stcs1), "valid config CS10007");
        assertTrue( memo1.validSystemNameConfig("CS10B7", 'S', stcs1), "valid config CS10B7");
        assertTrue( memo1.validSystemNameConfig("CL10048", 'L', stcs1), "valid config CL10048");
        assertTrue( memo1.validSystemNameConfig("CL10B48", 'L', stcs1), "valid config CL10B48");
        assertFalse( memo1.validSystemNameConfig("CL10049", 'L', stcs1), "invalid config CL10049");
        assertFalse( memo1.validSystemNameConfig("CL10B49", 'L', stcs1), "invalid config CL10B49");
        assertTrue( memo1.validSystemNameConfig("CS10024", 'S', stcs1), "valid config CS10024");
        assertTrue( memo1.validSystemNameConfig("CS10B24", 'S', stcs1), "valid config CS10B24");
        assertFalse( memo1.validSystemNameConfig("CS10025", 'S', stcs1), "invalid config CS10025");
        assertFalse( memo1.validSystemNameConfig("CS10B25", 'S', stcs1), "invalid config CS10B25");
        assertTrue( memo1.validSystemNameConfig("CT4128", 'T', stcs1), "valid config CT4128");
        assertTrue( memo1.validSystemNameConfig("CT4B128", 'T', stcs1), "valid config CT4B128");
        assertFalse( memo1.validSystemNameConfig("CT4129", 'T', stcs1), "invalid config CT4129");
        assertFalse( memo1.validSystemNameConfig("CT4B129", 'T', stcs1), "invalid config CT4129");
        assertTrue( memo1.validSystemNameConfig("CS4064", 'S', stcs1), "valid config CS4064");
        assertTrue( memo1.validSystemNameConfig("CS4B64", 'S', stcs1), "valid config CS4B64");
        assertFalse( memo1.validSystemNameConfig("CS4065", 'S', stcs1), "invalid config CS4065");
        assertFalse( memo1.validSystemNameConfig("CS4B65", 'S', stcs1), "invalid config CS4B65");
        assertFalse( memo1.validSystemNameConfig("CL11007", 'L', stcs1), "invalid config CL11007");
        assertFalse( memo1.validSystemNameConfig("CL11B7", 'L', stcs1), "invalid config CL11B7");
    }

    @Test
    public void testConvertSystemNameFormat() {
        assertEquals( "CL14B7", memo1.convertSystemNameToAlternate("CL14007"), "convert CL14007");
        assertEquals( "CS0B7", memo1.convertSystemNameToAlternate("CS7"), "convert CS7");
        assertEquals( "CT4B7", memo1.convertSystemNameToAlternate("CT4007"), "convert CT4007");
        assertEquals( "CL14007", memo1.convertSystemNameToAlternate("CL14B7"), "convert CL14B7");
        assertEquals( "CL7", memo1.convertSystemNameToAlternate("CL0B7"), "convert CL0B7");
        assertEquals( "CS4007", memo1.convertSystemNameToAlternate("CS4B7"), "convert CS4B7");
        assertEquals( "CL14008", memo1.convertSystemNameToAlternate("CL14B8"), "convert CL14B8");

        assertEquals( "", memo1.convertSystemNameToAlternate("CL128B7"), "convert CL128B7");
//        JUnitAppender.assertWarnMessage("node address field out of range in CMRI system name: CL128B7");
    }

    @Test
    public void testConvertSystemNameFormat2() {
        assertEquals( "K2L14B7", memo2.convertSystemNameToAlternate("K2L14007"), "convert K2L14007");
        assertEquals( "K2S0B7", memo2.convertSystemNameToAlternate("K2S7"), "convert K2S7");
        assertEquals( "K2T4B7", memo2.convertSystemNameToAlternate("K2T4007"), "convert K2T4007");
        assertEquals( "K2L14007", memo2.convertSystemNameToAlternate("K2L14B7"), "convert K2L14B7");
        assertEquals( "K2L7", memo2.convertSystemNameToAlternate("K2L0B7"), "convert K2L0B7");
        assertEquals( "K2S4007", memo2.convertSystemNameToAlternate("K2S4B7"), "convert K2S4B7");
        assertEquals( "K2L14008", memo2.convertSystemNameToAlternate("K2L14B8"), "convert K2L14B8");

        assertEquals( "", memo2.convertSystemNameToAlternate("K2L128B7"), "convert K2L128B7");
//        JUnitAppender.assertWarnMessage("node address field out of range in CMRI system name: K2L128B7");
    }

    @Test
    public void testNormalizeSystemName() {
        assertEquals( "CL14007", memo1.normalizeSystemName("CL14007"), "normalize CL14007");
        assertEquals( "CL7", memo1.normalizeSystemName("CL007"), "normalize CL007");
        assertEquals( "CL4007", memo1.normalizeSystemName("CL004007"), "normalize CL004007");
        assertEquals( "CL14B7", memo1.normalizeSystemName("CL14B7"), "normalize CL14B7");
        assertEquals( "CL0B7", memo1.normalizeSystemName("CL0B7"), "normalize CL0B7");
        assertEquals( "CL4B7", memo1.normalizeSystemName("CL004B7"), "normalize CL004B7");
        assertEquals( "CL14B8", memo1.normalizeSystemName("CL014B0008"), "normalize CL014B0008");

        assertEquals( "", memo1.normalizeSystemName("CL128B7"), "normalize CL128B7");
//        JUnitAppender.assertWarnMessage("node address field out of range in CMRI system name: CL128B7");
    }

    @Test
    public void testNormalizeSystemName2() {
        assertEquals( "K2L14007", memo2.normalizeSystemName("K2L14007"), "normalize K2L14007");
        assertEquals( "K2L7", memo2.normalizeSystemName("K2L007"), "normalize K2L007");
        assertEquals( "K2L4007", memo2.normalizeSystemName("K2L004007"), "normalize K2L004007");
        assertEquals( "K2L14B7", memo2.normalizeSystemName("K2L14B7"), "normalize K2L14B7");
        assertEquals( "K2L0B7", memo2.normalizeSystemName("K2L0B7"), "normalize K2L0B7");
        assertEquals( "K2L4B7", memo2.normalizeSystemName("K2L004B7"), "normalize K2L004B7");
        assertEquals( "K2L14B8", memo2.normalizeSystemName("K2L014B0008"), "normalize K2L014B0008");

        assertEquals( "", memo2.normalizeSystemName("K2L128B7"), "normalize K2L128B7");
//        JUnitAppender.assertWarnMessage("node address field out of range in CMRI system name: K2L128B7");
    }

    @Test
    public void testConstructSystemName() {
        assertEquals( "CL14007", memo1.makeSystemName("L", 14, 7), "make CL14007");
        assertEquals( "CT7", memo1.makeSystemName("T", 0, 7), "make CT7");

        assertEquals( "", memo1.makeSystemName("L", 0, 0), "make invalid 1");
        JUnitAppender.assertWarnMessage("invalid bit number proposed for system name");

        assertEquals( "", memo1.makeSystemName("L", 128, 7), "make invalid 2");
        JUnitAppender.assertWarnMessage("invalid node address proposed for system name");

        assertEquals( "", memo1.makeSystemName("R", 120, 7), "make invalid 3");
        JUnitAppender.assertErrorMessage("invalid type character proposed for system name");

        assertEquals( "CL0B1770", memo1.makeSystemName("L", 0, 1770), "make CL0B1770");
        assertEquals( "CS127999", memo1.makeSystemName("S", 127, 999), "make CS127999");
        assertEquals( "CS14B1000", memo1.makeSystemName("S", 14, 1000), "make CS14B1000");
    }

    @Test
    public void testConstructSystemName2() {
        assertEquals( "K2L14007", memo2.makeSystemName("L", 14, 7), "make K2L14007");
        assertEquals( "K2T7", memo2.makeSystemName("T", 0, 7), "make K2T7");

        assertEquals( "", memo2.makeSystemName("L", 0, 0), "make invalid 1");
        JUnitAppender.assertWarnMessage("invalid bit number proposed for system name");

        assertEquals( "", memo2.makeSystemName("L", 128, 7), "make invalid 2");
        JUnitAppender.assertWarnMessage("invalid node address proposed for system name");

        assertEquals( "", memo2.makeSystemName("R", 120, 7), "make invalid 3");
        JUnitAppender.assertErrorMessage("invalid type character proposed for system name");

        assertEquals( "K2L0B1770", memo2.makeSystemName("L", 0, 1770), "make K2L0B1770");
        assertEquals( "K2S127999", memo2.makeSystemName("S", 127, 999), "make K2S127999");
        assertEquals( "K2S14B1000", memo2.makeSystemName("S", 14, 1000), "make K2S14B1000");
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
        assertEquals( "", memo1.isOutputBitFree(18, 30), "test bit 30");
        assertEquals( "CT18034", memo1.isOutputBitFree(18, 34), "test bit 34");
        assertEquals( "", memo1.isOutputBitFree(18, 33), "test bit 33");
        assertEquals( "CT18034", memo1.isOutputBitFree(18, 35), "test bit 35");
        assertEquals( "CL18036", memo1.isOutputBitFree(18, 36), "test bit 36");
        assertEquals( "CL18037", memo1.isOutputBitFree(18, 37), "test bit 37");
        assertEquals( "", memo1.isOutputBitFree(18, 38), "test bit 38");
        assertEquals( "", memo1.isOutputBitFree(18, 39), "test bit 39");
        assertEquals( "", memo1.isOutputBitFree(18, 2000), "test bit 2000");

        assertEquals( "", memo1.isOutputBitFree(18, 0), "test bit bad bit");
        JUnitAppender.assertWarnMessage("invalid bit number in free bit test");

        assertEquals( "", memo1.isOutputBitFree(129, 34), "test bit bad node address");
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
        assertEquals( "", memo1.isInputBitFree(18, 10), "test bit 10");
        assertEquals( "", memo1.isInputBitFree(18, 11), "test bit 11");
        assertEquals( "CS18012", memo1.isInputBitFree(18, 12), "test bit 12");
        assertEquals( "", memo1.isInputBitFree(18, 13), "test bit 13");
        assertEquals( "CS18014", memo1.isInputBitFree(18, 14), "test bit 14");
        assertEquals( "", memo1.isInputBitFree(18, 15), "test bit 15");
        assertEquals( "CS18016", memo1.isInputBitFree(18, 16), "test bit 16");
        assertEquals( "CS18017", memo1.isInputBitFree(18, 17), "test bit 17");
        assertEquals( "", memo1.isInputBitFree(18, 18), "test bit 18");

        assertEquals( "", memo1.isInputBitFree(18, 0), "test bit bad bit");
        JUnitAppender.assertWarnMessage("invalid bit number in free bit test");

        assertEquals( "", memo1.isInputBitFree(129, 34), "test bit bad node address");
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

        assertEquals( "userS16", memo1.getUserNameFromSystemName("CS18016"), "test CS18016");
        assertEquals( "userS12", memo1.getUserNameFromSystemName("CS18012"), "test CS18012");
        assertEquals( "userS17", memo1.getUserNameFromSystemName("CS18017"), "test CS18017");
        assertEquals( "", memo1.getUserNameFromSystemName("CS18010"), "test undefined CS18010");
        assertEquals( "userL37", memo1.getUserNameFromSystemName("CL18037"), "test CL18037");
        assertEquals( "userL36", memo1.getUserNameFromSystemName("CL18036"), "test CL18036");
        assertEquals( "", memo1.getUserNameFromSystemName("CL18030"), "test undefined CL18030");
        assertEquals( "userT32", memo1.getUserNameFromSystemName("CT18032"), "test CT18032");
        assertEquals( "userT34", memo1.getUserNameFromSystemName("CT18034"), "test CT18034");
        assertEquals( "", memo1.getUserNameFromSystemName("CT18039"), "test undefined CT18039");
    }

    @Test
    public void testGetUserNameFromSystemName2() {
        jmri.SensorManager sMgr = jmri.InstanceManager.sensorManagerInstance();
        // create 4 new sensors
        sMgr.newSensor("K2S20016", "userS16");
        sMgr.newSensor("K2S20014", "userS14");
        sMgr.newSensor("K2S20017", "userS17");
        sMgr.newSensor("K2S20012", "userS12");

        jmri.LightManager lMgr = jmri.InstanceManager.lightManagerInstance();
        lMgr.newLight("K2L20036", "userL36");
        lMgr.newLight("K2L20037", "userL37");

        jmri.TurnoutManager tMgr = jmri.InstanceManager.turnoutManagerInstance();
        tMgr.newTurnout("K2T20032", "userT32");
        tMgr.newTurnout("K2T20034", "userT34");

        assertEquals( "userS16", memo2.getUserNameFromSystemName("K2S20016"), "test K2S20016");
        assertEquals( "userS12", memo2.getUserNameFromSystemName("K2S20012"), "test K2S20012");
        assertEquals( "userS17", memo2.getUserNameFromSystemName("K2S20017"), "test K2S20017");
        assertEquals( "", memo2.getUserNameFromSystemName("K2S20010"), "test undefined K2S18010");
        assertEquals( "userL37", memo2.getUserNameFromSystemName("K2L20037"), "test K2L20037");
        assertEquals( "userL36", memo2.getUserNameFromSystemName("K2L20036"), "test K2L20036");
        assertEquals( "", memo2.getUserNameFromSystemName("K2L20030"), "test undefined K2L20030");
        assertEquals( "userT32", memo2.getUserNameFromSystemName("K2T20032"), "test K2T20032");
        assertEquals( "userT34", memo2.getUserNameFromSystemName("K2T20034"), "test K2T20034");
        assertEquals( "", memo2.getUserNameFromSystemName("K2T20039"), "test undefined K2T20039");
    }

}
