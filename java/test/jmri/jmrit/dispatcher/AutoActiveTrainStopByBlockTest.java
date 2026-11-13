package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
 * Tests for deciding whether a Block going unoccupied means the tail of the
 * train has left it, {@link AutoActiveTrain#isTailClearOfBlock}.
 */
public class AutoActiveTrainStopByBlockTest {

    private final List<AllocatedSection> allocated = new ArrayList<>();

    private Block block(String name, boolean occupied) {
        return block(name, occupied, 0.0f);
    }

    private Block block(String name, boolean occupied, float lengthMm) {
        Block b = mock(Block.class);
        when(b.getLengthMm()).thenReturn(lengthMm);
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

    /**
     * Head and Tail train stopping in Z, watching Y, with the tail coming from X
     * and the undetected middle spanning Y. The Block states are changed in the
     * order they change on the layout.
     */
    @Test
    public void testHeadAndTailWaitsForTailThroughStoppingBlock() {
        Block x = block("X", true);    // tail
        Block y = block("Y", true);    // head passing through
        Block z = block("Z", false);
        section(Section.FORWARD, x, y, z);
        TrainDetection ht = TrainDetection.TRAINDETECTION_HEADANDTAIL;

        when(z.getState()).thenReturn(Block.OCCUPIED);     // head into Z
        when(y.getState()).thenReturn(Block.UNOCCUPIED);   // head leaves Y, middle not detected
        assertFalse(AutoActiveTrain.isTailClearOfBlock(ht, y, allocated), "tail still in X");

        when(y.getState()).thenReturn(Block.OCCUPIED);     // tail into Y
        when(x.getState()).thenReturn(Block.UNOCCUPIED);   // tail leaves X
        when(y.getState()).thenReturn(Block.UNOCCUPIED);   // tail leaves Y
        assertTrue(AutoActiveTrain.isTailClearOfBlock(ht, y, allocated), "tail has left Y, stop now");
    }

    @Test
    public void testHeadAndTailTailAlreadyInStoppingBlock() {
        Block x = block("X", false);
        Block y = block("Y", false);   // whole train already past X, tail leaves Y
        Block z = block("Z", true);
        section(Section.FORWARD, x, y, z);
        assertTrue(AutoActiveTrain.isTailClearOfBlock(TrainDetection.TRAINDETECTION_HEADANDTAIL, y, allocated));
    }

    @Test
    public void testHeadAndTailAcrossSectionsAndReverse() {
        Block a1 = block("A1", false);
        Block a2 = block("A2", true);   // tail, Section A is run in reverse: A2 then A1
        Block b1 = block("B1", false);  // stopping block, the head has left it
        Block b2 = block("B2", true);   // head
        section(Section.REVERSE, a1, a2);
        section(Section.FORWARD, b1, b2);
        TrainDetection ht = TrainDetection.TRAINDETECTION_HEADANDTAIL;
        assertFalse(AutoActiveTrain.isTailClearOfBlock(ht, b1, allocated), "tail still in A2, behind B1");
        assertTrue(AutoActiveTrain.hasOccupiedBlockBehind(a1, allocated), "A2 is behind A1 in reverse");
        assertFalse(AutoActiveTrain.hasOccupiedBlockBehind(a2, allocated), "nothing behind A2");
    }

    @Test
    public void testEntireTrainUnchanged() {
        Block x = block("X", true);
        Block y = block("Y", false);
        section(Section.FORWARD, x, y);
        assertTrue(AutoActiveTrain.isTailClearOfBlock(TrainDetection.TRAINDETECTION_WHOLETRAIN, y, allocated));
    }

    @Test
    public void testBlockNotAllocated() {
        Block x = block("X", true);
        section(Section.FORWARD, x);
        assertFalse(AutoActiveTrain.hasOccupiedBlockBehind(block("Other", false), allocated));
    }

    /**
     * How far the far end of a Block is from the start of the Section a train
     * is stopping in, {@link AutoActiveTrain#farEndFromSectionStart}, for
     * the tail fix.
     */
    @Test
    public void testFarEndFromSectionStart() {
        Block a1 = block("A1", false, 100.0f);
        Block a2 = block("A2", false, 200.0f);
        Block b1 = block("B1", false, 300.0f);
        Block b2 = block("B2", false, 400.0f);
        Block c1 = block("C1", false, 500.0f);
        section(Section.REVERSE, a1, a2);   // run A2 then A1
        section(Section.FORWARD, b1, b2);
        section(Section.FORWARD, c1);
        AllocatedSection stopping = allocated.get(1);
        assertEquals(Float.valueOf(0.0f), AutoActiveTrain.farEndFromSectionStart(a1, stopping, allocated), "A1 is next to B");
        assertEquals(Float.valueOf(-100.0f), AutoActiveTrain.farEndFromSectionStart(a2, stopping, allocated), "A1 lies between");
        assertEquals(Float.valueOf(300.0f), AutoActiveTrain.farEndFromSectionStart(b1, stopping, allocated));
        assertEquals(Float.valueOf(700.0f), AutoActiveTrain.farEndFromSectionStart(b2, stopping, allocated));
        assertNull(AutoActiveTrain.farEndFromSectionStart(c1, stopping, allocated), "beyond the Section");
        assertNull(AutoActiveTrain.farEndFromSectionStart(block("Other", false, 1.0f), stopping, allocated));
        when(a1.getLengthMm()).thenReturn(0.0f);
        assertNull(AutoActiveTrain.farEndFromSectionStart(a2, stopping, allocated), "A1 has no length");
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
