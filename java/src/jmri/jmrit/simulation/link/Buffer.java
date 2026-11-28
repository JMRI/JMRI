package jmri.jmrit.simulation.link;

import java.util.ArrayDeque;
import java.util.Deque;

import javax.annotation.Nonnull;

/**
 * A buffer of limited size in front of the medium, as a gateway's transmit
 * FIFO: messages waiting for the wire count against it until they start on
 * the wire. When it is full, one arriving either waits for room, holding up
 * every one behind it (a sender that busy-waits), or is dropped.
 * <p>
 * It must be the last stage in its direction towards the medium; anywhere
 * else it has no medium to wait for and never fills.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public final class Buffer extends Stage {

    /** What happens to a message that arrives when the buffer is full. */
    public enum WhenFull {
        /** It waits for room, and every message behind it waits too. */
        BLOCK,
        /** It is dropped. */
        DROP
    }

    private final int capacity;
    private final WhenFull whenFull;
    private int inUse = 0;
    private final Deque<LinkFrame> blocked = new ArrayDeque<>();
    private final Deque<Long> times = new ArrayDeque<>();

    /**
     * @param capacity how many messages it holds, at least 1
     * @param whenFull what happens to a message that arrives when it is full
     */
    public Buffer(int capacity, @Nonnull WhenFull whenFull) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity " + capacity);
        }
        this.capacity = capacity;
        this.whenFull = whenFull;
    }

    @Override
    protected void take(@Nonnull LinkFrame frame, long atNanos) {
        if (blocked.isEmpty() && inUse < capacity) {
            inUse++;
            pass(frame, atNanos, atNanos);
        } else if (whenFull == WhenFull.DROP) {
            drop(frame);
        } else {
            blocked.addLast(frame);
            times.addLast(atNanos);
        }
    }

    /**
     * A message this buffer passed on has started on the wire: its place is
     * free. Called by the medium.
     *
     * @param atNanos when
     */
    void started(long atNanos) {
        inUse--;
        if (!blocked.isEmpty()) {
            inUse++;
            pass(blocked.removeFirst(), times.removeFirst(), atNanos);
        }
    }

    @Override
    protected void releaseAll(long nowNanos) {
        while (!blocked.isEmpty()) {
            inUse++;
            pass(blocked.removeFirst(), times.removeFirst(), nowNanos);
        }
    }

    @Override
    @Nonnull
    public String describe() {
        return Bundle.getMessage(whenFull == WhenFull.BLOCK ? "StageBufferBlock" : "StageBufferDrop", capacity);
    }
}
