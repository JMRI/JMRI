package jmri.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.Block;
import jmri.BlockManager;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the listeners AbstractCabSignalManager adds to the BlockManager
 * and to each Block, through DefaultCabSignalManager.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DefaultCabSignalManagerTest {

    private BlockManager blockManager;

    private int beansListeners() {
        return blockManager.getPropertyChangeListeners(BlockManager.PROPERTY_BEANS).length;
    }

    @Test
    public void testDisposeRemovesBlockManagerListener() {
        int before = beansListeners();
        DefaultCabSignalManager manager = new DefaultCabSignalManager();
        assertEquals(before + 1, beansListeners(), "listening for Block changes");
        manager.dispose();
        assertEquals(before, beansListeners(), "no longer listening after dispose");
    }

    @Test
    public void testBlockListenersAddedOnceAndRemoved() {
        Block east = blockManager.provideBlock("IB1");
        Block west = blockManager.provideBlock("IB2");
        int eastBefore = east.getPropertyChangeListeners().length;
        int westBefore = west.getPropertyChangeListeners().length;

        DefaultCabSignalManager manager = new DefaultCabSignalManager();
        assertNotNull(manager.getCabSignal(new DccLocoAddress(1234, true)), "cab signal created");
        assertEquals(eastBefore + 1, east.getPropertyChangeListeners().length, "one listener on east");

        // adding a block makes the manager listen to the blocks again
        Block north = blockManager.provideBlock("IB3");
        assertEquals(eastBefore + 1, east.getPropertyChangeListeners().length, "still one listener on east");
        assertEquals(westBefore + 1, west.getPropertyChangeListeners().length, "still one listener on west");
        int northWith = north.getPropertyChangeListeners().length;

        manager.dispose();
        assertEquals(eastBefore, east.getPropertyChangeListeners().length, "east listener removed");
        assertEquals(westBefore, west.getPropertyChangeListeners().length, "west listener removed");
        assertEquals(northWith - 1, north.getPropertyChangeListeners().length, "north listener removed");
    }

    @Test
    public void testDisposeUsesTheBlockManagerItListensTo() {
        DefaultCabSignalManager manager = new DefaultCabSignalManager();
        int before = beansListeners();
        // the BlockManager the manager was created with is no longer the
        // default, as when the InstanceManager is cleared
        InstanceManager.deregister(blockManager, BlockManager.class);
        manager.dispose();
        assertEquals(before - 1, beansListeners(), "listener removed from the original BlockManager");
        assertFalse(InstanceManager.containsDefault(BlockManager.class), "no new BlockManager created by dispose");
        blockManager.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        blockManager = InstanceManager.getDefault(BlockManager.class);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
