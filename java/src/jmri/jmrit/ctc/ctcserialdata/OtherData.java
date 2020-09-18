package jmri.jmrit.ctc.ctcserialdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.ButtonGroup;
import jmri.jmrit.ctc.NBHSensor;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
public class OtherData {
    public final static String CTC_VERSION = "v2.0";

    public enum CTC_PANEL_TYPE {
// The values in paren's are the RadioGroup values set by "CommonSubs.numberButtonGroup",
// gotten by calling "CommonSubs.getButtonSelectedInt".
        USS(0), OTHER(1);
        private final int _mRadioGroupValue;
        private final static HashMap<Integer, CTC_PANEL_TYPE> map = new HashMap<>();
        private CTC_PANEL_TYPE (int radioGroupValue) { _mRadioGroupValue = radioGroupValue; }
        static { for (CTC_PANEL_TYPE value : CTC_PANEL_TYPE.values()) { map.put(value._mRadioGroupValue, value); }}
        public int getRadioGroupValue() { return _mRadioGroupValue; }
        public static CTC_PANEL_TYPE getRadioGroupValue(int radioGroupValue) { return map.get(radioGroupValue); }
    }

    public enum SIGNALS_ON_PANEL {
// The values in paren's are the RadioGroup values set by "CommonSubs.numberButtonGroup",
// gotten by calling "CommonSubs.getButtonSelectedInt".
        ALL(0), GREEN_OFF(1), NONE(2);
        private final int _mRadioGroupValue;
        private final static HashMap<Integer, SIGNALS_ON_PANEL> map = new HashMap<>();
        private SIGNALS_ON_PANEL (int radioGroupValue) { _mRadioGroupValue = radioGroupValue; }
        static { for (SIGNALS_ON_PANEL value : SIGNALS_ON_PANEL.values()) { map.put(value._mRadioGroupValue, value); }}
        public int getRadioGroupValue() { return _mRadioGroupValue; }
        public static SIGNALS_ON_PANEL getRadioGroupValue(int radioGroupValue) { return map.get(radioGroupValue); }
    }

    public enum VERTICAL_SIZE {
// The values in paren's are the RadioGroup values set by "CommonSubs.numberButtonGroup",
// gotten by calling "CommonSubs.getButtonSelectedInt".
        SMALL(0), MEDIUM(1), LARGE(2);
        private final int _mRadioGroupValue;
        private final static HashMap<Integer, VERTICAL_SIZE> map = new HashMap<>();
        private VERTICAL_SIZE (int radioGroupValue) { _mRadioGroupValue = radioGroupValue; }
        static { for (VERTICAL_SIZE value : VERTICAL_SIZE.values()) { map.put(value._mRadioGroupValue, value); }}
        public int getRadioGroupValue() { return _mRadioGroupValue; }
        public static VERTICAL_SIZE getRadioGroupValue(int radioGroupValue) { return map.get(radioGroupValue); }
    }

    public enum SIGNAL_SYSTEM_TYPE {
// The values in paren's are the RadioGroup values set by "CommonSubs.numberButtonGroup",
// gotten by calling "CommonSubs.getButtonSelectedInt".
        SIGNALHEAD(0), SIGNALMAST(1);
        private final int _mRadioGroupValue;
        private final static HashMap<Integer, SIGNAL_SYSTEM_TYPE> map = new HashMap<>();
        private SIGNAL_SYSTEM_TYPE (int radioGroupValue) { _mRadioGroupValue = radioGroupValue; }
        static { for (SIGNAL_SYSTEM_TYPE value : SIGNAL_SYSTEM_TYPE.values()) { map.put(value._mRadioGroupValue, value); }}
        public int getInt() { return _mRadioGroupValue; }
        public static SIGNAL_SYSTEM_TYPE getSignalSystemType(int radioGroupValue) { return map.get(radioGroupValue); }
        public static SIGNAL_SYSTEM_TYPE getSignalSystemType(ButtonGroup buttonGroup) { return map.get(ProjectsCommonSubs.getButtonSelectedInt(buttonGroup)); }
    }

//  Fleeting:
    public NBHSensor   _mFleetingToggleInternalSensor;
    public boolean  _mDefaultFleetingEnabled;
//  Global startup:
    public boolean  _mTUL_EnabledAtStartup = true;
    public SIGNAL_SYSTEM_TYPE _mSignalSystemType;
    public int      _mTUL_SecondsToLockTurnouts = 0;
//  Next unique # for each created Column:
    public int      _mNextUniqueNumber = 0;
//  CTC Debugging:
    public NBHSensor   _mCTCDebugSystemReloadInternalSensor;
    public NBHSensor   _mCTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor;
//  GUI design:
    public int      _mGUIDesign_NumberOfEmptyColumnsAtEnd;
    public CTC_PANEL_TYPE   _mGUIDesign_CTCPanelType;
    public boolean  _mGUIDesign_BuilderPlate;
    public SIGNALS_ON_PANEL _mGUIDesign_SignalsOnPanel;
    public boolean  _mGUIDesign_FleetingToggleSwitch;
    public boolean  _mGUIDesign_AnalogClockEtc;
    public boolean  _mGUIDesign_ReloadCTCSystemButton;
    public boolean  _mGUIDesign_CTCDebugOnToggle;
    public boolean  _mGUIDesign_CreateTrackPieces;
    public VERTICAL_SIZE _mGUIDesign_VerticalSize;
    public boolean  _mGUIDesign_OSSectionUnknownInconsistentRedBlink;
    public boolean  _mGUIDesign_TurnoutsOnPanel;

    public OtherData() {
        _mFleetingToggleInternalSensor = new NBHSensor("OtherData", "fleeting = ", "IS:FLEETING", "IS:FLEETING");  // NOI18N
        _mDefaultFleetingEnabled = false;
        _mSignalSystemType = SIGNAL_SYSTEM_TYPE.SIGNALMAST;
        _mCTCDebugSystemReloadInternalSensor = new NBHSensor("OtherData", "reload = ", "IS:RELOADCTC", "IS:RELOADCTC");  // NOI18N
        _mCTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor = new NBHSensor("OtherData", "debug = ", "IS:DEBUGCTC", "IS:DEBUGCTC");  // NOI18N
        _mGUIDesign_NumberOfEmptyColumnsAtEnd = 0;
        _mGUIDesign_CTCPanelType = CTC_PANEL_TYPE.USS;
        _mGUIDesign_BuilderPlate = false;
        _mGUIDesign_SignalsOnPanel = SIGNALS_ON_PANEL.ALL;
        _mGUIDesign_FleetingToggleSwitch = true;
        _mGUIDesign_AnalogClockEtc = false;
        _mGUIDesign_ReloadCTCSystemButton = true;
        _mGUIDesign_CTCDebugOnToggle = true;
        _mGUIDesign_CreateTrackPieces = false;
        _mGUIDesign_VerticalSize = VERTICAL_SIZE.SMALL;
        _mGUIDesign_OSSectionUnknownInconsistentRedBlink = false;
        _mGUIDesign_TurnoutsOnPanel = true;
    }

    public int getNextUniqueNumber() { return _mNextUniqueNumber++; }

    public void possiblySetToHighest(int value) { if (value + 1 > _mNextUniqueNumber) _mNextUniqueNumber = value + 1; }

}
