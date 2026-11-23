package jmri.implementation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import jmri.Block;
import jmri.BlockManager;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.Path;
import jmri.SignalMast;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
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


    /**
     * Looking ahead must not change the direction of the Blocks it passes:
     * Block tracking and Dispatcher use those directions.
     */
    @Test
    public void testNextBlockLeavesBlockDirectionsAlone() {
        useMastsFromMap();
        Block b1 = block("IB1", "One");
        Block b2 = block("IB2", "Two");
        Block b3 = block("IB3", "Three");
        join(b1, b2);
        join(b2, b3);
        b1.setDirection(Path.EAST);
        b2.setDirection(Path.WEST); // as last set by a train going the other way
        b3.setDirection(Path.NONE);

        cs.setBlock(b1);

        Assert.assertEquals("next block", b2, cs.getNextBlock());
        Assert.assertNull("no mast", cs.getNextMast()); // walks ahead to the end
        Assert.assertEquals("block list", Arrays.asList(b1, b2, b3), cs.getBlockList());
        Assert.assertEquals("next block direction unchanged", Path.WEST, b2.getDirection());
        Assert.assertEquals("block beyond direction unchanged", Path.NONE, b3.getDirection());
    }

    /**
     * On a loop with no facing mast, the walk ahead ends when it comes back
     * round to a Block it has passed.
     */
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    public void testWalkAheadEndsOnLoopWithNoMast() {
        useMastsFromMap();
        Block b1 = block("IB1", "One");
        Block b2 = block("IB2", "Two");
        Block b3 = block("IB3", "Three");
        join(b1, b2);
        join(b2, b3);
        join(b3, b1);
        b1.setDirection(Path.EAST);

        cs.setBlock(b1);

        Assert.assertNull("no mast on the loop", cs.getNextMast());
        Assert.assertEquals("each block once", Arrays.asList(b1, b2, b3), cs.getBlockList());
    }

    /**
     * The walk ahead finds the mast facing a Block beyond the next one,
     * following the direction of travel.
     */
    @Test
    public void testNextMastBeyondNextBlock() {
        useMastsFromMap();
        Block b1 = block("IB1", "One");
        Block b2 = block("IB2", "Two");
        Block b3 = block("IB3", "Three");
        join(b1, b2);
        join(b2, b3);
        b1.setDirection(Path.EAST);
        SignalMast mast = mast("IF$vsm:basic:one-searchlight($0001)", "Clear");
        masts.put(b2.getUserName() + ">" + b3.getUserName(), mast);

        cs.setBlock(b1);

        Assert.assertEquals("mast between next block and the one beyond", mast, cs.getNextMast());
    }

    /**
     * The Block list stops at the first mast at stop, and the mast checked
     * for each Block is the one at that Block's entry.
     */
    @Test
    public void testBlockListStopsAtMastAtStop() {
        useMastsFromMap();
        Block b1 = block("IB1", "One");
        Block b2 = block("IB2", "Two");
        Block b3 = block("IB3", "Three");
        Block b4 = block("IB4", "Four");
        join(b1, b2);
        join(b2, b3);
        join(b3, b4);
        b1.setDirection(Path.EAST);
        SignalMast clear = mast("IF$vsm:basic:one-searchlight($0001)", "Clear");
        // AbstractSignalMast.isAtStop() looks for a speed of "0", which the
        // basic signal system doesn't use, so this mast says it is at stop
        SignalMast stop = new VirtualSignalMast("IF$vsm:basic:one-searchlight($0002)") {
            @Override
            public boolean isAtStop() {
                return true;
            }
        };
        stop.setAspect("Stop");
        Assert.assertFalse("mast clear", clear.isAtStop());
        masts.put(b1.getUserName() + ">" + b2.getUserName(), clear);
        masts.put(b2.getUserName() + ">" + b3.getUserName(), stop);

        cs.setBlock(b1);

        Assert.assertEquals("to the mast at stop", Arrays.asList(b1, b2), cs.getBlockList());
    }

    private final Map<String, SignalMast> masts = new HashMap<>();

    /**
     * Look masts up from {@link #masts} by the pair of Blocks they are
     * between, so no Layout Editor panel is needed. Called by each test that
     * walks ahead, since subclasses replace {@link #setUp()}.
     */
    private void useMastsFromMap() {
        InstanceManager.setDefault(LayoutBlockManager.class, new LayoutBlockManager() {
            @Override
            public SignalMast getFacingSignalMast(Block facingBlock, Block protectedBlock) {
                return masts.get(facingBlock.getUserName() + ">"
                        + (protectedBlock == null ? null : protectedBlock.getUserName()));
            }

            @Override
            public SignalMast getSignalMastAtEndBumper(Block facingBlock, LayoutEditor panel) {
                return null;
            }
        });
    }

    private static SignalMast mast(String systemName, String aspect) {
        SignalMast mast = new VirtualSignalMast(systemName);
        mast.setAspect(aspect);
        return mast;
    }

    private static Block block(String systemName, String userName) {
        return InstanceManager.getDefault(BlockManager.class).createNewBlock(systemName, userName);
    }

    /**
     * Join two Blocks with a path each way, from a to b going east.
     */
    private static void join(Block a, Block b) {
        a.addPath(new Path(b, Path.EAST, Path.WEST));
        b.addPath(new Path(a, Path.WEST, Path.EAST));
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
        JUnitUtil.tearDown();
    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultCabSignalTest.class);

}
