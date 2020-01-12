package jmri.jmrit.ctc.editor.code;

import jmri.jmrit.ctc.ctcserialdata.CodeButtonHandlerData;

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
        returnValue._mOSSectionOccupiedExternalSensor = "";
        returnValue._mOSSectionOccupiedExternalSensor2 = "";
        returnValue._mOSSectionSwitchSlavedToUniqueID = CodeButtonHandlerData.SWITCH_NOT_SLAVED;
        returnValue._mGUIGeneratedAtLeastOnceAlready = false;
        returnValue._mCodeButtonDelayTime = programProperties._mCodeButtonDelayTime;
        returnValue._mSIDI_Enabled = false;
        returnValue._mSIDI_CodingTimeInMilliseconds = programProperties._mSIDI_CodingTimeInMilliseconds;
        returnValue._mSIDI_TimeLockingTimeInMilliseconds = programProperties._mSIDI_TimeLockingTimeInMilliseconds;
        returnValue._mSIDI_LeftRightTrafficSignalsCSVList = "";
        returnValue._mSIDI_RightLeftTrafficSignalsCSVList = "";
        returnValue._mSIDL_Enabled = false;
        returnValue._mSWDI_Enabled = false;
        returnValue._mSWDI_ExternalTurnout = "";
        returnValue._mSWDI_CodingTimeInMilliseconds = programProperties._mSWDI_CodingTimeInMilliseconds;
        returnValue._mSWDI_FeedbackDifferent = false;
        returnValue._mSWDI_GUITurnoutType = CodeButtonHandlerData.TURNOUT_TYPE.TURNOUT;
        returnValue._mSWDI_GUITurnoutLeftHand = false;
        returnValue._mSWDI_GUICrossoverLeftHand = false;
        returnValue._mSWDL_Enabled = false;
        returnValue._mCO_Enabled = false;
        returnValue._mCO_GroupingsListString = "";
        returnValue._mTRL_LeftTrafficLockingRulesSSVList = "";
        returnValue._mTRL_RightTrafficLockingRulesSSVList = "";
        returnValue._mTRL_Enabled = false;
        returnValue._mTUL_Enabled = false;
        returnValue._mTUL_ExternalTurnout = "";
        returnValue._mTUL_ExternalTurnoutFeedbackDifferent = false;
        returnValue._mTUL_NoDispatcherControlOfSwitch = false;
        returnValue._mTUL_ndcos_WhenLockedSwitchStateIsClosed = true;
        returnValue._mTUL_LockImplementation = CodeButtonHandlerData.LOCK_IMPLEMENTATION.GREGS;
        returnValue._mTUL_AdditionalExternalTurnout1 = "";
        returnValue._mTUL_AdditionalExternalTurnout1FeedbackDifferent = false;
        returnValue._mTUL_AdditionalExternalTurnout2 = "";
        returnValue._mTUL_AdditionalExternalTurnout2FeedbackDifferent = false;
        returnValue._mTUL_AdditionalExternalTurnout3 = "";
        returnValue._mTUL_AdditionalExternalTurnout3FeedbackDifferent = false;
        returnValue._mIL_Enabled = false;
        returnValue._mIL_ListOfCSVSignalNames = "";
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
    
//  uECBHDWSD is short for "updateExistingCodeButtonHandlerDataWithSubstitutedData"
    public static CodeButtonHandlerData uECBHDWSD_CodeButton(ProgramProperties programProperties, CodeButtonHandlerData returnValue) {
        returnValue._mCodeButtonInternalSensor = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mCodeButtonInternalSensorPattern);
        return returnValue;
    }
    public static CodeButtonHandlerData uECBHDWSD_SIDI(ProgramProperties programProperties, CodeButtonHandlerData returnValue) {
        returnValue._mSIDI_LeftInternalSensor = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mSIDI_LeftInternalSensorPattern);
        returnValue._mSIDI_NormalInternalSensor = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mSIDI_NormalInternalSensorPattern);
        returnValue._mSIDI_RightInternalSensor = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mSIDI_RightInternalSensorPattern);
        return returnValue;
    }
    public static CodeButtonHandlerData uECBHDWSD_SIDL(ProgramProperties programProperties, CodeButtonHandlerData returnValue) {
        returnValue._mSIDL_LeftInternalSensor = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mSIDL_LeftInternalSensorPattern);
        returnValue._mSIDL_NormalInternalSensor = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties. _mSIDL_NormalInternalSensorPattern);
        returnValue._mSIDL_RightInternalSensor = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mSIDL_RightInternalSensorPattern);
        return returnValue;
    }
    public static CodeButtonHandlerData uECBHDWSD_SWDI(ProgramProperties programProperties, CodeButtonHandlerData returnValue) {
        returnValue._mSWDI_NormalInternalSensor = substituteValueForPoundSigns(returnValue._mSwitchNumber, programProperties._mSWDI_NormalInternalSensorPattern);
        returnValue._mSWDI_ReversedInternalSensor = substituteValueForPoundSigns(returnValue._mSwitchNumber, programProperties._mSWDI_ReversedInternalSensorPattern);
        return returnValue;
    }
    public static CodeButtonHandlerData uECBHDWSD_SWDL(ProgramProperties programProperties, CodeButtonHandlerData returnValue) {
        returnValue._mSWDL_InternalSensor = substituteValueForPoundSigns(returnValue._mSwitchNumber, programProperties._mSWDL_InternalSensorPattern);
        return returnValue;
    }
    public static CodeButtonHandlerData uECBHDWSD_CallOn(ProgramProperties programProperties, CodeButtonHandlerData returnValue) {
        returnValue._mCO_CallOnToggleInternalSensor = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties. _mCO_CallOnToggleInternalSensorPattern);
        return returnValue;
    }
    
    public static CodeButtonHandlerData uECBHDWSD_TUL(ProgramProperties programProperties, CodeButtonHandlerData returnValue) {
        returnValue._mTUL_DispatcherInternalSensorLockToggle = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mTUL_DispatcherInternalSensorLockTogglePattern);
        returnValue._mTUL_DispatcherInternalSensorUnlockedIndicator = substituteValueForPoundSigns(returnValue._mSignalEtcNumber, programProperties._mTUL_DispatcherInternalSensorUnlockedIndicatorPattern);
        return returnValue;
    }
    
/*  This is the "heart" of the pattern match system: It substitutes the passed
    value whereever it see a single "#" in the passed template.  It does not
    support escapes, it will fix ALL "#" with that number in the passed template.
    It is indescriminate.
*/
    private static String substituteValueForPoundSigns(int value, String template) {
        int indexOf;
        while (-1 != (indexOf = template.indexOf('#'))) { template = template.substring(0, indexOf) + Integer.toString(value) + template.substring(indexOf+1); }
        return template;
    }
}
