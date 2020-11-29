/*
 *  @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
package jmri.jmrit.ctc;

import jmri.Sensor;

public class Fleeting {
    private final NBHSensor _mFleetingToggleInternalSensor;
    private final boolean _mDefaultFleetingEnabled;

    /**
     * Class to manage the Fleeting sensor (if defined).  Provides reasonable
     * return values if none defined.
     *
     * @param fleetingToggleInternalSensor  The defined internal fleeting sensor name (ex: "IS:FLEETING")
     * @param defaultFleetingEnabled        The users choice as to whether fleeting is initially enabled or not.
     */
    public Fleeting (NBHSensor fleetingToggleInternalSensor, boolean defaultFleetingEnabled) {
        _mFleetingToggleInternalSensor = fleetingToggleInternalSensor;
        _mDefaultFleetingEnabled = defaultFleetingEnabled;
        _mFleetingToggleInternalSensor.setKnownState(_mDefaultFleetingEnabled ? Sensor.ACTIVE : Sensor.INACTIVE);
    }


    /**
     * Stub routine for completeness, we can be consistent in higher level code with this defined (even if it does nothing).
     */
    public void removeAllListeners() {}   // None done.

    /**
     * Routine you can call to find out if fleeting is enabled.  Provides reasonable defaults if things don't exist.
     * @return True if fleeting is enabled, else false.
     */
    public boolean isFleetingEnabled() {
        if (_mFleetingToggleInternalSensor.valid()) return _mFleetingToggleInternalSensor.getKnownState() == Sensor.ACTIVE;
        return _mDefaultFleetingEnabled;
    }
}
