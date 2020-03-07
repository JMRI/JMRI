package jmri.jmrix.cmri.serial;

import jmri.Manager.NameValidity;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the serial address functions in memo1.
 * <p>
 * These used to be in a separate SerialAddress class, with its own test class.
 * This structure is a vestige of that.
 *
 * @author	Dave Duchamp Copyright 2004
 * @author Bob Jacobsen Copyright 2017
 */
public class SerialAddressTwoSystemTest {

    private jmri.jmrix.cmri.CMRISystemConnectionMemo memo1 = null;
    private SerialTrafficControlScaffold stcs1 = null;

    SerialNode c10;
    SerialNode c18;

    private jmri.jmrix.cmri.CMRISystemConnectionMemo memo2 = null;
    private SerialTrafficControlScaffold stcs2 = null;

    SerialNode k10;
    SerialNode k20;

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();

        // replace the 1st SerialTrafficController
        stcs1 = new SerialTrafficControlScaffold();
        memo1 = new jmri.jmrix.cmri.CMRISystemConnectionMemo();
        memo1.setTrafficController(stcs1);
        // create 1st nodes
        c10 = new SerialNode(10, SerialNode.SMINI, stcs1);
        c18 = new SerialNode(18, SerialNode.SMINI, stcs1);
        // create and register the 1st manager objects
        jmri.TurnoutManager l1 = new SerialTurnoutManager(memo1) {
            @Override
            public void notifyTurnoutCreationError(String conflict, int bitNum) {
            }
        };
        jmri.InstanceManager.setTurnoutManager(l1);
        jmri.LightManager lgt1 = new SerialLightManager(memo1) {
            @Override
            public void notifyLightCreationError(String conflict, int bitNum) {
            }
        };
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
        // create and register the 1st manager objects
        jmri.TurnoutManager l2 = new SerialTurnoutManager(memo2) {
            @Override
            public void notifyTurnoutCreationError(String conflict, int bitNum) {
            }
        };
        jmri.InstanceManager.setTurnoutManager(l2);
        jmri.LightManager lgt2 = new SerialLightManager(memo2) {
            @Override
            public void notifyLightCreationError(String conflict, int bitNum) {
            }
        };
        jmri.InstanceManager.setLightManager(lgt2);
        jmri.SensorManager s2 = new SerialSensorManager(memo2);
        jmri.InstanceManager.setSensorManager(s2);

    }

    @After
    public void tearDown() throws Exception {
        if (stcs1 != null) stcs1.terminateThreads();
        stcs1 = null;
        memo1 = null;
        if (stcs2 != null) stcs2.terminateThreads();
        stcs2 = null;
        memo2 = null;

	    JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    @Test
    public void testValidSystemNameFormat() {
        Assert.assertTrue("valid format - CL2", NameValidity.VALID == memo1.validSystemNameFormat("CL2", 'L'));
        Assert.assertTrue("valid format - CL0B2", NameValidity.VALID == memo1.validSystemNameFormat("CL0B2", 'L'));

        Assert.assertTrue("invalid format - CL", NameValidity.VALID != memo1.validSystemNameFormat("CL", 'L'));
//        JUnitAppender.assertWarnMessage("invalid character in number field of CMRI system name: CL");

        Assert.assertTrue("invalid format - CLB2", NameValidity.VALID != memo1.validSystemNameFormat("CLB2", 'L'));
//        JUnitAppender.assertWarnMessage("no node address before 'B' in CMRI system name: CLB2");

        Assert.assertTrue("valid format - CL2005", NameValidity.VALID == memo1.validSystemNameFormat("CL2005", 'L'));
        Assert.assertTrue("valid format - CL2B5", NameValidity.VALID == memo1.validSystemNameFormat("CL2B5", 'L'));
        Assert.assertTrue("valid format - CT2005", NameValidity.VALID == memo1.validSystemNameFormat("CT2005", 'T'));
        Assert.assertTrue("valid format - CT2B5", NameValidity.VALID == memo1.validSystemNameFormat("CT2B5", 'T'));
        Assert.assertTrue("valid format - CS2005", NameValidity.VALID == memo1.validSystemNameFormat("CS2005", 'S'));
        Assert.assertTrue("valid format - CS2B5", NameValidity.VALID == memo1.validSystemNameFormat("CS2B5", 'S'));

        Assert.assertTrue("invalid format - CY2005", NameValidity.VALID != memo1.validSystemNameFormat("CY2005", 'L'));
//        JUnitAppender.assertErrorMessage("invalid type character in CMRI system name: CY2005");

        Assert.assertTrue("invalid format - CY2B5", NameValidity.VALID != memo1.validSystemNameFormat("CY2B5", 'L'));
//        JUnitAppender.assertErrorMessage("invalid type character in CMRI system name: CY2B5");

        Assert.assertTrue("valid format - CL22001", NameValidity.VALID == memo1.validSystemNameFormat("CL22001", 'L'));
        Assert.assertTrue("valid format - CL22B1", NameValidity.VALID == memo1.validSystemNameFormat("CL22B1", 'L'));

        Assert.assertTrue("invalid format - CL22000", NameValidity.VALID != memo1.validSystemNameFormat("CL22000", 'L'));
//        JUnitAppender.assertWarnMessage("bit number not in range 1 - 999 in CMRI system name: CL22000");

        Assert.assertTrue("invalid format - CL22B0", NameValidity.VALID != memo1.validSystemNameFormat("CL22B0", 'L'));
//        JUnitAppender.assertWarnMessage("bit number field out of range in CMRI system name: CL22B0");

        Assert.assertTrue("valid format - CL2999", NameValidity.VALID == memo1.validSystemNameFormat("CL2999", 'L'));
        Assert.assertTrue("valid format - CL2B2048", NameValidity.VALID == memo1.validSystemNameFormat("CL2B2048", 'L'));

        Assert.assertTrue("invalid format - CL2B2049", NameValidity.VALID != memo1.validSystemNameFormat("CL2B2049", 'L'));
//        JUnitAppender.assertWarnMessage("bit number field out of range in CMRI system name: CL2B2049");

        Assert.assertTrue("valid format - CL127999", NameValidity.VALID == memo1.validSystemNameFormat("CL127999", 'L'));

        Assert.assertTrue("invalid format - CL128000", NameValidity.VALID != memo1.validSystemNameFormat("CL128000", 'L'));
//        JUnitAppender.assertWarnMessage("number field out of range in CMRI system name: CL128000");

        Assert.assertTrue("valid format - CL127B7", NameValidity.VALID == memo1.validSystemNameFormat("CL127B7", 'L'));

        Assert.assertTrue("invalid format - CL128B7", NameValidity.VALID != memo1.validSystemNameFormat("CL128B7", 'L'));
//        JUnitAppender.assertWarnMessage("node address field out of range in CMRI system name: CL128B7");

        Assert.assertTrue("invalid format - CL2oo5", NameValidity.VALID != memo1.validSystemNameFormat("CL2oo5", 'L'));
//        JUnitAppender.assertWarnMessage("invalid character in number field of CMRI system name: CL2oo5");

        Assert.assertTrue("invalid format - CL2aB5", NameValidity.VALID != memo1.validSystemNameFormat("CL2aB5", 'L'));
//        JUnitAppender.assertWarnMessage("invalid character in node address field of CMRI system name: CL2aB5");

        Assert.assertTrue("invalid format - CL2B5x", NameValidity.VALID != memo1.validSystemNameFormat("CL2B5x", 'L'));
//        JUnitAppender.assertWarnMessage("invalid character in bit number field of CMRI system name: CL2B5x");
    }

    @Test
    public void testGetBitFromSystemName() {
        Assert.assertEquals("CL2", 2, memo1.getBitFromSystemName("CL2"));
        Assert.assertEquals("CL2002", 2, memo1.getBitFromSystemName("CL2002"));
        Assert.assertEquals("CL1", 1, memo1.getBitFromSystemName("CL1"));
        Assert.assertEquals("CL2001", 1, memo1.getBitFromSystemName("CL2001"));
        Assert.assertEquals("CL999", 999, memo1.getBitFromSystemName("CL999"));
        Assert.assertEquals("CL2999", 999, memo1.getBitFromSystemName("CL2999"));

        Assert.assertEquals("CL29O9", 0, memo1.getBitFromSystemName("CL29O9"));
//        JUnitAppender.assertWarnMessage("invalid character in number field of system name: CL29O9");

        Assert.assertEquals("CL0B7", 7, memo1.getBitFromSystemName("CL0B7"));
        Assert.assertEquals("CL2B7", 7, memo1.getBitFromSystemName("CL2B7"));
        Assert.assertEquals("CL0B1", 1, memo1.getBitFromSystemName("CL0B1"));
        Assert.assertEquals("CL2B1", 1, memo1.getBitFromSystemName("CL2B1"));
        Assert.assertEquals("CL0B2048", 2048, memo1.getBitFromSystemName("CL0B2048"));
        Assert.assertEquals("CL11B2048", 2048, memo1.getBitFromSystemName("CL11B2048"));
    }

    @Test
    public void testGetBitFromSystemName2() {
        Assert.assertEquals("K2L2", 2, memo2.getBitFromSystemName("K2L2"));
        Assert.assertEquals("K2L2002", 2, memo2.getBitFromSystemName("K2L2002"));
        Assert.assertEquals("K2L1", 1, memo2.getBitFromSystemName("K2L1"));
        Assert.assertEquals("K2L2001", 1, memo2.getBitFromSystemName("K2L2001"));
        Assert.assertEquals("K2L999", 999, memo2.getBitFromSystemName("K2L999"));
        Assert.assertEquals("K2L2999", 999, memo2.getBitFromSystemName("K2L2999"));

        Assert.assertEquals("K2L29O9", 0, memo2.getBitFromSystemName("K2L29O9"));
//        JUnitAppender.assertWarnMessage("invalid character in number field of system name: K2L29O9");

        Assert.assertEquals("K2L0B7", 7, memo2.getBitFromSystemName("K2L0B7"));
        Assert.assertEquals("K2L2B7", 7, memo2.getBitFromSystemName("K2L2B7"));
        Assert.assertEquals("K2L0B1", 1, memo2.getBitFromSystemName("K2L0B1"));
        Assert.assertEquals("K2L2B1", 1, memo2.getBitFromSystemName("K2L2B1"));
        Assert.assertEquals("K2L0B2048", 2048, memo2.getBitFromSystemName("K2L0B2048"));
        Assert.assertEquals("K2L11B2048", 2048, memo2.getBitFromSystemName("K2L11B2048"));
    }

    @Test
    public void testGetNodeFromSystemName() {
        SerialNode d = new SerialNode(14, SerialNode.USIC_SUSIC, stcs1);
        SerialNode c = new SerialNode(17, SerialNode.SMINI, stcs1);
        SerialNode b = new SerialNode(127, SerialNode.SMINI, stcs1);
        Assert.assertEquals("node of CL14007", d, memo1.getNodeFromSystemName("CL14007", stcs1));
        Assert.assertEquals("node of CL14B7", d, memo1.getNodeFromSystemName("CL14B7", stcs1));
        Assert.assertEquals("node of CL127007", b, memo1.getNodeFromSystemName("CL127007", stcs1));
        Assert.assertEquals("node of CL127B7", b, memo1.getNodeFromSystemName("CL127B7", stcs1));
        Assert.assertEquals("node of CL17007", c, memo1.getNodeFromSystemName("CL17007", stcs1));
        Assert.assertEquals("node of CL17B7", c, memo1.getNodeFromSystemName("CL17B7", stcs1));
        Assert.assertEquals("node of CL11007", null, memo1.getNodeFromSystemName("CL11007", stcs1));
        Assert.assertEquals("node of CL11B7", null, memo1.getNodeFromSystemName("CL11B7", stcs1));
    }

    @Test
    public void testGetNodeFromSystemName2() {
        SerialNode d = new SerialNode(14, SerialNode.USIC_SUSIC, stcs2);
        SerialNode c = new SerialNode(17, SerialNode.SMINI, stcs2);
        SerialNode b = new SerialNode(127, SerialNode.SMINI, stcs2);
        Assert.assertEquals("node of K2L14007", d, memo2.getNodeFromSystemName("K2L14007", stcs2));
        Assert.assertEquals("node of K2L14B7", d, memo2.getNodeFromSystemName("K2L14B7", stcs2));
        Assert.assertEquals("node of K2L127007", b, memo2.getNodeFromSystemName("K2L127007", stcs2));
        Assert.assertEquals("node of K2L127B7", b, memo2.getNodeFromSystemName("K2L127B7", stcs2));
        Assert.assertEquals("node of K2L17007", c, memo2.getNodeFromSystemName("K2L17007", stcs2));
        Assert.assertEquals("node of K2L17B7", c, memo2.getNodeFromSystemName("K2L17B7", stcs2));
        Assert.assertEquals("node of K2L11007", null, memo2.getNodeFromSystemName("K2L11007", stcs2));
        Assert.assertEquals("node of K2L11B7", null, memo2.getNodeFromSystemName("K2L11B7", stcs2));
    }

    @Test
    public void testGetNodeAddressFromSystemName() {
        Assert.assertEquals("CL14007", 14, memo1.getNodeAddressFromSystemName("CL14007"));
        Assert.assertEquals("CL14B7", 14, memo1.getNodeAddressFromSystemName("CL14B7"));
        Assert.assertEquals("CL127007", 127, memo1.getNodeAddressFromSystemName("CL127007"));
        Assert.assertEquals("CL127B7", 127, memo1.getNodeAddressFromSystemName("CL127B7"));
        Assert.assertEquals("CL0B7", 0, memo1.getNodeAddressFromSystemName("CL0B7"));
        Assert.assertEquals("CL7", 0, memo1.getNodeAddressFromSystemName("CL7"));

        Assert.assertEquals("CLB7", -1, memo1.getNodeAddressFromSystemName("CLB7"));
        JUnitAppender.assertWarnMessage("no node address before 'B' in CMRI system name: CLB7");

        Assert.assertEquals("CR7", -1, memo1.getNodeAddressFromSystemName("CR7"));
        JUnitAppender.assertErrorMessage("invalid character in header field of system name: CR7");
    }

    @Test
    public void testGetNodeAddressFromSystemName2() {
        Assert.assertEquals("K2L14007", 14, memo2.getNodeAddressFromSystemName("K2L14007"));
        Assert.assertEquals("K2L14B7", 14, memo2.getNodeAddressFromSystemName("K2L14B7"));
        Assert.assertEquals("K2L127007", 127, memo2.getNodeAddressFromSystemName("K2L127007"));
        Assert.assertEquals("K2L127B7", 127, memo2.getNodeAddressFromSystemName("K2L127B7"));
        Assert.assertEquals("K2L0B7", 0, memo2.getNodeAddressFromSystemName("K2L0B7"));
        Assert.assertEquals("K2L7", 0, memo2.getNodeAddressFromSystemName("K2L7"));

        Assert.assertEquals("K2LB7", -1, memo2.getNodeAddressFromSystemName("K2LB7"));
        JUnitAppender.assertWarnMessage("no node address before 'B' in CMRI system name: K2LB7");

        Assert.assertEquals("K2R7", -1, memo2.getNodeAddressFromSystemName("K2R7"));
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
        Assert.assertNotNull("exists", c);
        Assert.assertTrue("valid config CL4007", memo1.validSystemNameConfig("CL4007", 'L', stcs1));
        Assert.assertTrue("valid config CL4B7", memo1.validSystemNameConfig("CL4B7", 'L', stcs1));
        Assert.assertTrue("valid config CS10007", memo1.validSystemNameConfig("CS10007", 'S', stcs1));
        Assert.assertTrue("valid config CS10B7", memo1.validSystemNameConfig("CS10B7", 'S', stcs1));
        Assert.assertTrue("valid config CL10048", memo1.validSystemNameConfig("CL10048", 'L', stcs1));
        Assert.assertTrue("valid config CL10B48", memo1.validSystemNameConfig("CL10B48", 'L', stcs1));
        Assert.assertTrue("invalid config CL10049", !memo1.validSystemNameConfig("CL10049", 'L', stcs1));
        Assert.assertTrue("invalid config CL10B49", !memo1.validSystemNameConfig("CL10B49", 'L', stcs1));
        Assert.assertTrue("valid config CS10024", memo1.validSystemNameConfig("CS10024", 'S', stcs1));
        Assert.assertTrue("valid config CS10B24", memo1.validSystemNameConfig("CS10B24", 'S', stcs1));
        Assert.assertTrue("invalid config CS10025", !memo1.validSystemNameConfig("CS10025", 'S', stcs1));
        Assert.assertTrue("invalid config CS10B25", !memo1.validSystemNameConfig("CS10B25", 'S', stcs1));
        Assert.assertTrue("valid config CT4128", memo1.validSystemNameConfig("CT4128", 'T', stcs1));
        Assert.assertTrue("valid config CT4B128", memo1.validSystemNameConfig("CT4B128", 'T', stcs1));
        Assert.assertTrue("invalid config CT4129", !memo1.validSystemNameConfig("CT4129", 'T', stcs1));
        Assert.assertTrue("invalid config CT4129", !memo1.validSystemNameConfig("CT4B129", 'T', stcs1));
        Assert.assertTrue("valid config CS4064", memo1.validSystemNameConfig("CS4064", 'S', stcs1));
        Assert.assertTrue("valid config CS4B64", memo1.validSystemNameConfig("CS4B64", 'S', stcs1));
        Assert.assertTrue("invalid config CS4065", !memo1.validSystemNameConfig("CS4065", 'S', stcs1));
        Assert.assertTrue("invalid config CS4B65", !memo1.validSystemNameConfig("CS4B65", 'S', stcs1));
        Assert.assertTrue("invalid config CL11007", !memo1.validSystemNameConfig("CL11007", 'L', stcs1));
        Assert.assertTrue("invalid config CL11B7", !memo1.validSystemNameConfig("CL11B7", 'L', stcs1));
    }

    @Test
    public void testConvertSystemNameFormat() {
        Assert.assertEquals("convert CL14007", "CL14B7", memo1.convertSystemNameToAlternate("CL14007"));
        Assert.assertEquals("convert CS7", "CS0B7", memo1.convertSystemNameToAlternate("CS7"));
        Assert.assertEquals("convert CT4007", "CT4B7", memo1.convertSystemNameToAlternate("CT4007"));
        Assert.assertEquals("convert CL14B7", "CL14007", memo1.convertSystemNameToAlternate("CL14B7"));
        Assert.assertEquals("convert CL0B7", "CL7", memo1.convertSystemNameToAlternate("CL0B7"));
        Assert.assertEquals("convert CS4B7", "CS4007", memo1.convertSystemNameToAlternate("CS4B7"));
        Assert.assertEquals("convert CL14B8", "CL14008", memo1.convertSystemNameToAlternate("CL14B8"));

        Assert.assertEquals("convert CL128B7", "", memo1.convertSystemNameToAlternate("CL128B7"));
//        JUnitAppender.assertWarnMessage("node address field out of range in CMRI system name: CL128B7");
    }

    @Test
    public void testConvertSystemNameFormat2() {
        Assert.assertEquals("convert K2L14007", "K2L14B7", memo2.convertSystemNameToAlternate("K2L14007"));
        Assert.assertEquals("convert K2S7", "K2S0B7", memo2.convertSystemNameToAlternate("K2S7"));
        Assert.assertEquals("convert K2T4007", "K2T4B7", memo2.convertSystemNameToAlternate("K2T4007"));
        Assert.assertEquals("convert K2L14B7", "K2L14007", memo2.convertSystemNameToAlternate("K2L14B7"));
        Assert.assertEquals("convert K2L0B7", "K2L7", memo2.convertSystemNameToAlternate("K2L0B7"));
        Assert.assertEquals("convert K2S4B7", "K2S4007", memo2.convertSystemNameToAlternate("K2S4B7"));
        Assert.assertEquals("convert K2L14B8", "K2L14008", memo2.convertSystemNameToAlternate("K2L14B8"));

        Assert.assertEquals("convert K2L128B7", "", memo2.convertSystemNameToAlternate("K2L128B7"));
//        JUnitAppender.assertWarnMessage("node address field out of range in CMRI system name: K2L128B7");
    }

    @Test
    public void testNormalizeSystemName() {
        Assert.assertEquals("normalize CL14007", "CL14007", memo1.normalizeSystemName("CL14007"));
        Assert.assertEquals("normalize CL007", "CL7", memo1.normalizeSystemName("CL007"));
        Assert.assertEquals("normalize CL004007", "CL4007", memo1.normalizeSystemName("CL004007"));
        Assert.assertEquals("normalize CL14B7", "CL14B7", memo1.normalizeSystemName("CL14B7"));
        Assert.assertEquals("normalize CL0B7", "CL0B7", memo1.normalizeSystemName("CL0B7"));
        Assert.assertEquals("normalize CL004B7", "CL4B7", memo1.normalizeSystemName("CL004B7"));
        Assert.assertEquals("normalize CL014B0008", "CL14B8", memo1.normalizeSystemName("CL014B0008"));

        Assert.assertEquals("normalize CL128B7", "", memo1.normalizeSystemName("CL128B7"));
//        JUnitAppender.assertWarnMessage("node address field out of range in CMRI system name: CL128B7");
    }

    @Test
    public void testNormalizeSystemName2() {
        Assert.assertEquals("normalize K2L14007", "K2L14007", memo2.normalizeSystemName("K2L14007"));
        Assert.assertEquals("normalize K2L007", "K2L7", memo2.normalizeSystemName("K2L007"));
        Assert.assertEquals("normalize K2L004007", "K2L4007", memo2.normalizeSystemName("K2L004007"));
        Assert.assertEquals("normalize K2L14B7", "K2L14B7", memo2.normalizeSystemName("K2L14B7"));
        Assert.assertEquals("normalize K2L0B7", "K2L0B7", memo2.normalizeSystemName("K2L0B7"));
        Assert.assertEquals("normalize K2L004B7", "K2L4B7", memo2.normalizeSystemName("K2L004B7"));
        Assert.assertEquals("normalize K2L014B0008", "K2L14B8", memo2.normalizeSystemName("K2L014B0008"));

        Assert.assertEquals("normalize K2L128B7", "", memo2.normalizeSystemName("K2L128B7"));
//        JUnitAppender.assertWarnMessage("node address field out of range in CMRI system name: K2L128B7");
    }

    @Test
    public void testConstructSystemName() {
        Assert.assertEquals("make CL14007", "CL14007", memo1.makeSystemName("L", 14, 7));
        Assert.assertEquals("make CT7", "CT7", memo1.makeSystemName("T", 0, 7));

        Assert.assertEquals("make invalid 1", "", memo1.makeSystemName("L", 0, 0));
        JUnitAppender.assertWarnMessage("invalid bit number proposed for system name");

        Assert.assertEquals("make invalid 2", "", memo1.makeSystemName("L", 128, 7));
        JUnitAppender.assertWarnMessage("invalid node address proposed for system name");

        Assert.assertEquals("make invalid 3", "", memo1.makeSystemName("R", 120, 7));
        JUnitAppender.assertErrorMessage("invalid type character proposed for system name");

        Assert.assertEquals("make CL0B1770", "CL0B1770", memo1.makeSystemName("L", 0, 1770));
        Assert.assertEquals("make CS127999", "CS127999", memo1.makeSystemName("S", 127, 999));
        Assert.assertEquals("make CS14B1000", "CS14B1000", memo1.makeSystemName("S", 14, 1000));
    }

    @Test
    public void testConstructSystemName2() {
        Assert.assertEquals("make K2L14007", "K2L14007", memo2.makeSystemName("L", 14, 7));
        Assert.assertEquals("make K2T7", "K2T7", memo2.makeSystemName("T", 0, 7));

        Assert.assertEquals("make invalid 1", "", memo2.makeSystemName("L", 0, 0));
        JUnitAppender.assertWarnMessage("invalid bit number proposed for system name");

        Assert.assertEquals("make invalid 2", "", memo2.makeSystemName("L", 128, 7));
        JUnitAppender.assertWarnMessage("invalid node address proposed for system name");

        Assert.assertEquals("make invalid 3", "", memo2.makeSystemName("R", 120, 7));
        JUnitAppender.assertErrorMessage("invalid type character proposed for system name");

        Assert.assertEquals("make K2L0B1770", "K2L0B1770", memo2.makeSystemName("L", 0, 1770));
        Assert.assertEquals("make K2S127999", "K2S127999", memo2.makeSystemName("S", 127, 999));
        Assert.assertEquals("make K2S14B1000", "K2S14B1000", memo2.makeSystemName("S", 14, 1000));
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
        Assert.assertEquals("test bit 30", "", memo1.isOutputBitFree(18, 30));
        Assert.assertEquals("test bit 34", "CT18034", memo1.isOutputBitFree(18, 34));
        Assert.assertEquals("test bit 33", "", memo1.isOutputBitFree(18, 33));
        Assert.assertEquals("test bit 35", "CT18034", memo1.isOutputBitFree(18, 35));
        Assert.assertEquals("test bit 36", "CL18036", memo1.isOutputBitFree(18, 36));
        Assert.assertEquals("test bit 37", "CL18037", memo1.isOutputBitFree(18, 37));
        Assert.assertEquals("test bit 38", "", memo1.isOutputBitFree(18, 38));
        Assert.assertEquals("test bit 39", "", memo1.isOutputBitFree(18, 39));
        Assert.assertEquals("test bit 2000", "", memo1.isOutputBitFree(18, 2000));

        Assert.assertEquals("test bit bad bit", "", memo1.isOutputBitFree(18, 0));
        JUnitAppender.assertWarnMessage("invalid bit number in free bit test");

        Assert.assertEquals("test bit bad node address", "", memo1.isOutputBitFree(129, 34));
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
        Assert.assertEquals("test bit 10", "", memo1.isInputBitFree(18, 10));
        Assert.assertEquals("test bit 11", "", memo1.isInputBitFree(18, 11));
        Assert.assertEquals("test bit 12", "CS18012", memo1.isInputBitFree(18, 12));
        Assert.assertEquals("test bit 13", "", memo1.isInputBitFree(18, 13));
        Assert.assertEquals("test bit 14", "CS18014", memo1.isInputBitFree(18, 14));
        Assert.assertEquals("test bit 15", "", memo1.isInputBitFree(18, 15));
        Assert.assertEquals("test bit 16", "CS18016", memo1.isInputBitFree(18, 16));
        Assert.assertEquals("test bit 17", "CS18017", memo1.isInputBitFree(18, 17));
        Assert.assertEquals("test bit 18", "", memo1.isInputBitFree(18, 18));

        Assert.assertEquals("test bit bad bit", "", memo1.isInputBitFree(18, 0));
        JUnitAppender.assertWarnMessage("invalid bit number in free bit test");

        Assert.assertEquals("test bit bad node address", "", memo1.isInputBitFree(129, 34));
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

        Assert.assertEquals("test CS18016", "userS16", memo1.getUserNameFromSystemName("CS18016"));
        Assert.assertEquals("test CS18012", "userS12", memo1.getUserNameFromSystemName("CS18012"));
        Assert.assertEquals("test CS18017", "userS17", memo1.getUserNameFromSystemName("CS18017"));
        Assert.assertEquals("test undefined CS18010", "", memo1.getUserNameFromSystemName("CS18010"));
        Assert.assertEquals("test CL18037", "userL37", memo1.getUserNameFromSystemName("CL18037"));
        Assert.assertEquals("test CL18036", "userL36", memo1.getUserNameFromSystemName("CL18036"));
        Assert.assertEquals("test undefined CL18030", "", memo1.getUserNameFromSystemName("CL18030"));
        Assert.assertEquals("test CT18032", "userT32", memo1.getUserNameFromSystemName("CT18032"));
        Assert.assertEquals("test CT18034", "userT34", memo1.getUserNameFromSystemName("CT18034"));
        Assert.assertEquals("test undefined CT18039", "", memo1.getUserNameFromSystemName("CT18039"));
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

        Assert.assertEquals("test K2S20016", "userS16", memo2.getUserNameFromSystemName("K2S20016"));
        Assert.assertEquals("test K2S20012", "userS12", memo2.getUserNameFromSystemName("K2S20012"));
        Assert.assertEquals("test K2S20017", "userS17", memo2.getUserNameFromSystemName("K2S20017"));
        Assert.assertEquals("test undefined K2S18010", "", memo2.getUserNameFromSystemName("K2S20010"));
        Assert.assertEquals("test K2L20037", "userL37", memo2.getUserNameFromSystemName("K2L20037"));
        Assert.assertEquals("test K2L20036", "userL36", memo2.getUserNameFromSystemName("K2L20036"));
        Assert.assertEquals("test undefined K2L20030", "", memo2.getUserNameFromSystemName("K2L20030"));
        Assert.assertEquals("test K2T20032", "userT32", memo2.getUserNameFromSystemName("K2T20032"));
        Assert.assertEquals("test K2T20034", "userT34", memo2.getUserNameFromSystemName("K2T20034"));
        Assert.assertEquals("test undefined K2T20039", "", memo2.getUserNameFromSystemName("K2T20039"));
    }

}
