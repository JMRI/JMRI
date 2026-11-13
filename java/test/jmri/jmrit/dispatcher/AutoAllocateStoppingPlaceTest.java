package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertSame;
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
 * Tests that {@link AutoAllocate} does not give an automatic train a Section
 * it can't stop in unless a Section beyond it can be given with it.
 * <p>
 * The layout is S1 - T - (L1 or L2) - S4, where T is a short turnout Section
 * the train can't stop in. Train A runs from S1 to S4.
 */
public class AutoAllocateStoppingPlaceTest {

    private DispatcherFrame df;
    private AutoAllocate aa;
    private Thread allocator;
    private final List<ActiveTrain> trains = new ArrayList<>();
    private final List<AllocationRequest> requests = new ArrayList<>();
    private final Map<Section, Integer> occupancy = new HashMap<>();
    private final Map<ActiveTrain, List<AllocatedSection>> allocated = new HashMap<>();
    private final AtomicInteger scansDone = new AtomicInteger();
    private Section s1, t, l1, l2, s4;
    private ActiveTrain a;

    private Section section(String name) {
        Section s = mock(Section.class);
        when(s.getSystemName()).thenReturn("IY" + name);
        when(s.getDisplayName()).thenReturn(name);
        when(s.getDisplayName(any())).thenReturn(name);
        when(s.getState()).thenReturn(Section.FREE);
        occupancy.put(s, Section.UNOCCUPIED);
        when(s.getOccupancy()).thenAnswer(invocation -> occupancy.get(s));
        return s;
    }

    private ActiveTrain automaticTrain(String name, Transit transit) {
        ActiveTrain at = mock(ActiveTrain.class);
        when(at.getTrainName()).thenReturn(name);
        when(at.getTransit()).thenReturn(transit);
        when(at.getMode()).thenReturn(ActiveTrain.AUTOMATIC);
        when(at.getAllocateMethod()).thenReturn(ActiveTrain.ALLOCATE_AS_FAR_AS_IT_CAN);
        when(at.getSignalType()).thenReturn(DispatcherFrame.SECTIONSALLOCATED);
        when(at.getStarted()).thenReturn(true);
        when(at.getAllocationDirectionFromSectionAndSeq(any(), anyInt())).thenAnswer(invocation ->
                transit.getDirectionFromSectionAndSeq(invocation.getArgument(0), invocation.getArgument(1)));
        allocated.put(at, new ArrayList<>());
        when(at.getAllocatedSectionList()).thenAnswer(invocation -> new ArrayList<>(allocated.get(at)));
        AutoActiveTrain aat = mock(AutoActiveTrain.class);
        when(aat.getAutoEngineer()).thenReturn(mock(AutoActiveTrain.AutoEngineer.class));
        // the train can stop in every Section but T
        when(aat.canStopIn(any(), anyInt())).thenAnswer(invocation -> invocation.getArgument(0) != t);
        when(at.getAutoActiveTrain()).thenReturn(aat);
        trains.add(at);
        return at;
    }

    private static AllocatedSection allocatedSection(ActiveTrain at, Section s, int seq, Section next) {
        AllocatedSection as = mock(AllocatedSection.class);
        when(as.getActiveTrain()).thenReturn(at);
        when(as.getSection()).thenReturn(s);
        when(as.getSequence()).thenReturn(seq);
        when(as.getNextSection()).thenReturn(next);
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
        return ar;
    }

    /*
     * Run one scan of the given requests and wait until it is done.
     */
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
    public void testShortSectionWaitsWhileNoRoadBeyondIsFree() {
        // both roads beyond T are taken: A must wait in S1, where it can stop
        occupancy.put(l1, Section.OCCUPIED);
        occupancy.put(l2, Section.OCCUPIED);
        AllocationRequest aToT = request(a, t, 2);
        scan(aToT);
        verify(df, never()).allocateSection(eq(aToT), any());
    }

    @Test
    public void testShortSectionIsAllocatedWithTheFreeRoadBeyond() {
        // L1, the primary road, is taken; L2 is free
        occupancy.put(l1, Section.OCCUPIED);
        AllocationRequest aToT = request(a, t, 2);
        AllocationRequest aToL2 = request(a, l2, 3);
        List<Section> roads = new ArrayList<>();
        roads.add(l1);
        roads.add(l2);
        // Dispatcher chooses the next Section while it allocates T: it must be L2
        when(df.allocateSection(aToT, null)).thenAnswer(invocation -> {
            Section next = aa.autoNextSectionChoice(roads, aToT, 3);
            return allocatedSection(a, t, 2, next);
        });
        when(df.findAllocationRequestInQueue(l2, 3, Section.FORWARD, a)).thenReturn(aToL2);
        scan(aToT);
        verify(df).allocateSection(aToT, null);
        // the road beyond is allocated with T, so the train never has to stop in T
        verify(df).allocateSection(aToL2, null);
    }

    @Test
    public void testPreparedChoiceIsOnlyForTheShortSection() {
        // once T has been allocated, the choice of road is Dispatcher's usual one again
        occupancy.put(l1, Section.OCCUPIED);
        AllocationRequest aToT = request(a, t, 2);
        List<Section> roads = new ArrayList<>();
        roads.add(l1);
        roads.add(l2);
        when(df.allocateSection(aToT, null)).thenAnswer(invocation ->
                allocatedSection(a, t, 2, aa.autoNextSectionChoice(roads, aToT, 3)));
        scan(aToT);
        occupancy.put(l1, Section.UNOCCUPIED);
        assertSame(l1, aa.autoNextSectionChoice(roads, request(a, t, 2), 3),
                "with no prepared choice the free primary road is chosen");
    }

    @Test
    public void testSectionTheTrainCanStopInIsAllocatedAlone() {
        // S4 is long enough: it is allocated whatever lies beyond it
        AllocationRequest aToS4 = request(a, s4, 4);
        scan(aToS4);
        verify(df).allocateSection(aToS4, null);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        s1 = section("S1");
        t = section("T");
        l1 = section("L1");
        l2 = section("L2");
        s4 = section("S4");
        Transit east = new DefaultTransit("East");
        east.addTransitSection(new TransitSection(s1, 1, Section.FORWARD));
        east.addTransitSection(new TransitSection(t, 2, Section.FORWARD));
        east.addTransitSection(new TransitSection(l1, 3, Section.FORWARD));
        east.addTransitSection(new TransitSection(l2, 3, Section.FORWARD, true));
        east.addTransitSection(new TransitSection(s4, 4, Section.FORWARD));

        df = mock(DispatcherFrame.class);
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

        a = automaticTrain("A", east);
        occupancy.put(s1, Section.OCCUPIED);
        allocated.get(a).add(allocatedSection(a, s1, 1, t));

        aa = new AutoAllocate(df, requests);
        allocator = ThreadingUtil.newThread(aa, "AutoAllocateStoppingPlaceTest");
        allocator.start();
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        aa.setAbort();
        allocator.join(10000);
        JUnitUtil.tearDown();
    }

}
