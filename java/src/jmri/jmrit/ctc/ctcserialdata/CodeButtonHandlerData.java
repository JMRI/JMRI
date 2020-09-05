// Warnings objectInputStream here about changes to this structure and how it will affect old/new programs:
//https://howtodoinjava.com/java/serialization/a-mini-guide-for-implementing-serializable-interface-objectInputStream-java/

package jmri.jmrit.ctc.ctcserialdata;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
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
public class CodeButtonHandlerData implements Serializable, Comparable<CodeButtonHandlerData> {
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

    @SuppressFBWarnings(value = "EQ_COMPARETO_USE_OBJECT_EQUALS", justification = "The code works fine as is, I have no idea why it is whining about this.")
    @Override
    public int compareTo(CodeButtonHandlerData codeButtonHandlerData) {
        return this._mGUIColumnNumber - codeButtonHandlerData._mGUIColumnNumber;
    }

    public CodeButtonHandlerData() {
        _mOSSectionSwitchSlavedToUniqueID = SWITCH_NOT_SLAVED;
        _mSWDI_GUITurnoutType = CodeButtonHandlerData.TURNOUT_TYPE.TURNOUT;
        _mTUL_LockImplementation = LOCK_IMPLEMENTATION.GREGS;
    }
    private static final long serialVersionUID = 1L;
//  Data and code used ONLY by the GUI designer, no use objectInputStream runtime system:
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
//  Used by the Editor only:
    public int _mSwitchNumber;         // Switch Indicators and lever #
    public int _mSignalEtcNumber;      // Signal Indicators, lever, locktoggle, callon and code button number
    public String myString() { return Bundle.getMessage("CBHD_SwitchNumber") + " " + _mSwitchNumber + ", " + Bundle.getMessage("CBHD_SignalNumberEtc") + " " + _mSignalEtcNumber + Bundle.getMessage("CBHD_ColumnNumber") + " " + _mGUIColumnNumber + (_mGUIGeneratedAtLeastOnceAlready ? "*" : "") + ", [" + _mUniqueID + "]"; }  // NOI18N
    public String myShortStringNoComma() { return _mSwitchNumber + "/" + _mSignalEtcNumber; }

    public static ArrayList <Field> getAllStringFields() {
        Field[] fields = CodeButtonHandlerData.class.getFields();
        ArrayList <Field> stringFields = new ArrayList<>();
        for (Field field : fields) {
            if (field.getType() == String.class) {
                stringFields.add(field);
            }
        }
        return stringFields;
    }

    public static ArrayList<Field> getAllInternalSensorStringFields() {
        return ProjectsCommonSubs.getAllPartialVariableNameStringFields(INTERNAL_SENSOR, CodeButtonHandlerData.class.getFields());
    }

//  Duplicates get ONLY ONE entry in the set (obviously).
    public HashSet<String> getAllInternalSensors() {
        HashSet<String> returnValue = new HashSet<>();
        ArrayList<Field> fields = getAllInternalSensorStringFields();
        for (Field field : fields) {
            try {
                returnValue.add((String)field.get(this));
             } catch (IllegalArgumentException | IllegalAccessException ex) { continue; }
        }
        return returnValue;
    }

//  You can call this at any time to get rid of leading / trailing spaces
//  in ALL Strings in this record.  In addition, any null entries are replaced
//  with "".
    public void trimAndFixAllStrings() {
        ArrayList <Field> stringFields = getAllStringFields();
        for (Field field : stringFields) {
            try {
                String unmodifiedString = (String)field.get(this);
                if (unmodifiedString != null) {
                    field.set(this, unmodifiedString.trim());
                }
                else
                    field.set(this, "");    // Null is replaced with "".
            } catch (IllegalAccessException e) {} // Skip this field on any error
        }
    }
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
    private static final String INTERNAL_SENSOR = "InternalSensor";     // NOI18N
//  Version of this file for supporting upgrade paths from prior versions:
//  Data used by the runtime (JMRI) and Editor systems:
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
