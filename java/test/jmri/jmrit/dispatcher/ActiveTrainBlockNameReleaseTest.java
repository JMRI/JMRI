package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.Section;
import jmri.Transit;
import jmri.TransitSection;
import jmri.implementation.DefaultSection;
import jmri.implementation.DefaultTransit;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests that the train name shown in the memory of an allocated Block is
 * removed when the Block is released, with "train name in allocated blocks".
 * <p>
 * Section 1 has Block 1 and Block 2, Section 2 has Block 2 and Block 3, so
 * Block 2 is in both. Each Block has a Layout Block with a memory.
 *
 * @author Keith Bruce 2026
 */
public class ActiveTrainBlockNameReleaseTest {

    private DispatcherFrame df;
    private ActiveTrain at;
    private Section section1;
    private Section section2;
    private final Block[] blocks = new Block[4];
    private final Memory[] memories = new Memory[4];

    private AllocatedSection allocate(Section s, int seq) {
        AllocatedSection as = mock(AllocatedSection.class);
        when(as.getSection()).thenReturn(s);
        when(as.getSequence()).thenReturn(seq);
        when(as.getAllocationNumber()).thenReturn(seq);
        at.addAllocatedSection(as);
        return as;
    }

    private void trainPasses(Block b) {
        b.goingActive();
        b.goingInactive();
    }

    @Test
    public void testMemoryClearedWhenBlockReleased() {
        AllocatedSection first = allocate(section1, 1);
        assertEquals("Train", memories[1].getValue(), "Block 1 shows the train when allocated");

        trainPasses(blocks[1]);
        assertEquals("Train", memories[1].getValue(), "Block 1 shows the train while allocated");

        at.removeAllocatedSection(first);
        assertNull(memories[1].getValue(), "Block 1 shows nothing once released");
    }

    @Test
    public void testBlockInAnotherAllocatedSectionKeepsName() {
        AllocatedSection first = allocate(section1, 1);
        allocate(section2, 2);
        when(df.checkForBlockInAllocatedSection(blocks[2], section1)).thenReturn(true);

        at.removeAllocatedSection(first);
        assertNull(memories[1].getValue(), "Block 1 shows nothing once released");
        assertEquals("Train", memories[2].getValue(), "Block 2 is still allocated in Section 2");

        trainPasses(blocks[2]);
        assertEquals("Train", memories[2].getValue(), "Block 2 shows the train while still allocated");
    }

    @Test
    public void testOccupiedBlockShowsItsValueWhenReleased() {
        AllocatedSection first = allocate(section1, 1);
        blocks[1].goingActive();
        blocks[1].setValue("Loco 47");

        at.removeAllocatedSection(first);
        assertEquals("Loco 47", memories[1].getValue(), "an occupied Block keeps its value");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initLayoutBlockManager();

        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        for (int i = 1; i <= 3; i++) {
            LayoutBlock lb = lbm.createNewLayoutBlock(null, "Block " + i);
            assertNotNull(lb);
            lb.initializeLayoutBlock();
            memories[i] = InstanceManager.memoryManagerInstance().provideMemory("IMBLOCK" + i);
            lb.setMemory(memories[i], "IMBLOCK" + i);
            blocks[i] = lb.getBlock();
            blocks[i].goingInactive();  // a detected Block, unoccupied
        }
        section1 = new DefaultSection("TS1");
        section1.addBlock(blocks[1]);
        section1.addBlock(blocks[2]);
        section2 = new DefaultSection("TS2");
        section2.addBlock(blocks[2]);
        section2.addBlock(blocks[3]);
        Transit transit = new DefaultTransit("TT1");
        transit.addTransitSection(new TransitSection(section1, 1, Section.FORWARD));
        transit.addTransitSection(new TransitSection(section2, 2, Section.FORWARD));

        df = mock(DispatcherFrame.class);
        when(df.requestAllocation(any(), any(), anyInt(), anyInt(), anyBoolean(), any(), anyBoolean()))
                .thenReturn(true);
        when(df.getNameInAllocatedBlock()).thenReturn(true);
        at = new ActiveTrain(transit, "Train", ActiveTrain.USER);
        at.setDispatcher(df);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
