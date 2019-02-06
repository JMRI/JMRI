package code;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 * 
 * This just maintains all of the properties a user can create.  Stupid simple.
 */
public class ProgramProperties {
    private static final String PROPERTIES_FILENAME = "ProgramProperties.xml";
    
    public static final String FILENAME_DEFAULT = "CTCSystem.xml";
    private static final String FILENAME_KEY = "_mFilename";
    public String _mFilename = FILENAME_DEFAULT;
    private static final String CODE_BUTTON_INTERNAL_SENSOR_PATTERN = "_mCodeButtonInternalSensorPattern";
    private static final String CODE_BUTTON_INTERNAL_SENSOR_PATTERN_DEFAULT = "IS#:CB";
    public String _mCodeButtonInternalSensorPattern = CODE_BUTTON_INTERNAL_SENSOR_PATTERN_DEFAULT;
    private static final String SIDI_CODING_TIME_IN_MILLISECONDS = "_mSIDI_CodingAndResponseTime";
    private static final String SIDI_CODING_TIME_IN_MILLISECONDS_DEFAULT = Integer.toString(2000);
    public int _mSIDI_CodingTimeInMilliseconds = Integer.parseInt(SIDI_CODING_TIME_IN_MILLISECONDS_DEFAULT);
    private static final String SIDI_LEFT_INTERNAL_SENSOR_PATTERN = "_mSIDI_LeftInternalSensorPattern";
    private static final String SIDI_LEFT_INTERNAL_SENSOR_PATTERN_DEFAULT = "IS#:LDGK";
    public String _mSIDI_LeftInternalSensorPattern = SIDI_LEFT_INTERNAL_SENSOR_PATTERN_DEFAULT;
    private static final String SIDI_NORMAL_INTERNAL_SENSOR_PATTERN = "_mSIDI_NormalInternalSensorPattern";
    private static final String SIDI_NORMAL_INTERNAL_SENSOR_PATTERN_DEFAULT = "IS#:NGK";
    public String _mSIDI_NormalInternalSensorPattern = SIDI_NORMAL_INTERNAL_SENSOR_PATTERN_DEFAULT;
    private static final String SIDI_RIGHT_INTERNAL_SENSOR_PATTERN = "_mSIDI_RightInternalSensorPattern";
    private static final String SIDI_RIGHT_INTERNAL_SENSOR_PATTERN_DEFAULT = "IS#:RDGK";
    public String _mSIDI_RightInternalSensorPattern = SIDI_RIGHT_INTERNAL_SENSOR_PATTERN_DEFAULT;
    private static final String SIDI_TIME_LOCKING_TIME_IN_MILLISECONDS = "_mSIDI_TimeLockingTimeInMilliseconds";
    private static final String SIDI_TIME_LOCKING_TIME_IN_MILLISECONDS_DEFAULT = Integer.toString(3000);
    public int _mSIDI_TimeLockingTimeInMilliseconds = Integer.parseInt(SIDI_TIME_LOCKING_TIME_IN_MILLISECONDS_DEFAULT);
    private static final String SIDL_LEFT_INTERNAL_SENSOR_PATTERN = "_mSIDL_LeftInternalSensorPattern";
    private static final String SIDL_LEFT_INTERNAL_SENSOR_PATTERN_DEFAULT = "IS#:LDGL";
    public String _mSIDL_LeftInternalSensorPattern = SIDL_LEFT_INTERNAL_SENSOR_PATTERN_DEFAULT;
    private static final String SIDL_NORMAL_INTERNAL_SENSOR_PATTERN = "_mSIDL_NormalInternalSensorPattern";
    private static final String SIDL_NORMAL_INTERNAL_SENSOR_PATTERN_DEFAULT = "IS#:NGL";
    public String _mSIDL_NormalInternalSensorPattern = SIDL_NORMAL_INTERNAL_SENSOR_PATTERN_DEFAULT;
    private static final String SIDL_RIGHT_INTERNAL_SENSOR_PATTERN = "_mSIDL_RightInternalSensorPattern";
    private static final String SIDL_RIGHT_INTERNAL_SENSOR_PATTERN_DEFAULT = "IS#:RDGL";
    public String _mSIDL_RightInternalSensorPattern = SIDL_RIGHT_INTERNAL_SENSOR_PATTERN_DEFAULT;
    private static final String SWDI_CODING_TIME_IN_MILLISECONDS = "_mSWDI_CodingTimeInMilliseconds";    
    private static final String SWDI_CODING_TIME_IN_MILLISECONDS_DEFAULT = Integer.toString(2000);
    public int _mSWDI_CodingTimeInMilliseconds = Integer.parseInt(SWDI_CODING_TIME_IN_MILLISECONDS_DEFAULT);
    private static final String SWDI_NORMAL_INTERNAL_SENSOR_PATTERN = "_mSWDI_NormalInternalSensorPattern";
    private static final String SWDI_NORMAL_INTERNAL_SENSOR_PATTERN_DEFAULT = "IS#:SWNI";
    public String _mSWDI_NormalInternalSensorPattern = SWDI_NORMAL_INTERNAL_SENSOR_PATTERN_DEFAULT;
    private static final String SWDI_REVERSED_INTERNAL_SENSOR_PATTERN = "_mSWDI_ReversedInternalSensorPattern";
    private static final String SWDI_REVERSED_INTERNAL_SENSOR_PATTERN_DEFAULT = "IS#:SWRI";
    public String _mSWDI_ReversedInternalSensorPattern = SWDI_REVERSED_INTERNAL_SENSOR_PATTERN_DEFAULT;
    private static final String SWDL_INTERNAL_SENSOR_PATTERN = "_mSWDL_InternalSensorPattern";
    private static final String SWDL_INTERNAL_SENSOR_PATTERN_DEFAULT = "IS#:LEVER";
    public String _mSWDL_InternalSensorPattern = SWDL_INTERNAL_SENSOR_PATTERN_DEFAULT;
    private static final String CO_CALL_ON_TOGGLE_INTERNAL_SENSOR_PATTERN = "_mCO_CallOnToggleInternalSensorPattern";
    private static final String CO_CALL_ON_TOGGLE_INTERNAL_SENSOR_PATTERN_DEFAULT = "IS#:CALLON";
    public String _mCO_CallOnToggleInternalSensorPattern = CO_CALL_ON_TOGGLE_INTERNAL_SENSOR_PATTERN_DEFAULT;
    private static final String TUL_DISPATCHER_INTERNAL_SENSOR_LOCK_TOGGLE_PATTERN = "_mTUL_DispatcherInternalSensorLockTogglePattern";
    private static final String TUL_DISPATCHER_INTERNAL_SENSOR_LOCK_TOGGLE_PATTERN_DEFAULT = "IS#:LOCKTOGGLE";
    public String _mTUL_DispatcherInternalSensorLockTogglePattern = TUL_DISPATCHER_INTERNAL_SENSOR_LOCK_TOGGLE_PATTERN_DEFAULT;
    private static final String TUL_DISPATCHER_INTERNAL_SENSOR_UNLOCKED_INDICATOR_PATTERN = "_mTUL_DispatcherInternalSensorUnlockedIndicatorPattern";
    private static final String TUL_DISPATCHER_INTERNAL_SENSOR_UNLOCKED_INDICATOR_PATTERN_DEFAULT = "IS#:UNLOCKEDINDICATOR";
    public String _mTUL_DispatcherInternalSensorUnlockedIndicatorPattern = TUL_DISPATCHER_INTERNAL_SENSOR_UNLOCKED_INDICATOR_PATTERN_DEFAULT;
    private static final String NO_CODE_BUTTON_DELAY_TIME_IN_MILLISECONDS = "_mCodeButtonDelayTime";
    private static final String NO_CODE_BUTTON_DELAY_TIME_IN_MILLISECONDS_DEFAULT = Integer.toString(0);
    public int _mCodeButtonDelayTime = Integer.parseInt(NO_CODE_BUTTON_DELAY_TIME_IN_MILLISECONDS_DEFAULT);
    
    public ProgramProperties() {
        try {
            File file = new File(PROPERTIES_FILENAME);
            FileInputStream fileInputStream = new FileInputStream(file);
            Properties properties = new Properties();
            properties.loadFromXML(fileInputStream);
            _mFilename = properties.getProperty(FILENAME_KEY, FILENAME_DEFAULT);
            _mCodeButtonInternalSensorPattern = properties.getProperty(CODE_BUTTON_INTERNAL_SENSOR_PATTERN, CODE_BUTTON_INTERNAL_SENSOR_PATTERN_DEFAULT);
            _mSIDI_CodingTimeInMilliseconds = Integer.valueOf(properties.getProperty(SIDI_CODING_TIME_IN_MILLISECONDS, SIDI_CODING_TIME_IN_MILLISECONDS_DEFAULT));
            _mSIDI_LeftInternalSensorPattern = properties.getProperty(SIDI_LEFT_INTERNAL_SENSOR_PATTERN, SIDI_LEFT_INTERNAL_SENSOR_PATTERN_DEFAULT);
            _mSIDI_NormalInternalSensorPattern = properties.getProperty(SIDI_NORMAL_INTERNAL_SENSOR_PATTERN, SIDI_NORMAL_INTERNAL_SENSOR_PATTERN_DEFAULT);
            _mSIDI_RightInternalSensorPattern = properties.getProperty(SIDI_RIGHT_INTERNAL_SENSOR_PATTERN, SIDI_RIGHT_INTERNAL_SENSOR_PATTERN_DEFAULT);
            _mSIDI_TimeLockingTimeInMilliseconds = Integer.valueOf(properties.getProperty(SIDI_TIME_LOCKING_TIME_IN_MILLISECONDS, SIDI_TIME_LOCKING_TIME_IN_MILLISECONDS_DEFAULT));
            _mSIDL_LeftInternalSensorPattern = properties.getProperty(SIDL_LEFT_INTERNAL_SENSOR_PATTERN, SIDL_LEFT_INTERNAL_SENSOR_PATTERN_DEFAULT);
            _mSIDL_NormalInternalSensorPattern = properties.getProperty(SIDL_NORMAL_INTERNAL_SENSOR_PATTERN, SIDL_NORMAL_INTERNAL_SENSOR_PATTERN_DEFAULT);
            _mSIDL_RightInternalSensorPattern = properties.getProperty(SIDL_RIGHT_INTERNAL_SENSOR_PATTERN, SIDL_RIGHT_INTERNAL_SENSOR_PATTERN_DEFAULT);
            _mSWDI_CodingTimeInMilliseconds = Integer.valueOf(properties.getProperty(SWDI_CODING_TIME_IN_MILLISECONDS, SWDI_CODING_TIME_IN_MILLISECONDS_DEFAULT));
            _mSWDI_NormalInternalSensorPattern = properties.getProperty(SWDI_NORMAL_INTERNAL_SENSOR_PATTERN, SWDI_NORMAL_INTERNAL_SENSOR_PATTERN_DEFAULT);
            _mSWDI_ReversedInternalSensorPattern = properties.getProperty(SWDI_REVERSED_INTERNAL_SENSOR_PATTERN, SWDI_REVERSED_INTERNAL_SENSOR_PATTERN_DEFAULT);
            _mSWDL_InternalSensorPattern = properties.getProperty(SWDL_INTERNAL_SENSOR_PATTERN, SWDL_INTERNAL_SENSOR_PATTERN_DEFAULT);
            _mCO_CallOnToggleInternalSensorPattern = properties.getProperty(CO_CALL_ON_TOGGLE_INTERNAL_SENSOR_PATTERN, CO_CALL_ON_TOGGLE_INTERNAL_SENSOR_PATTERN_DEFAULT);
            _mTUL_DispatcherInternalSensorLockTogglePattern = properties.getProperty(TUL_DISPATCHER_INTERNAL_SENSOR_LOCK_TOGGLE_PATTERN, TUL_DISPATCHER_INTERNAL_SENSOR_LOCK_TOGGLE_PATTERN_DEFAULT);
            _mTUL_DispatcherInternalSensorUnlockedIndicatorPattern = properties.getProperty(TUL_DISPATCHER_INTERNAL_SENSOR_UNLOCKED_INDICATOR_PATTERN, TUL_DISPATCHER_INTERNAL_SENSOR_UNLOCKED_INDICATOR_PATTERN_DEFAULT);
            _mCodeButtonDelayTime = Integer.valueOf(properties.getProperty(NO_CODE_BUTTON_DELAY_TIME_IN_MILLISECONDS, NO_CODE_BUTTON_DELAY_TIME_IN_MILLISECONDS_DEFAULT));
        }
        catch (IOException | NumberFormatException e) {
            _mFilename = FILENAME_DEFAULT;
            _mCodeButtonInternalSensorPattern = CODE_BUTTON_INTERNAL_SENSOR_PATTERN_DEFAULT;
            _mSIDI_CodingTimeInMilliseconds = Integer.valueOf(SIDI_CODING_TIME_IN_MILLISECONDS_DEFAULT);
            _mSIDI_LeftInternalSensorPattern = SIDI_LEFT_INTERNAL_SENSOR_PATTERN_DEFAULT;
            _mSIDI_NormalInternalSensorPattern = SIDI_NORMAL_INTERNAL_SENSOR_PATTERN_DEFAULT;
            _mSIDI_RightInternalSensorPattern = SIDI_RIGHT_INTERNAL_SENSOR_PATTERN_DEFAULT;
            _mSIDI_TimeLockingTimeInMilliseconds = Integer.valueOf(SIDI_TIME_LOCKING_TIME_IN_MILLISECONDS_DEFAULT);
            _mSIDL_LeftInternalSensorPattern = SIDL_LEFT_INTERNAL_SENSOR_PATTERN_DEFAULT;
            _mSIDL_NormalInternalSensorPattern = SIDL_NORMAL_INTERNAL_SENSOR_PATTERN_DEFAULT;
            _mSIDL_RightInternalSensorPattern = SIDL_RIGHT_INTERNAL_SENSOR_PATTERN_DEFAULT;
            _mSWDI_CodingTimeInMilliseconds = Integer.valueOf(SWDI_CODING_TIME_IN_MILLISECONDS_DEFAULT);
            _mSWDI_NormalInternalSensorPattern = SWDI_NORMAL_INTERNAL_SENSOR_PATTERN_DEFAULT;
            _mSWDI_ReversedInternalSensorPattern = SWDI_REVERSED_INTERNAL_SENSOR_PATTERN_DEFAULT;
            _mSWDL_InternalSensorPattern = SWDL_INTERNAL_SENSOR_PATTERN_DEFAULT;
            _mCO_CallOnToggleInternalSensorPattern = CO_CALL_ON_TOGGLE_INTERNAL_SENSOR_PATTERN_DEFAULT;
            _mTUL_DispatcherInternalSensorLockTogglePattern = TUL_DISPATCHER_INTERNAL_SENSOR_LOCK_TOGGLE_PATTERN_DEFAULT;
            _mTUL_DispatcherInternalSensorUnlockedIndicatorPattern = TUL_DISPATCHER_INTERNAL_SENSOR_UNLOCKED_INDICATOR_PATTERN_DEFAULT;
            _mCodeButtonDelayTime = Integer.valueOf(NO_CODE_BUTTON_DELAY_TIME_IN_MILLISECONDS_DEFAULT);
        }
    }
    
    static public boolean programPropertiesFileExists() {
        return (new File(PROPERTIES_FILENAME)).isFile();
    }
    
    public void close() {
        try {
            Properties properties = new Properties();
            properties.setProperty(FILENAME_KEY, _mFilename);
            properties.setProperty(CODE_BUTTON_INTERNAL_SENSOR_PATTERN, _mCodeButtonInternalSensorPattern);
            properties.setProperty(SIDI_CODING_TIME_IN_MILLISECONDS, Integer.toString(_mSIDI_CodingTimeInMilliseconds));
            properties.setProperty(SIDI_LEFT_INTERNAL_SENSOR_PATTERN, _mSIDI_LeftInternalSensorPattern);
            properties.setProperty(SIDI_NORMAL_INTERNAL_SENSOR_PATTERN, _mSIDI_NormalInternalSensorPattern);
            properties.setProperty(SIDI_RIGHT_INTERNAL_SENSOR_PATTERN, _mSIDI_RightInternalSensorPattern);
            properties.setProperty(SIDI_TIME_LOCKING_TIME_IN_MILLISECONDS, Integer.toString(_mSIDI_TimeLockingTimeInMilliseconds));
            properties.setProperty(SIDL_LEFT_INTERNAL_SENSOR_PATTERN, _mSIDL_LeftInternalSensorPattern);
            properties.setProperty(SIDL_NORMAL_INTERNAL_SENSOR_PATTERN, _mSIDL_NormalInternalSensorPattern);
            properties.setProperty(SIDL_RIGHT_INTERNAL_SENSOR_PATTERN, _mSIDL_RightInternalSensorPattern);
            properties.setProperty(SWDI_CODING_TIME_IN_MILLISECONDS, Integer.toString(_mSWDI_CodingTimeInMilliseconds));
            properties.setProperty(SWDI_NORMAL_INTERNAL_SENSOR_PATTERN, _mSWDI_NormalInternalSensorPattern);
            properties.setProperty(SWDI_REVERSED_INTERNAL_SENSOR_PATTERN, _mSWDI_ReversedInternalSensorPattern);
            properties.setProperty(SWDL_INTERNAL_SENSOR_PATTERN, _mSWDL_InternalSensorPattern);
            properties.setProperty(CO_CALL_ON_TOGGLE_INTERNAL_SENSOR_PATTERN, _mCO_CallOnToggleInternalSensorPattern);
            properties.setProperty(TUL_DISPATCHER_INTERNAL_SENSOR_LOCK_TOGGLE_PATTERN, _mTUL_DispatcherInternalSensorLockTogglePattern);
            properties.setProperty(TUL_DISPATCHER_INTERNAL_SENSOR_UNLOCKED_INDICATOR_PATTERN, _mTUL_DispatcherInternalSensorUnlockedIndicatorPattern);  
            properties.setProperty(NO_CODE_BUTTON_DELAY_TIME_IN_MILLISECONDS, Integer.toString(_mCodeButtonDelayTime));
            File file = new File(PROPERTIES_FILENAME);
            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                properties.storeToXML(fileOutputStream, PROPERTIES_FILENAME);
            }            
        } catch (IOException e) {}
    }
}
