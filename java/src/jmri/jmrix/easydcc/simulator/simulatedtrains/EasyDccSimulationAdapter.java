package jmri.jmrix.easydcc.simulator.simulatedtrains;

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
import jmri.jmrix.easydcc.simulator.SimulatorAdapter;

import org.openide.util.lookup.ServiceProvider;

/**
 * EasyDCC's part in simulated trains: for each EasyDCC Simulator connection, the
 * speed commands its simulated EasyDCC receives. It reports no detection, so
 * the trains report on sensors of another connection, such as Internal ones.
 * A connection to a real EasyDCC is never offered, nor is a simulator whose
 * Simulated trains setting leaves it out.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
@ServiceProvider(service = SimulationAdapter.class)
public class EasyDccSimulationAdapter implements SimulationAdapter {

    /** The speed source of each simulator, made once, when first offered. */
    private static final Map<SimulatorAdapter, EasyDccSimulatedSpeedSource> SOURCES = new WeakHashMap<>();

    /**
     * @param simulator a EasyDCC Simulator connection's simulated EasyDCC
     * @return the speed source following it
     */
    @Nonnull
    public static EasyDccSimulatedSpeedSource speedSource(@Nonnull SimulatorAdapter simulator) {
        synchronized (SOURCES) {
            return SOURCES.computeIfAbsent(simulator, s -> {
                EasyDccSimulatedSpeedSource source = new EasyDccSimulatedSpeedSource();
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
     * None: its simulator reports no detection.
     */
    @Override
    @Nonnull
    @InvokeOnAnyThread
    public List<SimulatedDetectorOutput> getDetectorOutputs() {
        return List.of();
    }
}
