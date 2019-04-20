package jmri.jmrit.ctc;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import javax.swing.Timer;
import jmri.Sensor;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 * 
 * This module will "simulate" a code button press when there is none.
 * 
 * It will delay the simulated pressing of a code button for a duration
 * specified in the constructor, so that the user can change multiple items
 * on the CTC panel (ONLY in this O.S. section) before the "code is sent to the field".
 */
public class CodeButtonSimulator {
    private final NBHSensor _mCodeButtonSensor;
    private final NBHSensor _mSwitchLeverSensor;
    private final NBHSensor _mLeftSensor;
    private final NBHSensor _mNormalSensor;
    private final NBHSensor _mRightSensor;
    private final NBHSensor _mDispatcherSensorLockToggle;
    private final PropertyChangeListener _mAnySensorPropertyChangeListener;
    private final Timer _mPauseTimer;
    private final ActionListener _mPauseActionListener;
    
    public CodeButtonSimulator( int                     pauseTimeInMilliseconds,
                                NBHSensor               codeButtonSensor,
                                SwitchDirectionLever    switchDirectionLever,
                                SignalDirectionLever    signalDirectionLever,
                                TurnoutLock             turnoutLock) {
        _mCodeButtonSensor = codeButtonSensor;
        if (switchDirectionLever != null) {
            _mSwitchLeverSensor = switchDirectionLever.getSwitchLeverSensor();
        } else { // None, create "fake" one:
            _mSwitchLeverSensor = new NBHSensor("", "", "", "", true);
        }
        if (signalDirectionLever != null) {
            SignalDirectionLever.LNR_NBHSensors Sensors = signalDirectionLever.getLevers();
            _mLeftSensor = Sensors._mLeftSensor;
            _mNormalSensor = Sensors._mNormalSensor;
            _mRightSensor = Sensors._mRightSensor;
        } else {
            _mLeftSensor = new NBHSensor("", "", "", "", true);
            _mNormalSensor = new NBHSensor("", "", "", "", true);
            _mRightSensor = new NBHSensor("", "", "", "", true);
        }
        if (turnoutLock != null) {
            _mDispatcherSensorLockToggle = turnoutLock.getDispatcherSensorLockToggle();
        } else {
            _mDispatcherSensorLockToggle = new NBHSensor("", "", "", "", true);
        }
        
        _mAnySensorPropertyChangeListener = (PropertyChangeEvent) -> { anySensorPropertyChangeEvent(); };
        _mSwitchLeverSensor.addPropertyChangeListener(_mAnySensorPropertyChangeListener);
        _mLeftSensor.addPropertyChangeListener(_mAnySensorPropertyChangeListener);
        _mNormalSensor.addPropertyChangeListener(_mAnySensorPropertyChangeListener);
        _mRightSensor.addPropertyChangeListener(_mAnySensorPropertyChangeListener);
        _mDispatcherSensorLockToggle.addPropertyChangeListener(_mAnySensorPropertyChangeListener);
        _mPauseActionListener = (ActionEvent) -> { pauseActionListener(); };
        _mPauseTimer = new Timer(pauseTimeInMilliseconds, _mPauseActionListener);
        _mPauseTimer.setRepeats(false);
    }
    
    public void removeAllListeners() {
        _mSwitchLeverSensor.removePropertyChangeListener(_mAnySensorPropertyChangeListener);
        _mLeftSensor.removePropertyChangeListener(_mAnySensorPropertyChangeListener);
        _mNormalSensor.removePropertyChangeListener(_mAnySensorPropertyChangeListener);
        _mRightSensor.removePropertyChangeListener(_mAnySensorPropertyChangeListener);
        _mDispatcherSensorLockToggle.removePropertyChangeListener(_mAnySensorPropertyChangeListener);
        _mPauseTimer.stop();
        _mPauseTimer.removeActionListener(_mPauseActionListener);
    }
    
    private void pauseActionListener() {
        _mPauseTimer.stop();    // Probably already so since it fired and their are no repeats, so it doesn't hurt to do this again in case some idiot wants repeats.....
        _mCodeButtonSensor.setKnownState(Sensor.ACTIVE);    // Cause events to be fired as if code button pressed.
        _mCodeButtonSensor.setKnownState(Sensor.INACTIVE);  // Reset it so that can happen again!
    }
    
//  If the timer is already running due to some other event prior to us being triggered "in parallel" with our event
//  (i.e. within the pause time interval), then we do nothing here:    
    private void anySensorPropertyChangeEvent() {
        if (_mPauseTimer.isRunning()) return;   // Nothing more, within the timer window.
        _mPauseTimer.start();                   // That's it.
    }
}
