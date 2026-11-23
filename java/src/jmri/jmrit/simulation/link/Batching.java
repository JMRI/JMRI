package jmri.jmrit.simulation.link;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Holds messages and passes them on together: when a number are waiting, or
 * a time after the first of them arrived, whichever comes first. This is a
 * sender that fills a buffer and sends it only when full or when a timer
 * ends, as a gateway that doesn't flush its network sends does, or a
 * USB-serial adapter's receive buffer.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public final class Batching extends Stage {

    private final int count;
    private final long waitNanos;
    private final List<Long> times = new ArrayList<>();
    private final List<LinkFrame> waiting = new ArrayList<>();
    private long batch = 0;

    /**
     * @param count     how many waiting messages send them at once, at least 1
     * @param waitNanos how long after the first one arrived they are sent if
     *                  fewer are waiting, not negative
     */
    public Batching(int count, long waitNanos) {
        if (count < 1 || waitNanos < 0) {
            throw new IllegalArgumentException("count " + count + ", wait " + waitNanos);
        }
        this.count = count;
        this.waitNanos = waitNanos;
    }

    @Override
    protected void take(@Nonnull LinkFrame frame, long atNanos) {
        waiting.add(frame);
        times.add(atNanos);
        if (waiting.size() >= count) {
            sendAll(atNanos);
        } else if (waiting.size() == 1) {
            long thisBatch = batch;
            at(atNanos + waitNanos, () -> {
                if (batch == thisBatch && !waiting.isEmpty()) {
                    sendAll(atNanos + waitNanos);
                }
            });
        }
    }

    private void sendAll(long atNanos) {
        for (int i = 0; i < waiting.size(); i++) {
            pass(waiting.get(i), times.get(i), atNanos);
        }
        waiting.clear();
        times.clear();
        batch++;
    }

    @Override
    protected void releaseAll(long nowNanos) {
        if (!waiting.isEmpty()) {
            sendAll(nowNanos);
        }
    }

    @Override
    @Nonnull
    public String describe() {
        return Bundle.getMessage("StageBatching", count, Latency.ms(waitNanos));
    }
}
