package jmri.jmrit.simulation.link;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * The time and the scheduling a {@link SimulatedLink} runs on. Every stage,
 * and the medium, runs on the clock's one thread, so they need no locks.
 * <p>
 * {@link #realTime()} is the clock a link uses: {@link System#nanoTime()}
 * and a thread of its own. Tests give a link a clock they advance by hand.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public interface LinkClock {

    /**
     * @return now, on the {@link System#nanoTime()} scale
     */
    long now();

    /**
     * Run a task on the clock's thread at a time, or as soon as possible if
     * that time has passed. The link keeps its own events in order, so the
     * clock need not order tasks due at about the same time.
     *
     * @param atNanos when, on the {@link System#nanoTime()} scale
     * @param task    the task
     */
    void schedule(long atNanos, @Nonnull Runnable task);

    /**
     * Stop running tasks, for good.
     */
    void dispose();

    /**
     * @return a new clock on {@link System#nanoTime()}, with a daemon thread
     *         of its own
     */
    @Nonnull
    static LinkClock realTime() {
        return new LinkClock() {
            private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "Simulated link");
                t.setDaemon(true);
                return t;
            });

            @Override
            public long now() {
                return System.nanoTime();
            }

            @Override
            public void schedule(long atNanos, @Nonnull Runnable task) {
                if (!executor.isShutdown()) {
                    executor.schedule(task, Math.max(0, atNanos - System.nanoTime()), TimeUnit.NANOSECONDS);
                }
            }

            @Override
            public void dispose() {
                executor.shutdownNow();
            }
        };
    }
}
