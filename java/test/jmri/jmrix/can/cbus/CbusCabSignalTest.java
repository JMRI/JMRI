package jmri.jmrix.can.cbus;

import jmri.Block;
import jmri.BlockManager;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Unit Tests for CBus Cab Signals
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusCabSignalTest extends jmri.implementation.DefaultCabSignalTest {

    private CanSystemConnectionMemo memo;
    private TrafficController tc;

    @Test
    @Override
    public void testSetBlock() {
        CbusCabSignal acs = new CbusCabSignal(memo,new DccLocoAddress(1234,true)){
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


    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initMemoryManager();
        InstanceManager.setDefault(jmri.BlockManager.class,new jmri.BlockManager());
        JUnitUtil.initLayoutBlockManager();

        // prepare the cab signal
        memo = new CanSystemConnectionMemo();
        tc = new TrafficControllerScaffold();
        memo.setTrafficController(tc);

        cs = new CbusCabSignal(memo,new DccLocoAddress(1234,true));
    }

    @AfterEach
    @Override
    public void tearDown() {
        memo.dispose();
        tc.terminateThreads();
        memo = null;
        tc = null;
        cs.dispose();
        cs = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusCabSignalTest.class);

}
