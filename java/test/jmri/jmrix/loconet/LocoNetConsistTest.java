package jmri.jmrix.loconet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.Consist;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmrit.consisttool.ConsistPreferencesManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2016,2017
 */
public class LocoNetConsistTest extends jmri.implementation.AbstractConsistTestBase {

    // infrastructure objects, populated by setUp;
    private LocoNetInterfaceScaffold lnis;
    private SlotManager slotmanager;
    private LocoNetSystemConnectionMemo memo;
    private LnThrottleManager ltm;

    //utility function, handle slot messages required to suppress
    // errors from the LnThrottleManager after constructor call.
    private void returnSlotInfo(){
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

    @Test
    public void testCtor2() {
        // DccLocoAddress constructor test.
        c = new LocoNetConsist(new DccLocoAddress(3, false),memo);
        returnSlotInfo();
        assertNotNull(c);
    }

    @Test
    @Override
    public void testGetConsistType(){
        // LocoNet consists default to CS consists.
        assertEquals( Consist.CS_CONSIST,c.getConsistType(),
            "default consist type");
    }

    @Test
    @Override
    public void testSetConsistTypeCS(){
        c.setConsistType( Consist.CS_CONSIST);
        assertEquals( Consist.CS_CONSIST, c.getConsistType(),
            "default consist type");
    }

    @Override
    @Test
    @jmri.util.junit.annotations.NotApplicable("LocoNet CS consists allow any valid address")
    public void checkAddressAllowedBad(){
        // LocoNet CS consists allow any valid address, so this test is empty
    }

    @Test
    public void testAddressAllowedGoodAdvanced(){
        returnSlotInfo();
        c.setConsistType( Consist.ADVANCED_CONSIST);
        assertTrue( c.isAddressAllowed( new DccLocoAddress(200,true)),
            "AddressAllowed");
    }

    @Test
    public void testAddressAllowedBadAdvanced(){
        returnSlotInfo();
        c.setConsistType( Consist.ADVANCED_CONSIST);
        assertFalse( c.isAddressAllowed( new DccLocoAddress(0,false)),
            "AddressAllowed");
    }

    @Test
    public void testSizeLimitCS(){
        c.setConsistType( Consist.CS_CONSIST);
        assertEquals( -1, c.sizeLimit(), "CS Consist Limit");
    }

    @Test
    public void testGetLocoDirectionCS(){
        returnSlotInfo();
        c.setConsistType( Consist.CS_CONSIST);
        DccLocoAddress addrA = new DccLocoAddress(200,true);
        DccLocoAddress addrB = new DccLocoAddress(250,true);
        c.restore(addrA,true); // use restore here, as it does not send
                           // any data to the command station
        c.restore(addrB,false); // revese direction.
        assertTrue( c.getLocoDirection(addrA), "Direction in CS Consist");
        assertFalse( c.getLocoDirection(addrB), "Direction in CS Consist");
    }

    @BeforeEach
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

        assertDoesNotThrow( () -> {
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
        }, "failed to add addresses to slot during setup");

        c = new LocoNetConsist(3,memo);
        returnSlotInfo();

    }

    @AfterEach
    @Override
    public void tearDown() {
        ltm.dispose();
        c = null;
        memo.dispose();
        JUnitUtil.tearDown();
    }

}
