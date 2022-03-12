// Warnings objectInputStream here about changes to this structure and how it will affect old/new programs:
//https://howtodoinjava.com/java/serialization/a-mini-guide-for-implementing-serializable-interface-objectInputStream-java/

package jmri.jmrit.ctc.ctcserialdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.swing.ButtonGroup;
import jmri.jmrit.ctc.*;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
public class CodeButtonHandlerData {
    public static final int SWITCH_NOT_SLAVED = -1;

    public enum LOCK_IMPLEMENTATION {
// The values in paren's are the RadioGroup values set by "CommonSubs.numberButtonGroup",
// gotten by calling "CommonSubs.getButtonSelectedInt".
        GREGS(0), OTHER(1);
        private final int _mRadioGroupValue;
        private final static HashMap<Integer, LOCK_IMPLEMENTATION> map = new HashMap<>();
        private LOCK_IMPLEMENTATION (int radioGroupValue) { _mRadioGroupValue = radioGroupValue; }
        static { for (LOCK_IMPLEMENTATION value : LOCK_IMPLEMENTATION.values()) { map.put(value._mRadioGroupValue, value); }}
        public int getInt() { return _mRadioGroupValue; }
        public static LOCK_IMPLEMENTATION getLockImplementation(int radioGroupValue) { return map.get(radioGroupValue); }
        public static LOCK_IMPLEMENTATION getLockImplementation(ButtonGroup buttonGroup) { return map.get(ProjectsCommonSubs.getButtonSelectedInt(buttonGroup)); }
    }

    public enum TURNOUT_TYPE {
// The values in paren's are the RadioGroup values set by "CommonSubs.numberButtonGroup",
// gotten by calling "CommonSubs.getButtonSelectedInt".
        TURNOUT(0), CROSSOVER(1), DOUBLE_CROSSOVER(2);
        private final int _mRadioGroupValue;
        private final static HashMap<Integer, TURNOUT_TYPE> map = new HashMap<>();
        private TURNOUT_TYPE (int radioGroupValue) { _mRadioGroupValue = radioGroupValue; }
        static { for (TURNOUT_TYPE value : TURNOUT_TYPE.values()) { map.put(value._mRadioGroupValue, value); }}
        public int getInt() { return _mRadioGroupValue; }
        public static TURNOUT_TYPE getTurnoutType(int radioGroupValue) { return map.get(radioGroupValue); }
        public static TURNOUT_TYPE getTurnoutType(ButtonGroup buttonGroup) { return map.get(ProjectsCommonSubs.getButtonSelectedInt(buttonGroup)); }
    }

    public enum TRAFFIC_DIRECTION {
        LEFT,
        BOTH,
        RIGHT;
    }

    public CodeButtonHandlerData(int uniqueID, int switchNumber, int signalEtcNumber, int guiColumnNumber) {
        _mUniqueID = uniqueID;
        _mSwitchNumber = switchNumber;
        _mSignalEtcNumber = signalEtcNumber;
        _mOSSectionSwitchSlavedToUniqueID = SWITCH_NOT_SLAVED;
        _mGUIColumnNumber = guiColumnNumber;
        _mSWDI_GUITurnoutType = CodeButtonHandlerData.TURNOUT_TYPE.TURNOUT;
        _mTUL_LockImplementation = LOCK_IMPLEMENTATION.GREGS;
    }

//  This number NEVER changes, and is how this object is uniquely identified:
    public int _mUniqueID = -1;         // FORCE serialization to write out the FIRST unique number 0 into the XML file (to make me happy!)
    public int _mSwitchNumber;         // Switch Indicators and lever #
    public int _mSignalEtcNumber;      // Signal Indicators, lever, locktoggle, callon and code button number

    public String myString() { return Bundle.getMessage("CBHD_SwitchNumber") + " " + _mSwitchNumber + ", " + Bundle.getMessage("CBHD_SignalNumberEtc") + " " + _mSignalEtcNumber + Bundle.getMessage("CBHD_ColumnNumber") + " " + _mGUIColumnNumber + (_mGUIGeneratedAtLeastOnceAlready ? "*" : "") + ", [" + _mUniqueID + "]"; }  // NOI18N
    public String myShortStringNoComma() { return _mSwitchNumber + "/" + _mSignalEtcNumber; }

/*
Because of "getAllInternalSensorStringFields", ANY JMRI sensor object that we
create should have "InternalSensor" (case sensitive,
example: _mCodeButtonInternalSensor) as ANY PART of their variable name and
declared as type String.  This will insure that the GUI program will write these
sensors out to a separate file for JMRI to load to automatically create
these senosrs.  Other sensors that pre-exist within JMRI should NOT have
that as part of their variable name (ex: _mOSSectionOccupiedExternalSensor).

Also, see CheckJMRIObject's "public static final String EXTERNAL_xxx" definitions
at the top for "automatic" JMRI object verification.
*/

    public NBHSensor            _mCodeButtonInternalSensor;
    public NBHSensor            _mOSSectionOccupiedExternalSensor;              // Required
    public NBHSensor            _mOSSectionOccupiedExternalSensor2;             // Optional
    public int                  _mOSSectionSwitchSlavedToUniqueID;
    public int                  _mGUIColumnNumber;
    public boolean              _mGUIGeneratedAtLeastOnceAlready;
    public int                  _mCodeButtonDelayTime;
//  Signal Direction Indicators:
    public boolean              _mSIDI_Enabled;
    public NBHSensor            _mSIDI_LeftInternalSensor;
    public NBHSensor            _mSIDI_NormalInternalSensor;
    public NBHSensor            _mSIDI_RightInternalSensor;
    public int                  _mSIDI_CodingTimeInMilliseconds;
    public int                  _mSIDI_TimeLockingTimeInMilliseconds;
    public TRAFFIC_DIRECTION    _mSIDI_TrafficDirection;
    public ArrayList<NBHSignal> _mSIDI_LeftRightTrafficSignals = new ArrayList<>();
    public ArrayList<NBHSignal> _mSIDI_RightLeftTrafficSignals = new ArrayList<>();
//  Signal Direction Lever:
    public boolean              _mSIDL_Enabled;
    public NBHSensor            _mSIDL_LeftInternalSensor;
    public NBHSensor            _mSIDL_NormalInternalSensor;
    public NBHSensor            _mSIDL_RightInternalSensor;
//  Switch Direction Indicators:
    public boolean              _mSWDI_Enabled;
    public NBHSensor            _mSWDI_NormalInternalSensor;
    public NBHSensor            _mSWDI_ReversedInternalSensor;
    public NBHTurnout           _mSWDI_ExternalTurnout;
    public int                  _mSWDI_CodingTimeInMilliseconds;
    public boolean              _mSWDI_FeedbackDifferent;
    public TURNOUT_TYPE         _mSWDI_GUITurnoutType;
    public boolean              _mSWDI_GUITurnoutLeftHand;
    public boolean              _mSWDI_GUICrossoverLeftHand;
//  Switch Direction Lever:
    public boolean              _mSWDL_Enabled;
    public NBHSensor            _mSWDL_InternalSensor;
//  Call On:
    public boolean              _mCO_Enabled;
    public NBHSensor            _mCO_CallOnToggleInternalSensor;
    public ArrayList<CallOnData> _mCO_GroupingsList = new ArrayList<>();
//  Traffic Locking:
    public boolean              _mTRL_Enabled;
    public ArrayList<TrafficLockingData> _mTRL_LeftTrafficLockingRules = new ArrayList<>();
    public ArrayList<TrafficLockingData> _mTRL_RightTrafficLockingRules = new ArrayList<>();
//  Turnout Locking:
    public boolean              _mTUL_Enabled;
    public NBHSensor            _mTUL_DispatcherInternalSensorLockToggle;
    public NBHTurnout           _mTUL_ExternalTurnout;
    public boolean              _mTUL_ExternalTurnoutFeedbackDifferent;
    public NBHSensor            _mTUL_DispatcherInternalSensorUnlockedIndicator;
    public boolean              _mTUL_NoDispatcherControlOfSwitch;
    public boolean              _mTUL_ndcos_WhenLockedSwitchStateIsClosed;
    public boolean              _mTUL_GUI_IconsEnabled;
    public LOCK_IMPLEMENTATION  _mTUL_LockImplementation;
    public NBHTurnout           _mTUL_AdditionalExternalTurnout1;
    public boolean              _mTUL_AdditionalExternalTurnout1FeedbackDifferent;
    public NBHTurnout           _mTUL_AdditionalExternalTurnout2;
    public boolean              _mTUL_AdditionalExternalTurnout2FeedbackDifferent;
    public NBHTurnout           _mTUL_AdditionalExternalTurnout3;
    public boolean              _mTUL_AdditionalExternalTurnout3FeedbackDifferent;
//  Indication Locking (Signals):
    public boolean              _mIL_Enabled;
    public ArrayList<NBHSignal>      _mIL_Signals = new ArrayList<>();

}
