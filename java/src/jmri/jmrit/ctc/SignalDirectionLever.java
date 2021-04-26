/*
 *  @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
package jmri.jmrit.ctc;

import jmri.Sensor;

public class SignalDirectionLever {
    public static class LNR_NBHSensors {   // L = Left, N = Normal, R = Right.
        public final NBHSensor _mLeftSensor;
        public final NBHSensor _mNormalSensor;
        public final NBHSensor _mRightSensor;
        public LNR_NBHSensors(NBHSensor leftSensor, NBHSensor normalSensor, NBHSensor rightSensor) {
            _mLeftSensor = leftSensor;
            _mNormalSensor = normalSensor;
            _mRightSensor = rightSensor;
        }
    }

    private NBHSensor _mLeftSensor;
    private NBHSensor _mNormalSensor;
    private NBHSensor _mRightSensor;

    public SignalDirectionLever(String userIdentifier, NBHSensor leftSensor, NBHSensor normalSensor, NBHSensor rightSensor) {
        _mLeftSensor = leftSensor;
        _mNormalSensor = normalSensor;
        _mRightSensor = rightSensor;
        if (!_mLeftSensor.valid() && !_mRightSensor.valid()) {
            new CTCException("SignalDirectionLever", userIdentifier, Bundle.getMessage("SignalDirectionLeverMustHaveOne"), Bundle.getMessage("SignalDirectionLeverOneOrBoth")).logError();
        }
        _mLeftSensor.setKnownState(Sensor.INACTIVE);
        _mNormalSensor.setKnownState(Sensor.ACTIVE);
        _mRightSensor.setKnownState(Sensor.INACTIVE);
    }

    public void removeAllListeners() {}   // None done.
    public LNR_NBHSensors getLevers() { return new LNR_NBHSensors(_mLeftSensor, _mNormalSensor, _mRightSensor); }

    public int getPresentSignalDirectionLeverState() {
        if (_mLeftSensor.getKnownState() == Sensor.ACTIVE) return CTCConstants.LEFTTRAFFIC;
        if (_mNormalSensor.getKnownState() == Sensor.ACTIVE) return CTCConstants.SIGNALSNORMAL;
        if (_mRightSensor.getKnownState() == Sensor.ACTIVE) return CTCConstants.RIGHTTRAFFIC;
        return CTCConstants.OUTOFCORRESPONDENCE;    // Huh?
    }
}
