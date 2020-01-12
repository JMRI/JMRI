package jmri.jmrit.ctc.ctcserialdata;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.ButtonGroup;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
public class OtherData implements Serializable {
    private final static int FILE_VERSION = 0;
    
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
    
    public static ArrayList<Field> getAllInternalSensorStringFields() {
        return ProjectsCommonSubs.getAllPartialVariableNameStringFields(INTERNAL_SENSOR, OtherData.class.getFields());
    }
    
//  Duplicates get ONLY ONE entry in the set (obviously).    
    @SuppressFBWarnings(value = "DE_MIGHT_IGNORE", justification = "Just skip problem fields is fine")
    public HashSet<String> getAllInternalSensors() {
        HashSet<String> returnValue = new HashSet<>();
        ArrayList<Field> fields = getAllInternalSensorStringFields();
        for (Field field : fields) {
            try {
                returnValue.add((String)field.get(this));
            } catch (IllegalArgumentException | IllegalAccessException ex) {}
        }
        return returnValue;
    }
/*    
Because of "getAllInternalSensorStringFields", ANY JMRI sensor object that we
create should have "InternalSensor" (case sensitive) as ANY PART of their
variable name and declared as type String.
*/    
    private static final String INTERNAL_SENSOR = "InternalSensor";             // NOI18N
//  Version of this file for supporting upgrade paths from prior versions:
    public int      _mFileVersion;
//  Fleeting:
    public String   _mFleetingToggleInternalSensor;
    public boolean  _mDefaultFleetingEnabled;
//  Global startup:
    public boolean  _mTUL_EnabledAtStartup = true;
    public SIGNAL_SYSTEM_TYPE _mSignalSystemType;
    public int      _mTUL_SecondsToLockTurnouts = 0;
//  Next unique # for each created Column:    
    public int      _mNextUniqueNumber = 0;
//  CTC Debugging:    
    public String   _mCTCDebugSystemReloadInternalSensor;
    public String   _mCTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor;
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
        _mFleetingToggleInternalSensor = "IS:FLEETING";                                 // NOI18N
        _mDefaultFleetingEnabled = false;
        _mSignalSystemType = SIGNAL_SYSTEM_TYPE.SIGNALMAST;
        _mCTCDebugSystemReloadInternalSensor = "IS:RELOADCTC";                          // NOI18N
        _mCTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor = "IS:DEBUGCTC";    // NOI18N
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
    
    public OtherData deepCopy() {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(this);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return (OtherData)objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException ex) { return null;}
    }

//  Figure out if we need to convert from prior verion(s) (As of 2/20/19, no):
    public void upgradeSelf() {
        if (_mFileVersion == FILE_VERSION) { _mFileVersion = FILE_VERSION; }    // Get around complaints by Travis.
//  I had to get rid of this stub, because SpotBugs complained:         
/*        
        for (int oldVersion = _mFileVersion; oldVersion < FILE_VERSION; oldVersion++) {
            switch(oldVersion) {
                case 0:
                    break;
            }
        }
*/
    }
}
