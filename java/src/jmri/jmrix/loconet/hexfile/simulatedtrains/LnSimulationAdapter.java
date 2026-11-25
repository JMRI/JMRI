package jmri.jmrix.loconet.hexfile.simulatedtrains;

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
import jmri.jmrix.loconet.hexfile.LnHexFilePort;
import jmri.jmrix.loconet.hexfile.LnSimulatedCommandStation;

import org.openide.util.lookup.ServiceProvider;

/**
 * LocoNet's part in simulated trains: for each LocoNet Simulator connection,
 * the slots of its simulated command station, and sensor input reports for
 * its sensors. A connection to a real LocoNet is never offered, nor is a
 * simulator whose Simulated trains setting leaves it out.
 * <p>
 * A simulator without a simulated command station hands out debug
 * throttles, which simulated trains already obey, as the Internal
 * connection's.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
@ServiceProvider(service = SimulationAdapter.class)
public class LnSimulationAdapter implements SimulationAdapter {

    /** The sensor output of each simulator, made once, when first offered. */
    private static final Map<LnHexFilePort, LnSimulatedSensorOutput> OUTPUTS = new WeakHashMap<>();

    /**
     * @param simulator a LocoNet Simulator connection's simulated LocoNet
     * @return the output reporting on its sensors
     */
    @Nonnull
    public static LnSimulatedSensorOutput sensorOutput(@Nonnull LnHexFilePort simulator) {
        synchronized (OUTPUTS) {
            return OUTPUTS.computeIfAbsent(simulator, LnSimulatedSensorOutput::new);
        }
    }

    /** The speed source of each simulated command station, made once, when first offered. */
    private static final Map<LnSimulatedCommandStation, LnSimulatedSpeedSource> SOURCES = new WeakHashMap<>();

    /**
     * @param commandStation a LocoNet Simulator connection's simulated
     *                       command station
     * @return the speed source following its slots
     */
    @Nonnull
    public static LnSimulatedSpeedSource speedSource(@Nonnull LnSimulatedCommandStation commandStation) {
        synchronized (SOURCES) {
            return SOURCES.computeIfAbsent(commandStation, cs -> {
                LnSimulatedSpeedSource source = new LnSimulatedSpeedSource();
                cs.addSlotListener(source);
                return source;
            });
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * The slots of each simulator's simulated command station. Without one,
     * the simulator's throttles are debug throttles, which simulated trains
     * already obey.
     */
    @Override
    @Nonnull
    @InvokeOnAnyThread
    public List<SimulatedSpeedSource> getSpeedSources() {
        List<SimulatedSpeedSource> list = new ArrayList<>();
        for (LnHexFilePort s : simulators()) {
            LnSimulatedCommandStation cs = s.getSimulatedCommandStation();
            if (cs != null) {
                list.add(speedSource(cs));
            }
        }
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    @InvokeOnAnyThread
    public List<SimulatedDetectorOutput> getDetectorOutputs() {
        List<SimulatedDetectorOutput> list = new ArrayList<>();
        for (LnHexFilePort s : simulators()) {
            list.add(sensorOutput(s));
        }
        return list;
    }

    /**
     * @return the LocoNet Simulator connections in use whose Simulated trains
     *         setting doesn't leave them out
     */
    private static List<LnHexFilePort> simulators() {
        List<LnHexFilePort> list = new ArrayList<>();
        ConnectionConfigManager ccm = InstanceManager.getNullableDefault(ConnectionConfigManager.class);
        if (ccm == null) {
            return list;
        }
        for (ConnectionConfig c : ccm.getConnections()) {
            if (c.getAdapter() instanceof LnHexFilePort) {
                LnHexFilePort s = (LnHexFilePort) c.getAdapter();
                SystemConnectionMemo memo = s.getSystemConnectionMemo();
                if (s.isForSimulatedTrains() && memo != null
                        && InstanceManager.getList(SystemConnectionMemo.class).contains(memo)) {
                    list.add(s);
                }
            }
        }
        return list;
    }
}
