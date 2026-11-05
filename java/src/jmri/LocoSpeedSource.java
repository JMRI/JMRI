package jmri;

import javax.annotation.Nonnull;

/**
 * Something that sees the speed and direction commanded for locos, by loco
 * address, as they change, and when a loco address is released.
 * <p>
 * It reports what was commanded, not what the loco does: a decoder's momentum
 * and the loco's speed for each setting are the roster's business. It is
 * keyed by loco address, not by throttle or session, so that whoever follows
 * a loco can follow it when one throttle releases it and another takes it.
 * <p>
 * Every throttle manager derived from
 * {@link jmri.jmrix.AbstractThrottleManager} is a source for the throttles
 * JMRI holds: its own throttle windows, Dispatcher, Warrants, scripts, and the
 * WiThrottle and JSON servers' clients. A connection whose throttles' commands
 * can be seen on its bus may add a source for the commands of throttles JMRI
 * doesn't hold.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public interface LocoSpeedSource {

    /**
     * Told of each change of a loco address's commanded speed or direction,
     * and when the address is released.
     * <p>
     * A listener is told on the thread that made the change, which may be any
     * thread, so it should hand the work on rather than do it there. It must
     * not throw: a source catches and logs anything thrown, and goes on to
     * tell its other listeners.
     */
    interface Listener {

        /**
         * The speed or direction commanded for a loco address has changed.
         *
         * @param address   the loco address
         * @param speed     the speed setting, as
         *                  {@link Throttle#getSpeedSetting()} gives it: 0 to 1,
         *                  or less than 0 for an emergency stop
         * @param forward   true if the direction is forward
         * @param timeNanos when the source was told of the change, on the
         *                  {@link System#nanoTime()} scale
         */
        void speedCommanded(@Nonnull LocoAddress address, float speed, boolean forward, long timeNanos);

        /**
         * A loco address has been released: the source no longer has a
         * throttle or session for it, and will not report it until one is
         * made again. The loco may still be moving, at the last speed
         * commanded.
         *
         * @param address   the loco address
         * @param timeNanos when it was released, on the {@link System#nanoTime()}
         *                  scale
         */
        void released(@Nonnull LocoAddress address, long timeNanos);
    }

    /**
     * Add a listener. Adding one already added does nothing.
     *
     * @param listener the listener
     */
    void addLocoSpeedListener(@Nonnull Listener listener);

    /**
     * Remove a listener. Removing one not added does nothing.
     *
     * @param listener the listener
     */
    void removeLocoSpeedListener(@Nonnull Listener listener);
}
