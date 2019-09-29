/*
 *  @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
package jmri.jmrit.ctc;

import jmri.Sensor;

public class SwitchDirectionLever {
    private NBHSensor _mSwitchLeverSensor;
    public SwitchDirectionLever(String userIdentifier,
                                String switchLeverSensor) {
        _mSwitchLeverSensor = new NBHSensor("SwitchDirectionLever", userIdentifier, "switchLeverSensor", switchLeverSensor, false); // NOI18N
        if (_mSwitchLeverSensor.getKnownState() == Sensor.UNKNOWN) {
            _mSwitchLeverSensor.setKnownState(Sensor.ACTIVE);
        }
    }

    public void removeAllListeners() {}   // None done.
    public NBHSensor getSwitchLeverSensor() { return _mSwitchLeverSensor; }

    public int getPresentState() {
        int presentState =  _mSwitchLeverSensor.getKnownState();
        if (presentState == Sensor.ACTIVE) return CTCConstants.SWITCHNORMAL;
        if (presentState == Sensor.INACTIVE) return CTCConstants.SWITCHREVERSED;
        return CTCConstants.OUTOFCORRESPONDENCE;    // HUH?
    }
}
