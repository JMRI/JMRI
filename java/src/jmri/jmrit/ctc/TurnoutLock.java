/*
 *  @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
package jmri.jmrit.ctc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import jmri.Sensor;
import jmri.Turnout;
import jmri.jmrit.ctc.ctcserialdata.CodeButtonHandlerData;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;

/**
This only works with Digitrax DS54's and DS64's configured to LOCALLY change the switch via either
a pushbutton or toggle switch.  Specifically in JMRI / DS64 programmer, OpSw 21 SHOULD
be checked.  DS54's should be similarly configured.

The other way:
One would NOT check OpSw21, and then one would have to write JMRI software (or a script)
to process the message from the DS54/DS64, and then send the appropriate turnout
"CLOSED/THROWN" command to the turnout to effect the change.

Advantage:	No turnout movement when crew requests change unless allowed by Dispatcher.
Disadvantage:   Software MUST be running in order to throw turnout "normally".  In other
		words cannot run the layout without the computer, and with all turnouts
		controlled by the dispatcher set to "local control".

This modules way:
The purpose of this module is to "countermand" attempts by the field crews to throw
a switch that is under dispatcher control.  This works ONLY for switches that have feedback.

Advantage:	Computer NOT necessary to throw switch.
Disadvantage:   Switch will "partially throw" until the feedback contact changes and sends
                a message to the software, which then countermands it.  If a train is on the
		switch, it may be derailed.

NOTES:
	If a route is cleared thru, you MUST be prevented from UNLOCKING a locked switch.
Failure to provide such an object will just allow unlocking while a route is cleared thru.

If dispatcherSensorLockToggle is None, then INSURE that you call "ExternalLockTurnout" at some
point to lock the turnout, since this starts up with the turnout unlocked!

* See the documentation for the matrix regarding Command and Feedback normal/reversed.
 */

public class TurnoutLock {
    private final NBHSensor _mDispatcherSensorLockToggle;
    private int _mCommandedState = Turnout.CLOSED;  // Assume
    private ArrayList<NBHTurnout> _mTurnoutsMonitored = new ArrayList<>();
    private PropertyChangeListener _mTurnoutsMonitoredPropertyChangeListener = null;
    private boolean _mLocked = false;
    private NBHSensor _mDispatcherSensorUnlockedIndicator;
    private boolean _mNoDispatcherControlOfSwitch = false;
    private int _m_ndcos_WhenLockedSwitchState = 0;

    public TurnoutLock( String userIdentifier,
                        String dispatcherSensorLockToggle,          // Toggle switch that indicates lock/unlock on the panel.  If None, then PERMANENTLY locked by the Dispatcher!
                        String actualTurnout,                       // The turnout being locked: LTxx a real turnout, like LT69.
                        boolean actualTurnoutFeedbackDifferent,     // True / False, in case feedback backwards but switch command above isn't!
                        String dispatcherSensorUnlockedIndicator,   // Display unlocked status (when ACTIVE) back to the Dispatcher.
                        boolean noDispatcherControlOfSwitch,        // Dispatcher doesn't control the switch.  If TRUE, then provide:
                        int ndcos_WhenLockedSwitchState,            // When Dispatcher does lock, switch should be set to: CLOSED/THROWN
                        CodeButtonHandlerData.LOCK_IMPLEMENTATION _mLockImplementation,  // Someday, choose which one to implement.  Right now, my own.
                        boolean turnoutLocksEnabledAtStartup,
                        String additionalTurnout1,
                        boolean additionalTurnout1FeebackReversed,
                        String additionalTurnout2,
                        boolean additionalTurnout2FeebackReversed,
                        String additionalTurnout3,
                        boolean additionalTurnout3FeebackReversed) {
        _mDispatcherSensorLockToggle = new NBHSensor("TurnoutLock", userIdentifier, "dispatcherSensorLockToggle", dispatcherSensorLockToggle, true);    // NOI18N
        addTurnoutMonitored(userIdentifier, "actualTurnout", actualTurnout, actualTurnoutFeedbackDifferent, true);
        _mDispatcherSensorUnlockedIndicator = new NBHSensor("TurnoutLock", userIdentifier, "dispatcherSensorUnlockedIndicator", dispatcherSensorUnlockedIndicator, true);   // NOI18N
        _mDispatcherSensorLockToggle.setKnownState(turnoutLocksEnabledAtStartup ? Sensor.INACTIVE : Sensor.ACTIVE);
        _mNoDispatcherControlOfSwitch = noDispatcherControlOfSwitch;
        _m_ndcos_WhenLockedSwitchState = ndcos_WhenLockedSwitchState;
        addTurnoutMonitored(userIdentifier, "additionalTurnout1", additionalTurnout1, additionalTurnout1FeebackReversed, false);    // NOI18N
        addTurnoutMonitored(userIdentifier, "additionalTurnout2", additionalTurnout2, additionalTurnout2FeebackReversed, false);    // NOI18N
        addTurnoutMonitored(userIdentifier, "additionalTurnout3", additionalTurnout3, additionalTurnout3FeebackReversed, false);    // NOI18N
        updateDispatcherSensorIndicator(turnoutLocksEnabledAtStartup);
        _mTurnoutsMonitoredPropertyChangeListener = (PropertyChangeEvent e) -> { handleTurnoutChange(e); };
        for (NBHTurnout tempTurnout : _mTurnoutsMonitored) {
            if (tempTurnout.getKnownState() == Turnout.UNKNOWN) {
                tempTurnout.setCommandedState(_mCommandedState);    // MUST be done before "addPropertyChangeListener":
            }
            tempTurnout.addPropertyChangeListener(_mTurnoutsMonitoredPropertyChangeListener);
        }
    }

    public void removeAllListeners() {
        for (NBHTurnout tempTurnout : _mTurnoutsMonitored) {
            tempTurnout.removePropertyChangeListener(_mTurnoutsMonitoredPropertyChangeListener);
        }
    }

    public NBHSensor getDispatcherSensorLockToggle() { return _mDispatcherSensorLockToggle; }

    private void addTurnoutMonitored(String userIdentifier, String parameter, String actualTurnout, boolean FeedbackDifferent, boolean required) {
        boolean actualTurnoutPresent = !ProjectsCommonSubs.isNullOrEmptyString(actualTurnout);
        if (required && !actualTurnoutPresent) {
            (new CTCException("TurnoutLock", userIdentifier, parameter, Bundle.getMessage("RequiredTurnoutMissing"))).logError();   // NOI18N
            return;
        }
        if (actualTurnoutPresent) { // IF there is something there, try it:
            NBHTurnout tempTurnout = new NBHTurnout("TurnoutLock", userIdentifier, parameter, actualTurnout, FeedbackDifferent);    // NOI18N
            if (tempTurnout.valid()) _mTurnoutsMonitored.add(tempTurnout);
        }
    }

//  Was propertyChange:
    private void handleTurnoutChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("KnownState")) { // NOI18N
            if (_mLocked) {                                                 // Act on locked only!
                NBHTurnout turnout = null;  // Not found.
                for (int index = 0; index < _mTurnoutsMonitored.size(); index++) { // Find matching entry:
                    if (e.getSource() == _mTurnoutsMonitored.get(index).getBean()) { // Matched:
                        turnout = _mTurnoutsMonitored.get(index);
                        break;
                    }
                }
                if (turnout != null) { // Safety check:
                    if (_mCommandedState != turnout.getKnownState()) {      // Someone in the field messed with it:
                        turnoutSetCommandedState(turnout, _mCommandedState);       // Just directly restore it
                    }
                }
            }
        }
    }

/*
External software calls this from initialization code in order to lock the turnout.  When this code starts
up the lock status is UNLOCKED so that initialization code can do whatever to the turnout.
This routine DOES NOT modify the state of the switch, ONLY the lock!
*/
    public void externalLockTurnout() {
        _mDispatcherSensorLockToggle.setKnownState(Sensor.INACTIVE);
        updateDispatcherSensorIndicator(true);
    }

//  Ditto above routine, except opposite:
    public void externalUnlockTurnout() {
        _mDispatcherSensorLockToggle.setKnownState(Sensor.ACTIVE);
        updateDispatcherSensorIndicator(false);
    }

//  External software calls this (from CodeButtonHandler typically) to inform us of a valid code button push:
    public void codeButtonPressed() {
        boolean newLockedState = getNewLockedState();
        if (newLockedState == _mLocked) return; // Nothing changed
        if (_mNoDispatcherControlOfSwitch || newLockedState == true) { // No dispatcher control of switch, or LOCKING them, "normalize" the switch:
            for (NBHTurnout turnout : _mTurnoutsMonitored) {
                turnoutSetCommandedState(turnout, _m_ndcos_WhenLockedSwitchState);     // Make it so.
            }
        }
        updateDispatcherSensorIndicator(newLockedState);
    }

// External software calls this (from CodeButtonHandler typically) to tell us of the new state of the turnout:
    public void dispatcherCommandedState(int commandedState) {
        if (commandedState == CTCConstants.SWITCHNORMAL) _mCommandedState = Turnout.CLOSED; else _mCommandedState = Turnout.THROWN;
    }

    public boolean turnoutPresentlyLocked() { return _mLocked; }

    public boolean getNewLockedState() {
        return _mDispatcherSensorLockToggle.getKnownState() == Sensor.INACTIVE;
    }

    public boolean tryingToChangeLockStatus() { return getNewLockedState() != _mLocked; }

    private void turnoutSetCommandedState(NBHTurnout turnout, int state) {
        _mCommandedState = state;
        turnout.setCommandedState(state);
    }

    private void updateDispatcherSensorIndicator(boolean newLockedState) {
        _mLocked = newLockedState;
        _mDispatcherSensorUnlockedIndicator.setKnownState(_mLocked ? Sensor.INACTIVE : Sensor.ACTIVE);
    }
}
