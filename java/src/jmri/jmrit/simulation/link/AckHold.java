package jmri.jmrit.simulation.link;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A TCP sender with Nagle's algorithm talking to a receiver that delays its
 * acknowledgements. The first message of a burst goes at once; any that
 * follow while it is unacknowledged wait, and go together when the
 * acknowledgement comes, the delayed-acknowledgement time later. They are
 * then unacknowledged in turn, so a steady stream goes in lumps.
 * <p>
 * This is what holds the frames of a group JMRI sends to a gateway that acks
 * late.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public final class AckHold extends Stage {

    private final long ackNanos;
    private final List<LinkFrame> waiting = new ArrayList<>();
    private final List<Long> times = new ArrayList<>();
    private boolean unacknowledged = false;

    /**
     * @param ackNanos how long the receiver takes to acknowledge, not negative
     */
    public AckHold(long ackNanos) {
        if (ackNanos < 0) {
            throw new IllegalArgumentException("ack " + ackNanos);
        }
        this.ackNanos = ackNanos;
    }

    @Override
    protected void take(@Nonnull LinkFrame frame, long atNanos) {
        if (!unacknowledged) {
            pass(frame, atNanos, atNanos);
            sent(atNanos);
        } else {
            waiting.add(frame);
            times.add(atNanos);
        }
    }

    private void sent(long atNanos) {
        unacknowledged = true;
        at(atNanos + ackNanos, () -> acknowledged(atNanos + ackNanos));
    }

    private void acknowledged(long atNanos) {
        unacknowledged = false;
        if (!waiting.isEmpty()) {
            for (int i = 0; i < waiting.size(); i++) {
                pass(waiting.get(i), times.get(i), atNanos);
            }
            waiting.clear();
            times.clear();
            sent(atNanos);
        }
    }

    @Override
    protected void releaseAll(long nowNanos) {
        for (int i = 0; i < waiting.size(); i++) {
            pass(waiting.get(i), times.get(i), nowNanos);
        }
        waiting.clear();
        times.clear();
    }

    @Override
    @Nonnull
    public String describe() {
        return Bundle.getMessage("StageAckHold", Latency.ms(ackNanos));
    }
}
