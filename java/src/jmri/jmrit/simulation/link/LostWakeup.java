package jmri.jmrit.simulation.link;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A forwarder that misses a wake-up: a message arriving while it is still
 * busy with the one before is not noticed until another message arrives
 * after it has finished, and then goes on with that one. If none comes, it
 * waits for ever.
 * <p>
 * This is the fault in canpi-server as released, which held a frame until
 * the next one.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public final class LostWakeup extends Stage {

    private final long busyNanos;
    private long busyUntil = Long.MIN_VALUE;
    private final List<LinkFrame> stuck = new ArrayList<>();
    private final List<Long> times = new ArrayList<>();
    private volatile int stuckCount = 0; // read on any thread

    /**
     * @param busyNanos how long it is busy with each message, not negative
     */
    public LostWakeup(long busyNanos) {
        if (busyNanos < 0) {
            throw new IllegalArgumentException("busy " + busyNanos);
        }
        this.busyNanos = busyNanos;
    }

    @Override
    protected void take(@Nonnull LinkFrame frame, long atNanos) {
        if (busyUntil != Long.MIN_VALUE && atNanos - busyUntil < 0) {
            stuck.add(frame); // arrived while busy: not noticed
            times.add(atNanos);
            stuckCount = stuck.size();
            return;
        }
        for (int i = 0; i < stuck.size(); i++) {
            pass(stuck.get(i), times.get(i), atNanos);
        }
        stuck.clear();
        times.clear();
        stuckCount = 0;
        pass(frame, atNanos, atNanos);
        busyUntil = atNanos + busyNanos;
    }

    /**
     * @return how many messages are stuck now, waiting for another to arrive
     */
    public int getStuck() {
        return stuckCount;
    }

    @Override
    protected void releaseAll(long nowNanos) {
        for (int i = 0; i < stuck.size(); i++) {
            pass(stuck.get(i), times.get(i), nowNanos);
        }
        stuck.clear();
        times.clear();
        stuckCount = 0;
    }

    @Override
    @Nonnull
    public String describe() {
        return Bundle.getMessage("StageLostWakeup", Latency.ms(busyNanos));
    }
}
