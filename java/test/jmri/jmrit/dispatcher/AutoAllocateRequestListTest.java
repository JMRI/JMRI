package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Check that the Auto Allocator copes with Dispatcher's list of allocation
 * requests being changed by another thread while it takes its copy.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class AutoAllocateRequestListTest {

    // a list that another thread shortens just after its size has been read
    private static class ShortenedWhileReadList extends ArrayList<AllocationRequest> {

        private int sizeCalls;
        private final int shortenOnCall;

        ShortenedWhileReadList(int shortenOnCall) {
            this.shortenOnCall = shortenOnCall;
        }

        @Override
        public int size() {
            int size = super.size();
            if (++sizeCalls == shortenOnCall && size > 0) {
                // the other thread removes the last request now
                super.remove(size - 1);
            }
            return size;
        }
    }

    private static AllocationRequest request(String trainName, int priority) {
        ActiveTrain at = mock(ActiveTrain.class);
        when(at.getTrainName()).thenReturn(trainName);
        when(at.getPriority()).thenReturn(priority);
        AllocationRequest ar = mock(AllocationRequest.class);
        when(ar.getActiveTrain()).thenReturn(at);
        return ar;
    }

    @Test
    public void testCopyWhileAnotherThreadRemovesARequest() {
        AutoAllocate autoAllocate = new AutoAllocate(mock(DispatcherFrame.class), new ArrayList<>());
        AllocationRequest low = request("Low", 1);
        AllocationRequest high = request("High", 9);
        AllocationRequest middle = request("Middle", 5);
        List<AllocationRequest> requests = new ShortenedWhileReadList(3);
        requests.add(low);
        requests.add(high);
        requests.add(middle);

        // throws IndexOutOfBoundsException if the copy reads the size and the
        // requests separately
        autoAllocate.copyAndSortARs(requests);

        assertEquals(List.of(high, middle, low), autoAllocate.getOrderedRequests(),
                "the requests as they were when copied, highest priority first");
    }

    @Test
    public void testCopyInPriorityOrder() {
        AutoAllocate autoAllocate = new AutoAllocate(mock(DispatcherFrame.class), new ArrayList<>());
        AllocationRequest a = request("A", 2);
        AllocationRequest b = request("B", 2);
        AllocationRequest c = request("C", 7);
        autoAllocate.copyAndSortARs(new ArrayList<>(List.of(b, a, c)));
        assertEquals(List.of(c, a, b), autoAllocate.getOrderedRequests(),
                "highest priority first, then by train name");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
