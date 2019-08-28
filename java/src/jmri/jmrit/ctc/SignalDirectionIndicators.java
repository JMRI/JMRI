/*
 *  @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
package jmri.jmrit.ctc;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import javax.swing.Timer;
import jmri.Sensor;
import jmri.SignalAppearanceMap;
import jmri.SignalHead;
import jmri.implementation.AbstractSignalHead;
import jmri.implementation.AbstractSignalMast;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;

public final class SignalDirectionIndicators implements SignalDirectionIndicatorsInterface {
    static final HashSet<NBHAbstractSignalCommon> _mSignalsUsed = new HashSet<>();
    public static void resetSignalsUsed() { _mSignalsUsed.clear(); }
    private NBHSensor _mLeftSensor;
    private NBHSensor _mNormalSensor;
    private NBHSensor _mRightSensor;
    private int _mPresentSignalDirectionLever = CTCConstants.SIGNALSNORMAL;             // Default
    private final ArrayList<NBHAbstractSignalCommon> _mSignalListLeftRight = new ArrayList<>();
    private final ArrayList<NBHAbstractSignalCommon> _mSignalListRightLeft = new ArrayList<>();
    private Fleeting _mFleetingObject;
    private final RequestedDirectionObserved _mRequestedDirectionObserver = new RequestedDirectionObserved();
    private final Timer _mTimeLockingTimer;
    private final ActionListener _mTimeLockingTimerActionListener;
    private final Timer _mCodingTimeTimer;
    private final ActionListener _mCodingTimeTimerActionListener;
    private int _mPresentDirection;
    private CodeButtonHandler _mCodeButtonHandler = null;
    @Override
    public void setCodeButtonHandler(CodeButtonHandler codeButtonHandler) { _mCodeButtonHandler = codeButtonHandler; }

    private LinkedList<SignalHeadPropertyChangeListenerMaintainer> _mSignalHeadPropertyChangeListenerLinkedList = new LinkedList<>();
    @SuppressWarnings("LeakingThisInConstructor")   // NOI18N
    private class SignalHeadPropertyChangeListenerMaintainer {
        private final NBHAbstractSignalCommon _mSignal;
        private final PropertyChangeListener _mPropertyChangeListener = (PropertyChangeEvent e) -> { handleSignalChange(e); };
        public SignalHeadPropertyChangeListenerMaintainer(NBHAbstractSignalCommon signal) {
            _mSignal = signal;
            _mSignal.addPropertyChangeListener(_mPropertyChangeListener);
            _mSignalHeadPropertyChangeListenerLinkedList.add(this); // "leaking this in constructor" is OK here, since this is the last thing we do.  And we are NOT multi-threaded when this happens.
        }
        public void removePropertyChangeListener() {
            _mSignal.removePropertyChangeListener(_mPropertyChangeListener);
        }
    }

/*  From: https://docs.oracle.com/javase/tutorial/collections/implementations/list.html
    CopyOnWriteArrayList is a List implementation backed up by a copy-on-write array.
    This implementation is similar in nature to CopyOnWriteArraySet. No synchronization
    is necessary, even during iteration, and iterators are guaranteed never to throw
    ConcurrentModificationException. This implementation is well suited to maintaining
    event-handler lists, in which change is infrequent, and traversal is frequent and
    potentially time-consuming.
*/
//  private final CopyOnWriteArrayList<TrafficDirection> _mTimeLockingChangeObservers = new CopyOnWriteArrayList<>();

    public SignalDirectionIndicators(   String userIdentifier,
                                        String leftSensor,
                                        String normalSensor,
                                        String rightSensor,
                                        int codingTimeInMilliseconds,
                                        int timeLockingTimeInMilliseconds,
                                        String signalListLeftRightCSV,
                                        String signalListRightLeftCSV,
                                        Fleeting fleetingObject) {

// We need to give time to the ABS system to set signals.  See CALL to routine "allSignalsRedSetThemAllHeld", comments above that line:
        if (codingTimeInMilliseconds < 100) codingTimeInMilliseconds = 100;
        _mTimeLockingTimerActionListener = (ActionEvent) -> { timeLockingDone(); };
        _mTimeLockingTimer = new Timer(codingTimeInMilliseconds + timeLockingTimeInMilliseconds, _mTimeLockingTimerActionListener);
        _mTimeLockingTimer.setRepeats(false);
        _mCodingTimeTimerActionListener = (ActionEvent) -> { codingTimeDone(); };
        _mCodingTimeTimer = new Timer(codingTimeInMilliseconds, _mCodingTimeTimerActionListener);
        _mCodingTimeTimer.setRepeats(false);
        try {
            _mLeftSensor = new NBHSensor("SignalDirectionIndicators",  userIdentifier, Bundle.getMessage("SignalDirectionIndicatorsLeftSensor"), leftSensor, true);         // NOI18N
            _mNormalSensor = new NBHSensor("SignalDirectionIndicators", userIdentifier, Bundle.getMessage("SignalDirectionIndicatorsNormalSensor"), normalSensor, false);   // NOI18N
            _mRightSensor = new NBHSensor("SignalDirectionIndicators", userIdentifier, Bundle.getMessage("SignalDirectionIndicatorsRightSensor"), rightSensor, true);       // NOI18N
//  Partially plagerized from GUI code:
            boolean leftInternalSensorPresent = _mLeftSensor.valid();
            boolean entriesInLeftRightTrafficSignalsCSVList = !signalListLeftRightCSV.isEmpty();
            boolean rightInternalSensorPresent = _mRightSensor.valid();
            boolean entriesInRightLeftTrafficSignalsCSVList = !signalListRightLeftCSV.isEmpty();
            if (!leftInternalSensorPresent && !rightInternalSensorPresent) { throw new CTCException("SignalDirectionIndicators", userIdentifier, Bundle.getMessage("SignalDirectionIndicatorsMustHaveOne"), Bundle.getMessage("SignalDirectionIndicatorsError1")); }                        // NOI18N
            if (leftInternalSensorPresent && !entriesInRightLeftTrafficSignalsCSVList) { throw new CTCException("SignalDirectionIndicators", userIdentifier, Bundle.getMessage("SignalDirectionIndicatorsInvalidCombination"), Bundle.getMessage("SignalDirectionIndicatorsError2")); }     // NOI18N
            if (rightInternalSensorPresent && !entriesInLeftRightTrafficSignalsCSVList) { throw new CTCException("SignalDirectionIndicators", userIdentifier, Bundle.getMessage("SignalDirectionIndicatorsInvalidCombination"), Bundle.getMessage("SignalDirectionIndicatorsError3")); }    // NOI18N
            if (!leftInternalSensorPresent && entriesInRightLeftTrafficSignalsCSVList) { throw new CTCException("SignalDirectionIndicators", userIdentifier, Bundle.getMessage("SignalDirectionIndicatorsInvalidCombination"), Bundle.getMessage("SignalDirectionIndicatorsError4")); }      // NOI18N
            if (!rightInternalSensorPresent && entriesInLeftRightTrafficSignalsCSVList) { throw new CTCException("SignalDirectionIndicators", userIdentifier, Bundle.getMessage("SignalDirectionIndicatorsInvalidCombination"), Bundle.getMessage("SignalDirectionIndicatorsError5")); }    // NOI18N

            ArrayList<String> listOfSignals;
            listOfSignals = ProjectsCommonSubs.getArrayListFromCSV(signalListLeftRightCSV);
            for (String signalText : listOfSignals) {
                NBHAbstractSignalCommon signal = NBHAbstractSignalCommon.getExistingSignal("SignalDirectionIndicators", userIdentifier, "signalListLeftRightCSV" + " " + signalListLeftRightCSV, signalText);   // NOI18N
                new SignalHeadPropertyChangeListenerMaintainer(signal); // Lazy, constructor does EVERYTHING and leaves a bread crumb trail to this object.
                _mSignalListLeftRight.add(signal);
                addSignal(userIdentifier, signal);
            }
            listOfSignals = ProjectsCommonSubs.getArrayListFromCSV(signalListRightLeftCSV);
            for (String signalText : listOfSignals) {
                NBHAbstractSignalCommon signal = NBHAbstractSignalCommon.getExistingSignal("SignalDirectionIndicators", userIdentifier, "signalListRightLeftCSV" + " " + signalListRightLeftCSV, signalText);   // NOI18N
                new SignalHeadPropertyChangeListenerMaintainer(signal); // Lazy, constructor does EVERYTHING and leaves a bread crumb trail to this object.
                _mSignalListRightLeft.add(signal);
                addSignal(userIdentifier, signal);
            }
            _mFleetingObject = fleetingObject;
            setSignalDirectionIndicatorsToDirection(CTCConstants.SIGNALSNORMAL);
            forceAllSignalsToHeld();
          }
          catch (CTCException e) { e.logError(); return; }
    }

    @Override
    public void removeAllListeners() {
        _mCodingTimeTimer.stop();       // Safety:
        _mCodingTimeTimer.removeActionListener(_mCodingTimeTimerActionListener);
        _mTimeLockingTimer.stop();
        _mTimeLockingTimer.removeActionListener(_mTimeLockingTimerActionListener);
        _mSignalHeadPropertyChangeListenerLinkedList.forEach((signalHeadPropertyChangeListenerMaintainer) -> {
            signalHeadPropertyChangeListenerMaintainer.removePropertyChangeListener();
        });
    }

    @Override
    public boolean isNonfunctionalObject() { return false; }

    @Override
    public void setPresentSignalDirectionLever(int presentSignalDirectionLever) { _mPresentSignalDirectionLever = presentSignalDirectionLever; }

    @Override
    public boolean isRunningTime() { return _mTimeLockingTimer.isRunning(); }

    @Override
    public void osSectionBecameOccupied() {
        _mCodingTimeTimer.stop();
        _mTimeLockingTimer.stop();      // MUST be done before the next line:
        possiblyUpdateSignalIndicationSensors();
    }

    @Override
    public void codeButtonPressed(int requestedDirection, boolean requestedChangeInSignalDirection) {
// Valid to process:
        _mCodingTimeTimer.stop();
        _mRequestedDirectionObserver.setRequestedDirection(requestedDirection);         // Superfluous since "setSignalsHeldto" does the same, but I'll leave it here
        if (requestedDirection == CTCConstants.SIGNALSNORMAL) {     // Wants ALL STOP.
            if (_mPresentDirection != CTCConstants.SIGNALSNORMAL) { // And is NOT all stop, run time:
                _mTimeLockingTimer.start();
                requestedChangeInSignalDirection = true;    // And override what is passed
            }
        }
// ONLY start the coding timer IF we aren't running time.
        if (!isRunningTime()) { startCodingTime(); }
        if (requestedChangeInSignalDirection) setSignalDirectionIndicatorsToOUTOFCORRESPONDENCE();
        setSignalsHeldTo(requestedDirection);
    }

    @Override
    public void startCodingTime() {
        _mCodingTimeTimer.start();
    }

    @Override
    public boolean signalsNormal() {
        return _mPresentDirection == CTCConstants.SIGNALSNORMAL;
    }

    @Override
    public boolean signalsNormalOrOutOfCorrespondence() {
        return _mPresentDirection == CTCConstants.SIGNALSNORMAL || _mPresentDirection == CTCConstants.OUTOFCORRESPONDENCE;
    }

    @Override
    public int getPresentDirection() {
        return _mPresentDirection;
    }

    @Override
    public boolean inCorrespondence() {
        return _mPresentDirection != CTCConstants.OUTOFCORRESPONDENCE;
    }

    @Override
    public void forceAllSignalsToHeld() {
        setSignalsHeldTo(CTCConstants.SIGNALSNORMAL);
    }

    @Override
    public int getSignalsInTheFieldDirection() {
        boolean LRCanGo = false;
        boolean RLCanGo = false;
        for (NBHAbstractSignalCommon signal : _mSignalListLeftRight) {
            if (!signal.isDanger()) { LRCanGo = true; break; }
        }
        for (NBHAbstractSignalCommon signal : _mSignalListRightLeft) {
            if (!signal.isDanger()) { RLCanGo = true; break; }
        }
        if (LRCanGo && RLCanGo) {
            CTCException.logError(Bundle.getMessage("SignalDirectionIndicatorsError6"));    // NOI18N
            setSignalDirectionIndicatorsToOUTOFCORRESPONDENCE();    // ooppss!
            return CTCConstants.OUTOFCORRESPONDENCE;
        }
        if (LRCanGo) return CTCConstants.RIGHTTRAFFIC;
        if (RLCanGo) return CTCConstants.LEFTTRAFFIC;
        return CTCConstants.SIGNALSNORMAL;
    }

    @Override
    public void setSignalDirectionIndicatorsToOUTOFCORRESPONDENCE() {
        setSignalDirectionIndicatorsToDirection(CTCConstants.OUTOFCORRESPONDENCE);
    }

    @Override
    public void setRequestedDirection(int direction) {
        _mRequestedDirectionObserver.setRequestedDirection(direction);
    }

    private void addSignal(String userIdentifier, NBHAbstractSignalCommon signal) throws CTCException {
        if (!_mSignalsUsed.add(signal)) { throw new CTCException("SignalDirectionIndicators", userIdentifier, signal.getDisplayName(), Bundle.getMessage("SignalDirectionIndicatorsDuplicateHomeSignal")); }    // NOI18N
    }

    private void setSignalsHeldTo(int direction) {
        switch (direction) {
            case CTCConstants.LEFTTRAFFIC:
                setLRSignalsHeldTo(true);
                setRLSignalsHeldTo(false);
                break;
            case CTCConstants.RIGHTTRAFFIC:
                setLRSignalsHeldTo(false);
                setRLSignalsHeldTo(true);
                break;
            default:    // Could be OUTOFCORRESPONDENCE or SIGNALSNORMAL:
                setLRSignalsHeldTo(true);
                setRLSignalsHeldTo(true);
                break;
        }
        _mRequestedDirectionObserver.setRequestedDirection(direction);
    }

    private void setRLSignalsHeldTo(boolean held) { _mSignalListRightLeft.forEach((signal) -> {
        signal.setHeld(held);
        });
}
    private void setLRSignalsHeldTo(boolean held) { _mSignalListLeftRight.forEach((signal) -> {
        signal.setHeld(held);
        });
}

    private void setSignalDirectionIndicatorsToFieldSignalsState() {
        setSignalDirectionIndicatorsToDirection(getSignalsInTheFieldDirection());
    }

    private void setSignalDirectionIndicatorsToDirection(int direction) {
        switch (direction) {
            case CTCConstants.RIGHTTRAFFIC:
                _mLeftSensor.setKnownState(Sensor.INACTIVE);
                _mNormalSensor.setKnownState(Sensor.INACTIVE);
                _mRightSensor.setKnownState(Sensor.ACTIVE);
                break;
            case CTCConstants.LEFTTRAFFIC:
                _mLeftSensor.setKnownState(Sensor.ACTIVE);
                _mNormalSensor.setKnownState(Sensor.INACTIVE);
                _mRightSensor.setKnownState(Sensor.INACTIVE);
                break;
            case CTCConstants.SIGNALSNORMAL:
                _mLeftSensor.setKnownState(Sensor.INACTIVE);
                _mNormalSensor.setKnownState(Sensor.ACTIVE);
                _mRightSensor.setKnownState(Sensor.INACTIVE);
                break;
            default: // Either OUTOFCORRESPONDENCE or invalid passed value:
                _mLeftSensor.setKnownState(Sensor.INACTIVE);
                _mNormalSensor.setKnownState(Sensor.INACTIVE);
                _mRightSensor.setKnownState(Sensor.INACTIVE);
                break;
        }
        _mPresentDirection = direction;
    }

    private void timeLockingDone() {
        setSignalDirectionIndicatorsToFieldSignalsState();  // They ALWAYS reflect the field, even if error!
        cancelLockedRoute();
    }

//  Called by "codingTime" object when it's timer fires:
    private void codingTimeDone() {
        if (!isRunningTime()) { // Not running time, signals can change dynamically:
/*
    In "CodeButtonPressed", we have taken off the "held" bits if a direction was requested.  The ABS system
    then takes over and attempts to change the signal.  And since some time has passed ("codingTimeInMilliseconds"),
    the signals have had a - chance - to change indication from red.  At this moment in time, if the signal is still
    red, we will set to held all non held signals that are still red.  In this way, if the Dispatcher coded
    a signal for right traffic, and the block to the right was occupied, and we took off the held bit, the
    signal would stay red, but NOT be held.  Then if the block to the right became un-occupied, the signal would
    change to non-red.  This is NOT what Rick Moser wants in discussion on 1/19/17.  He said that the signal should
    REMAIN red even if occupancy goes clear (non fleeting).
*/
//  A way to test if "cancelLockedRoute();" below is called.  Make our signal system inconsistent with
//  our route allocation logic, to verify if the signal system stays red, we deallocate our allocation earlier.
//          for (NBHAbstractSignalCommon signal : _mSignalListLeftRight) {
//              signal.setAppearance(SignalHead.RED);
//          }
            if (allSignalsRedSetThemAllHeld(_mRequestedDirectionObserver.getRequestedDirection())) {
                cancelLockedRoute();
            }
            setSignalDirectionIndicatorsToFieldSignalsState();  // They ALWAYS reflect the field, even if error!
        }
    }

    private void cancelLockedRoute() {
        if (_mCodeButtonHandler != null) { _mCodeButtonHandler.cancelLockedRoute(); }
    }

//  We return an indication of whether or not all signals are red.
//  If true, then they all were red, else false.  If requestedDirection is not left or right, then default "true" (fail safe)!
    private boolean allSignalsRedSetThemAllHeld(int requestedDirection) {
        if (requestedDirection == CTCConstants.LEFTTRAFFIC) {
            boolean allRed = true;
            for (NBHAbstractSignalCommon signal : _mSignalListRightLeft) {   // Can't use lambda here!
                if (!signal.isDanger()) { allRed = false; break; }
            }
            if (allRed) { _mSignalListRightLeft.forEach((signalHead) -> signalHead.setHeld(true)); }
            return allRed;
        } else if (requestedDirection == CTCConstants.RIGHTTRAFFIC) {
            boolean allRed = true;
            for (NBHAbstractSignalCommon signal : _mSignalListLeftRight) {   // Can't use lambda here!
                if (!signal.isDanger()) { allRed = false; break; }
            }
            if (allRed) { _mSignalListLeftRight.forEach((signalHead) -> signalHead.setHeld(true)); }
            return allRed;
        }
        return true;
    }

/*  With the introduction of SignalMast objects, I had to modify this routine
    to support them ("changedToUniversalRed"):
*/
    private void handleSignalChange(PropertyChangeEvent e) {
        if (_mFleetingObject != null) {
            if (!_mFleetingObject.isFleetingEnabled()) {
                if (changedToUniversalRed(e)) {    // Signal (SignalMast, SignalHead) changed to Red:
                    boolean forceAllSignalsToHeld = false;
                    if (_mPresentSignalDirectionLever == CTCConstants.RIGHTTRAFFIC) {
                        for (NBHAbstractSignalCommon signal : _mSignalListLeftRight) {
                            if (e.getSource() == signal.getBean()) {
                                forceAllSignalsToHeld = true;
                                break;
                            }
                        }
                    } else if (_mPresentSignalDirectionLever == CTCConstants.LEFTTRAFFIC) {
                        for (NBHAbstractSignalCommon signal : _mSignalListRightLeft) {
                            if (e.getSource() == signal.getBean()) {
                                forceAllSignalsToHeld = true;
                                break;
                            }
                        }
                    }
                    if (forceAllSignalsToHeld) forceAllSignalsToHeld();
                }
            }
        }
        possiblyUpdateSignalIndicationSensors();
    }

    private boolean changedToUniversalRed(PropertyChangeEvent e) {
        Object source = e.getSource();
        if (source instanceof AbstractSignalHead) {
            if (e.getPropertyName().equals("Appearance")) { // NOI18N
                return SignalHead.RED == (int)e.getNewValue();
            }
        } else if (source instanceof AbstractSignalMast) {
            if (e.getPropertyName().equals("Aspect")) { // NOI18N
                AbstractSignalMast source2 = (AbstractSignalMast)source;
                return source2.getAspect().equals(source2.getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.DANGER));
            }
        }
        return false;   // If none of the above, don't know, assume not red.
    }

    private void possiblyUpdateSignalIndicationSensors() {
        if (!_mCodingTimeTimer.isRunning() && !isRunningTime()) {   // Not waiting for coding time and not running time, signals can change dynamically:
            setSignalDirectionIndicatorsToFieldSignalsState();      // They ALWAYS reflect the field, even if error!
        }
    }
}
