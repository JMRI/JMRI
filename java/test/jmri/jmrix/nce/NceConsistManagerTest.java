package jmri.jmrix.nce;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.DccLocoAddress;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class NceConsistManagerTest extends jmri.implementation.AbstractConsistManagerTestBase {

    private NceTrafficControlScaffold tcis = null;
    private NceSystemConnectionMemo memo = null;

    @Override
    @Ignore("Causes error message reading consist memory")
    @Test
    public void testGetConsist(){
        // getConsist with a valid address should always return
        // a consist.
        DccLocoAddress addr = new DccLocoAddress(5,false);
        Assert.assertNotNull("add consist",cm.getConsist(addr));
        tcis.sendTestReply(new NceReply(tcis,"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"));

    }


    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        tcis = new NceTrafficControlScaffold();
        memo = new NceSystemConnectionMemo();
        memo.setNceTrafficController(tcis);
        cm = new NceConsistManager(memo);
    }

    @After
    @Override
    public void tearDown() {
        cm = null;
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NceConsistManagerTest.class.getName());

}
