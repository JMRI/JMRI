package jmri.jmrix.maple;

import jmri.implementation.AbstractSensor;

/**
 * Extend jmri.AbstractSensor for Maple systems.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2008
 */
public class SerialSensor extends AbstractSensor {

    /**
     * Create a Sensor object, with only system name.
     * <p>
     * 'systemName' has already been validated in SerialSensorManager
     *
     * @param systemName the system name for this Sensor
     */
    public SerialSensor(String systemName) {
        super(systemName);
        _knownState = UNKNOWN;
    }

    /**
     * Create a Sensor object, with both system and user names.
     * <p>
     * 'systemName' has already been validated in SerialSensorManager
     *
     * @param systemName the system name for this Sensor
     * @param userName   the user name for this Sensor
     */
    public SerialSensor(String systemName, String userName) {
        super(systemName, userName);
        _knownState = UNKNOWN;
    }

    /**
     * Request an update on status.
     * <p>
     * Since status is continually being updated, this isn't active now.
     * Eventually, we may want to have this move the related AIU to the top of
     * the polling queue.
     */
    @Override
    public void requestUpdateFromLayout() {
    }

}
