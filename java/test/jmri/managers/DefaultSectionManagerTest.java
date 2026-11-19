package jmri.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DefaultSectionManagerTest {

    @Test
    public void testCTor() {
        SectionManager t = new jmri.managers.DefaultSectionManager();
        assertNotNull( t, "exists");
    }

    @Test
    public void testInstanceManagerAccess() {
        SectionManager t = InstanceManager.getDefault(SectionManager.class);
        assertNotNull( t, "exists");
    }

    /**
     * A line with a dead end at both ends, A - B - C, and no signal masts:
     * one Section of A, B and C, not a list that runs back and forth to the
     * 100-block limit, and no second Section starting from C.
     */
    @Test
    public void testGenerateBlockSectionsForLineWithTwoDeadEnds() {
        Block a = new Block("IB1", "A");
        Block b = new Block("IB2", "B");
        Block c = new Block("IB3", "C");
        jmri.jmrit.display.layoutEditor.LayoutBlock la = layoutBlock(a, 0, b);
        jmri.jmrit.display.layoutEditor.LayoutBlock lb = layoutBlock(b, 1, a, c);
        jmri.jmrit.display.layoutEditor.LayoutBlock lc = layoutBlock(c, 0, b);
        List<jmri.jmrit.display.layoutEditor.LayoutBlock> order = List.of(la, lb, lc);
        TreeSet<jmri.jmrit.display.layoutEditor.LayoutBlock> set =
                new TreeSet<>((x, y) -> Integer.compare(order.indexOf(x), order.indexOf(y)));
        set.addAll(order);
        Map<Block, jmri.jmrit.display.layoutEditor.LayoutBlock> byBlock = Map.of(a, la, b, lb, c, lc);
        jmri.jmrit.display.layoutEditor.LayoutBlockManager lbm =
                mock(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class);
        when(lbm.getNamedBeanSet()).thenReturn(set);
        byBlock.forEach((k, v) -> when(lbm.getLayoutBlock(k)).thenReturn(v));
        InstanceManager.setDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class, lbm);

        SectionManager sm = InstanceManager.getDefault(SectionManager.class);
        sm.generateBlockSections();
        // the line can't be entered, so it has no entry point
        jmri.util.JUnitAppender.assertErrorMessage("Unable to find a next block after block 'C'");

        assertEquals(1, sm.getNamedBeanSet().size(), "one Section for the line");
        Section s = sm.getNamedBeanSet().first();
        assertEquals(List.of(a, b, c), s.getBlockList());
    }

    private static jmri.jmrit.display.layoutEditor.LayoutBlock layoutBlock(Block block, int throughPaths, Block... neighbours) {
        jmri.jmrit.display.layoutEditor.LayoutBlock lb = mock(jmri.jmrit.display.layoutEditor.LayoutBlock.class);
        when(lb.getBlock()).thenReturn(block);
        when(lb.getDisplayName()).thenReturn(block.getUserName());
        when(lb.getNumberOfThroughPaths()).thenReturn(throughPaths);
        when(lb.getNumberOfNeighbours()).thenReturn(neighbours.length);
        for (int i = 0; i < neighbours.length; i++) {
            when(lb.getNeighbourAtIndex(i)).thenReturn(neighbours[i]);
        }
        return lb;
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(SectionManagerTest.class);

}
