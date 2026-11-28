package jmri.jmrix.sprog.simulator.simulatedtrains;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.InvokeOnAnyThread;
import jmri.SystemConnectionMemo;
import jmri.jmrit.simulation.SimulatedDetectorOutput;
import jmri.jmrit.simulation.SimulatedSpeedSource;
import jmri.jmrit.simulation.SimulationAdapter;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.sprog.simulator.SimulatorAdapter;

import org.openide.util.lookup.ServiceProvider;

/**
 * SPROG's part in simulated trains: for each SPROG Simulator connection, the
 * speed commands its simulated SPROG receives. A SPROG has no detection, so
 * the trains report on sensors of another connection, such as Internal ones.
 * A connection to a real SPROG is never offered, nor is a simulator whose
 * Simulated trains setting leaves it out.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
@ServiceProvider(service = SimulationAdapter.class)
public class SprogSimulationAdapter implements SimulationAdapter {

    /** The speed source of each simulator, made once, when first offered. */
    private static final Map<SimulatorAdapter, SprogSimulatedSpeedSource> SOURCES = new WeakHashMap<>();

    /**
     * @param simulator a SPROG Simulator connection's simulated SPROG
     * @return the speed source following it
     */
    @Nonnull
    public static SprogSimulatedSpeedSource speedSource(@Nonnull SimulatorAdapter simulator) {
        synchronized (SOURCES) {
            return SOURCES.computeIfAbsent(simulator, s -> {
                SprogSimulatedSpeedSource source = new SprogSimulatedSpeedSource();
                s.addMessageListener(source);
                return source;
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    @InvokeOnAnyThread
    public List<SimulatedSpeedSource> getSpeedSources() {
        List<SimulatedSpeedSource> list = new ArrayList<>();
        ConnectionConfigManager ccm = InstanceManager.getNullableDefault(ConnectionConfigManager.class);
        if (ccm == null) {
            return list;
        }
        for (ConnectionConfig c : ccm.getConnections()) {
            if (c.getAdapter() instanceof SimulatorAdapter) {
                SimulatorAdapter s = (SimulatorAdapter) c.getAdapter();
                SystemConnectionMemo memo = s.getSystemConnectionMemo();
                if (s.isForSimulatedTrains() && memo != null
                        && InstanceManager.getList(SystemConnectionMemo.class).contains(memo)) {
                    list.add(speedSource(s));
                }
            }
        }
        return list;
    }

    /**
     * {@inheritDoc}
     * <p>
     * None: a SPROG has no detection.
     */
    @Override
    @Nonnull
    @InvokeOnAnyThread
    public List<SimulatedDetectorOutput> getDetectorOutputs() {
        return List.of();
    }
}
