package jmri.jmrit.dispatcher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import jmri.InstanceManager;
import jmri.Section;
import jmri.Transit;
import jmri.TransitSection;
import jmri.implementation.DefaultSection;
import jmri.implementation.DefaultTransit;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * A train that is reset when done and reaches the end of its Transit goes on
 * to sequence 2 of the Transit, also when it was started part way along the
 * Transit and so has not been given sequence 2 before.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class DispatcherFrameResetWhenDoneTest {

    private DispatcherFrame d;
    private Transit transit;
    private Section s1, s2, s3;

    private ActiveTrain trainAtEnd(Section secondAllocated) {
        ActiveTrain at = mock(ActiveTrain.class);
        when(at.getTrainName()).thenReturn("Train");
        when(at.getActiveTrainName()).thenReturn("Train");
        when(at.getTransit()).thenReturn(transit);
        when(at.getStarted()).thenReturn(true);
        when(at.getResetWhenDone()).thenReturn(true);
        when(at.getDelayedRestart()).thenReturn(ActiveTrain.NODELAY);
        when(at.getEndBlockSection()).thenReturn(s3);
        when(at.getEndBlockSectionSequenceNumber()).thenReturn(3);
        when(at.getNextSectionSeqNumber()).thenReturn(3);
        when(at.getLastAllocatedSection()).thenReturn(s2);
        when(at.getSecondAllocatedSection()).thenReturn(secondAllocated);
        when(at.getAllocatedSectionList()).thenReturn(new ArrayList<>());
        when(at.getAllocationDirectionFromSectionAndSeq(any(), anyInt())).thenReturn(Section.FORWARD);
        return at;
    }

    @Test
    public void testStartedPartWayGoesOnToSequenceTwo() {
        // started at sequence 3, so never given sequence 2
        ActiveTrain at = trainAtEnd(null);
        AllocatedSection as = d.allocateSection(new AllocationRequest(s3, 3, Section.FORWARD, at), null);

        Assertions.assertNotNull(as, "end Section allocated");
        Assertions.assertSame(s2, as.getNextSection(), "goes on to the Section at sequence 2");
        Assertions.assertEquals(2, as.getNextSectionSequence());
    }

    @Test
    public void testStartedAtTheStartGoesOnToSequenceTwo() {
        ActiveTrain at = trainAtEnd(s2);
        AllocatedSection as = d.allocateSection(new AllocationRequest(s3, 3, Section.FORWARD, at), null);

        Assertions.assertNotNull(as, "end Section allocated");
        Assertions.assertSame(s2, as.getNextSection(), "goes on to the Section at sequence 2");
        Assertions.assertEquals(2, as.getNextSectionSequence());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");
        d = InstanceManager.getDefault(DispatcherFrame.class);
        d.setAutoAllocate(false);
        d.setUseConnectivity(false);
        s1 = new DefaultSection("IY1");
        s2 = new DefaultSection("IY2");
        s3 = new DefaultSection("IY3");
        transit = new DefaultTransit("IT1");
        transit.addTransitSection(new TransitSection(s1, 1, Section.FORWARD));
        transit.addTransitSection(new TransitSection(s2, 2, Section.FORWARD));
        transit.addTransitSection(new TransitSection(s3, 3, Section.FORWARD));
    }

    @AfterEach
    public void tearDown() {
        if (d != null) {
            JUnitUtil.dispose(d);
        }
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
