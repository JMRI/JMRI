package jmri.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import jmri.Meter;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks that a disposed average meter stops reading its meter.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DefaultAverageMeterDisposeTest {

    @Test
    public void testDisposeStopsUpdates() throws InterruptedException {
        AtomicInteger reads = new AtomicInteger();
        Meter m = new DefaultMeter.DefaultVoltageMeter("IVTEST", Meter.Unit.NoPrefix, 0, 10, 1,
                new MeterUpdateTask(-1, -1) {
                    @Override
                    public void requestUpdateFromLayout() {
                    }
                }) {
            @Override
            public double getKnownAnalogValue() {
                reads.incrementAndGet();
                return 1.0;
            }
        };
        DefaultAverageMeter am = new DefaultAverageMeter("IVAVG");
        am.setMeter(m);
        JUnitUtil.waitFor(() -> reads.get() >= 1, "meter read");

        am.dispose();
        Thread.sleep(50); // a read already handed to the GUI thread may still happen
        ThreadingUtil.runOnGUI(() -> { });
        int atDispose = reads.get();
        Thread.sleep(600); // more than two update periods
        ThreadingUtil.runOnGUI(() -> { });
        assertEquals(atDispose, reads.get(), "meter reads after dispose");
    }

    @Test
    public void testRemoveMeterWithUpdateQueued() {
        AtomicInteger reads = new AtomicInteger();
        Meter m = new DefaultMeter.DefaultVoltageMeter("IVTEST2", Meter.Unit.NoPrefix, 0, 10, 1,
                new MeterUpdateTask(-1, -1) {
                    @Override
                    public void requestUpdateFromLayout() {
                    }
                }) {
            @Override
            public double getKnownAnalogValue() {
                reads.incrementAndGet();
                return 1.0;
            }
        };
        DefaultAverageMeter am = new DefaultAverageMeter("IVAVG2");
        am.setMeter(m);
        JUnitUtil.waitFor(() -> reads.get() >= 1, "meter read");

        List<Throwable> uncaught = Collections.synchronizedList(new ArrayList<>());
        Thread.UncaughtExceptionHandler saved = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> uncaught.add(e));
        try {
            ThreadingUtil.runOnGUI(() -> {
                // hold the GUI thread for more than one update period,
                // so that the next update is queued behind this
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                am.setMeter(null);
            });
            // the queued update runs after the meter was removed
            ThreadingUtil.runOnGUI(() -> { });
            ThreadingUtil.runOnGUI(() -> { });
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(saved);
        }
        assertEquals("[]", uncaught.toString(), "exceptions on the GUI thread");
        assertEquals(null, am.getMeter());
        am.dispose();
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
