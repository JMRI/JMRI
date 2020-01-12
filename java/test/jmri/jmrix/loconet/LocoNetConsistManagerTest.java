package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import org.junit.*;

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

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp()  {
        JUnitUtil.setUp();
        LnTrafficController lnis = new LocoNetInterfaceScaffold();
        SlotManager slotmanager = new SlotManager(lnis);
        memo = new LocoNetSystemConnectionMemo(lnis, slotmanager);
        memo.setThrottleManager(new LnThrottleManager(memo));
        cm = new LocoNetConsistManager(memo);
    }

    @After
    @Override
    public void tearDown() {
        cm = null;
        ((LnThrottleManager)memo.getThrottleManager()).dispose();
        memo.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LocoNetConsistManagerTest.class);
}
