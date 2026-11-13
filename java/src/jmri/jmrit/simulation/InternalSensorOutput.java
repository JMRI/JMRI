package jmri.jmrit.simulation;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.InvokeOnAnyThread;
import jmri.InvokeOnLayoutThread;
import jmri.Manager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.implementation.AbstractSensor;
import jmri.jmrix.internal.InternalSensorManager;
import jmri.managers.ProxySensorManager;
import jmri.util.ThreadingUtil;

/**
 * Reports simulated detection on internal sensors: sets the sensor on the
 * layout thread, giving its raw state listeners the time the change happened.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
@javax.annotation.concurrent.ThreadSafe
public class InternalSensorOutput implements SimulatedDetectorOutput {

    /**
     * {@inheritDoc}
     * <p>
     * True for a sensor of the Internal connection.
     */
    @Override
    @InvokeOnLayoutThread
    public boolean reports(@Nonnull Sensor sensor) {
        if (!(sensor instanceof AbstractSensor)) {
            return false;
        }
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        if (sm instanceof ProxySensorManager) {
            for (Manager<Sensor> m : ((ProxySensorManager) sm).getManagerList()) {
                if (m instanceof InternalSensorManager && m.getBySystemName(sensor.getSystemName()) == sensor) {
                    return true;
                }
            }
            return false;
        }
        return sm instanceof InternalSensorManager && sm.getBySystemName(sensor.getSystemName()) == sensor;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sets the sensor on the layout thread (Swing thread), later, giving its
     * raw state listeners the time of the change.
     */
    @Override
    @InvokeOnAnyThread
    public void report(@Nonnull Sensor sensor, int state, long timeNanos) {
        AbstractSensor s = (AbstractSensor) sensor;
        ThreadingUtil.runOnLayoutEventually(() -> s.setOwnState(state, timeNanos));
    }
}
