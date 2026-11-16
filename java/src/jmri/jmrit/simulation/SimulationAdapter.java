package jmri.jmrit.simulation;

import java.util.List;

import javax.annotation.Nonnull;

import jmri.InvokeOnAnyThread;

/**
 * A system's part in simulated trains: the speed sources and detector outputs
 * for that system's simulator connections. Each system provides one as a
 * service ({@code @ServiceProvider(service = SimulationAdapter.class)}); the
 * Internal connection's are built into {@link SimulatedLayout}.
 * <p>
 * The simulator asks each adapter when a train is placed and when the
 * profile's connections change, never before a train is placed, so an
 * adapter costs nothing until then. An adapter offers sources and outputs
 * only for connections that are simulators: with a connection to real
 * hardware, simulated trains must never report detection or take commands.
 * While a connection lasts, its adapter offers the same source and output
 * objects each time it is asked; those no longer offered are dropped.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public interface SimulationAdapter {

    /**
     * @return the speed sources for this system's simulator connections, the
     *         same objects each time while those connections last
     */
    @Nonnull
    @InvokeOnAnyThread
    List<SimulatedSpeedSource> getSpeedSources();

    /**
     * @return the detector outputs for this system's simulator connections,
     *         the same objects each time while those connections last
     */
    @Nonnull
    @InvokeOnAnyThread
    List<SimulatedDetectorOutput> getDetectorOutputs();

    /**
     * @return the reporter outputs for this system's simulator connections,
     *         the same objects each time while those connections last; none
     *         unless the system has one
     */
    @Nonnull
    @InvokeOnAnyThread
    default List<SimulatedReporterOutput> getReporterOutputs() {
        return List.of();
    }
}
