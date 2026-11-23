package jmri.jmrit.simulation.link;

import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;

/**
 * One step of an interface's handling of messages in one direction: it takes
 * each message at a time, and passes it on at the same time or later, or
 * drops it. Each stage keeps its own state, and runs only on its link's
 * clock thread.
 * <p>
 * A stage passes a message on with {@link #pass}; one held for later asks its
 * link to call it back at a time with {@link #at}.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public abstract class Stage {

    private SimulatedLink link;
    private Receiver next;
    // counted on the clock thread, read on any
    private final AtomicLong passed = new AtomicLong();
    private final AtomicLong dropped = new AtomicLong();
    private final AtomicLong heldNanos = new AtomicLong();
    private final AtomicLong longestHeldNanos = new AtomicLong();

    /** Whatever a stage passes its messages on to: the next stage or the medium. */
    interface Receiver {

        void take(@Nonnull LinkFrame frame, long atNanos);
    }

    final void connect(@Nonnull SimulatedLink link, @Nonnull Receiver next) {
        this.link = link;
        this.next = next;
    }

    /**
     * Take a message arriving at a time. Called on the link's clock thread,
     * in time order.
     *
     * @param frame   the message
     * @param atNanos when it arrives here
     */
    protected abstract void take(@Nonnull LinkFrame frame, long atNanos);

    /**
     * Pass a message on to the next stage.
     *
     * @param frame     the message
     * @param arrived   when it arrived here
     * @param onAtNanos when it goes on; not before it arrived
     */
    protected final void pass(@Nonnull LinkFrame frame, long arrived, long onAtNanos) {
        long held = Math.max(0, onAtNanos - arrived);
        passed.incrementAndGet();
        heldNanos.addAndGet(held);
        longestHeldNanos.accumulateAndGet(held, Math::max);
        long on = arrived + held;
        if (held == 0) {
            next.take(frame, on);
        } else {
            link.at(on, () -> next.take(frame, on));
        }
    }

    /**
     * Drop a message: it never arrives.
     *
     * @param frame the message
     */
    protected final void drop(@Nonnull LinkFrame frame) {
        dropped.incrementAndGet();
    }

    /**
     * Have this stage called back at a time, in order with every other event
     * of the link.
     *
     * @param atNanos when
     * @param task    what to do then
     */
    protected final void at(long atNanos, @Nonnull Runnable task) {
        link.at(atNanos, task);
    }

    /**
     * Pass on at once every message held, because the stage is being taken
     * out of its link. By default a stage holds nothing for later.
     *
     * @param nowNanos now
     */
    protected void releaseAll(long nowNanos) {
    }

    /**
     * @return a short description of the stage and its numbers, for the
     *         simulator window and logs
     */
    @Nonnull
    public abstract String describe();

    /**
     * @return how many messages it has passed on
     */
    public final long getPassed() {
        return passed.get();
    }

    /**
     * @return how many messages it has dropped
     */
    public final long getDropped() {
        return dropped.get();
    }

    /**
     * @return the time, in total, it has held the messages it passed on
     */
    public final long getHeldNanos() {
        return heldNanos.get();
    }

    /**
     * @return the longest it has held one message
     */
    public final long getLongestHeldNanos() {
        return longestHeldNanos.get();
    }

    @Override
    public String toString() {
        return describe();
    }
}
