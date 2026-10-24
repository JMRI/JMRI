package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.AbstractList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import jmri.jmrit.dispatcher.TaskAllocateRelease.TaskAction;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 * Tests that repeated requests for the same check are merged while one is
 * already waiting at the end of the auto allocator queue.
 */
public class AutoAllocateQueueTest {

    private final CountDownLatch inTask = new CountDownLatch(1);
    private final CountDownLatch finishTask = new CountDownLatch(1);
    private final AtomicInteger scans = new AtomicInteger();
    private final AtomicInteger releaseChecks = new AtomicInteger();
    private AutoAllocate aa;
    private Thread allocator;

    @Test
    public void testRepeatedScansAreMerged() throws InterruptedException {
        // the allocator is busy with a scan
        aa.scanAllocationRequests(new TaskAllocateRelease(TaskAction.SCAN_REQUESTS));
        assertTrue(inTask.await(10, TimeUnit.SECONDS), "allocator is running the first scan");

        // a scan requested now must still run, as the running one may have missed the change
        for (int i = 0; i < 5; i++) {
            aa.scanAllocationRequests(new TaskAllocateRelease(TaskAction.SCAN_REQUESTS));
        }
        assertEquals(1, aa.taskList.size(), "five requests while waiting give one scan");

        for (int i = 0; i < 3; i++) {
            aa.scanAllocationRequests(new TaskAllocateRelease(TaskAction.AUTO_RELEASE));
        }
        assertEquals(2, aa.taskList.size(), "three auto release requests give one check");

        // not merged with a scan queued before another task
        aa.scanAllocationRequests(new TaskAllocateRelease(TaskAction.SCAN_REQUESTS));
        aa.scanAllocationRequests(new TaskAllocateRelease(TaskAction.SCAN_REQUESTS));
        assertEquals(3, aa.taskList.size(), "a scan after another task is queued again");

        finishTask.countDown();
        JUnitUtil.waitFor(() -> scans.get() == 3 && releaseChecks.get() == 1, "all queued tasks run");
        JUnitUtil.waitFor(() -> aa.taskList.isEmpty(), "queue empty");

        // once the queued scan has started, a new request is queued again
        aa.scanAllocationRequests(new TaskAllocateRelease(TaskAction.SCAN_REQUESTS));
        JUnitUtil.waitFor(() -> scans.get() == 4, "new scan run");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        DispatcherFrame d = mock(DispatcherFrame.class);
        doAnswer(invocation -> {
            releaseChecks.incrementAndGet();
            return null;
        }).when(d).checkAutoRelease();
        // each scan starts by looking at the size of the request list
        List<AllocationRequest> requests = new AbstractList<AllocationRequest>() {
            @Override
            public AllocationRequest get(int index) {
                throw new IndexOutOfBoundsException();
            }

            @Override
            public int size() {
                scans.incrementAndGet();
                inTask.countDown();
                try {
                    finishTask.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return 0;
            }
        };
        aa = new AutoAllocate(d, requests);
        allocator = ThreadingUtil.newThread(aa, "AutoAllocateQueueTest");
        allocator.start();
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        finishTask.countDown();
        aa.setAbort();
        allocator.join(10000);
        JUnitUtil.tearDown();
    }

}
