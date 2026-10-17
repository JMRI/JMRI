package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import jmri.Block;
import jmri.Section;
import jmri.Transit;
import jmri.TransitSection;
import jmri.implementation.DefaultSection;
import jmri.implementation.DefaultTransit;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link ActiveTrain#removeAllocatedSection} keeping the last
 * allocated Section and its sequence number consistent.
 */
public class ActiveTrainRemoveAllocatedSectionTest {

    private final List<Section> sections = new ArrayList<>();
    private Transit transit;
    private ActiveTrain at;
    private DispatcherFrame df;

    private AllocatedSection allocate(int seq) {
        AllocatedSection as = mock(AllocatedSection.class);
        when(as.getSection()).thenReturn(sections.get(seq - 1));
        when(as.getSequence()).thenReturn(seq);
        when(as.getAllocationNumber()).thenReturn(seq);
        if (seq < sections.size()) {
            when(as.getNextSection()).thenReturn(sections.get(seq));
            when(as.getNextSectionSequence()).thenReturn(seq + 1);
        }
        at.addAllocatedSection(as);
        return as;
    }

    @Test
    public void testLastAllocatedSequenceAfterReleasingFurthestSection() {
        // the train starts in the third Section of its Transit
        at.setStartBlock(sections.get(2).getBlockList().get(0));
        at.setStartBlockSectionSequenceNumber(3);
        at.initializeFirstAllocation();
        allocate(3);
        allocate(4);
        AllocatedSection furthest = allocate(5);
        assertSame(sections.get(5), at.getNextSectionToAllocate());
        // the train has already asked for the sixth Section
        AllocationRequest queuedForSixth = mock(AllocationRequest.class);
        when(df.findAllocationRequestInQueue(sections.get(5), 6, Section.FORWARD, at)).thenReturn(queuedForSixth);
        assertSame(sections.get(4), at.getLastAllocatedSection());
        assertEquals(5, at.getLastAllocatedSectionSeqNumber());

        // release the furthest Section, as from the Allocated Sections table
        at.removeAllocatedSection(furthest);

        assertSame(sections.get(3), at.getLastAllocatedSection(), "last allocated is now the fourth Section");
        assertEquals(4, at.getLastAllocatedSectionSeqNumber(), "sequence number of the fourth Section");
        TransitSection ts = transit.getTransitSectionFromSectionAndSeq(at.getLastAllocatedSection(),
                at.getLastAllocatedSectionSeqNumber());
        assertNotNull(ts, "the last allocated Section can be found in the Transit");
        assertSame(sections.get(4), at.getNextSectionToAllocate(), "the released Section is allocated next");
        assertEquals(5, at.getNextSectionSeqNumber(), "sequence number of the released Section");
        verify(df).cancelAllocationRequest(queuedForSixth);
    }

    @Test
    public void testReleasingSectionTheTrainHasBeenInKeepsNextSection() {
        at.setStartBlock(sections.get(2).getBlockList().get(0));
        at.setStartBlockSectionSequenceNumber(3);
        at.initializeFirstAllocation();
        allocate(3);
        allocate(4);
        AllocatedSection furthest = allocate(5);
        // the train has been in the furthest Section, so it is not ahead of it
        when(furthest.getEntered()).thenReturn(true);

        at.removeAllocatedSection(furthest);

        assertSame(sections.get(3), at.getLastAllocatedSection());
        assertSame(sections.get(5), at.getNextSectionToAllocate(), "next Section is unchanged");
        assertEquals(6, at.getNextSectionSeqNumber());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        transit = new DefaultTransit("TT1");
        for (int i = 1; i <= 6; i++) {
            Block b = new Block("IB" + i, "Block " + i);
            Section s = new DefaultSection("TS" + i);
            s.addBlock(b);
            sections.add(s);
            transit.addTransitSection(new TransitSection(s, i, Section.FORWARD));
        }
        df = mock(DispatcherFrame.class);
        when(df.requestAllocation(any(), any(), anyInt(), anyInt(), anyBoolean(), any(), anyBoolean()))
                .thenReturn(true);
        at = new ActiveTrain(transit, "Train", ActiveTrain.USER);
        at.setDispatcher(df);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
