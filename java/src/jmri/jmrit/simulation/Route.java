package jmri.jmrit.simulation;

import javax.annotation.Nonnull;

/**
 * The way a train takes through one Block, for comparing with other trains in
 * or near the same Block.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
interface Route {

    /**
     * @param other another route
     * @return true if both follow the same track the same way, so positions
     *         along them can be compared directly
     */
    boolean sameAs(@Nonnull Route other);

    /**
     * @param other another route
     * @return true if both follow the same track opposite ways
     */
    boolean reverseOf(@Nonnull Route other);

    /**
     * Do two routes share track, other than as the same or reversed route, so
     * that trains on both would touch? With no geometry inside a Block, a
     * turnout or crossing fouls the whole of the Blocks it joins.
     *
     * @param other another route
     * @return true if trains on the two routes would foul each other
     */
    boolean fouls(@Nonnull Route other);

    /**
     * @return this route followed the other way
     */
    @Nonnull
    Route reversed();
}
