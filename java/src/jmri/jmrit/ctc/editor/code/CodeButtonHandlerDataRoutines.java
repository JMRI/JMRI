package jmri.jmrit.ctc.editor.code;

import java.util.ArrayList;
import jmri.jmrit.ctc.NBHSensor;
import jmri.jmrit.ctc.NBHSignal;
import jmri.jmrit.ctc.NBHTurnout;
import jmri.jmrit.ctc.ctcserialdata.CallOnData;
import jmri.jmrit.ctc.ctcserialdata.CodeButtonHandlerData;
import jmri.jmrit.ctc.ctcserialdata.TrafficLockingData;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 *
 * The purpose of this module is to support patterned information
 * in CodeButtonHandlerData objects.  It is also a "factory" to produce
 * new CodeButtonHandlerData objects from those patterns..
 */
public class CodeButtonHandlerDataRoutines {
    public static CodeButtonHandlerData createNewCodeButtonHandlerData(int newUniqueID, int newSwitchNumber, int newSignalEtcNumber, int newGUIColumnNumber, ProgramProperties programProperties) {
        CodeButtonHandlerData returnValue = new CodeButtonHandlerData(newUniqueID, newSwitchNumber, newSignalEtcNumber, newGUIColumnNumber);
        returnValue = updateExistingCodeButtonHandlerDataWithSubstitutedData(programProperties, returnValue);
        returnValue._mOSSectionOccupiedExternalSensor = new NBHSensor("CodeButtonHandlerDataRoutines", "Empty _mOSSectionOccupiedExternalSensor", "", "", true);
        returnValue._mOSSectionOccupiedExternalSensor2 = new NBHSensor("CodeButtonHandlerDataRoutines", "Empty _mOSSectionOccupiedExternalSensor2", "", "", true);
        returnValue._mOSSectionSwitchSlavedToUniqueID = CodeButtonHandlerData.SWITCH_NOT_SLAVED;
        returnValue._mGUIGeneratedAtLeastOnceAlready = false;
        returnValue._mCodeButtonDelayTime = programProperties._mCodeButtonDelayTime;
        returnValue._mSIDI_Enabled = false;
        returnValue._mSIDI_CodingTimeInMilliseconds = programProperties._mSIDI_CodingTimeInMilliseconds;
        returnValue._mSIDI_TimeLockingTimeInMilliseconds = programProperties._mSIDI_TimeLockingTimeInMilliseconds;
        returnValue._mSIDI_LeftRightTrafficSignals = new ArrayList<NBHSignal>();
        returnValue._mSIDI_RightLeftTrafficSignals = new ArrayList<NBHSignal>();
        returnValue._mSIDL_Enabled = false;
        returnValue._mSWDI_Enabled = false;
        returnValue._mSWDI_ExternalTurnout = new NBHTurnout("CodeButtonHandlerDataRoutines", "Empty _mSWDI_ExternalTurnout", "", "", true);
        returnValue._mSWDI_CodingTimeInMilliseconds = programProperties._mSWDI_CodingTimeInMilliseconds;
        returnValue._mSWDI_FeedbackDifferent = false;
        returnValue._mSWDI_GUITurnoutType = CodeButtonHandlerData.TURNOUT_TYPE.TURNOUT;
        returnValue._mSWDI_GUITurnoutLeftHand = false;
        returnValue._mSWDI_GUICrossoverLeftHand = false;
        returnValue._mSWDL_Enabled = false;
        returnValue._mCO_Enabled = false;
        returnValue._mCO_GroupingsList = new ArrayList<CallOnData>();
        returnValue._mTRL_LeftTrafficLockingRules = new ArrayList<TrafficLockingData>();
        returnValue._mTRL_RightTrafficLockingRules = new ArrayList<TrafficLockingData>();
        returnValue._mTRL_Enabled = false;
        returnValue._mTUL_Enabled = false;
        returnValue._mTUL_ExternalTurnout = new NBHTurnout("CodeButtonHandlerDataRoutines", "Empty _mTUL_ExternalTurnout", "", "", true);
        returnValue._mTUL_ExternalTurnoutFeedbackDifferent = false;
        returnValue._mTUL_NoDispatcherControlOfSwitch = false;
        returnValue._mTUL_ndcos_WhenLockedSwitchStateIsClosed = true;
        returnValue._mTUL_LockImplementation = CodeButtonHandlerData.LOCK_IMPLEMENTATION.GREGS;
        returnValue._mTUL_AdditionalExternalTurnout1 = new NBHTurnout("CodeButtonHandlerDataRoutines", "Empty _mTUL_AdditionalExternalTurnout1", "", "", true);
        returnValue._mTUL_AdditionalExternalTurnout1FeedbackDifferent = false;
        returnValue._mTUL_AdditionalExternalTurnout2 = new NBHTurnout("CodeButtonHandlerDataRoutines", "Empty _mTUL_AdditionalExternalTurnout2", "", "", true);
        returnValue._mTUL_AdditionalExternalTurnout2FeedbackDifferent = false;
        returnValue._mTUL_AdditionalExternalTurnout3 = new NBHTurnout("CodeButtonHandlerDataRoutines", "Empty _mTUL_AdditionalExternalTurnout3", "", "", true);
        returnValue._mTUL_AdditionalExternalTurnout3FeedbackDifferent = false;
        returnValue._mIL_Enabled = false;
        returnValue._mIL_Signals = new ArrayList<NBHSignal>();
        return returnValue;
    }

    public static CodeButtonHandlerData updateExistingCodeButtonHandlerDataWithSubstitutedData(ProgramProperties programProperties, CodeButtonHandlerData returnValue) {
        uECBHDWSD_CodeButton(programProperties, returnValue);
        uECBHDWSD_SIDI(programProperties, returnValue);
        uECBHDWSD_SIDL(programProperties, returnValue);
        uECBHDWSD_SWDI(programProperties, returnValue);
        uECBHDWSD_SWDL(programProperties, returnValue);
        uECBHDWSD_CallOn(programProperties, returnValue);
        uECBHDWSD_TUL(programProperties, returnValue);
        return returnValue;
    }

//         _mFleetingToggleInternalSensor = new NBHSensor("OtherData", "fleeting", "IS:FLEETING", "IS:FLEETING");  // NOI18N

//  uECBHDWSD is short for "updateExistingCodeButtonHandlerDataWithSubstitutedData"
    public static CodeButtonHandlerData uECBHDWSD_CodeButton(ProgramProperties programProperties, CodeButtonHandlerData returnValue) {
        String sensorName = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mCodeButtonInternalSensorPattern);
        NBHSensor sensor = new NBHSensor("CodeButtonHandlerDataRoutines", "Code", sensorName, sensorName);  // NOI18N
        returnValue._mCodeButtonInternalSensor = sensor.valid() ? sensor : null;
        return returnValue;
    }

    public static CodeButtonHandlerData uECBHDWSD_SIDI(ProgramProperties programProperties, CodeButtonHandlerData returnValue) {
//         returnValue._mSIDI_LeftInternalSensor = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mSIDI_LeftInternalSensorPattern);
        String sensorName = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mSIDI_LeftInternalSensorPattern);
        NBHSensor sensor = new NBHSensor("CodeButtonHandlerDataRoutines", "SIDI", sensorName, sensorName);  // NOI18N
        returnValue._mSIDI_LeftInternalSensor = sensor.valid() ? sensor : null;
//         returnValue._mSIDI_NormalInternalSensor = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mSIDI_NormalInternalSensorPattern);
        sensorName = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mSIDI_NormalInternalSensorPattern);
        sensor = new NBHSensor("CodeButtonHandlerDataRoutines", "SIDI", sensorName, sensorName);  // NOI18N
        returnValue._mSIDI_NormalInternalSensor = sensor.valid() ? sensor : null;
//         returnValue._mSIDI_RightInternalSensor = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mSIDI_RightInternalSensorPattern);
        sensorName = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mSIDI_RightInternalSensorPattern);
        sensor = new NBHSensor("CodeButtonHandlerDataRoutines", "SIDI", sensorName, sensorName);  // NOI18N
        returnValue._mSIDI_RightInternalSensor = sensor.valid() ? sensor : null;

        return returnValue;
    }

    public static CodeButtonHandlerData uECBHDWSD_SIDL(ProgramProperties programProperties, CodeButtonHandlerData returnValue) {
//         returnValue._mSIDL_LeftInternalSensor = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mSIDL_LeftInternalSensorPattern);
        String sensorName = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mSIDL_LeftInternalSensorPattern);
        NBHSensor sensor = new NBHSensor("CodeButtonHandlerDataRoutines", "SIDL", sensorName, sensorName);  // NOI18N
        returnValue._mSIDL_LeftInternalSensor = sensor.valid() ? sensor : null;

//         returnValue._mSIDL_NormalInternalSensor = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties. _mSIDL_NormalInternalSensorPattern);
        sensorName = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mSIDL_NormalInternalSensorPattern);
        sensor = new NBHSensor("CodeButtonHandlerDataRoutines", "SIDL", sensorName, sensorName);  // NOI18N
        returnValue._mSIDL_NormalInternalSensor = sensor.valid() ? sensor : null;

//         returnValue._mSIDL_RightInternalSensor = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mSIDL_RightInternalSensorPattern);
        sensorName = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mSIDL_RightInternalSensorPattern);
        sensor = new NBHSensor("CodeButtonHandlerDataRoutines", "SIDL", sensorName, sensorName);  // NOI18N
        returnValue._mSIDL_RightInternalSensor = sensor.valid() ? sensor : null;

        return returnValue;
    }

    public static CodeButtonHandlerData uECBHDWSD_SWDI(ProgramProperties programProperties, CodeButtonHandlerData returnValue) {
//         returnValue._mSWDI_NormalInternalSensor = substituteValueForPoundSigns(returnValue._mSwitchNumber, programProperties._mSWDI_NormalInternalSensorPattern);
        String sensorName = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mSWDI_NormalInternalSensorPattern);
        NBHSensor sensor = new NBHSensor("CodeButtonHandlerDataRoutines", "SWDI", sensorName, sensorName);  // NOI18N
        returnValue._mSWDI_NormalInternalSensor = sensor.valid() ? sensor : null;

//         returnValue._mSWDI_ReversedInternalSensor = substituteValueForPoundSigns(returnValue._mSwitchNumber, programProperties._mSWDI_ReversedInternalSensorPattern);
        sensorName = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mSWDI_ReversedInternalSensorPattern);
        sensor = new NBHSensor("CodeButtonHandlerDataRoutines", "SWDI", sensorName, sensorName);  // NOI18N
        returnValue._mSWDI_ReversedInternalSensor = sensor.valid() ? sensor : null;

        return returnValue;
    }

    public static CodeButtonHandlerData uECBHDWSD_SWDL(ProgramProperties programProperties, CodeButtonHandlerData returnValue) {
//         returnValue._mSWDL_InternalSensor = substituteValueForPoundSigns(returnValue._mSwitchNumber, programProperties._mSWDL_InternalSensorPattern);
        String sensorName = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mSWDL_InternalSensorPattern);
        NBHSensor sensor = new NBHSensor("CodeButtonHandlerDataRoutines", "SWDL", sensorName, sensorName);  // NOI18N
        returnValue._mSWDL_InternalSensor = sensor.valid() ? sensor : null;

        return returnValue;
    }

    public static CodeButtonHandlerData uECBHDWSD_CallOn(ProgramProperties programProperties, CodeButtonHandlerData returnValue) {
//         returnValue._mCO_CallOnToggleInternalSensor = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties. _mCO_CallOnToggleInternalSensorPattern);
        String sensorName = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mCO_CallOnToggleInternalSensorPattern);
        NBHSensor sensor = new NBHSensor("CodeButtonHandlerDataRoutines", "CO", sensorName, sensorName);  // NOI18N
        returnValue._mCO_CallOnToggleInternalSensor = sensor.valid() ? sensor : null;
        return returnValue;
    }

    public static CodeButtonHandlerData uECBHDWSD_TUL(ProgramProperties programProperties, CodeButtonHandlerData returnValue) {
//         returnValue._mTUL_DispatcherInternalSensorLockToggle = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mTUL_DispatcherInternalSensorLockTogglePattern);
        String sensorName = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mTUL_DispatcherInternalSensorLockTogglePattern);
        NBHSensor sensor = new NBHSensor("CodeButtonHandlerDataRoutines", "TUL", sensorName, sensorName);  // NOI18N
        returnValue._mTUL_DispatcherInternalSensorLockToggle = sensor.valid() ? sensor : null;

//         returnValue._mTUL_DispatcherInternalSensorUnlockedIndicator = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mTUL_DispatcherInternalSensorUnlockedIndicatorPattern);
        sensorName = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mTUL_DispatcherInternalSensorUnlockedIndicatorPattern);
        sensor = new NBHSensor("CodeButtonHandlerDataRoutines", "TUL", sensorName, sensorName);  // NOI18N
        returnValue._mTUL_DispatcherInternalSensorUnlockedIndicator = sensor.valid() ? sensor : null;

        return returnValue;
    }

    /**
     * This is the "heart" of the pattern match system: It substitutes the passed
     * value where ever it see a single "#" in the passed template.  It does not
     * support escapes, it will fix ALL "#" with that number in the passed template.
     * It is indiscriminate.
     *
     * @param value     The "number" that will be substituted where the template parameter is
     * @param template  The pattern used to generate the result.
     * @return          Modified string.  ALL locations modified indiscriminately.
     */
    private static String substituteValueForPoundSigns(int value, String template) {
        int indexOf;
        while (-1 != (indexOf = template.indexOf('#'))) { template = template.substring(0, indexOf) + Integer.toString(value) + template.substring(indexOf+1); }
        return template;
    }
}
