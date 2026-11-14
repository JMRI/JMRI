package jmri.util.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeouts;

/**
 * Tests for ThreadSafeTimeouts.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class ThreadSafeTimeoutsTest {

    @Test
    public void testCopyWhileAnotherThreadSetsATimeout() throws InterruptedException {
        Timeouts shared = new ThreadSafeTimeouts();
        for (int i = 0; i < 20; i++) {
            shared.setTimeout("Timeout" + i, 1000 + i);
        }
        AtomicBoolean copying = new AtomicBoolean(true);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        // one thread keeps setting a timeout to the value it already has,
        // as Jemmy operators do, while this thread copies the timeouts
        Thread setter = new Thread(() -> {
            while (copying.get()) {
                shared.setTimeout("Timeout7", 1007);
            }
        }, "ThreadSafeTimeoutsTest setter");
        setter.start();
        try {
            for (int i = 0; i < 100000 && failure.get() == null; i++) {
                try {
                    Timeouts copy = shared.cloneThis();
                    assertEquals(1007, copy.getTimeout("Timeout7"), "copied value");
                } catch (RuntimeException | AssertionError e) {
                    failure.set(e);
                }
            }
        } finally {
            copying.set(false);
            setter.join();
        }
        assertNull(failure.get(), "timeouts copied while another thread sets one");
    }

    @Test
    public void testUsedForTheCurrentTimeouts() {
        assertTrue(JemmyProperties.getCurrentTimeouts() instanceof ThreadSafeTimeouts,
                "the current Jemmy timeouts can be shared between threads");
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
