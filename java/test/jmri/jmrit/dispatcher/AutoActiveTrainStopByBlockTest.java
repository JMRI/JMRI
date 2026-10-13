package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jmri.Block;
import jmri.Section;
import jmri.jmrit.dispatcher.ActiveTrain.TrainDetection;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for deciding whether an automatic train can be stopped by watching
 * a Block go unoccupied, {@link AutoActiveTrain#canStopByBlockOccupancy}.
 */
public class AutoActiveTrainStopByBlockTest {

    private final List<AllocatedSection> allocated = new ArrayList<>();

    private Block block(String name, boolean occupied) {
        Block b = mock(Block.class);
        when(b.getDisplayName()).thenReturn(name);
        when(b.getState()).thenReturn(occupied ? Block.OCCUPIED : Block.UNOCCUPIED);
        return b;
    }

    private void section(int direction, Block... blocks) {
        Section s = mock(Section.class);
        when(s.getBlockList()).thenReturn(Arrays.asList(blocks));
        AllocatedSection as = mock(AllocatedSection.class);
        when(as.getSection()).thenReturn(s);
        when(as.getDirection()).thenReturn(direction);
        allocated.add(as);
    }

    @Test
    public void testRearmostOccupiedBlockForward() {
        Block a1 = block("A1", false);
        Block a2 = block("A2", true);   // tail
        Block b1 = block("B1", false);  // undetected middle
        Block b2 = block("B2", true);   // head
        section(Section.FORWARD, a1, a2);
        section(Section.FORWARD, b1, b2);
        assertSame(a2, AutoActiveTrain.getRearmostOccupiedBlock(allocated));
    }

    @Test
    public void testRearmostOccupiedBlockReverse() {
        Block a1 = block("A1", true);   // tail, travelling from A2 to A1
        Block a2 = block("A2", true);
        section(Section.REVERSE, a1, a2);
        assertSame(a2, AutoActiveTrain.getRearmostOccupiedBlock(allocated));
    }

    @Test
    public void testRearmostOccupiedBlockNone() {
        section(Section.FORWARD, block("A1", false));
        assertNull(AutoActiveTrain.getRearmostOccupiedBlock(allocated));
    }

    @Test
    public void testEntireTrainAlwaysStopsByBlock() {
        Block a1 = block("A1", false);
        section(Section.FORWARD, a1);
        assertTrue(AutoActiveTrain.canStopByBlockOccupancy(TrainDetection.TRAINDETECTION_WHOLETRAIN, a1, allocated));
    }

    @Test
    public void testHeadOnlyNeverStopsByBlock() {
        Block a1 = block("A1", true);
        section(Section.FORWARD, a1);
        assertFalse(AutoActiveTrain.canStopByBlockOccupancy(TrainDetection.TRAINDETECTION_HEADONLY, a1, allocated));
    }

    @Test
    public void testHeadAndTailStopsOnlyByTailBlock() {
        Block a1 = block("A1", true);   // tail
        Block a2 = block("A2", true);   // locomotive just leaving the previous Section
        Block b1 = block("B1", true);   // head
        section(Section.FORWARD, a1, a2);
        section(Section.FORWARD, b1);
        assertTrue(AutoActiveTrain.canStopByBlockOccupancy(TrainDetection.TRAINDETECTION_HEADANDTAIL, a1, allocated),
                "tail is in A1");
        assertFalse(AutoActiveTrain.canStopByBlockOccupancy(TrainDetection.TRAINDETECTION_HEADANDTAIL, a2, allocated),
                "A2 clears when the locomotive leaves it, the tail is still in A1");
        assertFalse(AutoActiveTrain.canStopByBlockOccupancy(TrainDetection.TRAINDETECTION_HEADANDTAIL, null, allocated));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
