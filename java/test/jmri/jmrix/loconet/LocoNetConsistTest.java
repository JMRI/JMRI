package jmri.jmrix.loconet;

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import jmri.DccLocoAddress;

/**
 * LocoNetConsistTest.java
 *
 * Description:	tests for the jmri.jmrix.nce.LocoNetConsist class
 *
 * @author	Paul Bender Copyright (C) 2016
 */

public class LocoNetConsistTest {

    // infrastructure objects, populated by setUp;
    LocoNetInterfaceScaffold lnis;
    SlotManager slotmanager;
    LocoNetSystemConnectionMemo memo;

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

    @Test public void testCtor() {
        LocoNetConsist m = new LocoNetConsist(3,memo);
        ReturnSlotInfo();
        Assert.assertNotNull(m);
    }

    @Test public void testCtor2() {
        // DccLocoAddress constructor test.
        LocoNetConsist c = new LocoNetConsist(new DccLocoAddress(3, false),memo);
        ReturnSlotInfo();
        Assert.assertNotNull(c);
    }

    @Ignore("Not quite ready yet")
    @Test(expected=java.lang.NullPointerException.class) 
    public void checkDisposeMethod(){
        LocoNetConsist c =  new LocoNetConsist(3,memo);
        ReturnSlotInfo();
        jmri.DccLocoAddress A = new jmri.DccLocoAddress(200,true);
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250,true);
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        c.restore(B,false); // use restore here, as it does not send
                           // any data to the command station
        // before dispose, this should succeed.
        Assert.assertTrue("Advanced Consist Contains",c.contains(A));
        Assert.assertTrue("Advanced Consist Contains",c.contains(B));
        c.dispose();
        // after dispose, this should fail
        Assert.assertTrue("Advanced Consist Contains",c.contains(A));
        Assert.assertTrue("Advanced Consist Contains",c.contains(B));
    }

    @Test public void testGetConsistType(){
        LocoNetConsist c = new LocoNetConsist(3,memo);
        ReturnSlotInfo();
        // LocoNet consists default to CS consists.
        Assert.assertEquals("default consist type",jmri.Consist.CS_CONSIST,c.getConsistType());
    }

    @Test public void testSetConsistTypeAdvanced(){
        LocoNetConsist c = new LocoNetConsist(3,memo);
        ReturnSlotInfo();
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        Assert.assertEquals("default consist type",jmri.Consist.ADVANCED_CONSIST,c.getConsistType());
    }

    @Test public void testSetConsistTypeCS(){
        LocoNetConsist c = new LocoNetConsist(3,memo);
        ReturnSlotInfo();
        c.setConsistType(jmri.Consist.CS_CONSIST);
        Assert.assertEquals("default consist type",jmri.Consist.CS_CONSIST,c.getConsistType());
    }

    @Test public void testSetConsistTypeOther(){
        LocoNetConsist c = new LocoNetConsist(3,memo);
        ReturnSlotInfo();
        c.setConsistType(255);
        // make sure an error message is generated.
        jmri.util.JUnitAppender.assertErrorMessage("Consist Type Not Supported");
    }

    @Test public void checkAddressAllowedGoodCS(){
        LocoNetConsist c = new LocoNetConsist(3,memo);
        ReturnSlotInfo();
        c.setConsistType(jmri.Consist.CS_CONSIST);
        Assert.assertTrue("AddressAllowed", c.isAddressAllowed(new jmri.DccLocoAddress(200,true)));
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

    @Test public void checkSizeLimitAdvanced(){
        LocoNetConsist c = new LocoNetConsist(3,memo);
        ReturnSlotInfo();
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        Assert.assertEquals("Advanced Consist Limit",-1,c.sizeLimit());   
    } 

    @Test public void checkSizeLimitCS(){
        LocoNetConsist c = new LocoNetConsist(3,memo);
        ReturnSlotInfo();
        c.setConsistType(jmri.Consist.CS_CONSIST);
        Assert.assertEquals("CS Consist Limit",-1,c.sizeLimit());   
    } 

    @Test public void checkContainsAdvanced(){
        LocoNetConsist c = new LocoNetConsist(3,memo);
        ReturnSlotInfo();
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        jmri.DccLocoAddress A = new jmri.DccLocoAddress(200,true);
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250,true);
        // nothing added, should be false for all.
        Assert.assertFalse("Advanced Consist Contains",c.contains(A));   
        Assert.assertFalse("Advanced Consist Contains",c.contains(B));   
        // add just A
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        Assert.assertTrue("Advanced Consist Contains",c.contains(A));   
        Assert.assertFalse("Advanced Consist Contains",c.contains(B));   
        // then add B
        c.restore(B,false);
        Assert.assertTrue("Advanced Consist Contains",c.contains(A));   
        Assert.assertTrue("Advanced Consist Contains",c.contains(B));   
    }

    @Test public void checkContainsCS(){
        LocoNetConsist c = new LocoNetConsist(3,memo);
        ReturnSlotInfo();
        c.setConsistType(jmri.Consist.CS_CONSIST);
        jmri.DccLocoAddress A = new jmri.DccLocoAddress(200,true);
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250,true);
        // nothing added, should be false for all.
        Assert.assertFalse("CS Consist Contains",c.contains(A));   
        Assert.assertFalse("CS Consist Contains",c.contains(B));   
        // add just A
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        Assert.assertTrue("CS Consist Contains",c.contains(A));   
        Assert.assertFalse("CS Consist Contains",c.contains(B));   
        // then add B
        c.restore(B,false);
        Assert.assertTrue("CS Consist Contains",c.contains(A));   
        Assert.assertTrue("CS Consist Contains",c.contains(B));   
    }

    @Test public void checkGetLocoDirectionAdvanced(){
        LocoNetConsist c = new LocoNetConsist(3,memo);
        ReturnSlotInfo();
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        jmri.DccLocoAddress A = new jmri.DccLocoAddress(200,true);
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250,true);
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        c.restore(B,false); // revese direction.
        Assert.assertTrue("Direction in Advanced Consist",c.getLocoDirection(A));   
        Assert.assertFalse("Direction in Advanced Consist",c.getLocoDirection(B));   
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
        Assert.assertTrue("Direction in CS Consist",c.getLocoDirection(A));   
        Assert.assertFalse("Direction in CS Consist",c.getLocoDirection(B));   
    }


    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        // prepare an interface
        lnis = new LocoNetInterfaceScaffold();
        slotmanager = new SlotManager(lnis);
        memo = new LocoNetSystemConnectionMemo(lnis,slotmanager);
        memo.setThrottleManager(new LnThrottleManager(memo));

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
          Assert.fail("failed to add addresses to slot durring setup");
        }

    }
   
    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

}
