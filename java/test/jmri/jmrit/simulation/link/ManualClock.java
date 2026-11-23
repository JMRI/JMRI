package jmri.jmrit.simulation.link;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A clock for tests: it stands still until advanced, and then runs the tasks
 * due, in time order, on the test's thread.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class ManualClock implements LinkClock {

    private long now;
    private final List<long[]> times = new ArrayList<>();
    private final List<Runnable> tasks = new ArrayList<>();
    private boolean disposed = false;

    /**
     * @param startNanos the time it starts at
     */
    public ManualClock(long startNanos) {
        now = startNanos;
    }

    @Override
    public long now() {
        return now;
    }

    @Override
    public void schedule(long atNanos, @Nonnull Runnable task) {
        if (!disposed) {
            times.add(new long[]{atNanos});
            tasks.add(task);
        }
    }

    @Override
    public void dispose() {
        disposed = true;
    }

    /**
     * Run what is due now, and anything it makes due now.
     */
    public void runDue() {
        advanceTo(now);
    }

    /**
     * Move to a time, running every task due on the way, in time order (the
     * order given, for tasks due at the same time), with the clock at each
     * task's time as it runs.
     *
     * @param toNanos the time
     */
    public void advanceTo(long toNanos) {
        while (true) {
            int next = -1;
            for (int i = 0; i < times.size(); i++) {
                if (times.get(i)[0] - toNanos <= 0 && (next < 0 || times.get(i)[0] < times.get(next)[0])) {
                    next = i;
                }
            }
            if (next < 0) {
                break;
            }
            long at = times.remove(next)[0];
            Runnable task = tasks.remove(next);
            now = Math.max(now, at);
            task.run();
        }
        now = Math.max(now, toNanos);
    }

    /**
     * @return true if it has been disposed of
     */
    public boolean isDisposed() {
        return disposed;
    }
}
