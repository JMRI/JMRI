package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests that with "train name in allocated blocks" Dispatcher gives the Blocks
 * of an allocated Section the train as their allocated value, and leaves the
 * Block values, which record the train in each Block, to Block tracking.
 * <p>
 * Section 1 has Block 1 and Block 2. Each Block has a Layout Block with a
 * memory.
 *
 * @author Keith Bruce 2026
 */
public class ActiveTrainAllocatedBlockValueTest {

    private DispatcherFrame df;
    private Transit transit;
    private Section section1;
    private final Block[] blocks = new Block[3];
    private final Memory[] memories = new Memory[3];

    private AllocatedSection allocate(ActiveTrain at, Section s, int seq) {
        AllocatedSection as = mock(AllocatedSection.class);
        when(as.getSection()).thenReturn(s);
        when(as.getSequence()).thenReturn(seq);
        when(as.getAllocationNumber()).thenReturn(seq);
        at.addAllocatedSection(as);
        return as;
    }

    private ActiveTrain train() {
        ActiveTrain at = new ActiveTrain(transit, "Train", ActiveTrain.USER);
        at.setDispatcher(df);
        return at;
    }

    @Test
    public void testAllocationLeavesBlockValuesAlone() {
        blocks[1].goingActive();
        blocks[1].setValue("Loco 47");  // the train, identified by Block tracking

        allocate(train(), section1, 1);

        assertEquals("Loco 47", blocks[1].getValue(), "Block 1 still records the train in it");
        assertNull(blocks[2].getValue(), "Block 2 still records no train in it");
        assertEquals("Train", blocks[1].getAllocatedValue(), "Block 1 is allocated to the train");
        assertEquals("Train", blocks[2].getAllocatedValue(), "Block 2 is allocated to the train");
        assertEquals("Loco 47", memories[1].getValue(), "Block 1 shows the train in it");
        assertEquals("Train", memories[2].getValue(), "Block 2 shows the train it is allocated to");
    }

    @Test
    public void testRosterEntryAsAllocatedValue() {
        when(df.getRosterEntryInBlock()).thenReturn(true);
        RosterEntry re = new RosterEntry();
        re.setId("Loco 47");
        ActiveTrain at = new ActiveTrain(transit, "Loco 47", ActiveTrain.ROSTER);
        at.setDispatcher(df);
        at.setRosterEntry(re);

        allocate(at, section1, 1);

        assertSame(re, blocks[2].getAllocatedValue(), "the roster entry with 'Use roster entry'");
        assertNull(blocks[2].getValue(), "Block 2 still records no train in it");
    }

    @Test
    public void testReleaseRemovesAllocatedValue() {
        ActiveTrain at = train();
        AllocatedSection as = allocate(at, section1, 1);
        at.removeAllocatedSection(as);
        assertNull(blocks[2].getAllocatedValue(), "Block 2 is no longer allocated");
        assertNull(memories[2].getValue(), "Block 2 shows nothing");
    }

    @Test
    public void testOptionOffGivesNoAllocatedValue() {
        when(df.getNameInAllocatedBlock()).thenReturn(false);
        allocate(train(), section1, 1);
        assertNull(blocks[2].getAllocatedValue(), "no allocated value without the option");
        assertNull(memories[2].getValue(), "Block 2 shows nothing");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initLayoutBlockManager();
        JUnitUtil.initRosterConfigManager();

        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        for (int i = 1; i <= 2; i++) {
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
        transit = new DefaultTransit("TT1");
        transit.addTransitSection(new TransitSection(section1, 1, Section.FORWARD));

        df = mock(DispatcherFrame.class);
        when(df.requestAllocation(any(), any(), anyInt(), anyInt(), anyBoolean(), any(), anyBoolean()))
                .thenReturn(true);
        when(df.getNameInAllocatedBlock()).thenReturn(true);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
