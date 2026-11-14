package jmri.jmrix.can.cbus.simulator;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.InvokeOnAnyThread;
import jmri.jmrit.simulation.SimulatedDetectorOutput;
import jmri.jmrit.simulation.SimulatedReporterOutput;
import jmri.jmrit.simulation.SimulatedSpeedSource;
import jmri.jmrit.simulation.SimulationAdapter;

import org.openide.util.lookup.ServiceProvider;

/**
 * CBUS's part in simulated trains: for each CAN Simulation connection, its
 * simulated command stations' speed commands and its CBUS sensors' detection.
 * A connection to a real bus is never offered, even if it has a simulator.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
@ServiceProvider(service = SimulationAdapter.class)
public class CbusSimulationAdapter implements SimulationAdapter {

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    @InvokeOnAnyThread
    public List<SimulatedSpeedSource> getSpeedSources() {
        List<SimulatedSpeedSource> list = new ArrayList<>();
        for (CbusSimulator s : simulators()) {
            list.add(s.getSimulatedSpeedSource());
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
        for (CbusSimulator s : simulators()) {
            list.add(s.getSimulatedSensorOutput());
        }
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    @InvokeOnAnyThread
    public List<SimulatedReporterOutput> getReporterOutputs() {
        List<SimulatedReporterOutput> list = new ArrayList<>();
        for (CbusSimulator s : simulators()) {
            list.add(s.getSimulatedReporterOutput());
        }
        return list;
    }

    private static List<CbusSimulator> simulators() {
        List<CbusSimulator> list = new ArrayList<>();
        for (CbusSimulator s : InstanceManager.getList(CbusSimulator.class)) {
            if (s.isOnCanSimulation()) {
                list.add(s);
            }
        }
        return list;
    }
}
