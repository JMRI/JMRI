package jmri.jmrit.simulation.link;

import javax.annotation.Nonnull;

/**
 * What a system adds to a {@link SimulatedLink}: how long each of its
 * messages takes on its wire, and how the wire is shared.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public interface WireTiming {

    /**
     * @param message the system's message
     * @return how long it takes on the wire, in nanoseconds
     */
    long nanosOnWire(@Nonnull Object message);

    /**
     * @param message the system's message
     * @return its priority for {@link ArbitrationPolicy#PRIORITY}: lower wins
     */
    int priority(@Nonnull Object message);

    /**
     * @return how the wire is shared
     */
    @Nonnull
    ArbitrationPolicy policy();
}
