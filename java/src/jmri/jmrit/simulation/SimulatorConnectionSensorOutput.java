package jmri.jmrit.simulation;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.InvokeOnAnyThread;
import jmri.InvokeOnLayoutThread;
import jmri.JmriException;
import jmri.Manager;
import jmri.Sensor;
import jmri.SystemConnectionMemo;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.PortAdapter;
import jmri.util.ThreadingUtil;

/**
 * Reports simulated detection on the sensors of any simulator connection by
 * setting their known state, as a script or a sensor icon does, so that a
 * layout on a simulated LocoNet, NCE, DCC-EX or other connection needs no
 * extra sensors. The connection's own code then does what it does for a
 * sensor set from JMRI.
 * <p>
 * A connection with its own output, one that sends what its detectors would
 * send on its bus, is put ahead of this one, so this one only reports the
 * sensors no other output does. A sensor is reported only while its
 * connection is listed by the {@link ConnectionConfigManager} and says it is
 * a simulator, so nothing is set on a connection with real hardware behind
 * it, or on one that has gone.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
@javax.annotation.concurrent.ThreadSafe
public class SimulatorConnectionSensorOutput implements SimulatedDetectorOutput {

    /**
     * {@inheritDoc}
     * <p>
     * True for a sensor of a listed simulator connection.
     */
    @Override
    @InvokeOnLayoutThread
    public boolean reports(@Nonnull Sensor sensor) {
        ConnectionConfigManager manager = InstanceManager.getNullableDefault(ConnectionConfigManager.class);
        if (manager == null) {
            return false;
        }
        String prefix;
        try {
            prefix = Manager.getSystemPrefix(sensor.getSystemName());
        } catch (jmri.NamedBean.BadSystemNameException e) {
            return false;
        }
        for (ConnectionConfig c : manager.getConnections()) {
            if (!c.isSimulator() || c.getDisabled()) {
                continue;
            }
            PortAdapter a = c.getAdapter();
            SystemConnectionMemo memo = (a == null) ? null : a.getSystemConnectionMemo();
            if (memo != null && prefix.equals(memo.getSystemPrefix())) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sets the sensor's known state on the layout thread (Swing thread),
     * later, if its connection is still a listed simulator then.
     */
    @Override
    @InvokeOnAnyThread
    public void report(@Nonnull Sensor sensor, int state, long timeNanos) {
        ThreadingUtil.runOnLayoutEventually(() -> {
            if (!reports(sensor)) {
                return;
            }
            try {
                sensor.setKnownState(state);
            } catch (JmriException e) {
                log.warn("Simulated trains could not set sensor \"{}\": {}", sensor.getDisplayName(), e.getMessage());
            }
        });
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SimulatorConnectionSensorOutput.class);
}
