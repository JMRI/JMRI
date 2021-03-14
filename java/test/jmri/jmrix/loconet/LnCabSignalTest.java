package jmri.jmrix.loconet;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;


/**
 * Unit test for LocoNet Cab Signals
 *
 * @author Paul Bender Copyright (C) 2019
 */
public class LnCabSignalTest extends jmri.implementation.DefaultCabSignalTest {

    private LocoNetInterfaceScaffold lnis;
    private LocoNetSystemConnectionMemo memo;

    @Test
    @Override
    public void testSetBlock() {
        LnCabSignal acs = new LnCabSignal(memo,new DccLocoAddress(1234,true)){
            @Override
            public jmri.SignalMast getNextMast(){
                // don't check for signal masts, they aren't setup for this
                // test.
                return null;
            }
        };

        Block b1 = InstanceManager.getDefault(BlockManager.class).provideBlock("IB12");
        // set the block contents to our locomotive address.
        b1.setValue(new DccLocoAddress(1234,true));
        // call setBlock() for the cab signal.
        acs.setBlock();
        // and verify getBlock returns the block we set.
        Assert.assertEquals("Block set",b1,acs.getBlock());

        acs.dispose(); // verify no exceptions
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initMemoryManager();
        InstanceManager.setDefault(jmri.BlockManager.class,new jmri.BlockManager());
        JUnitUtil.initLayoutBlockManager();

        // prepare an interface
        memo = new LocoNetSystemConnectionMemo("L", "LocoNet");
        lnis = new LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);
        cs = new LnCabSignal(memo,new DccLocoAddress(1234,true));
    }

    @Override
    @AfterEach
    public void tearDown() {
        cs.dispose(); // verify no exceptions
        cs = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    //private final static Logger log = LoggerFactory.getLogger(LnCabSignalTest.class);

}
