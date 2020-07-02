package jmri.jmrix.loconet;

import jmri.BlockManager;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.SignalMastManager;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2019
 */
public class LnCabSignalIT extends jmri.implementation.DefaultCabSignalIT {

    private LocoNetInterfaceScaffold lnis;
    private LocoNetSystemConnectionMemo memo;

    @Override
    @Test
    public void testSignalSequenceIdTag() throws jmri.JmriException {
        // since this is on loconet, use a transponding tag.
        runSequence(new TranspondingTag("LD1234"));
    }

    @Override
    protected void checkBlock(jmri.CabSignal lcs, String currentBlock, String nextBlock, String mastName) {
        BlockManager bm = InstanceManager.getDefault(BlockManager.class);
        SignalMastManager smm = InstanceManager.getDefault(SignalMastManager.class);
        Assert.assertEquals("Block set", bm.getBlock(currentBlock), lcs.getBlock());
        Assert.assertEquals("next Block set", bm.getBlock(nextBlock), lcs.getNextBlock());
        Assert.assertEquals("Mast set", smm.getSignalMast(mastName), lcs.getNextMast());
        if (mastName != "") {
            new org.netbeans.jemmy.QueueTool().waitEmpty(100); // wait for signal to settle.
            // mast expected, so check the aspect.
            JUnitUtil.waitFor(() -> {
                return "Clear".equals(lcs.getNextMast().getAspect().toString());
            });
            Assert.assertEquals("Mast " + mastName + " Aspect clear", "Clear", lcs.getNextMast().getAspect());
            //for a clear aspect, the semaphore value sent should be 18
            Assert.assertEquals("E5 10 7F 00 00 00 09 52 18 00 70 00 00 00 00 00",
                    lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        }
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
        InstanceManager.setDefault(BlockManager.class, new BlockManager());
        JUnitUtil.initLayoutBlockManager();
        JUnitUtil.initDefaultSignalMastManager();
        JUnitUtil.initSignalMastLogicManager();

        // prepare an interface
        memo = new LocoNetSystemConnectionMemo("L", "LocoNet");
        lnis = new LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);
        cs = new LnCabSignal(memo, new DccLocoAddress(1234, true));
    }

    @Override
    @AfterEach
    public void tearDown() {
        cs.dispose(); // verify no exceptions
        cs = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    //private final static Logger log = LoggerFactory.getLogger(LnCabSignalTest.class);
}
