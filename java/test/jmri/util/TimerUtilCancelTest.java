package jmri.util;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks that the task returned by the repeating GUI-thread schedule
 * methods of TimerUtil stops the repeats when cancelled.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class TimerUtilCancelTest {

    private void checkCancel(boolean fixedRate) throws InterruptedException {
        AtomicInteger runs = new AtomicInteger();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runs.incrementAndGet();
            }
        };
        TimerTask scheduled = fixedRate
                ? TimerUtil.scheduleAtFixedRateOnGUIThread(task, 10, 10)
                : TimerUtil.scheduleOnGUIThread(task, 10, 10);
        JUnitUtil.waitFor(() -> runs.get() >= 2, "task runs");

        scheduled.cancel();
        Thread.sleep(30); // a run already handed to the GUI thread may still happen
        ThreadingUtil.runOnGUI(() -> { });
        int atCancel = runs.get();
        Thread.sleep(100);
        ThreadingUtil.runOnGUI(() -> { });
        assertEquals(atCancel, runs.get(), "runs after cancel");
    }

    @Test
    public void testCancelFixedRateOnGUIThread() throws InterruptedException {
        checkCancel(true);
    }

    @Test
    public void testCancelFixedDelayOnGUIThread() throws InterruptedException {
        checkCancel(false);
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
