package jmri.jmrit.ctc.editor.code;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import jmri.util.FileUtil;
import jmri.jmrit.ctc.CTCFiles;

/**
 * This just maintains all of the properties a user can create. Stupid simple.
 * <p>
 * Default values are assigned when the class instance is created.  The current values
 * are loaded from the PanelPro data xml file.
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
public class ProgramProperties {

    private static final String CODE_BUTTON_INTERNAL_SENSOR_PATTERN = "_mCodeButtonInternalSensorPattern";  // NOI18N
    private static final String CODE_BUTTON_INTERNAL_SENSOR_PATTERN_DEFAULT = "IS#:CB"; // NOI18N
    public String _mCodeButtonInternalSensorPattern = CODE_BUTTON_INTERNAL_SENSOR_PATTERN_DEFAULT;
    private static final String SIDI_CODING_TIME_IN_MILLISECONDS = "_mSIDI_CodingAndResponseTime";  // NOI18N
    private static final String SIDI_CODING_TIME_IN_MILLISECONDS_DEFAULT = Integer.toString(2000);
    public int _mSIDI_CodingTimeInMilliseconds = Integer.parseInt(SIDI_CODING_TIME_IN_MILLISECONDS_DEFAULT);
    private static final String SIDI_LEFT_INTERNAL_SENSOR_PATTERN = "_mSIDI_LeftInternalSensorPattern"; // NOI18N
    private static final String SIDI_LEFT_INTERNAL_SENSOR_PATTERN_DEFAULT = "IS#:LDGK";// NOI18N
    public String _mSIDI_LeftInternalSensorPattern = SIDI_LEFT_INTERNAL_SENSOR_PATTERN_DEFAULT;
    private static final String SIDI_NORMAL_INTERNAL_SENSOR_PATTERN = "_mSIDI_NormalInternalSensorPattern";// NOI18N
    private static final String SIDI_NORMAL_INTERNAL_SENSOR_PATTERN_DEFAULT = "IS#:NGK";// NOI18N
    public String _mSIDI_NormalInternalSensorPattern = SIDI_NORMAL_INTERNAL_SENSOR_PATTERN_DEFAULT;
    private static final String SIDI_RIGHT_INTERNAL_SENSOR_PATTERN = "_mSIDI_RightInternalSensorPattern";// NOI18N
    private static final String SIDI_RIGHT_INTERNAL_SENSOR_PATTERN_DEFAULT = "IS#:RDGK";// NOI18N
    public String _mSIDI_RightInternalSensorPattern = SIDI_RIGHT_INTERNAL_SENSOR_PATTERN_DEFAULT;
    private static final String SIDI_TIME_LOCKING_TIME_IN_MILLISECONDS = "_mSIDI_TimeLockingTimeInMilliseconds";// NOI18N
    private static final String SIDI_TIME_LOCKING_TIME_IN_MILLISECONDS_DEFAULT = Integer.toString(3000);
    public int _mSIDI_TimeLockingTimeInMilliseconds = Integer.parseInt(SIDI_TIME_LOCKING_TIME_IN_MILLISECONDS_DEFAULT);
    private static final String SIDL_LEFT_INTERNAL_SENSOR_PATTERN = "_mSIDL_LeftInternalSensorPattern";// NOI18N
    private static final String SIDL_LEFT_INTERNAL_SENSOR_PATTERN_DEFAULT = "IS#:LDGL";// NOI18N
    public String _mSIDL_LeftInternalSensorPattern = SIDL_LEFT_INTERNAL_SENSOR_PATTERN_DEFAULT;
    private static final String SIDL_NORMAL_INTERNAL_SENSOR_PATTERN = "_mSIDL_NormalInternalSensorPattern";// NOI18N
    private static final String SIDL_NORMAL_INTERNAL_SENSOR_PATTERN_DEFAULT = "IS#:NGL";// NOI18N
    public String _mSIDL_NormalInternalSensorPattern = SIDL_NORMAL_INTERNAL_SENSOR_PATTERN_DEFAULT;
    private static final String SIDL_RIGHT_INTERNAL_SENSOR_PATTERN = "_mSIDL_RightInternalSensorPattern";// NOI18N
    private static final String SIDL_RIGHT_INTERNAL_SENSOR_PATTERN_DEFAULT = "IS#:RDGL";// NOI18N
    public String _mSIDL_RightInternalSensorPattern = SIDL_RIGHT_INTERNAL_SENSOR_PATTERN_DEFAULT;
    private static final String SWDI_CODING_TIME_IN_MILLISECONDS = "_mSWDI_CodingTimeInMilliseconds";// NOI18N
    private static final String SWDI_CODING_TIME_IN_MILLISECONDS_DEFAULT = Integer.toString(2000);
    public int _mSWDI_CodingTimeInMilliseconds = Integer.parseInt(SWDI_CODING_TIME_IN_MILLISECONDS_DEFAULT);
    private static final String SWDI_NORMAL_INTERNAL_SENSOR_PATTERN = "_mSWDI_NormalInternalSensorPattern";// NOI18N
    private static final String SWDI_NORMAL_INTERNAL_SENSOR_PATTERN_DEFAULT = "IS#:SWNI";// NOI18N
    public String _mSWDI_NormalInternalSensorPattern = SWDI_NORMAL_INTERNAL_SENSOR_PATTERN_DEFAULT;
    private static final String SWDI_REVERSED_INTERNAL_SENSOR_PATTERN = "_mSWDI_ReversedInternalSensorPattern";// NOI18N
    private static final String SWDI_REVERSED_INTERNAL_SENSOR_PATTERN_DEFAULT = "IS#:SWRI";// NOI18N
    public String _mSWDI_ReversedInternalSensorPattern = SWDI_REVERSED_INTERNAL_SENSOR_PATTERN_DEFAULT;
    private static final String SWDL_INTERNAL_SENSOR_PATTERN = "_mSWDL_InternalSensorPattern";// NOI18N
    private static final String SWDL_INTERNAL_SENSOR_PATTERN_DEFAULT = "IS#:LEVER";// NOI18N
    public String _mSWDL_InternalSensorPattern = SWDL_INTERNAL_SENSOR_PATTERN_DEFAULT;
    private static final String CO_CALL_ON_TOGGLE_INTERNAL_SENSOR_PATTERN = "_mCO_CallOnToggleInternalSensorPattern";// NOI18N
    private static final String CO_CALL_ON_TOGGLE_INTERNAL_SENSOR_PATTERN_DEFAULT = "IS#:CALLON";// NOI18N
    public String _mCO_CallOnToggleInternalSensorPattern = CO_CALL_ON_TOGGLE_INTERNAL_SENSOR_PATTERN_DEFAULT;
    private static final String TUL_DISPATCHER_INTERNAL_SENSOR_LOCK_TOGGLE_PATTERN = "_mTUL_DispatcherInternalSensorLockTogglePattern";// NOI18N
    private static final String TUL_DISPATCHER_INTERNAL_SENSOR_LOCK_TOGGLE_PATTERN_DEFAULT = "IS#:LOCKTOGGLE";// NOI18N
    public String _mTUL_DispatcherInternalSensorLockTogglePattern = TUL_DISPATCHER_INTERNAL_SENSOR_LOCK_TOGGLE_PATTERN_DEFAULT;
    private static final String TUL_DISPATCHER_INTERNAL_SENSOR_UNLOCKED_INDICATOR_PATTERN = "_mTUL_DispatcherInternalSensorUnlockedIndicatorPattern";// NOI18N
    private static final String TUL_DISPATCHER_INTERNAL_SENSOR_UNLOCKED_INDICATOR_PATTERN_DEFAULT = "IS#:UNLOCKEDINDICATOR";// NOI18N
    public String _mTUL_DispatcherInternalSensorUnlockedIndicatorPattern = TUL_DISPATCHER_INTERNAL_SENSOR_UNLOCKED_INDICATOR_PATTERN_DEFAULT;
    private static final String NO_CODE_BUTTON_DELAY_TIME_IN_MILLISECONDS = "_mCodeButtonDelayTime";// NOI18N
    private static final String NO_CODE_BUTTON_DELAY_TIME_IN_MILLISECONDS_DEFAULT = Integer.toString(0);
    public int _mCodeButtonDelayTime = Integer.parseInt(NO_CODE_BUTTON_DELAY_TIME_IN_MILLISECONDS_DEFAULT);

    public ProgramProperties() {
        _mCodeButtonInternalSensorPattern = CODE_BUTTON_INTERNAL_SENSOR_PATTERN_DEFAULT;
        _mSIDI_CodingTimeInMilliseconds = Integer.parseInt(SIDI_CODING_TIME_IN_MILLISECONDS_DEFAULT);
        _mSIDI_LeftInternalSensorPattern = SIDI_LEFT_INTERNAL_SENSOR_PATTERN_DEFAULT;
        _mSIDI_NormalInternalSensorPattern = SIDI_NORMAL_INTERNAL_SENSOR_PATTERN_DEFAULT;
        _mSIDI_RightInternalSensorPattern = SIDI_RIGHT_INTERNAL_SENSOR_PATTERN_DEFAULT;
        _mSIDI_TimeLockingTimeInMilliseconds = Integer.parseInt(SIDI_TIME_LOCKING_TIME_IN_MILLISECONDS_DEFAULT);
        _mSIDL_LeftInternalSensorPattern = SIDL_LEFT_INTERNAL_SENSOR_PATTERN_DEFAULT;
        _mSIDL_NormalInternalSensorPattern = SIDL_NORMAL_INTERNAL_SENSOR_PATTERN_DEFAULT;
        _mSIDL_RightInternalSensorPattern = SIDL_RIGHT_INTERNAL_SENSOR_PATTERN_DEFAULT;
        _mSWDI_CodingTimeInMilliseconds = Integer.parseInt(SWDI_CODING_TIME_IN_MILLISECONDS_DEFAULT);
        _mSWDI_NormalInternalSensorPattern = SWDI_NORMAL_INTERNAL_SENSOR_PATTERN_DEFAULT;
        _mSWDI_ReversedInternalSensorPattern = SWDI_REVERSED_INTERNAL_SENSOR_PATTERN_DEFAULT;
        _mSWDL_InternalSensorPattern = SWDL_INTERNAL_SENSOR_PATTERN_DEFAULT;
        _mCO_CallOnToggleInternalSensorPattern = CO_CALL_ON_TOGGLE_INTERNAL_SENSOR_PATTERN_DEFAULT;
        _mTUL_DispatcherInternalSensorLockTogglePattern = TUL_DISPATCHER_INTERNAL_SENSOR_LOCK_TOGGLE_PATTERN_DEFAULT;
        _mTUL_DispatcherInternalSensorUnlockedIndicatorPattern = TUL_DISPATCHER_INTERNAL_SENSOR_UNLOCKED_INDICATOR_PATTERN_DEFAULT;
        _mCodeButtonDelayTime = Integer.parseInt(NO_CODE_BUTTON_DELAY_TIME_IN_MILLISECONDS_DEFAULT);
    }

    private static final String PROPERTIES_FILENAME = "ProgramProperties.xml";  // NOI18N
    public void importExternalProgramProperties() {
        try {
            File file = CTCFiles.getFile(PROPERTIES_FILENAME);
            Properties properties = new Properties();
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                properties.loadFromXML(fileInputStream);
            }

            _mCodeButtonInternalSensorPattern = properties.getProperty(CODE_BUTTON_INTERNAL_SENSOR_PATTERN, CODE_BUTTON_INTERNAL_SENSOR_PATTERN_DEFAULT);
            _mSIDI_CodingTimeInMilliseconds = Integer.parseInt(properties.getProperty(SIDI_CODING_TIME_IN_MILLISECONDS, SIDI_CODING_TIME_IN_MILLISECONDS_DEFAULT));
            _mSIDI_LeftInternalSensorPattern = properties.getProperty(SIDI_LEFT_INTERNAL_SENSOR_PATTERN, SIDI_LEFT_INTERNAL_SENSOR_PATTERN_DEFAULT);
            _mSIDI_NormalInternalSensorPattern = properties.getProperty(SIDI_NORMAL_INTERNAL_SENSOR_PATTERN, SIDI_NORMAL_INTERNAL_SENSOR_PATTERN_DEFAULT);
            _mSIDI_RightInternalSensorPattern = properties.getProperty(SIDI_RIGHT_INTERNAL_SENSOR_PATTERN, SIDI_RIGHT_INTERNAL_SENSOR_PATTERN_DEFAULT);
            _mSIDI_TimeLockingTimeInMilliseconds = Integer.parseInt(properties.getProperty(SIDI_TIME_LOCKING_TIME_IN_MILLISECONDS, SIDI_TIME_LOCKING_TIME_IN_MILLISECONDS_DEFAULT));
            _mSIDL_LeftInternalSensorPattern = properties.getProperty(SIDL_LEFT_INTERNAL_SENSOR_PATTERN, SIDL_LEFT_INTERNAL_SENSOR_PATTERN_DEFAULT);
            _mSIDL_NormalInternalSensorPattern = properties.getProperty(SIDL_NORMAL_INTERNAL_SENSOR_PATTERN, SIDL_NORMAL_INTERNAL_SENSOR_PATTERN_DEFAULT);
            _mSIDL_RightInternalSensorPattern = properties.getProperty(SIDL_RIGHT_INTERNAL_SENSOR_PATTERN, SIDL_RIGHT_INTERNAL_SENSOR_PATTERN_DEFAULT);
            _mSWDI_CodingTimeInMilliseconds = Integer.parseInt(properties.getProperty(SWDI_CODING_TIME_IN_MILLISECONDS, SWDI_CODING_TIME_IN_MILLISECONDS_DEFAULT));
            _mSWDI_NormalInternalSensorPattern = properties.getProperty(SWDI_NORMAL_INTERNAL_SENSOR_PATTERN, SWDI_NORMAL_INTERNAL_SENSOR_PATTERN_DEFAULT);
            _mSWDI_ReversedInternalSensorPattern = properties.getProperty(SWDI_REVERSED_INTERNAL_SENSOR_PATTERN, SWDI_REVERSED_INTERNAL_SENSOR_PATTERN_DEFAULT);
            _mSWDL_InternalSensorPattern = properties.getProperty(SWDL_INTERNAL_SENSOR_PATTERN, SWDL_INTERNAL_SENSOR_PATTERN_DEFAULT);
            _mCO_CallOnToggleInternalSensorPattern = properties.getProperty(CO_CALL_ON_TOGGLE_INTERNAL_SENSOR_PATTERN, CO_CALL_ON_TOGGLE_INTERNAL_SENSOR_PATTERN_DEFAULT);
            _mTUL_DispatcherInternalSensorLockTogglePattern = properties.getProperty(TUL_DISPATCHER_INTERNAL_SENSOR_LOCK_TOGGLE_PATTERN, TUL_DISPATCHER_INTERNAL_SENSOR_LOCK_TOGGLE_PATTERN_DEFAULT);
            _mTUL_DispatcherInternalSensorUnlockedIndicatorPattern = properties.getProperty(TUL_DISPATCHER_INTERNAL_SENSOR_UNLOCKED_INDICATOR_PATTERN, TUL_DISPATCHER_INTERNAL_SENSOR_UNLOCKED_INDICATOR_PATTERN_DEFAULT);
            _mCodeButtonDelayTime = Integer.parseInt(properties.getProperty(NO_CODE_BUTTON_DELAY_TIME_IN_MILLISECONDS, NO_CODE_BUTTON_DELAY_TIME_IN_MILLISECONDS_DEFAULT));
        } catch (IOException | NumberFormatException e) {
            log.error("ProgramProperties: {}", e.getMessage());
        }
    }
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProgramProperties.class);
}
