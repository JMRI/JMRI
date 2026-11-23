package jmri.jmrit.simulation.link;

import java.util.function.LongConsumer;

import javax.annotation.Nonnull;

/**
 * A message on its way through a link, with what to do when it comes out.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public final class LinkFrame {

    private final Object message;
    private final SimulatedLink.Direction direction;
    private final long sentNanos;
    private final LongConsumer onArrival;

    LinkFrame(@Nonnull Object message, @Nonnull SimulatedLink.Direction direction, long sentNanos,
            @Nonnull LongConsumer onArrival) {
        this.message = message;
        this.direction = direction;
        this.sentNanos = sentNanos;
        this.onArrival = onArrival;
    }

    /**
     * @return the system's message, as the system gave it
     */
    @Nonnull
    public Object getMessage() {
        return message;
    }

    /**
     * @return which way it is going
     */
    @Nonnull
    public SimulatedLink.Direction getDirection() {
        return direction;
    }

    /**
     * @return when it was sent into the link
     */
    public long getSentNanos() {
        return sentNanos;
    }

    void arrive(long atNanos) {
        onArrival.accept(atNanos);
    }
}
