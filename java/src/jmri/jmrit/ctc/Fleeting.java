/*
 *  @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
package jmri.jmrit.ctc;

import jmri.jmrit.ctc.CTCException;
import jmri.JmriException;
import jmri.Sensor;

public class Fleeting {
    private NBHSensor _mFleetingToggleInternalSensor;
    private boolean _mDefaultFleetingEnabled;
    public Fleeting (String fleetingToggleInternalSensor, boolean defaultFleetingEnabled) {
        _mFleetingToggleInternalSensor = new NBHSensor("Fleeting", "", "fleetingToggleInternalSensor", fleetingToggleInternalSensor, true); // NOI18N
        _mDefaultFleetingEnabled = defaultFleetingEnabled;
        _mFleetingToggleInternalSensor.setKnownState(_mDefaultFleetingEnabled ? Sensor.ACTIVE : Sensor.INACTIVE);
    }
    
    public void removeAllListeners() {}   // None done.
    
    public boolean isFleetingEnabled () {
        if (_mFleetingToggleInternalSensor.valid()) return _mFleetingToggleInternalSensor.getKnownState() == Sensor.ACTIVE;
        return _mDefaultFleetingEnabled;
    }
}
