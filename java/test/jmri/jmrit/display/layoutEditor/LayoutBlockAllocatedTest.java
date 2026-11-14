package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.Block;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Check that a LayoutBlock keeps its allocated (extra colour) state when the
 * block becomes occupied while advanced routing is enabled.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class LayoutBlockAllocatedTest {

    private LayoutBlock layoutBlock = null;

    @Test
    public void testAllocatedKeptWhenOccupied() throws JmriException {
        InstanceManager.getDefault(LayoutBlockManager.class).enableAdvancedRouting(true);
        layoutBlock.initializeLayoutBlock();
        Block block = layoutBlock.getBlock();

        layoutBlock.setUseExtraColor(true);
        assertEquals(LayoutBlock.RESERVED, layoutBlock.getBlockStatus(), "allocated and empty");

        // a train enters the allocated block
        block.setState(Block.OCCUPIED);
        assertEquals(Block.OCCUPIED, layoutBlock.getBlockStatus(), "occupied");
        assertTrue(layoutBlock.getUseExtraColor(), "still allocated while occupied");

        // the train leaves, the block is still part of the allocation
        block.setState(Block.UNOCCUPIED);
        assertTrue(layoutBlock.getUseExtraColor(), "still allocated after the train left");
        assertEquals(LayoutBlock.RESERVED, layoutBlock.getBlockStatus(), "reported as reserved to the routing");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        layoutBlock = new LayoutBlock("ILB998", "Allocated Block");
    }

    @AfterEach
    public void tearDown() {
        layoutBlock.dispose();
        layoutBlock = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
