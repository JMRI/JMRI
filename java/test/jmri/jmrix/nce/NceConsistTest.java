package jmri.jmrix.nce;

import jmri.DccLocoAddress;
import jmri.util.JUnitUtil;
import jmri.InstanceManager;
import jmri.jmrit.consisttool.ConsistPreferencesManager;
import jmri.util.junit.annotations.*;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * NceConsistTest.java
 *
 * Test for the jmri.jmrix.nce.NceConsist class
 *
 * @author Paul Bender Copyright (C) 2016,2017
 * @author Ken Cameron Copyright (C) 2023
 */

public class NceConsistTest extends jmri.implementation.AbstractConsistTestBase {

    // infrastructure objects, populated by setUp;
    NceInterfaceScaffold nnis;
    NceSystemConnectionMemo memo;


    @Test public void testCtor2() {
        // DccLocoAddress constructor test.
        NceConsist c = new NceConsist(new DccLocoAddress(3, false),memo){
           @Override
           void killConsist(int address, boolean isLong){
           }
        };
        // send a reply the memory read instruction trigged by the constructor above.
        nnis.sendTestReply(new NceReply(nnis,"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"),null);
        Assert.assertNotNull(c);
        c.dispose();
    }

    @Override
    @Test public void checkSizeLimitAdvanced(){
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        Assert.assertEquals("Advanced Consist Limit",6,c.sizeLimit());
    }

    @Override
    @Test public void checkContainsAdvanced(){
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        jmri.DccLocoAddress A = new jmri.DccLocoAddress(200,true);
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250,true);
        // nothing added, should be false for all.
        Assert.assertFalse("Advanced Consist Contains",c.contains(A));
        Assert.assertFalse("Advanced Consist Contains",c.contains(B));
        // add just A
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        nnis.sendTestReply(new NceReply(nnis,"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"),null);
        Assert.assertTrue("Advanced Consist Contains",c.contains(A));
        Assert.assertFalse("Advanced Consist Contains",c.contains(B));
        // then add B
        c.restore(B,false);
        nnis.sendTestReply(new NceReply(nnis,"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"),null);
        Assert.assertTrue("Advanced Consist Contains",c.contains(A));
        Assert.assertTrue("Advanced Consist Contains",c.contains(B));
    }

    @Override
    @Test public void checkGetLocoDirectionAdvanced(){
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        jmri.DccLocoAddress A = new jmri.DccLocoAddress(200,true);
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250,true);
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        nnis.sendTestReply(new NceReply(nnis,"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"),null);
        c.restore(B,false); // revese direction.
        nnis.sendTestReply(new NceReply(nnis,"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"),null);
        Assert.assertTrue("Direction in Advanced Consist",c.getLocoDirection(A));
        Assert.assertFalse("Direction in Advanced Consist",c.getLocoDirection(B));
    }

    @Override
    @Test
    @Disabled("Remove requires response from command station")
    @ToDo("re-write parent class test here and include simulated command station response") 
    public void checkRemoveWithGetRosterIDAdvanced(){
    }

    @Override
    @Test
    @Disabled("Remove requires response from command station")
    @ToDo("re-write parent class test here and include simulated command station response") 
    public void checkAddRemoveWithRosterUpdateAdvanced(){
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        InstanceManager.setDefault(ConsistPreferencesManager.class,new ConsistPreferencesManager());
        // prepare an interface
        nnis = new NceInterfaceScaffold();
        memo = new NceSystemConnectionMemo();
        memo.setNceTrafficController(nnis);
        nnis.csm = new NceCmdStationMemory();

        c = new NceConsist(3,memo){
           @Override
           void killConsist(int address, boolean isLong){
           }
        };
        // send a reply the memory read instruction trigged by the constructor above.
        nnis.sendTestReply(new NceReply(nnis,"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"),null);
    }

    @AfterEach
    @Override
    public void tearDown() {
        c.dispose();
        c = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
