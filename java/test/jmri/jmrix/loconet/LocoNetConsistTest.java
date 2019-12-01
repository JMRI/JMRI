package jmri.jmrix.loconet;

import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmrit.consisttool.ConsistPreferencesManager;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author	Paul Bender Copyright (C) 2016,2017
 */

public class LocoNetConsistTest extends jmri.implementation.AbstractConsistTestBase {

    // infrastructure objects, populated by setUp;
    LocoNetInterfaceScaffold lnis;
    SlotManager slotmanager;
    LocoNetSystemConnectionMemo memo;
    LnThrottleManager ltm;

    //utility function, handle slot messages required to suppress
    // errors from the LnThrottleManager after constructor call.
    private void ReturnSlotInfo(){
               // echo of the original message
        LocoNetMessage m1 = new LocoNetMessage(4);
        m1.setOpCode(0xBF);
        m1.setElement(1, 0x00);
        m1.setElement(2, 0x03);
        slotmanager.message(m1);
        // reply says its in slot 3
        LocoNetMessage m2 = new LocoNetMessage(14);
        m2.setElement(0, 0xe7);
        m2.setElement(1, 0xe);
        m2.setElement(2, 3);
        m2.setElement(3, 3);
        m2.setElement(4, 3);
        m2.setElement(5, 0);
        m2.setElement(6, 0);
        m2.setElement(7, 0);
        m2.setElement(8, 0);
        m2.setElement(9, 0);
        m2.setElement(10, 0);
        m2.setElement(11, 0);
        m2.setElement(12, 0);
        m2.setElement(13, 0x15);
        slotmanager.message(m2);
    }

    @Test public void testCtor2() {
        // DccLocoAddress constructor test.
        LocoNetConsist c = new LocoNetConsist(new DccLocoAddress(3, false),memo);
        ReturnSlotInfo();
        Assert.assertNotNull(c);
    }

    @Test
    @Override
    public void testGetConsistType(){
        // LocoNet consists default to CS consists.
        Assert.assertEquals("default consist type",jmri.Consist.CS_CONSIST,c.getConsistType());
    }

    @Test
    @Override
    public void testSetConsistTypeCS(){
        c.setConsistType(jmri.Consist.CS_CONSIST);
        Assert.assertEquals("default consist type",jmri.Consist.CS_CONSIST,c.getConsistType());
    }

    @Override
    @Test public void checkAddressAllowedBad(){
        // LocoNet CS consists allow any valid address, so this test is empty
    }

    @Test public void checkAddressAllowedGoodAdvanced(){
        LocoNetConsist c = new LocoNetConsist(3,memo);
        ReturnSlotInfo();
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        Assert.assertTrue("AddressAllowed", c.isAddressAllowed(new jmri.DccLocoAddress(200,true)));
    }

    @Test public void checkAddressAllowedBadAdvanced(){
        LocoNetConsist c = new LocoNetConsist(3,memo);
        ReturnSlotInfo();
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        Assert.assertFalse("AddressAllowed", c.isAddressAllowed(new jmri.DccLocoAddress(0,false)));
    }

    @Test public void checkSizeLimitCS(){
        c.setConsistType(jmri.Consist.CS_CONSIST);
        Assert.assertEquals("CS Consist Limit",-1,c.sizeLimit());
    }

    @Test public void checkGetLocoDirectionCS(){
        LocoNetConsist c = new LocoNetConsist(3,memo);
        ReturnSlotInfo();
        c.setConsistType(jmri.Consist.CS_CONSIST);
        jmri.DccLocoAddress A = new jmri.DccLocoAddress(200,true);
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250,true);
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        c.restore(B,false); // revese direction.
        Assert.assertTrue("Direction in CS Consist", c.getLocoDirection(A));
        Assert.assertFalse("Direction in CS Consist", c.getLocoDirection(B));
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        InstanceManager.setDefault(ConsistPreferencesManager.class,new ConsistPreferencesManager());
        // prepare an interface
        lnis = new LocoNetInterfaceScaffold();
        slotmanager = new SlotManager(lnis);
        memo = new LocoNetSystemConnectionMemo(lnis, slotmanager);
        ltm = new LnThrottleManager(memo);
        memo.setThrottleManager(ltm);
        memo.setLnTrafficController(lnis);

        try {
        // set slot 3 to address 3
        LocoNetMessage m = new LocoNetMessage(13);
        m.setOpCode(LnConstants.OPC_WR_SL_DATA);
        m.setElement(1, 0x0E);
        m.setElement(2, 0x03);
        m.setElement(4, 0x03);
        slotmanager.slot(3).setSlot(m);

        // set slot 4 to address 255
        m.setElement(2, 0x04);
        m.setElement(4, 0x7F);
        m.setElement(9, 0x01);
        slotmanager.slot(4).setSlot(m);
        } catch(LocoNetException lne) {
          Assert.fail("failed to add addresses to slot during setup");
        }
        c = new LocoNetConsist(3,memo);
        ReturnSlotInfo();

    }

    @After
    @Override
    public void tearDown() {
        ltm.dispose();
        c = null;
        memo.dispose();
        JUnitUtil.tearDown();
    }

}
