package jmri.jmrit.dispatcher;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.InstanceManager;
import jmri.Section;
import jmri.Transit;
import jmri.jmrit.dispatcher.TaskAllocateRelease.TaskAction;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * A train terminated while the auto allocator is working on it: whatever the
 * allocator gives it meanwhile is taken back, and it is given nothing more.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class DispatcherFrameTerminateRaceTest {

    private LayoutEditor le;
    private DispatcherFrame d;
    private ActiveTrain train;
    private final AtomicBoolean terminating = new AtomicBoolean();
    private final CountDownLatch allocatorBusy = new CountDownLatch(1);
    private final CountDownLatch allocatorGoOn = new CountDownLatch(1);

    private AutoAllocate allocator() throws ReflectiveOperationException {
        Field f = DispatcherFrame.class.getDeclaredField("autoAllocate");
        f.setAccessible(true);
        return (AutoAllocate) f.get(d);
    }

    @SuppressWarnings("unchecked")
    private List<AllocationRequest> requests() throws ReflectiveOperationException {
        Field f = DispatcherFrame.class.getDeclaredField("allocationRequests");
        f.setAccessible(true);
        return (List<AllocationRequest>) f.get(d);
    }

    /** Waits until every task now queued for the allocator has been done. */
    private void drainAllocator() throws Exception {
        CountDownLatch done = new CountDownLatch(1);
        allocator().scanAllocationRequests(new TaskAllocateRelease(TaskAction.BARRIER, done::countDown));
        Assertions.assertTrue(done.await(10, TimeUnit.SECONDS), "allocator finished its queue");
    }

    @Test
    public void testAllocationMadeWhileTerminatingIsReleased() throws Exception {
        // the allocator is busy, as when it is working out an allocation for this train
        allocator().scanAllocationRequests(new TaskAllocateRelease(TaskAction.BARRIER, () -> {
            allocatorBusy.countDown();
            try {
                allocatorGoOn.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
        Assertions.assertTrue(allocatorBusy.await(10, TimeUnit.SECONDS), "allocator busy");
        // the train's request, and a scan of the requests queued before the train is terminated
        Section requested = mock(Section.class);
        when(requested.getState()).thenReturn(Section.FREE);
        AllocationRequest request = new AllocationRequest(requested, 2, Section.FORWARD, train);
        requests().add(request);
        allocator().scanAllocationRequests(new TaskAllocateRelease(TaskAction.SCAN_REQUESTS));

        ThreadingUtil.runOnGUI(() -> d.terminateActiveTrain(train, true, false));

        // the allocation it was working on completes after the train was terminated
        Section section = mock(Section.class);
        AllocatedSection late = mock(AllocatedSection.class);
        when(late.getSection()).thenReturn(section);
        when(late.getActiveTrain()).thenReturn(train);
        when(late.getSectionName()).thenReturn("Late Section");
        d.getAllocatedSectionsList().add(late);

        allocatorGoOn.countDown();
        drainAllocator();

        Assertions.assertFalse(d.getAllocatedSectionsList().contains(late), "late Section released");
        verify(train).releaseAllocatedSection(late);
        verify(late).dispose();
        Assertions.assertFalse(requests().contains(request), "request removed");
        verify(requested, never()).setState(Section.FORWARD);
    }

    @Test
    public void testTerminatingTrainIsGivenNothingMore() throws Exception {
        terminating.set(true);
        Section section = mock(Section.class);
        when(section.getState()).thenReturn(Section.FREE);
        Assertions.assertFalse(d.requestAllocation(train, section, Section.FORWARD, 1, false, null),
                "no request from a terminating train");
        AllocationRequest ar = new AllocationRequest(section, 1, Section.FORWARD, train);
        Assertions.assertNull(d.allocateSection(ar, null), "nothing allocated to a terminating train");
        Assertions.assertTrue(d.getAllocatedSectionsList().isEmpty());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");
        le = new LayoutEditor("Terminate Race Test");
        d = InstanceManager.getDefault(DispatcherFrame.class);
        d.setAutoAllocate(true);

        train = mock(ActiveTrain.class);
        when(train.getTrainName()).thenReturn("Train");
        when(train.getActiveTrainName()).thenReturn("Train");
        when(train.getNextTrain()).thenReturn("");
        when(train.getTransit()).thenReturn(mock(Transit.class));
        doAnswer(i -> {
            terminating.set(true);
            return null;
        }).when(train).setTerminating();
        when(train.isTerminating()).thenAnswer(i -> terminating.get());
    }

    @AfterEach
    public void tearDown() {
        allocatorGoOn.countDown();
        if (d != null) {
            JUnitUtil.dispose(d);
        }
        if (le != null) {
            JUnitUtil.dispose(le);
        }
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
