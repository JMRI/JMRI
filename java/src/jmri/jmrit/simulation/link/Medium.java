package jmri.jmrit.simulation.link;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * The bus or wire both directions share. Messages queue for it and take
 * their time on it one at a time; when several are waiting as it comes free,
 * the system's {@link ArbitrationPolicy} picks the next. A message comes off
 * the wire when its time on it ends.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public final class Medium {

    private final SimulatedLink link;
    private final WireTiming timing;
    private final List<Waiting> waiting = new ArrayList<>();
    private long freeAt = Long.MIN_VALUE;
    private boolean startScheduled = false;
    private long sequence = 0;
    private final AtomicLong carried = new AtomicLong();
    private final AtomicLong waitedNanos = new AtomicLong();

    private static final class Waiting {

        final LinkFrame frame;
        final long arrived;
        final long sequence;
        final int priority;
        final Stage.Receiver after;
        final Buffer buffer;

        Waiting(LinkFrame frame, long arrived, long sequence, int priority, Stage.Receiver after, Buffer buffer) {
            this.frame = frame;
            this.arrived = arrived;
            this.sequence = sequence;
            this.priority = priority;
            this.after = after;
            this.buffer = buffer;
        }
    }

    Medium(@Nonnull SimulatedLink link, @Nonnull WireTiming timing) {
        this.link = link;
        this.timing = timing;
    }

    /**
     * Take a message onto the queue for the wire.
     *
     * @param frame   the message
     * @param atNanos when it reaches the wire
     * @param after   where it goes when it comes off the wire
     * @param buffer  the buffer it came through, to be told when it starts on
     *                the wire; or null
     */
    void take(@Nonnull LinkFrame frame, long atNanos, @Nonnull Stage.Receiver after, @CheckForNull Buffer buffer) {
        waiting.add(new Waiting(frame, atNanos, sequence++, timing.priority(frame.getMessage()), after, buffer));
        scheduleStart(Math.max(atNanos, freeAt));
    }

    private void scheduleStart(long atNanos) {
        if (!startScheduled) {
            startScheduled = true;
            link.at(atNanos, () -> {
                startScheduled = false;
                start(atNanos);
            });
        }
    }

    private void start(long nowNanos) {
        Waiting next = null;
        for (Waiting w : waiting) {
            if (w.arrived - nowNanos > 0) {
                continue; // not here yet
            }
            if (next == null || before(w, next)) {
                next = w;
            }
        }
        if (next == null) {
            if (!waiting.isEmpty()) {
                long earliest = waiting.get(0).arrived;
                for (Waiting w : waiting) {
                    earliest = w.arrived - earliest < 0 ? w.arrived : earliest;
                }
                scheduleStart(earliest);
            }
            return;
        }
        waiting.remove(next);
        long end = nowNanos + timing.nanosOnWire(next.frame.getMessage());
        freeAt = end;
        carried.incrementAndGet();
        waitedNanos.addAndGet(nowNanos - next.arrived);
        if (next.buffer != null) {
            next.buffer.started(nowNanos);
        }
        Waiting done = next;
        link.at(end, () -> done.after.take(done.frame, end));
        if (!waiting.isEmpty()) {
            scheduleStart(end);
        }
    }

    private boolean before(@Nonnull Waiting a, @Nonnull Waiting b) {
        if (timing.policy() == ArbitrationPolicy.PRIORITY && a.priority != b.priority) {
            return a.priority < b.priority;
        }
        return a.sequence < b.sequence;
    }

    /**
     * @return how many messages it has carried
     */
    public long getCarried() {
        return carried.get();
    }

    /**
     * @return the time, in total, messages waited for the wire
     */
    public long getWaitedNanos() {
        return waitedNanos.get();
    }
}
