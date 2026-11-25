package jmri.jmrix.dccpp.simulator.simulatedtrains;

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
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.jmrix.dccpp.DCCppTrafficController;
import jmri.jmrix.dccpp.simulator.DCCppSimulatorAdapter;

import org.openide.util.lookup.ServiceProvider;

/**
 * DCC-EX's part in simulated trains: for each DCC-EX Simulator connection,
 * its simulated command station's throttle commands and its sensors'
 * detection. A connection to a real command station is never offered, nor is
 * a simulator whose Simulated trains option leaves it out.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
@ServiceProvider(service = SimulationAdapter.class)
public class DCCppSimulationAdapter implements SimulationAdapter {

    /** The speed source and sensor output of each simulator, made once, when first offered. */
    private static final Map<DCCppSimulatorAdapter, Parts> PARTS = new WeakHashMap<>();

    private static final class Parts {

        final DCCppSimulatedSpeedSource speedSource = new DCCppSimulatedSpeedSource();
        final DCCppSimulatedSensorOutput sensorOutput;

        Parts(DCCppSimulatorAdapter simulator) {
            sensorOutput = new DCCppSimulatedSensorOutput(simulator);
            simulator.addThrottleCommandListener(speedSource);
        }
    }

    /**
     * @param simulator a DCC-EX Simulator connection's simulated command
     *                  station
     * @return its speed source and sensor output
     */
    private static Parts parts(DCCppSimulatorAdapter simulator) {
        synchronized (PARTS) {
            return PARTS.computeIfAbsent(simulator, Parts::new);
        }
    }

    /**
     * @param simulator a DCC-EX Simulator connection's simulated command
     *                  station
     * @return the speed source following it
     */
    @Nonnull
    public static DCCppSimulatedSpeedSource speedSource(@Nonnull DCCppSimulatorAdapter simulator) {
        return parts(simulator).speedSource;
    }

    /**
     * @param simulator a DCC-EX Simulator connection's simulated command
     *                  station
     * @return the output reporting on its sensors
     */
    @Nonnull
    public static DCCppSimulatedSensorOutput sensorOutput(@Nonnull DCCppSimulatorAdapter simulator) {
        return parts(simulator).sensorOutput;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    @InvokeOnAnyThread
    public List<SimulatedSpeedSource> getSpeedSources() {
        List<SimulatedSpeedSource> list = new ArrayList<>();
        for (DCCppSimulatorAdapter a : simulators()) {
            list.add(speedSource(a));
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
        for (DCCppSimulatorAdapter a : simulators()) {
            list.add(sensorOutput(a));
        }
        return list;
    }

    private static List<DCCppSimulatorAdapter> simulators() {
        List<DCCppSimulatorAdapter> list = new ArrayList<>();
        for (SystemConnectionMemo m : InstanceManager.getList(SystemConnectionMemo.class)) {
            if (!(m instanceof DCCppSystemConnectionMemo)) {
                continue;
            }
            DCCppTrafficController tc = ((DCCppSystemConnectionMemo) m).getDCCppTrafficController();
            if (tc != null && tc.controller instanceof DCCppSimulatorAdapter
                    && ((DCCppSimulatorAdapter) tc.controller).isForSimulatedTrains()) {
                list.add((DCCppSimulatorAdapter) tc.controller);
            }
        }
        return list;
    }
}
