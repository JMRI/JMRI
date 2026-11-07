package jmri.jmrix.can.cbus.simulator;

import javax.annotation.Nonnull;

import jmri.InvokeOnAnyThread;
import jmri.InvokeOnLayoutThread;
import jmri.Manager;
import jmri.Sensor;
import jmri.jmrit.simulation.SimulatedDetectorOutput;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusSend;
import jmri.jmrix.can.cbus.CbusSensor;
import jmri.util.ThreadingUtil;

/**
 * Reports simulated detection on a CAN Simulation connection's CBUS sensors,
 * as a detector module on the bus would: the event that sets the sensor to
 * its new state arrives as a frame from the bus, received at the time the
 * change happened.
 * <p>
 * The frame is the one the sensor itself sends for that state
 * ({@link CbusSensor#getAddrActive()}, {@link CbusSensor#getAddrInactive()}),
 * so long and short events, two-event sensors and inverted sensors are all
 * reported as configured. The frames are checked against frames worked out
 * from the CBUS specification in the tests, not here.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
@javax.annotation.concurrent.ThreadSafe
public class CbusSimulatedSensorOutput implements SimulatedDetectorOutput {

    private final CanSystemConnectionMemo memo;
    private final CbusSend send;
    private volatile boolean disposed = false;

    /**
     * @param memo the CAN Simulation connection
     */
    public CbusSimulatedSensorOutput(@Nonnull CanSystemConnectionMemo memo) {
        this.memo = memo;
        this.send = new CbusSend(memo);
    }

    /**
     * {@inheritDoc}
     * <p>
     * True for a CBUS sensor of this connection, by its system name's prefix.
     */
    @Override
    @InvokeOnLayoutThread
    public boolean reports(@Nonnull Sensor sensor) {
        if (disposed || !(sensor instanceof CbusSensor)) {
            return false;
        }
        return memo.getSystemPrefix().equals(Manager.getSystemPrefix(sensor.getSystemName()));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Builds the frame on the layout thread (Swing thread), later, as the
     * sensor's events and inversion are read there, then sends it as received
     * at the time of the change.
     */
    @Override
    @InvokeOnAnyThread
    public void report(@Nonnull Sensor sensor, int state, long timeNanos) {
        CbusSensor s = (CbusSensor) sensor;
        ThreadingUtil.runOnLayoutEventually(() -> {
            if (disposed) {
                return;
            }
            CanMessage m = (state == Sensor.ACTIVE) ? s.getAddrActive() : s.getAddrInactive();
            send.sendAt(new CanReply(m), timeNanos);
        });
    }

    /**
     * Report nothing more.
     */
    @InvokeOnAnyThread
    public void dispose() {
        disposed = true;
    }
}
