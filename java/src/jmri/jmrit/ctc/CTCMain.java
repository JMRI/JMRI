/**
 *  @author Gregory J. Bedlek Copyright (C) 2018, 2019
 *  Comment to force another CI build
 */

package jmri.jmrit.ctc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import jmri.Sensor;
import jmri.Turnout;
import jmri.jmrit.ctc.ctcserialdata.CTCSerialData;
import jmri.jmrit.ctc.ctcserialdata.CodeButtonHandlerData;
import jmri.jmrit.ctc.ctcserialdata.OtherData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CTCMain {
    private final static Logger log = LoggerFactory.getLogger(CTCMain.class);
    private final CTCSerialData _mCTCSerialData = new CTCSerialData();
    private final ArrayList<CodeButtonHandler> _mCodeButtonHandlersArrayList = new ArrayList<>();       // "Const" after initialization completes.
    private NBHSensor _mCTCDebugSystemReloadInternalSensor = null;
    private final PropertyChangeListener _mCTCDebugSystemReloadInternalSensorPropertyChangeListener = (PropertyChangeEvent e) -> { handleCTCDebugSystemReload(e); };
    private NBHSensor _mCTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor = null;
    private final PropertyChangeListener _mCTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensorPropertyChangeListener = (PropertyChangeEvent e) -> { handleLogging(e); };
    private final HashMap<Integer, SignalDirectionIndicatorsInterface> _mSIDIHashMap = new HashMap<>(); // "Const" after initialization completes.
    private final HashMap<Integer, SwitchDirectionIndicators> _mSWDIHashMap = new HashMap<>();          // "Const" after initialization completes.
    private final HashMap<Integer, CodeButtonHandler> _mCBHashMap = new HashMap<>();                    // "Const" after initialization completes.
    private final LockedRoutesManager _mLockedRoutesManager = new LockedRoutesManager();
    private javax.swing.Timer _mLockTurnoutsTimer = null;

//  So that external python script can set locks on all of the lockable turnouts:
    public void externalLockTurnout() {
        for (CodeButtonHandler codeButtonHandler : _mCodeButtonHandlersArrayList) {
            codeButtonHandler.externalLockTurnout();
        }
    }

    private String _mFilenameRead = null;
    public CTCMain() {}
    public void readDataFromXMLFile(String filename) {
        _mFilenameRead = filename;
        startup();
    }

    private void handleCTCDebugSystemReload(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("KnownState") && (int)e.getNewValue() == Sensor.ACTIVE) {    // NOI18N
            rereadXMLFile();
        }
    }

    public boolean _mCTCDebug_TrafficLockingRuleTriggeredDisplayLoggingEnabled = false;
    private void handleLogging(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("KnownState")) {         // NOI18N
            _mCTCDebug_TrafficLockingRuleTriggeredDisplayLoggingEnabled = (int)e.getNewValue() == Sensor.ACTIVE;
            if (_mCTCDebug_TrafficLockingRuleTriggeredDisplayLoggingEnabled) _mLockedRoutesManager.dump();
        }
    }

    public void rereadXMLFile() {
        if (_mFilenameRead != null) { // Safety check that someone loaded a file before.
            log.info(Bundle.getMessage("CTCMainSuttingDown"));          // NOI18N
            shutdown();
            startup();
            log.info("CTC {} {} {}", CTCSerialData.CTCVersion, Bundle.getMessage("CTCMainReloadedFile"), _mFilenameRead);   // NOI18N
        }
        else
        {
            log.warn(Bundle.getMessage("CTCMainNoFileLoaded")); // NOI18N
        }
    }

//  Do this prior to rereadXMLFile().  This will insure all objects are disconnected from propertyChangeListeners:
    private void shutdown() {
        for (CodeButtonHandler codeButtonHandler : _mCodeButtonHandlersArrayList) {
            codeButtonHandler.removeAllListeners();
        }
        _mLockedRoutesManager.removeAllListeners();
        _mCTCDebugSystemReloadInternalSensor.removePropertyChangeListener(_mCTCDebugSystemReloadInternalSensorPropertyChangeListener);
        _mCTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor.removePropertyChangeListener(_mCTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensorPropertyChangeListener);
    }

    private void startup() {
        _mLockedRoutesManager.clearAllLockedRoutes();
        SignalDirectionIndicators.resetSignalsUsed();
        if (!_mCTCSerialData.readDataFromXMLFile(_mFilenameRead)) {
            CTCException e = new CTCException("CTCMain", "", "readDataFromXMLFile", Bundle.getMessage("CTCMainFailedToRead") + " " + _mFilenameRead);   // NOI18N
            e.logError();
        }

//  One of's:
        OtherData otherData = _mCTCSerialData.getOtherData();
        Fleeting fleeting = new Fleeting(   otherData._mFleetingToggleInternalSensor,
                                            otherData._mDefaultFleetingEnabled);

        ArrayList <CodeButtonHandlerData> codeButtonHandlerDataList = _mCTCSerialData.getCodeButtonHandlerDataArrayList();
        LinkedList <TrafficLocking> trafficLockingFileReadComplete = new LinkedList<>();

//  For each code button defined:
        codeButtonHandlerDataList.forEach((codeButtonHandlerData) -> {

            boolean slavedSwitch = codeButtonHandlerData._mOSSectionSwitchSlavedToUniqueID != -1;
            String userIdentifier = codeButtonHandlerData.myShortStringNoComma() + ": ";
            boolean turnoutLockingOnlyEnabled
                    = !codeButtonHandlerData._mSIDI_Enabled
                    && !codeButtonHandlerData._mSIDL_Enabled
                    && !codeButtonHandlerData._mSWDI_Enabled
                    && !codeButtonHandlerData._mSWDL_Enabled
                    && !codeButtonHandlerData._mCO_Enabled
                    && !codeButtonHandlerData._mTRL_Enabled
                    && codeButtonHandlerData._mTUL_Enabled
                    && !codeButtonHandlerData._mIL_Enabled;

// Slave Switch: null
            SignalDirectionIndicatorsInterface signalDirectionIndicators = (codeButtonHandlerData._mSIDI_Enabled && !slavedSwitch) ?
                new SignalDirectionIndicators(  userIdentifier,
                                                codeButtonHandlerData._mSIDI_LeftInternalSensor,
                                                codeButtonHandlerData._mSIDI_NormalInternalSensor,
                                                codeButtonHandlerData._mSIDI_RightInternalSensor,
                                                codeButtonHandlerData._mSIDI_CodingTimeInMilliseconds,
                                                codeButtonHandlerData._mSIDI_TimeLockingTimeInMilliseconds,
                                                codeButtonHandlerData._mSIDI_LeftRightTrafficSignalsCSVList,
                                                codeButtonHandlerData._mSIDI_RightLeftTrafficSignalsCSVList,
                                                fleeting)
                : new SignalDirectionIndicatorsNull();
            _mSIDIHashMap.put(codeButtonHandlerData._mUniqueID, signalDirectionIndicators);

// Slave Switch: null
            SignalDirectionLever signalDirectionLever = (codeButtonHandlerData._mSIDL_Enabled && !slavedSwitch) ?
                new SignalDirectionLever(   userIdentifier,
                                            codeButtonHandlerData._mSIDL_LeftInternalSensor,
                                            codeButtonHandlerData._mSIDL_NormalInternalSensor,
                                            codeButtonHandlerData._mSIDL_RightInternalSensor)
                : null;

// Slave Switch: Valid
            SwitchDirectionIndicators switchDirectionIndicators = codeButtonHandlerData._mSWDI_Enabled ?
                new SwitchDirectionIndicators(  userIdentifier,
                                                codeButtonHandlerData._mSWDI_NormalInternalSensor,
                                                codeButtonHandlerData._mSWDI_ReversedInternalSensor,
                                                codeButtonHandlerData._mSWDI_ExternalTurnout,
                                                codeButtonHandlerData._mSWDI_CodingTimeInMilliseconds,
                                                codeButtonHandlerData._mSWDI_FeedbackDifferent)
                : null;
            if (switchDirectionIndicators != null) _mSWDIHashMap.put(codeButtonHandlerData._mUniqueID, switchDirectionIndicators);

// Slave Switch: Valid
            SwitchDirectionLever switchDirectionLever = codeButtonHandlerData._mSWDL_Enabled ?
                new SwitchDirectionLever(   userIdentifier,
                                            codeButtonHandlerData._mSWDL_InternalSensor)
                : null;

// Slave Switch: null
            CallOn callOn = (codeButtonHandlerData._mCO_Enabled && !slavedSwitch) ?
                new CallOn( _mLockedRoutesManager,
                            userIdentifier,
                            codeButtonHandlerData._mCO_CallOnToggleInternalSensor,
                            codeButtonHandlerData._mCO_GroupingsListString,
                            otherData._mSignalSystemType)
                : null;

// Slave Switch: Valid
            TurnoutLock turnoutLock = codeButtonHandlerData._mTUL_Enabled ?
                new TurnoutLock(userIdentifier,
                                codeButtonHandlerData._mTUL_DispatcherInternalSensorLockToggle,
                                codeButtonHandlerData._mTUL_ExternalTurnout,
                                codeButtonHandlerData._mTUL_ExternalTurnoutFeedbackDifferent,
                                codeButtonHandlerData._mTUL_DispatcherInternalSensorUnlockedIndicator,
                                codeButtonHandlerData._mTUL_NoDispatcherControlOfSwitch,
                                codeButtonHandlerData._mTUL_ndcos_WhenLockedSwitchStateIsClosed ? Turnout.CLOSED : Turnout.THROWN,
                                codeButtonHandlerData._mTUL_LockImplementation,
                                otherData._mTUL_EnabledAtStartup,
                                codeButtonHandlerData._mTUL_AdditionalExternalTurnout1,
                                codeButtonHandlerData._mTUL_AdditionalExternalTurnout1FeedbackDifferent,
                                codeButtonHandlerData._mTUL_AdditionalExternalTurnout2,
                                codeButtonHandlerData._mTUL_AdditionalExternalTurnout2FeedbackDifferent,
                                codeButtonHandlerData._mTUL_AdditionalExternalTurnout3,
                                codeButtonHandlerData._mTUL_AdditionalExternalTurnout3FeedbackDifferent)
                : null;

// Slave Switch: duplicate other referenced entry, otherwise handle IL normally:
            IndicationLockingSignals indicationLockingSignals = null;   // Default if not enabled
            if (slavedSwitch) {
                CodeButtonHandlerData slavedSwitchCodeButtonHandlerData = _mCTCSerialData.getCodeButtonHandlerDataViaUniqueID(codeButtonHandlerData._mOSSectionSwitchSlavedToUniqueID);
                if (slavedSwitchCodeButtonHandlerData != null)  { // Safety check
                    indicationLockingSignals = new IndicationLockingSignals(userIdentifier,
                                                                            slavedSwitchCodeButtonHandlerData._mIL_ListOfCSVSignalNames,
                                                                            codeButtonHandlerData._mSWDI_ExternalTurnout,
                                                                            otherData._mSignalSystemType);
                }
            } else if (codeButtonHandlerData._mIL_Enabled) {
                indicationLockingSignals = new IndicationLockingSignals(userIdentifier,
                                                                        codeButtonHandlerData._mIL_ListOfCSVSignalNames,
                                                                        codeButtonHandlerData._mSWDI_ExternalTurnout,
                                                                        otherData._mSignalSystemType);
            }

// Slave Switch: null
            TrafficLocking trafficLocking = (codeButtonHandlerData._mTRL_Enabled && !slavedSwitch) ?
                new TrafficLocking( userIdentifier,
                                    codeButtonHandlerData._mTRL_LeftTrafficLockingRulesSSVList,
                                    codeButtonHandlerData._mTRL_RightTrafficLockingRulesSSVList,
                                    _mLockedRoutesManager)
                    : null;
            if (trafficLocking != null) trafficLockingFileReadComplete.add(trafficLocking);

            CodeButtonHandler codeButtonHandler = new CodeButtonHandler(turnoutLockingOnlyEnabled,
                                                                        _mLockedRoutesManager,
                                                                        userIdentifier,
                                                                        codeButtonHandlerData._mUniqueID,
                                                                        codeButtonHandlerData._mCodeButtonInternalSensor,
                                                                        codeButtonHandlerData._mCodeButtonDelayTime,
                                                                        codeButtonHandlerData._mOSSectionOccupiedExternalSensor,
                                                                        codeButtonHandlerData._mOSSectionOccupiedExternalSensor2,
                                                                        signalDirectionIndicators,
                                                                        signalDirectionLever,
                                                                        switchDirectionIndicators,
                                                                        switchDirectionLever,
                                                                        fleeting,
                                                                        callOn,
                                                                        trafficLocking,
                                                                        turnoutLock,
                                                                        indicationLockingSignals);
            _mCodeButtonHandlersArrayList.add(codeButtonHandler);
            _mCBHashMap.put(codeButtonHandlerData._mUniqueID, codeButtonHandler);
        });
        _mCTCDebugSystemReloadInternalSensor = new NBHSensor("CTCMain", "", "_mCTCDebugSystemReloadInternalSensor", otherData._mCTCDebugSystemReloadInternalSensor, true);  // NOI18N
        _mCTCDebugSystemReloadInternalSensor.setKnownState(Sensor.INACTIVE);
        _mCTCDebugSystemReloadInternalSensor.addPropertyChangeListener(_mCTCDebugSystemReloadInternalSensorPropertyChangeListener);
        _mCTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor = new NBHSensor("CTCMain", "", "_mCTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor", otherData._mCTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor, true); // NOI18N
        _mCTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor.setKnownState(Sensor.INACTIVE);
        _mCTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor.addPropertyChangeListener(_mCTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensorPropertyChangeListener);

        for (TrafficLocking trafficLocking : trafficLockingFileReadComplete) { // Call these routines to give them a chance to initialize:
            trafficLocking.fileReadComplete(_mCBHashMap, _mSWDIHashMap);
        }

/*  As a final item, if the developer wants us to lock all of the lockable
    turnouts after a time period, create a GUI timer to do that, so that
    when we call the objects, they are called on the GUI thread for safety.
    In the called routines, sensors will be updated, but I don't know how
    thread safe they are, or whether they will directly update GUI objects,
    since GUI objects "visually back" the sensors.
*/
        if (otherData._mTUL_SecondsToLockTurnouts > 0) { // Enabled:
            _mLockTurnoutsTimer = new javax.swing.Timer(otherData._mTUL_SecondsToLockTurnouts * 1000, lockTurnoutsTimerTicked);
            _mLockTurnoutsTimer.setRepeats(false);
            _mLockTurnoutsTimer.start();
        }
    }

//  One shot routine:
    private final java.awt.event.ActionListener lockTurnoutsTimerTicked = new java.awt.event.ActionListener() {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
//  Shut down this timer so this doesn't happen again:
            _mLockTurnoutsTimer.stop();
            _mLockTurnoutsTimer.removeActionListener(lockTurnoutsTimerTicked);
            _mLockTurnoutsTimer = null;
            externalLockTurnout();
        }
    };

}
