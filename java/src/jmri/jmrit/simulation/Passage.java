package jmri.jmrit.simulation;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * The way through one Block from an entry, as the track model finds it with
 * the turnouts as they are set at the time.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
final class Passage {

    /** Why a train can't go on: nothing stops it. */
    enum Stop {
        /** The train can run through the Block and on into the next. */
        NONE,
        /** The track ends at the far end of the Block; the train stops there. */
        END_OF_TRACK,
        /**
         * The way through the Block is not set (points set against, turnouts
         * unknown or inconsistent, or track the simulator can't follow): a
         * train must not enter it.
         */
        BLOCKED
    }

    final TrackEntry entry;
    final double length;
    final Route route;
    @CheckForNull
    final TrackEntry next;
    @CheckForNull
    final TrackEntry back;
    final Stop stop;
    @CheckForNull
    final Message reason;

    /**
     * @param entry  where the Block is entered
     * @param length length of the way through, mm
     * @param route  the way through, for comparing with other trains'
     * @param next   entry into the next Block, or null if the train can't go on
     * @param back   entry into this Block from its far end facing back, if known
     * @param stop   why the train can't go on, if it can't
     * @param reason a description of why, for the warning
     */
    Passage(@Nonnull TrackEntry entry, double length, @Nonnull Route route, @CheckForNull TrackEntry next,
            @CheckForNull TrackEntry back, @Nonnull Stop stop, @CheckForNull Message reason) {
        this.entry = entry;
        this.length = length;
        this.route = route;
        this.next = next;
        this.back = back;
        this.stop = stop;
        this.reason = reason;
    }
}
