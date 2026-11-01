package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
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
 * Releasing the furthest allocated Section ahead of a train, as with the
 * Release button in the Allocated Sections table.
 * <p>
 * Uses only API that is also on master, so it can be run there to show the
 * problem: on master it fails with "expected: &lt;2&gt; but was: &lt;1&gt;".
 */
public class ActiveTrainReleaseAheadTest {

    private final List<Section> sections = new ArrayList<>();
    private Transit transit;
    private ActiveTrain at;

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
    public void testReleaseFurthestSectionAheadOfTrain() {
        // The train is in Section 1 of its Transit, with Sections 2 and 3
        // allocated ahead of it, and has asked for Section 4.
        at.setStartBlock(sections.get(0).getBlockList().get(0));
        at.setStartBlockSectionSequenceNumber(1);
        at.initializeFirstAllocation();
        allocate(1);
        allocate(2);
        AllocatedSection third = allocate(3);
        assertEquals(3, at.getLastAllocatedSectionSeqNumber());
        assertSame(sections.get(3), at.getNextSectionToAllocate());

        // The dispatcher releases Section 3 before the train reaches it.
        at.removeAllocatedSection(third);

        assertSame(sections.get(1), at.getLastAllocatedSection());
        assertEquals(2, at.getLastAllocatedSectionSeqNumber());
        // AutoAllocate looks this up before allocating anything more to the train
        assertNotNull(transit.getTransitSectionFromSectionAndSeq(
                at.getLastAllocatedSection(), at.getLastAllocatedSectionSeqNumber()));
        // the released Section is next on the route, so it is the next to allocate
        assertSame(sections.get(2), at.getNextSectionToAllocate());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        transit = new DefaultTransit("TT1");
        for (int i = 1; i <= 5; i++) {
            Block b = new Block("IB" + i, "Block " + i);
            Section s = new DefaultSection("TS" + i);
            s.addBlock(b);
            sections.add(s);
            transit.addTransitSection(new TransitSection(s, i, Section.FORWARD));
        }
        DispatcherFrame df = mock(DispatcherFrame.class);
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
