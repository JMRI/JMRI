package jmri.jmrit.dispatcher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import jmri.Section;
import jmri.jmrit.dispatcher.TaskAllocateRelease.TaskAction;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 * Tests that an allocation queued on the auto allocator's thread, as a
 * request allocated by hand is when auto allocation is on, is made only if
 * the request is still wanted when its turn comes.
 */
public class AutoAllocateImmediateTest {

    private DispatcherFrame d;
    private AutoAllocate aa;
    private Thread allocator;

    private void runQueued(AllocationRequest ar) {
        aa.scanAllocationRequests(new TaskAllocateRelease(TaskAction.ALLOCATE_IMMEDIATE, ar));
        CountDownLatch done = new CountDownLatch(1);
        aa.scanAllocationRequests(new TaskAllocateRelease(TaskAction.BARRIER, done::countDown));
        JUnitUtil.waitFor(() -> done.getCount() == 0, "queued tasks run");
    }

    @Test
    public void testRequestStillWantedIsAllocated() {
        AllocationRequest ar = mock(AllocationRequest.class);
        when(ar.getActiveTrain()).thenReturn(mock(ActiveTrain.class));
        when(ar.getSection()).thenReturn(mock(Section.class));
        runQueued(ar);
        verify(d, timeout(1000)).allocateSection(ar, null);
    }

    @Test
    public void testRequestDisposedBeforeItsTurnIsNotAllocated() {
        // allocated by a scan, or cancelled, while it waited: disposed, so its train and Section are gone
        AllocationRequest ar = mock(AllocationRequest.class);
        runQueued(ar);
        verify(d, never()).allocateSection(any(), any());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        d = mock(DispatcherFrame.class);
        aa = new AutoAllocate(d, new ArrayList<>());
        allocator = ThreadingUtil.newThread(aa, "AutoAllocateImmediateTest");
        allocator.start();
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        aa.setAbort();
        allocator.join(10000);
        JUnitUtil.tearDown();
    }

}
