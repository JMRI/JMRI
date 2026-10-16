package jmri.jmrit.logixng.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test that the LogixNG thread accepts many events while it is busy and
 * keeps running after an exception.
 *
 * @author Keith Bruce 2026
 */
public class LogixNG_ThreadQueueTest {

    @Test
    public void testManyEventsWhileBusy() throws InterruptedException {
        LogixNG_Thread thread = LogixNG_Thread.getThread(LogixNG_Thread.DEFAULT_LOGIXNG_THREAD);

        CountDownLatch busy = new CountDownLatch(1);
        CountDownLatch started = new CountDownLatch(1);
        thread.runOnLogixNGEventually(() -> {
            started.countDown();
            try {
                busy.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        assertTrue(started.await(10, TimeUnit.SECONDS), "LogixNG thread is busy");

        // Many sensor changes while the LogixNG thread is busy must not
        // throw an exception into the code that changed the sensor.
        AtomicInteger count = new AtomicInteger();
        int numEvents = 5000;
        for (int i = 0; i < numEvents; i++) {
            thread.runOnLogixNGEventually(count::incrementAndGet);
        }

        busy.countDown();

        // runOnLogixNG() returns when its action has run, which is after all
        // the events queued above.
        thread.runOnLogixNG(() -> {});
        assertEquals(numEvents, count.get(), "all events are executed");

        // A long queue is reported once
        JUnitAppender.assertWarnMessageStartsWith("The LogixNG thread ");
    }

    @Test
    public void testThreadSurvivesException() throws InterruptedException {
        LogixNG_Thread thread = LogixNG_Thread.getThread(LogixNG_Thread.DEFAULT_LOGIXNG_THREAD);

        thread.runOnLogixNGEventually(() -> {
            throw new IllegalStateException("Test exception");
        });

        CountDownLatch done = new CountDownLatch(1);
        thread.runOnLogixNGEventually(done::countDown);
        assertTrue(done.await(5, TimeUnit.SECONDS), "LogixNG thread still runs after an exception");
        JUnitAppender.assertErrorMessageStartsWith("Exception on LogixNG thread ");

        // An exception in runOnLogixNG() must not leave the caller waiting
        thread.runOnLogixNG(() -> {
            throw new IllegalStateException("Test exception");
        });
        JUnitAppender.assertErrorMessageStartsWith("Exception on LogixNG thread ");

        AtomicInteger count = new AtomicInteger();
        thread.runOnLogixNG(count::incrementAndGet);
        assertEquals(1, count.get(), "LogixNG thread still runs after an exception");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }

}
