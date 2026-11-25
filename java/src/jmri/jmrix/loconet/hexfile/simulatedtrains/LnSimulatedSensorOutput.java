package jmri.jmrix.loconet.hexfile.simulatedtrains;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.InvokeOnAnyThread;
import jmri.InvokeOnLayoutThread;
import jmri.Manager;
import jmri.Sensor;
import jmri.jmrit.simulation.SimulatedDetectorOutput;
import jmri.jmrix.loconet.LnSensor;
import jmri.jmrix.loconet.LnSensorAddress;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.hexfile.LnHexFilePort;

/**
 * Reports simulated detection on a LocoNet Simulator connection's sensors,
 * as an occupancy detector such as a BDL168 does: it puts a sensor input
 * report on the simulated LocoNet, which the simulator echoes back as every
 * LocoNet message is.
 * <p>
 * The input report is built here as the LocoNet Personal Edition gives it,
 * not with JMRI's LocoNet sensor code: 0xB2, IN1 = A6..A0, IN2 = 0 X I L
 * A10..A7, then the checksum, where the input's number less one is A10..A0
 * then I, X is 1 and L is the input's level, 1 for occupied.
 * <p>
 * An inverted sensor's input is reported the other way round, so that the
 * sensor ends up in the state the simulated train gives it. The report's
 * received time is when the connection reads its echo, a moment after the
 * change.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
@javax.annotation.concurrent.ThreadSafe
public class LnSimulatedSensorOutput implements SimulatedDetectorOutput {

    private final LnHexFilePort port;

    /**
     * @param port the LocoNet Simulator connection's simulated LocoNet
     */
    public LnSimulatedSensorOutput(@Nonnull LnHexFilePort port) {
        this.port = port;
    }

    /**
     * {@inheritDoc}
     * <p>
     * True for a LocoNet sensor of this connection, by its system name's
     * prefix.
     */
    @Override
    @InvokeOnLayoutThread
    public boolean reports(@Nonnull Sensor sensor) {
        return input(sensor) >= 0;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if the sensor is not one this output
     *                                  {@link #reports}
     */
    @Override
    @InvokeOnAnyThread
    public void report(@Nonnull Sensor sensor, int state, long timeNanos) {
        int input = input(sensor);
        if (input < 0) {
            throw new IllegalArgumentException("Not a LocoNet Simulator sensor: " + sensor.getSystemName());
        }
        LnTrafficController tc = port.getSystemConnectionMemo().getLnTrafficController();
        if (tc != null) {
            tc.sendLocoNetMessage(inputReport(input, (state == Sensor.ACTIVE) != sensor.getInverted()));
        }
    }

    /**
     * @param input  the input's number less one, 0 to 4095
     * @param active true for occupied
     * @return the sensor input report
     */
    @Nonnull
    static LocoNetMessage inputReport(int input, boolean active) {
        int a = input >> 1;
        LocoNetMessage m = new LocoNetMessage(4);
        m.setElement(0, 0xB2);
        m.setElement(1, a & 0x7F);
        m.setElement(2, 0x40 | ((input & 1) << 5) | (active ? 0x10 : 0) | ((a >> 7) & 0x0F));
        m.setElement(3, 0xFF ^ 0xB2 ^ m.getElement(1) ^ m.getElement(2));
        return m;
    }

    /**
     * @param sensor a sensor
     * @return its input's number less one, 0 to 4095, from its system name,
     *         or -1 if it is not a LocoNet sensor of this connection
     */
    private int input(@CheckForNull Sensor sensor) {
        if (!(sensor instanceof LnSensor)) {
            return -1;
        }
        String prefix = port.getSystemConnectionMemo().getSystemPrefix();
        String name = sensor.getSystemName();
        if (!prefix.equals(Manager.getSystemPrefix(name))) {
            return -1;
        }
        try {
            LnSensorAddress a = new LnSensorAddress(name, prefix);
            if (!a.isValid()) {
                return -1;
            }
            int n = a.getHighBits() * 256 + a.getLowBits() * 2 + (a.getASBit() != 0 ? 1 : 0);
            return n >= 0 && n < 4096 ? n : -1;
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return -1;
        }
    }
}
