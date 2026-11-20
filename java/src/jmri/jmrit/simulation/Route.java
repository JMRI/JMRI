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
     * that trains on both would touch? Where {@link #foulingPieces} is not
     * known, a turnout or crossing fouls the whole of the Blocks it joins.
     *
     * @param other another route
     * @return true if trains on the two routes would foul each other
     */
    boolean fouls(@Nonnull Route other);

    /**
     * Where along two routes that foul each other they do so: each piece of
     * track they share, or turnout or crossing where they cross, as a part of
     * each route's length.
     *
     * @param other another route, one this route {@link #fouls}
     * @return the pieces, each as {from, to} along this route, {from, to}
     *         along the other (both parts of 0 to 1; from equals to at a
     *         turnout or crossing), and 1 if the other runs the opposite way
     *         along a shared piece, else 0; or none if not known, when
     *         trains foul as soon as both are in the Blocks
     */
    @Nonnull
    default double[][] foulingPieces(@Nonnull Route other) {
        return new double[0][];
    }

    /**
     * @return this route followed the other way
     */
    @Nonnull
    Route reversed();
}
