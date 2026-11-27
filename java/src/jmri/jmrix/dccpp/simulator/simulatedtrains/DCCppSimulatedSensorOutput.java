package jmri.jmrix.dccpp.simulator.simulatedtrains;

import javax.annotation.Nonnull;

import jmri.InvokeOnAnyThread;
import jmri.InvokeOnLayoutThread;
import jmri.Manager;
import jmri.Sensor;
import jmri.jmrit.simulation.SimulatedDetectorOutput;
import jmri.jmrix.dccpp.DCCppSensor;
import jmri.jmrix.dccpp.simulator.DCCppSimulatorAdapter;
import jmri.util.ThreadingUtil;

/**
 * Reports simulated detection on a DCC-EX Simulator connection's sensors, as
 * a DCC-EX command station reports a sensor input changing: {@code <Q n>}
 * when input n goes active, {@code <q n>} when it goes inactive, sent from the
 * simulated command station like any other reply.
 * <p>
 * An inverted sensor's input is reported the other way round, so that the
 * sensor ends up in the state the simulated train gives it. The reply's
 * received time is when the connection reads it, a moment after the change.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
@javax.annotation.concurrent.ThreadSafe
public class DCCppSimulatedSensorOutput implements SimulatedDetectorOutput {

    private final DCCppSimulatorAdapter adapter;

    /**
     * @param adapter the DCC-EX Simulator connection's simulated command
     *                station
     */
    public DCCppSimulatedSensorOutput(@Nonnull DCCppSimulatorAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * {@inheritDoc}
     * <p>
     * True for a sensor of this connection with a numbered input, by its
     * system name's prefix. From the first call, when simulated trains start
     * using this connection, the simulated command station sends no more
     * random sensor replies.
     */
    @Override
    @InvokeOnLayoutThread
    public boolean reports(@Nonnull Sensor sensor) {
        if (!(sensor instanceof DCCppSensor)) {
            return false;
        }
        adapter.stopRandomSensorReplies();
        String prefix = adapter.getSystemConnectionMemo().getSystemPrefix();
        return prefix.equals(Manager.getSystemPrefix(sensor.getSystemName())) && input(sensor) >= 0;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The reply is sent on the layout thread (Swing thread), later, so that
     * the command station's replies are always written by long-lived threads.
     *
     * @throws IllegalArgumentException if the sensor is not one this output
     *                                  {@link #reports}
     */
    @Override
    @InvokeOnAnyThread
    public void report(@Nonnull Sensor sensor, int state, long timeNanos) {
        int input = (sensor instanceof DCCppSensor) ? input(sensor) : -1;
        if (input < 0) {
            throw new IllegalArgumentException("Not a DCC-EX sensor input: " + sensor.getSystemName());
        }
        boolean active = (state == Sensor.ACTIVE) != sensor.getInverted();
        ThreadingUtil.runOnLayoutEventually(() -> adapter.reportSensorInput(input, active));
    }

    /**
     * @param sensor a DCC-EX sensor
     * @return its input number, from its system name, or -1 if it has none
     */
    private static int input(Sensor sensor) {
        String name = sensor.getSystemName();
        String number = name.substring(Manager.getSystemPrefixLength(name) + 1);
        try {
            int n = Integer.parseInt(number);
            return n >= 0 ? n : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
