package jmri.jmrix.nce;

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import jmri.DccLocoAddress;

/**
 * NceConsistTest.java
 *
 * Description:	tests for the jmri.jmrix.nce.NceConsist class
 *
 * @author	Paul Bender Copyright (C) 2016
 */

public class NceConsistTest {

    // infrastructure objects, populated by setUp;
    NceInterfaceScaffold nnis;
    NceSystemConnectionMemo memo;


    @Test public void testCtor() {
        NceConsist m = new NceConsist(3,memo);
        // send a reply the memory read instruction trigged by the message above.
        nnis.sendTestReply(new NceReply(nnis,"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"),null);
        Assert.assertNotNull(m);
    }

    @Test public void testCtor2() {
        // DccLocoAddress constructor test.
        NceConsist c = new NceConsist(new DccLocoAddress(3, false),memo);
        // send a reply the memory read instruction trigged by the message above.
        nnis.sendTestReply(new NceReply(nnis,"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"),null);
        Assert.assertNotNull(c);
    }

    @Ignore("not quite ready yet")
    @Test public void checkDisposeMethod(){
        NceConsist c =  new NceConsist(3,memo);
        // send a reply the memory read instruction trigged by the message above.
        nnis.sendTestReply(new NceReply(nnis,"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"),null);
        // verify that c has been added to the traffic controller's
        // list of listeners.
        int listeners = nnis.numListeners();
        c.dispose();
        Assert.assertEquals("dispose check",listeners -1, nnis.numListeners());
    }

    @Test public void testGetConsistType(){
        NceConsist c = new NceConsist(3,memo);
        // send a reply the memory read instruction trigged by the message above.
        nnis.sendTestReply(new NceReply(nnis,"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"),null);
        Assert.assertEquals("default consist type",jmri.Consist.ADVANCED_CONSIST,c.getConsistType());
    }

    @Test public void testSetConsistTypeAdvanced(){
        NceConsist c = new NceConsist(3,memo);
        // send a reply the memory read instruction trigged by the message above.
        nnis.sendTestReply(new NceReply(nnis,"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"),null);
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        Assert.assertEquals("default consist type",jmri.Consist.ADVANCED_CONSIST,c.getConsistType());
    }

    @Test public void testSetConsistTypeCS(){
        NceConsist c = new NceConsist(3,memo);
        // send a reply the memory read instruction trigged by the message above.
        nnis.sendTestReply(new NceReply(nnis,"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"),null);
        c.setConsistType(jmri.Consist.CS_CONSIST);
        jmri.util.JUnitAppender.assertErrorMessage("Consist Type Not Supported");
    }

    @Test public void testSetConsistTypeOther(){
        NceConsist c = new NceConsist(3,memo);
        // send a reply the memory read instruction trigged by the message above.
        nnis.sendTestReply(new NceReply(nnis,"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"),null);
        c.setConsistType(255);
        // make sure an error message is generated.
        jmri.util.JUnitAppender.assertErrorMessage("Consist Type Not Supported");
    }

    @Test public void checkAddressAllowedGoodAdvanced(){
        NceConsist c = new NceConsist(3,memo);
        // send a reply the memory read instruction trigged by the message above.
        nnis.sendTestReply(new NceReply(nnis,"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"),null);
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        Assert.assertTrue("AddressAllowed", c.isAddressAllowed(new jmri.DccLocoAddress(200,true)));
    }

    @Test public void checkAddressAllowedBadAdvanced(){
        NceConsist c = new NceConsist(3,memo);
        // send a reply the memory read instruction trigged by the message above.
        nnis.sendTestReply(new NceReply(nnis,"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"),null);
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        Assert.assertFalse("AddressAllowed", c.isAddressAllowed(new jmri.DccLocoAddress(0,false)));
    }

    @Test public void checkSizeLimitAdvanced(){
        NceConsist c = new NceConsist(3,memo);
        // send a reply the memory read instruction trigged by the message above.
        nnis.sendTestReply(new NceReply(nnis,"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"),null);
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        Assert.assertEquals("Advanced Consist Limit",6,c.sizeLimit());   
    } 

    @Test public void checkContainsAdvanced(){
        NceConsist c = new NceConsist(3,memo);
        // send a reply the memory read instruction trigged by the message above.
        nnis.sendTestReply(new NceReply(nnis,"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"),null);
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

    @Test public void checkGetLocoDirectionAdvanced(){
        NceConsist c = new NceConsist(3,memo);
        // send a reply the memory read instruction trigged by the message above.
        nnis.sendTestReply(new NceReply(nnis,"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"),null);
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        jmri.DccLocoAddress A = new jmri.DccLocoAddress(200,true);
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250,true);
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        c.restore(B,false); // revese direction.
        Assert.assertTrue("Direction in Advanced Consist",c.getLocoDirection(A));   
        Assert.assertFalse("Direction in Advanced Consist",c.getLocoDirection(B));   
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        // prepare an interface
        nnis = new NceInterfaceScaffold();
        memo = new NceSystemConnectionMemo();
        memo.setNceTrafficController(nnis);
    }
   
    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

}
