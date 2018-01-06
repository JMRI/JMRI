package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LocoNetConsistManagerTest extends jmri.implementation.AbstractConsistManagerTestBase {

    @Ignore("Need to implement a loconet specific version of this test that responds to slot messages")
    @Test
    @Override
    public void testGetConsist() {
    }

    @Ignore("Need to implement a loconet specific version of this test that responds to slot messages")
    @Test
    @Override
    public void testDelConsist() {
    }

    @Test
    @Override
    public void testIsCommandStationConsistPossible(){
       // possible for LocoNet
       Assert.assertTrue("CS Consist Possible",cm.isCommandStationConsistPossible());
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        LnTrafficController lnis = new LocoNetInterfaceScaffold();
        SlotManager slotmanager = new SlotManager(lnis);
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo(lnis, slotmanager);
        cm = new LocoNetConsistManager(memo);
    }

    @After
    @Override
    public void tearDown() {
        cm = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LocoNetConsistManagerTest.class);
}
