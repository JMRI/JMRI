package jmri.jmrit.trainrecord;

import javax.annotation.Nonnull;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.internal.InternalSensorManager;
import jmri.managers.ProxySensorManager;

/**
 * Says which Blocks have a detector that train tracking can measure from.
 * <p>
 * A Block's occupancy sensor is a detector when hardware on the layout sets
 * it. A sensor of the Internal connection has no hardware behind it: a
 * script, Logix, LogixNG or another system's logic sets it, so the moments
 * it changes are not the moments a train passed a place on the track, and
 * nothing is measured from it. Trains are still followed through its Block.
 * <p>
 * The one exception is a session with no layout hardware at all, where
 * every connection is a simulator. There the Internal connection's sensors
 * are the detectors of simulated trains, and are measured from; what is
 * measured is of the simulated trains, not of a layout.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public final class TrainTrackingDetectors {

    private TrainTrackingDetectors() {
        // static methods only
    }

    /**
     * Has this Block a detector that can be measured from?
     *
     * @param block a Block
     * @return false if it has no occupancy sensor, or its sensor is one of
     *         the Internal connection's on a layout with hardware
     */
    public static boolean isDetector(@Nonnull Block block) {
        Sensor sensor = block.getSensor();
        if (sensor == null) {
            return false;
        }
        return !isInternal(sensor) || isSimulated();
    }

    /**
     * Is this a session with no layout hardware, so that anything measured
     * is of simulated trains?
     *
     * @return true if every connection says it is a simulator; false if any
     *         doesn't, or the connections aren't known
     */
    public static boolean isSimulated() {
        ConnectionConfigManager manager = InstanceManager.getNullableDefault(ConnectionConfigManager.class);
        if (manager == null) {
            return false;
        }
        for (ConnectionConfig c : manager.getConnections()) {
            if (!c.isSimulator()) {
                return false;
            }
        }
        return true;
    }

    private static boolean isInternal(Sensor sensor) {
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
}
