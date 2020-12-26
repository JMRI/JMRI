package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LocoNetConsistManagerTest extends jmri.implementation.AbstractConsistManagerTestBase {

    private LocoNetSystemConnectionMemo memo = null;

    @Test
    @Override
    public void testIsCommandStationConsistPossible(){
       // possible for LocoNet
       Assert.assertTrue("CS Consist Possible",cm.isCommandStationConsistPossible());
    }

    @BeforeEach
    @Override
    public void setUp()  {
        JUnitUtil.setUp();
        LnTrafficController lnis = new LocoNetInterfaceScaffold();
        SlotManager slotmanager = new SlotManager(lnis);
        memo = new LocoNetSystemConnectionMemo(lnis, slotmanager);
        memo.setThrottleManager(new LnThrottleManager(memo));
        cm = new LocoNetConsistManager(memo);
    }

    @AfterEach
    @Override
    public void tearDown() {
        cm = null;
        ((LnThrottleManager)memo.getThrottleManager()).dispose();
        memo.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LocoNetConsistManagerTest.class);
}
