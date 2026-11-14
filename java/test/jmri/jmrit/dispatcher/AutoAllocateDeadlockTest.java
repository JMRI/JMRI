package jmri.jmrit.dispatcher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import jmri.InstanceManager;
import jmri.Section;
import jmri.Transit;
import jmri.TransitSection;
import jmri.implementation.DefaultTransit;
import jmri.jmrit.dispatcher.TaskAllocateRelease.TaskAction;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 * Tests that {@link AutoAllocate}, preventing deadlock, gives a train track from
 * one standing place to the next only where every train can keep running.
 * <p>
 * Two loops, A (roads A1 and A2) and B (roads B1 and B2), joined by a single
 * line S1, S2, S3. Train E runs from A1 to loop B, train W from B1 to loop A.
 * Both can stop in the loops, but not on the single line.
 */
public class AutoAllocateDeadlockTest {

    private DispatcherFrame df;
    private AutoAllocate aa;
    private Thread allocator;
    private final List<ActiveTrain> trains = new ArrayList<>();
    private final List<AllocationRequest> requests = new ArrayList<>();
    private final Map<Section, Integer> occupancy = new HashMap<>();
    private final Map<ActiveTrain, List<AllocatedSection>> allocated = new HashMap<>();
    private final AtomicInteger scansDone = new AtomicInteger();
    private final List<Section> loopSections = new ArrayList<>();
    private Section a1, a2, s1, s2, s3, b1, b2;
    private ActiveTrain e, w;

    private Section section(String name, boolean loop) {
        Section s = mock(Section.class);
        when(s.getSystemName()).thenReturn("IY" + name);
        when(s.getDisplayName()).thenReturn(name);
        when(s.getDisplayName(any())).thenReturn(name);
        when(s.getState()).thenReturn(Section.FREE);
        when(s.getBlockList()).thenReturn(new ArrayList<>());
        when(s.getActualLength()).thenReturn(1000);
        occupancy.put(s, Section.UNOCCUPIED);
        when(s.getOccupancy()).thenAnswer(invocation -> occupancy.get(s));
        if (loop) {
            loopSections.add(s);
        }
        return s;
    }

    private ActiveTrain train(String name, Transit transit, Section start) {
        ActiveTrain at = mock(ActiveTrain.class);
        when(at.getTrainName()).thenReturn(name);
        when(at.getTransit()).thenReturn(transit);
        when(at.getMode()).thenReturn(ActiveTrain.AUTOMATIC);
        when(at.getAllocateMethod()).thenReturn(ActiveTrain.ALLOCATE_AS_FAR_AS_IT_CAN);
        when(at.getSignalType()).thenReturn(DispatcherFrame.SECTIONSALLOCATED);
        when(at.getStarted()).thenReturn(true);
        when(at.getPriority()).thenReturn(5);
        when(at.getStartBlockSectionSequenceNumber()).thenReturn(1);
        when(at.getEndBlockSectionSequenceNumber()).thenReturn(5);
        when(at.getAllocationDirectionFromSectionAndSeq(any(), anyInt())).thenAnswer(invocation ->
                transit.getDirectionFromSectionAndSeq(invocation.getArgument(0), invocation.getArgument(1)));
        allocated.put(at, new ArrayList<>());
        when(at.getAllocatedSectionList()).thenAnswer(invocation -> new ArrayList<>(allocated.get(at)));
        when(at.getLastAllocatedSection()).thenAnswer(invocation -> {
            List<AllocatedSection> list = allocated.get(at);
            return list.isEmpty() ? null : list.get(list.size() - 1).getSection();
        });
        when(at.getLastAllocatedSectionSeqNumber()).thenAnswer(invocation -> {
            List<AllocatedSection> list = allocated.get(at);
            return list.isEmpty() ? 0 : list.get(list.size() - 1).getSequence();
        });
        AutoActiveTrain aat = mock(AutoActiveTrain.class);
        when(aat.getAutoEngineer()).thenReturn(mock(AutoActiveTrain.AutoEngineer.class));
        when(aat.canStopIn(any(), anyInt())).thenAnswer(invocation -> loopSections.contains(invocation.getArgument(0)));
        when(at.getAutoActiveTrain()).thenReturn(aat);
        trains.add(at);
        allocated.get(at).add(allocatedSection(at, start, 1));
        occupancy.put(start, Section.OCCUPIED);
        return at;
    }

    private static AllocatedSection allocatedSection(ActiveTrain at, Section s, int seq) {
        AllocatedSection as = mock(AllocatedSection.class);
        when(as.getActiveTrain()).thenReturn(at);
        when(as.getSection()).thenReturn(s);
        when(as.getSequence()).thenReturn(seq);
        return as;
    }

    private AllocationRequest request(ActiveTrain at, Section s, int seq) {
        String name = s.getDisplayName();
        int direction = at.getTransit().getDirectionFromSectionAndSeq(s, seq);
        AllocationRequest ar = mock(AllocationRequest.class);
        when(ar.getActiveTrain()).thenReturn(at);
        when(ar.getSection()).thenReturn(s);
        when(ar.getSectionName()).thenReturn(name);
        when(ar.getSectionSeqNumber()).thenReturn(seq);
        when(ar.getSectionDirection()).thenReturn(direction);
        // allocating it adds it to the train and disposes of the request, as Dispatcher does
        when(df.allocateSection(ar, null)).thenAnswer(invocation -> {
            AllocatedSection as = allocatedSection(at, s, seq);
            allocated.get(at).add(as);
            when(ar.getSection()).thenReturn(null);
            return as;
        });
        return ar;
    }

    private void scan(AllocationRequest... ars) {
        requests.clear();
        for (AllocationRequest ar : ars) {
            requests.add(ar);
        }
        int done = scansDone.get();
        aa.scanAllocationRequests(new TaskAllocateRelease(TaskAction.SCAN_REQUESTS));
        aa.scanAllocationRequests(new TaskAllocateRelease(TaskAction.AUTO_RELEASE));
        JUnitUtil.waitFor(() -> scansDone.get() > done, "scan done");
    }

    @Test
    public void testOnlyOneTrainIsGivenTheSingleLine() {
        AllocationRequest eToS1 = request(e, s1, 2);
        AllocationRequest wToS3 = request(w, s3, 2);
        scan(eToS1, wToS3);
        verify(df).allocateSection(eToS1, null);
        verify(df, never()).allocateSection(eq(wToS3), any());
    }

    @Test
    public void testTheRestOfTheRunFollows() {
        AllocationRequest eToS1 = request(e, s1, 2);
        AllocationRequest wToS3 = request(w, s3, 2);
        scan(eToS1, wToS3);
        AllocationRequest eToS2 = request(e, s2, 3);
        scan(eToS2, wToS3);
        verify(df).allocateSection(eToS2, null);
        AllocationRequest eToS3 = request(e, s3, 4);
        scan(eToS3, wToS3);
        verify(df).allocateSection(eToS3, null);
        verify(df, never()).allocateSection(eq(wToS3), any());
        // loop B's free road, B2, is reserved for E as the end of its run
        List<Section> roads = new ArrayList<>();
        roads.add(b1);
        roads.add(b2);
        org.junit.jupiter.api.Assertions.assertSame(b2, aa.autoNextSectionChoice(roads, eToS3, 5));
    }

    @Test
    public void testNotGivenTheLineWithNoRoadFreeAtTheFarEnd() {
        occupancy.put(b2, Section.OCCUPIED);    // something with no Active Train stands in B2
        AllocationRequest eToS1 = request(e, s1, 2);
        scan(eToS1);
        verify(df, never()).allocateSection(eq(eToS1), any());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        a1 = section("A1", true);
        a2 = section("A2", true);
        s1 = section("S1", false);
        s2 = section("S2", false);
        s3 = section("S3", false);
        b1 = section("B1", true);
        b2 = section("B2", true);
        Transit east = new DefaultTransit("East");
        east.addTransitSection(new TransitSection(a1, 1, Section.FORWARD));
        east.addTransitSection(new TransitSection(s1, 2, Section.FORWARD));
        east.addTransitSection(new TransitSection(s2, 3, Section.FORWARD));
        east.addTransitSection(new TransitSection(s3, 4, Section.FORWARD));
        east.addTransitSection(new TransitSection(b1, 5, Section.FORWARD));
        east.addTransitSection(new TransitSection(b2, 5, Section.FORWARD, true));
        Transit west = new DefaultTransit("West");
        west.addTransitSection(new TransitSection(b1, 1, Section.REVERSE));
        west.addTransitSection(new TransitSection(s3, 2, Section.REVERSE));
        west.addTransitSection(new TransitSection(s2, 3, Section.REVERSE));
        west.addTransitSection(new TransitSection(s1, 4, Section.REVERSE));
        west.addTransitSection(new TransitSection(a1, 5, Section.REVERSE));
        west.addTransitSection(new TransitSection(a2, 5, Section.REVERSE, true));

        df = mock(DispatcherFrame.class);
        when(df.getPreventDeadlock()).thenReturn(true);
        when(df.getActiveTrainsList()).thenReturn(trains);
        when(df.getAllocatedSectionsList()).thenAnswer(invocation -> {
            List<AllocatedSection> list = new ArrayList<>();
            for (ActiveTrain at : trains) {
                list.addAll(allocated.get(at));
            }
            return list;
        });
        doAnswer(invocation -> {
            scansDone.incrementAndGet();
            return null;
        }).when(df).checkAutoRelease();
        InstanceManager.setDefault(DispatcherFrame.class, df);

        e = train("E", east, a1);
        w = train("W", west, b1);

        aa = new AutoAllocate(df, requests);
        allocator = ThreadingUtil.newThread(aa, "AutoAllocateDeadlockTest");
        allocator.start();
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        aa.setAbort();
        allocator.join(10000);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
