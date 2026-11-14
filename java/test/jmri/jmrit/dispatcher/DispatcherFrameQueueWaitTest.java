package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jmri.InstanceManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Tests that {@link DispatcherFrame#queueWaitForEmpty} waits for the task the
 * auto allocator is running, not only for the queue to be empty.
 */
@DisabledIfHeadless
public class DispatcherFrameQueueWaitTest {

    private LayoutEditor le;
    private DispatcherFrame d;
    private final CountDownLatch inTask = new CountDownLatch(1);
    private final CountDownLatch finishTask = new CountDownLatch(1);

    /*
     * Queue an auto release task that blocks until finishTask is released,
     * and wait until the allocator is running it.
     */
    private void startLongTask() throws InterruptedException {
        AllocatedSection as = mock(AllocatedSection.class);
        when(as.getExited()).thenAnswer(invocation -> {
            inTask.countDown();
            finishTask.await(10, TimeUnit.SECONDS);
            return false;
        });
        d.getAllocatedSectionsList().add(as);
        d.queueReleaseOfCompletedAllocations();
        assertTrue(inTask.await(10, TimeUnit.SECONDS), "allocator is running the task");
    }

    @Test
    public void testWaitsForRunningTask() throws InterruptedException {
        startLongTask();
        Thread waiter = new Thread(d::queueWaitForEmpty, "queueWaitForEmpty test");
        waiter.start();
        waiter.join(300);
        assertTrue(waiter.isAlive(), "still waiting while the task runs");
        finishTask.countDown();
        waiter.join(10000);
        assertFalse(waiter.isAlive(), "returned when the task finished");
    }

    @Test
    public void testGuiThreadKeepsRunningWhileWaiting() throws InterruptedException {
        startLongTask();
        CountDownLatch returned = new CountDownLatch(1);
        ThreadingUtil.runOnGUIEventually(() -> {
            d.queueWaitForEmpty();
            returned.countDown();
        });
        CountDownLatch guiRan = new CountDownLatch(1);
        ThreadingUtil.runOnGUIEventually(guiRan::countDown);
        assertTrue(guiRan.await(10, TimeUnit.SECONDS), "GUI thread handles other events while waiting");
        assertEquals(1, returned.getCount(), "still waiting while the task runs");
        finishTask.countDown();
        assertTrue(returned.await(10, TimeUnit.SECONDS), "returned when the task finished");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        // auto allocate and auto release on
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/MultiBlockStopdispatcheroptions.xml");
        le = new LayoutEditor("Queue Wait Test");
        d = InstanceManager.getDefault(DispatcherFrame.class);
    }

    @AfterEach
    public void tearDown() {
        finishTask.countDown();
        if (d != null) {
            JUnitUtil.dispose(d);
        }
        if (le != null) {
            JUnitUtil.dispose(le);
        }
        // do not leave auto allocate on for tests that run after this one
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
