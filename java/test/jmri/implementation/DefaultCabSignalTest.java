package jmri.implementation;

import jmri.Block;
import jmri.BlockManager;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Unit tests for the DefaultCabSignal
 *
 * @author Paul Bender Copyright (C) 2019
 */
public class DefaultCabSignalTest {

    protected jmri.CabSignal cs = null;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",cs);
        //check the defaults.
        Assert.assertEquals("Address",new DccLocoAddress(1234,true),cs.getCabSignalAddress());
        Assert.assertNull("current block",cs.getBlock());
        Assert.assertNull("next block",cs.getNextBlock());
        Assert.assertNull("next mast",cs.getNextMast());
        Assert.assertTrue("cab signal active",cs.isCabSignalActive());
    }

    @Test
    public void testSetBlock() {
        DefaultCabSignal acs = new DefaultCabSignal(new DccLocoAddress(1234,true)){
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
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initMemoryManager();
        InstanceManager.setDefault(jmri.BlockManager.class,new jmri.BlockManager());
        JUnitUtil.initLayoutBlockManager();
        cs = new DefaultCabSignal(new DccLocoAddress(1234,true));
    }

    @AfterEach
    public void tearDown() {
        cs.dispose(); // verify no exceptions
        cs = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultCabSignalTest.class);

}
