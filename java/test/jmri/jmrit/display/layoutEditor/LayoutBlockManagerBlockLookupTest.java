package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import jmri.Block;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Check LayoutBlockManager.getLayoutBlock(Block) finds the right LayoutBlock
 * without walking through every LayoutBlock on each call.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class LayoutBlockManagerBlockLookupTest {

    private int scans = 0;

    private LayoutBlockManager lbm;

    @Test
    public void testLookupByBlock() {
        List<LayoutBlock> layoutBlocks = new ArrayList<>();
        for (int i = 1; i <= 200; i++) {
            LayoutBlock lb = lbm.createNewLayoutBlock("ILB" + i, "Lookup Block " + i);
            assertNotNull(lb);
            lb.initializeLayoutBlock();
            layoutBlocks.add(lb);
        }
        for (LayoutBlock lb : layoutBlocks) {
            assertSame(lb, lbm.getLayoutBlock(lb.getBlock()), "first lookup");
        }

        scans = 0;
        for (int repeat = 0; repeat < 5; repeat++) {
            for (LayoutBlock lb : layoutBlocks) {
                assertSame(lb, lbm.getLayoutBlock(lb.getBlock()), "repeated lookup");
            }
        }
        assertEquals(0, scans, "repeated lookups do not walk the LayoutBlock list");

        // a Block without a LayoutBlock
        assertNull(lbm.getLayoutBlock(new Block("IB_NOT_LAYOUT")), "plain block");

        // a deleted LayoutBlock is no longer returned
        LayoutBlock removed = layoutBlocks.get(10);
        Block removedBlock = removed.getBlock();
        lbm.deleteLayoutBlock(removed);
        assertNull(lbm.getLayoutBlock(removedBlock), "deleted LayoutBlock");
        assertSame(layoutBlocks.get(11), lbm.getLayoutBlock(layoutBlocks.get(11).getBlock()), "others still found");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        lbm = new LayoutBlockManager() {
            @Override
            public SortedSet<LayoutBlock> getNamedBeanSet() {
                scans++;
                return super.getNamedBeanSet();
            }
        };
        InstanceManager.setDefault(LayoutBlockManager.class, lbm);
    }

    @AfterEach
    public void tearDown() {
        lbm.dispose();
        lbm = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
