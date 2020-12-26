package jmri.jmrit.ctc;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import jmri.*;
import org.slf4j.LoggerFactory;

/**
 * This is the "master" class that handles everything when a code button is
 * pressed.  As such, it has a LOT of external data passed into it's constructor,
 * and operates and modifies all objects it contains on a dynamic basis both
 * when the button is pressed, and when external events happen that affect this
 * object.
 * <p>
 * Notes:
 * <p>
 * Changing both signal direction to non signals normal and switch direction at the same time "is allowed".
 * Lock/Unlock is the LOWEST priority!  Call on is the HIGHEST priority.
 * <p>
 * As of V1.04 of the CTC system, preconditioning (a.k.a. stacking) is supported.  It is enabled
 * by setting the internal sensor (automatically created) "IS:PRECONDITIONING_ENABLED" to active.
 * Any other value inactivates this feature.  For example, the user can create a toggle
 * switch to activate / inactivate it.
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019, 2020
 */
public class CodeButtonHandler {
    private final boolean _mTurnoutLockingOnlyEnabled;
    private final LockedRoutesManager _mLockedRoutesManager;
    private final String _mUserIdentifier;
    private final int _mUniqueID;
    private final NBHSensor _mCodeButtonInternalSensor;
    private final PropertyChangeListener _mCodeButtonInternalSensorPropertyChangeListener;
    private final NBHSensor _mOSSectionOccupiedExternalSensor;
    private final NBHSensor _mOSSectionOccupiedExternalSensor2;
    private final PropertyChangeListener _mOSSectionOccupiedExternalSensorPropertyChangeListener;
    private final SignalDirectionIndicatorsInterface _mSignalDirectionIndicators;
    private final SignalDirectionLever _mSignalDirectionLever;
    private final SwitchDirectionIndicators _mSwitchDirectionIndicators;
    private final SwitchDirectionLever _mSwitchDirectionLever;
    private final Fleeting _mFleeting;
    private final CallOn _mCallOn;
    private final TrafficLocking _mTrafficLocking;
    private final TurnoutLock _mTurnoutLock;
    private final IndicationLockingSignals _mIndicationLockingSignals;
    private final CodeButtonSimulator _mCodeButtonSimulator;
    private LockedRoute _mLockedRoute = null;

    private static final Sensor _mPreconditioningEnabledSensor = initializePreconditioningEnabledSensor();
    private static class PreconditioningData {
        public boolean  _mCodeButtonPressed = false;    // If false, values in these don't matter:
        public int      _mSignalDirectionLeverWas = CTCConstants.OUTOFCORRESPONDENCE;   // Safety:
        public int      _mSwitchDirectionLeverWas = CTCConstants.OUTOFCORRESPONDENCE;
    }

    private static Sensor initializePreconditioningEnabledSensor() {
        Sensor returnValue = InstanceManager.sensorManagerInstance().newSensor("IS:PRECONDITIONING_ENABLED", null); // NOI18N
        int knownState = returnValue.getKnownState();
        if (Sensor.ACTIVE != knownState && Sensor.INACTIVE != knownState) {
            try {returnValue.setKnownState(Sensor.INACTIVE); } catch (JmriException ex) {
                LoggerFactory.getLogger(CodeButtonHandler.class).debug("Sensor problem, preconditioning won't work.");          // NOI18N
            }
        }
        return returnValue;
    }
    private PreconditioningData _mPreconditioningData = new PreconditioningData();

    public CodeButtonHandler(   boolean turnoutLockingOnlyEnabled,                              // If this is NOT an O.S. section, but only a turnout lock, then this is true.
                                LockedRoutesManager lockedRoutesManager,
                                String userIdentifier,
                                int uniqueID,
                                NBHSensor codeButtonInternalSensor,                             // Required
                                int codeButtonDelayInMilliseconds,                              // If 0, REAL code button, if > 0, tower operations (simulated code button).
                                NBHSensor osSectionOccupiedExternalSensor,                      // Required, if ACTIVE prevents turnout, lock or call on from occuring.
                                NBHSensor osSectionOccupiedExternalSensor2,                     // Optional, if ACTIVE prevents turnout, lock or call on from occuring.
                                SignalDirectionIndicatorsInterface signalDirectionIndicators,   // Required
                                SignalDirectionLever signalDirectionLever,
                                SwitchDirectionIndicators switchDirectionIndicators,
                                SwitchDirectionLever switchDirectionLever,
                                Fleeting fleeting,                                              // If null, then ALWAYS fleeting!
                                CallOn callOn,
                                TrafficLocking trafficLocking,
                                TurnoutLock turnoutLock,
                                IndicationLockingSignals indicationLockingSignals) {            // Needed for check of adjacent OS Section(s), and optionally turnoutLock.
        signalDirectionIndicators.setCodeButtonHandler(this);
        _mTurnoutLockingOnlyEnabled = turnoutLockingOnlyEnabled;
        _mLockedRoutesManager = lockedRoutesManager;
        _mUserIdentifier = userIdentifier;
        _mUniqueID = uniqueID;
        _mSignalDirectionIndicators = signalDirectionIndicators;
        _mSignalDirectionLever = signalDirectionLever;
        _mSwitchDirectionIndicators = switchDirectionIndicators;
        _mSwitchDirectionLever = switchDirectionLever;
        _mFleeting = fleeting;
        _mCallOn = callOn;
        _mTrafficLocking = trafficLocking;
        _mTurnoutLock = turnoutLock;
        _mIndicationLockingSignals = indicationLockingSignals;
        _mCodeButtonInternalSensor = codeButtonInternalSensor;
        _mCodeButtonInternalSensor.setKnownState(Sensor.INACTIVE);
        _mCodeButtonInternalSensorPropertyChangeListener = (PropertyChangeEvent e) -> { codeButtonStateChange(e); };
        _mCodeButtonInternalSensor.addPropertyChangeListener(_mCodeButtonInternalSensorPropertyChangeListener);

        _mOSSectionOccupiedExternalSensorPropertyChangeListener = (PropertyChangeEvent e) -> { osSectionPropertyChangeEvent(e); };
        _mOSSectionOccupiedExternalSensor = osSectionOccupiedExternalSensor;
        _mOSSectionOccupiedExternalSensor.addPropertyChangeListener(_mOSSectionOccupiedExternalSensorPropertyChangeListener);

// NO property change for this, only used for turnout locking:
        _mOSSectionOccupiedExternalSensor2 = osSectionOccupiedExternalSensor2;

        if (codeButtonDelayInMilliseconds > 0) { // SIMULATED code button:
            _mCodeButtonSimulator = new CodeButtonSimulator(codeButtonDelayInMilliseconds,
                                                            _mCodeButtonInternalSensor,
                                                            _mSwitchDirectionLever,
                                                            _mSignalDirectionLever,
                                                            _mTurnoutLock);
        } else {
            _mCodeButtonSimulator = null;
        }
    }

    /**
     * This routine SHOULD ONLY be called by CTCMain when the CTC system is shutdown
     * in order to clean up all resources prior to a restart.  Nothing else should
     * call this.
     */
    public void removeAllListeners() {
//  Remove our registered listeners first:
        _mCodeButtonInternalSensor.removePropertyChangeListener(_mCodeButtonInternalSensorPropertyChangeListener);
        _mOSSectionOccupiedExternalSensor.removePropertyChangeListener(_mOSSectionOccupiedExternalSensorPropertyChangeListener);
//  Give each object a chance to remove theirs also:
        if (_mSignalDirectionIndicators != null) _mSignalDirectionIndicators.removeAllListeners();
        if (_mSignalDirectionLever != null) _mSignalDirectionLever.removeAllListeners();
        if (_mSwitchDirectionIndicators != null) _mSwitchDirectionIndicators.removeAllListeners();
        if (_mSwitchDirectionLever != null) _mSwitchDirectionLever.removeAllListeners();
        if (_mFleeting != null) _mFleeting.removeAllListeners();
        if (_mCallOn != null) _mCallOn.removeAllListeners();
        if (_mTrafficLocking != null) _mTrafficLocking.removeAllListeners();
        if (_mTurnoutLock != null) _mTurnoutLock.removeAllListeners();
        if (_mIndicationLockingSignals != null) _mIndicationLockingSignals.removeAllListeners();
        if (_mCodeButtonSimulator != null) _mCodeButtonSimulator.removeAllListeners();
    }

    /**
     * SignalDirectionIndicators calls us here when time locking is done.
     */
    public void cancelLockedRoute() {
        _mLockedRoutesManager.cancelLockedRoute(_mLockedRoute);     // checks passed parameter for null for us
        _mLockedRoute = null;       // Not valid anymore.
    }

    public boolean uniqueIDMatches(int uniqueID) { return _mUniqueID == uniqueID; }
    public NBHSensor getOSSectionOccupiedExternalSensor() { return _mOSSectionOccupiedExternalSensor; }

    private void osSectionPropertyChangeEvent(PropertyChangeEvent e) {
        if (isPrimaryOSSectionOccupied()) { // MUST ALWAYS process PRIMARY OS occupied state change to ACTIVE (It's the only one that comes here anyways!)
            if (_mFleeting != null && !_mFleeting.isFleetingEnabled()) { // Impliment "stick" here:
                _mSignalDirectionIndicators.forceAllSignalsToHeld();
            }
            _mSignalDirectionIndicators.osSectionBecameOccupied();
        }
        else { // Process pre-conditioning if available:
            if (_mPreconditioningData._mCodeButtonPressed) {
                doCodeButtonPress();
                _mPreconditioningData._mCodeButtonPressed = false;
            }
        }
    }

    public void externalLockTurnout() {
        if (_mTurnoutLock != null) _mTurnoutLock.externalLockTurnout();
    }

    private void codeButtonStateChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("KnownState") && (int)e.getNewValue() == Sensor.ACTIVE) {
//  NOTE: If the primary O.S. section is occupied, you CANT DO ANYTHING via a CTC machine, except:
//  Preconditioning: IF the O.S. section is occupied, then it is a pre-conditioning request:
            if (isPrimaryOSSectionOccupied()) {
                if (Sensor.ACTIVE == _mPreconditioningEnabledSensor.getKnownState()) {  // ONLY if turned on:
                    _mPreconditioningData._mSignalDirectionLeverWas = getCurrentSignalDirectionLever(false);
                    _mPreconditioningData._mSwitchDirectionLeverWas = getSwitchDirectionLeverRequestedState(false);
                    _mPreconditioningData._mCodeButtonPressed = true;   // Do this LAST so that the above variables are stable in this object,
                                                                        // in case there is a multi-threading issue (yea, lock it would be better,
                                                                        // but this is good enough for now!)
                }
            }
            doCodeButtonPress();
        }
    }

    private void doCodeButtonPress() {
        if (_mSignalDirectionIndicators.isRunningTime()) return;    // If we are running time, IGNORE all requests from the user:
        possiblyAllowLockChange();                              // MUST unlock first, otherwise if dispatcher wanted to unlock and change switch state, it wouldn't!
        possiblyAllowTurnoutChange();                           // Change turnout
//  IF the call on was accepted, then we DON'T attempt to change the signals to a more favorable
//  aspect here.  Additionally see the comments above CallOn.java/"codeButtonPressed" for an explanation
//  of a "fake out" that happens in that routine, and it's effect on this code here:
        if (!possiblyAllowCallOn()) {                           // NO call on occured or was allowed or requested:
            possiblyAllowSignalDirectionChange();               // Slave to it!
        }
    }

// Returns true if call on was actually done, else false
    private boolean possiblyAllowCallOn() {
        boolean returnStatus = false;
        if (allowCallOnChange()) {
            HashSet<Sensor> sensors = new HashSet<>();                  // Initial O.S. section sensor(s):
            sensors.add(_mOSSectionOccupiedExternalSensor.getBean());   // Always.
//  If there is a switch direction indicator, and it is reversed, then add the other sensor if valid here:
            if (_mSwitchDirectionIndicators != null && _mSwitchDirectionIndicators.getLastIndicatorState() == CTCConstants.SWITCHREVERSED) {
                if (_mOSSectionOccupiedExternalSensor2.valid()) sensors.add(_mOSSectionOccupiedExternalSensor2.getBean());
            }
//  NOTE: We DO NOT support preconditioning of call on, ergo false passed to "getCurrentSignalDirectionLever"
            TrafficLockingInfo trafficLockingInfo = _mCallOn.codeButtonPressed(sensors, _mUserIdentifier, _mSignalDirectionIndicators, getCurrentSignalDirectionLever(false));
            if (trafficLockingInfo._mLockedRoute != null) { // Was allocated:
                _mLockedRoute = trafficLockingInfo._mLockedRoute;
            }
            returnStatus = trafficLockingInfo._mReturnStatus;
        }
        if (_mCallOn != null) _mCallOn.resetToggle();
        return returnStatus;
    }

/*
Rules from http://www.ctcparts.com/about.htm
    "An important note though for programming logic is that the interlocking limits
must be clear and all power switches within the interlocking limits aligned
appropriately for the back to train route for this feature to activate."
*/
    private boolean allowCallOnChange() {
// Safety checks:
        if (_mCallOn == null) return false;
// Rules:
        if (isPrimaryOSSectionOccupied()) return false;
        if (_mSignalDirectionIndicators.isRunningTime()) return false;
        if (_mSignalDirectionIndicators.getSignalsInTheFieldDirection() != CTCConstants.SIGNALSNORMAL) return false;
        if (!areOSSensorsAvailableInRoutes()) return false;
        return true;
    }

//  If it doesn't exist, this returns OUTOFCORRESPONDENCE, else return it's present state:
//  NOTE: IF a preconditioned input was available, it OVERRIDES actual Signal Direction Lever (which is ignored in this case).
    private int getCurrentSignalDirectionLever(boolean allowMergeInPreconditioning) {
        if (_mSignalDirectionLever == null) return CTCConstants.OUTOFCORRESPONDENCE;
        if (allowMergeInPreconditioning && _mPreconditioningData._mCodeButtonPressed) { // We can check and it is available:
            if (_mPreconditioningData._mSignalDirectionLeverWas == CTCConstants.LEFTTRAFFIC
            || _mPreconditioningData._mSignalDirectionLeverWas == CTCConstants.RIGHTTRAFFIC) { // Was valid:
                return _mPreconditioningData._mSignalDirectionLeverWas;
            }
        }
        return _mSignalDirectionLever.getPresentSignalDirectionLeverState();
    }

    private void possiblyAllowTurnoutChange() {
        if (allowTurnoutChange()) {
            int requestedState = getSwitchDirectionLeverRequestedState(true);
            notifyTurnoutLockObjectOfNewAlignment(requestedState);          // Tell lock object this is new alignment
            if (_mSwitchDirectionIndicators != null) { // Safety:
                _mSwitchDirectionIndicators.codeButtonPressed(requestedState);  // Also sends commmands to move the points
            }
        }
    }

    private boolean allowTurnoutChange() {
// Safety checks:
// Rules:
        if (!_mSignalDirectionIndicators.signalsNormal()) return false;
        if (routeClearedAcross()) return false;               // Something was cleared thru, NO CHANGE
        if (isEitherOSSectionOccupied()) return false;
// 6/28/16: If the switch direction indicators are presently "OUTOFCORRESPONDENCE", IGNORE request, as we are presently working on a change:
        if (!switchDirectionIndicatorsInCorrespondence()) return false;
        if (!turnoutPresentlyLocked()) return false;
        if (!areOSSensorsAvailableInRoutes()) return false;
        return true;
    }

    private void notifyTurnoutLockObjectOfNewAlignment(int requestedState) {
        if (_mTurnoutLock != null) _mTurnoutLock.dispatcherCommandedState(requestedState);
    }

//  If it doesn't exist, this returns OUTOFCORRESPONDENCE, else return it's present state:
//  NOTE: IF a preconditioned input was available, it OVERRIDES actual Switch Direction Lever (which is ignored in this case).
    private int getSwitchDirectionLeverRequestedState(boolean allowMergeInPreconditioning) {
        if (_mSwitchDirectionLever == null) return CTCConstants.OUTOFCORRESPONDENCE;
        if (allowMergeInPreconditioning && _mPreconditioningData._mCodeButtonPressed) { // We can check and it is available:
            if (_mPreconditioningData._mSwitchDirectionLeverWas == CTCConstants.SWITCHNORMAL
            || _mPreconditioningData._mSwitchDirectionLeverWas == CTCConstants.SWITCHREVERSED) { // Was valid:
                return _mPreconditioningData._mSwitchDirectionLeverWas;
            }
        }
        return _mSwitchDirectionLever.getPresentState();
    }

// If it doesn't exist, this returns true.
    private boolean switchDirectionIndicatorsInCorrespondence() {
        if (_mSwitchDirectionIndicators != null) return _mSwitchDirectionIndicators.inCorrespondence();
        return true;
    }

    private void possiblyAllowSignalDirectionChange() {
        if (allowSignalDirectionChangePart1()) {
            int presentSignalDirectionLever = getCurrentSignalDirectionLever(true);
            int presentSignalDirectionIndicatorsDirection = _mSignalDirectionIndicators.getPresentDirection();  // Object always exists!
            boolean requestedChangeInSignalDirection = (presentSignalDirectionLever != presentSignalDirectionIndicatorsDirection);
// If Dispatcher is asking for a cleared signal direction:
            if (presentSignalDirectionLever != CTCConstants.SIGNALSNORMAL) {
                if (!requestedChangeInSignalDirection) return;  // If presentSignalDirectionLever is the same as the current state, DO NOTHING!
            }
// If user is trying to change direction, FORCE to "SIGNALSNORMAL" per Rick Moser response of 6/29/16:
            if (presentSignalDirectionLever == CTCConstants.LEFTTRAFFIC && presentSignalDirectionIndicatorsDirection == CTCConstants.RIGHTTRAFFIC)
                presentSignalDirectionLever = CTCConstants.SIGNALSNORMAL;
            else if (presentSignalDirectionLever == CTCConstants.RIGHTTRAFFIC && presentSignalDirectionIndicatorsDirection == CTCConstants.LEFTTRAFFIC)
                presentSignalDirectionLever = CTCConstants.SIGNALSNORMAL;

            if (allowSignalDirectionChangePart2(presentSignalDirectionLever)) {
// Tell SignalDirectionIndicators what the current requested state is:
                _mSignalDirectionIndicators.setPresentSignalDirectionLever(presentSignalDirectionLever);
                _mSignalDirectionIndicators.codeButtonPressed(presentSignalDirectionLever, requestedChangeInSignalDirection);
            }
        }
    }

    private boolean allowSignalDirectionChangePart1() {
// Safety Checks:
        if (_mSignalDirectionLever == null) return false;
// Rules:
// 6/28/16: If the signal direction indicators are presently "OUTOFCORRESPONDENCE", IGNORE request, as we are presently working on a change:
        if (!_mSignalDirectionIndicators.inCorrespondence()) return false;
        if (!turnoutPresentlyLocked()) return false;
        return true;                                    // Allowed "so far".
    }

    private boolean allowSignalDirectionChangePart2(int presentSignalDirectionLever) {
// Safety Checks: (none so far)
// Rules:
        if (presentSignalDirectionLever != CTCConstants.SIGNALSNORMAL) {
// If asking for a route and these indicates an error (a conflict), DO NOTHING!
            if (!trafficLockingValid(presentSignalDirectionLever)) return false;       // Do NOTHING at this time!
        }
        return true;                                    // Allowed
    }

    private boolean trafficLockingValid(int presentSignalDirectionLever) {
// If asking for a route and it indicates an error (a conflict), DO NOTHING!
        if (_mTrafficLocking != null) {
            TrafficLockingInfo trafficLockingInfo = _mTrafficLocking.valid(presentSignalDirectionLever, _mFleeting.isFleetingEnabled());
            _mLockedRoute = trafficLockingInfo._mLockedRoute;   // Can be null! This is the bread crumb trail when running time expires.
            return trafficLockingInfo._mReturnStatus;
        }
        return true;        // Valid
    }

    private void possiblyAllowLockChange() {
        if (allowLockChange()) _mTurnoutLock.codeButtonPressed();
    }

    private boolean allowLockChange() {
// Safety checks:
        if (_mTurnoutLock == null) return false;
// Rules:
// Degenerate case: If we ONLY have a lock toggle switch, code button and lock indicator then:
// if these 3 are null and the provided signalDirectionIndocatorsObject is non functional, therefore ALWAYS allow it!
//      if (_mSignalDirectionIndicators.isNonfunctionalObject() && _mSignalDirectionLever == null && _mSwitchDirectionIndicators == null && _mSwitchDirectionLever == null) return true;
//  If this is a normal O.S. section, then if either is occupied, DO NOT allow unlock.
//  If this is NOT an O.S. section, but only a lock, AND the dispatcher is trying
//  to UNLOCK or LOCK this section, occupancy is not considered:
        if (!_mTurnoutLockingOnlyEnabled) { // Normal O.S. section:
            if (isEitherOSSectionOccupied()) return false;
        }
        if (!_mTurnoutLock.tryingToChangeLockStatus()) return false;
        if (routeClearedAcross()) return false;
        if (!_mSignalDirectionIndicators.signalsNormal()) return false;
        if (!switchDirectionIndicatorsInCorrespondence()) return false;
        if (!areOSSensorsAvailableInRoutes()) return false;
        return true;
    }

    private boolean routeClearedAcross() {
        if (_mIndicationLockingSignals != null) return _mIndicationLockingSignals.routeClearedAcross();
        return false; // Default: Nothing to evaluate, nothing cleared thru!
    }

    private boolean turnoutPresentlyLocked() {
        if (_mTurnoutLock == null) return true;     // Doesn't exist, assume locked so that anything can be done to it.
        return _mTurnoutLock.turnoutPresentlyLocked();
    }

//  For "isEitherOSSectionOccupied" and "isPrimaryOSSectionOccupied" below,
//  INCONSISTENT, UNKNOWN and OCCUPIED are all considered OCCUPIED(ACTIVE).
    private boolean isEitherOSSectionOccupied() {
        return _mOSSectionOccupiedExternalSensor.getKnownState() != Sensor.INACTIVE || _mOSSectionOccupiedExternalSensor2.getKnownState() != Sensor.INACTIVE;
    }

//  See "isEitherOSSectionOccupied" comment.
    private boolean isPrimaryOSSectionOccupied() {
        return _mOSSectionOccupiedExternalSensor.getKnownState() != Sensor.INACTIVE;
    }

    private boolean areOSSensorsAvailableInRoutes() {
        HashSet<Sensor> sensors = new HashSet<>();
        sensors.add(_mOSSectionOccupiedExternalSensor.getBean());
        if (_mOSSectionOccupiedExternalSensor2.valid()) sensors.add(_mOSSectionOccupiedExternalSensor2.getBean());
        return _mLockedRoutesManager.checkRoute(sensors, _mUserIdentifier, "Turnout Check");
    }
}
