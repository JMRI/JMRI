package jmri.implementation;

import java.util.Hashtable;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Check that a Signal Mast Logic goes to danger promptly when a block on its
 * route becomes occupied.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DefaultSignalMastLogicOccupancyTest {

    @Test
    public void testOccupiedBlockGivesDangerWithoutCheckDelay() {
        VirtualSignalMast source = new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)");
        VirtualSignalMast dest = new VirtualSignalMast("IF$vsm:basic:one-searchlight($2)");
        Block block = InstanceManager.getDefault(BlockManager.class).provideBlock("IB1");
        block.setState(Block.UNOCCUPIED);

        SignalMastLogicManager smlm = InstanceManager.getDefault(SignalMastLogicManager.class);
        smlm.setSignalLogicDelay(20);
        SignalMastLogic sml = smlm.newSignalMastLogic(source);
        sml.setDestinationMast(dest);
        Hashtable<Block, Integer> blocks = new Hashtable<>();
        blocks.put(block, Block.UNOCCUPIED);
        sml.setBlocks(blocks, dest);
        sml.initialise();
        JUnitUtil.waitFor(() -> "Approach".equals(source.getAspect()), "source cleared");

        // a long settling delay, as used on a layout with noisy detection
        int delay = 1000;
        smlm.setSignalLogicDelay(delay);

        long start = System.currentTimeMillis();
        block.setState(Block.OCCUPIED);
        JUnitUtil.waitFor(() -> "Stop".equals(source.getAspect()), "source shows Stop for occupied block");
        long elapsed = System.currentTimeMillis() - start;
        assertEquals("Stop", source.getAspect());
        // going to danger only waits for the aspect delay (half the logic delay),
        // not for the full logic delay first
        assertTrue(elapsed < delay, "danger shown after " + elapsed + " ms, expected less than " + delay + " ms");

        sml.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
