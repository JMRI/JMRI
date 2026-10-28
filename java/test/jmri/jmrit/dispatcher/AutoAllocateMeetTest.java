package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
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
 * Tests for meet planning in {@link AutoAllocate}.
 * <p>
 * The layout is a single line with a passing loop in the middle:
 * S1 - S2 - (L1 or L2) - S4 - S5.
 * Train A runs from S1 to S5, train B from S5 to S1.
 */
public class AutoAllocateMeetTest {

    private DispatcherFrame df;
    private AutoAllocate aa;
    private Thread allocator;
    private final List<ActiveTrain> trains = new ArrayList<>();
    private final List<AllocationRequest> requests = new ArrayList<>();
    private final Map<Section, Integer> occupancy = new HashMap<>();
    private final AtomicInteger scansDone = new AtomicInteger();
    private Section s1, s2, l1, l2, s4, s5;

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

    private static Transit transit(String name, int direction, Section... seq) {
        Transit t = new DefaultTransit(name);
        int n = 0;
        for (Section s : seq) {
            n++;
            t.addTransitSection(new TransitSection(s, n, direction));
        }
        return t;
    }

    private static Transit transitWithLoop(String name, int direction, Section a, Section b,
            Section loop, Section alternate, Section c, Section d) {
        Transit t = new DefaultTransit(name);
        t.addTransitSection(new TransitSection(a, 1, direction));
        t.addTransitSection(new TransitSection(b, 2, direction));
        t.addTransitSection(new TransitSection(loop, 3, direction));
        t.addTransitSection(new TransitSection(alternate, 3, direction, true));
        t.addTransitSection(new TransitSection(c, 4, direction));
        t.addTransitSection(new TransitSection(d, 5, direction));
        return t;
    }

    private final Map<ActiveTrain, List<AllocatedSection>> allocated = new HashMap<>();

    private ActiveTrain train(String name, Transit t, int mode) {
        ActiveTrain at = mock(ActiveTrain.class);
        when(at.getTrainName()).thenReturn(name);
        when(at.getTransit()).thenReturn(t);
        when(at.getMode()).thenReturn(mode);
        when(at.getAllocateMethod()).thenReturn(ActiveTrain.ALLOCATE_AS_FAR_AS_IT_CAN);
        when(at.getSignalType()).thenReturn(DispatcherFrame.SECTIONSALLOCATED);
        when(at.getStarted()).thenReturn(true);
        when(at.getAllocationDirectionFromSectionAndSeq(any(), anyInt())).thenAnswer(invocation ->
                t.getDirectionFromSectionAndSeq(invocation.getArgument(0), invocation.getArgument(1)));
        allocated.put(at, new ArrayList<>());
        when(at.getAllocatedSectionList()).thenAnswer(invocation -> new ArrayList<>(allocated.get(at)));
        trains.add(at);
        return at;
    }

    /*
     * Set the Sections allocated to a train, the first one occupied.
     */
    private void at(ActiveTrain at, Section occupied, int seq, Section... ahead) {
        List<AllocatedSection> list = allocated.get(at);
        list.clear();
        occupancy.put(occupied, Section.OCCUPIED);
        list.add(allocatedSection(at, occupied, seq));
        int n = seq;
        for (Section s : ahead) {
            list.add(allocatedSection(at, s, ++n));
        }
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
        // the auto release check is queued after the scan, so it runs once the scan is done
        aa.scanAllocationRequests(new TaskAllocateRelease(TaskAction.AUTO_RELEASE));
        JUnitUtil.waitFor(() -> scansDone.get() > done, "scan done");
    }

    @Test
    public void testOpposingTrainsMeetInPassingLoop() {
        s1 = section("S1");
        s2 = section("S2");
        l1 = section("L1");
        l2 = section("L2");
        s4 = section("S4");
        s5 = section("S5");
        ActiveTrain a = train("A", transitWithLoop("East", Section.FORWARD, s1, s2, l1, l2, s4, s5),
                ActiveTrain.DISPATCHED);
        ActiveTrain b = train("B", transitWithLoop("West", Section.REVERSE, s5, s4, l1, l2, s2, s1),
                ActiveTrain.DISPATCHED);
        at(a, s1, 1);
        at(b, s5, 1);

        // A may enter the single line towards the loop
        AllocationRequest aToS2 = request(a, s2, 2);
        scan(aToS2);
        verify(df).allocateSection(aToS2, null);

        // A is steered into the loop track B will not use
        List<Section> loop = new ArrayList<>();
        loop.add(l1);
        loop.add(l2);
        Section aLoop = aa.autoNextSectionChoice(loop, request(a, l1, 3), 3);
        assertSame(l2, aLoop, "A is given the loop track planned for it");

        // A has reached the loop, B is still at the far end: A waits in the loop
        occupancy.put(s1, Section.UNOCCUPIED);
        at(a, l2, 3);
        AllocationRequest aToS4 = request(a, s4, 4);
        scan(aToS4);
        verify(df, never()).allocateSection(aToS4, null);

        // B comes through the single line into the other loop track
        AllocationRequest bToS4 = request(b, s4, 2);
        scan(aToS4, bToS4);
        verify(df, never()).allocateSection(aToS4, null);
        verify(df).allocateSection(bToS4, null);
        assertSame(l1, aa.autoNextSectionChoice(loop, request(b, l1, 3), 3),
                "B is given the other loop track");

        // once B is in the loop, A can leave it
        occupancy.put(s5, Section.UNOCCUPIED);
        at(b, l1, 3);
        scan(aToS4);
        verify(df).allocateSection(aToS4, null);
    }

    /*
     * Set up the meet of the first test up to the point where A waits in the
     * loop for B. Returns A's request to leave the loop.
     */
    private AllocationRequest aWaitsInLoopForB(ActiveTrain a, ActiveTrain b) {
        at(a, s1, 1);
        at(b, s5, 1);
        AllocationRequest aToS2 = request(a, s2, 2);
        scan(aToS2);
        verify(df).allocateSection(aToS2, null);
        occupancy.put(s1, Section.UNOCCUPIED);
        at(a, l2, 3);
        AllocationRequest aToS4 = request(a, s4, 4);
        scan(aToS4);
        verify(df, never()).allocateSection(aToS4, null);
        return aToS4;
    }

    private ActiveTrain[] meetTrains() {
        s1 = section("S1");
        s2 = section("S2");
        l1 = section("L1");
        l2 = section("L2");
        s4 = section("S4");
        s5 = section("S5");
        ActiveTrain a = train("A", transitWithLoop("East", Section.FORWARD, s1, s2, l1, l2, s4, s5),
                ActiveTrain.DISPATCHED);
        ActiveTrain b = train("B", transitWithLoop("West", Section.REVERSE, s5, s4, l1, l2, s2, s1),
                ActiveTrain.DISPATCHED);
        return new ActiveTrain[]{a, b};
    }

    @Test
    public void testMeetIsDroppedWhenOtherTrainIsTerminated() {
        ActiveTrain[] t = meetTrains();
        AllocationRequest aToS4 = aWaitsInLoopForB(t[0], t[1]);

        // B is terminated before reaching the loop: A must not wait for it forever
        trains.remove(t[1]);
        occupancy.put(s5, Section.UNOCCUPIED);
        scan(aToS4);
        verify(df).allocateSection(aToS4, null);
    }

    @Test
    public void testMeetIsDroppedWhenOtherTrainIsReset() {
        ActiveTrain[] t = meetTrains();
        AllocationRequest aToS4 = aWaitsInLoopForB(t[0], t[1]);

        // B is reset to run again and has lost its allocations: A must not wait for it
        allocated.get(t[1]).clear();
        occupancy.put(s5, Section.UNOCCUPIED);
        scan(aToS4);
        verify(df).allocateSection(aToS4, null);
    }

    @Test
    public void testDispatchedTrainWithoutPositionDoesNotBlockAllocation() {
        s1 = section("S1");
        s2 = section("S2");
        Section x1 = section("X1");
        Section x2 = section("X2");
        ActiveTrain a = train("A", transit("East", Section.FORWARD, s1, s2), ActiveTrain.DISPATCHED);
        // a manually driven train elsewhere, not yet in its first Section
        train("B", transit("Other", Section.FORWARD, x1, x2), ActiveTrain.DISPATCHED);
        at(a, s1, 1);

        AllocationRequest aToS2 = request(a, s2, 2);
        scan(aToS2);
        verify(df).allocateSection(aToS2, null);
    }

    @Test
    public void testOtherAutomaticTrainIsNotHalted() {
        s1 = section("S1");
        s2 = section("S2");
        Section x1 = section("X1");
        Section x2 = section("X2");
        // A can take S2 or X2: B, not yet in its first Section, will need S2,
        // so finding B's position is part of deciding whether A may go on
        Transit east = new DefaultTransit("East");
        east.addTransitSection(new TransitSection(s1, 1, Section.FORWARD));
        east.addTransitSection(new TransitSection(s2, 2, Section.FORWARD));
        east.addTransitSection(new TransitSection(x2, 2, Section.FORWARD, true));
        ActiveTrain a = train("A", east, ActiveTrain.DISPATCHED);
        ActiveTrain b = train("B", transit("Other", Section.FORWARD, x1, s2), ActiveTrain.AUTOMATIC);
        AutoActiveTrain aat = mock(AutoActiveTrain.class);
        AutoActiveTrain.AutoEngineer engineer = mock(AutoActiveTrain.AutoEngineer.class);
        when(aat.getAutoEngineer()).thenReturn(engineer);
        when(b.getAutoActiveTrain()).thenReturn(aat);
        at(a, s1, 1);

        AllocationRequest aToS2 = request(a, s2, 2);
        scan(aToS2);
        verify(df).allocateSection(aToS2, null);
        verify(engineer, never()).setHalt(anyBoolean());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
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
        aa = new AutoAllocate(df, requests);
        allocator = ThreadingUtil.newThread(aa, "AutoAllocateMeetTest");
        allocator.start();
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        aa.setAbort();
        allocator.join(10000);
        JUnitUtil.tearDown();
    }

}
