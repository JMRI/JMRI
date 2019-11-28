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
import javax.swing.ButtonGroup;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
public class CodeButtonHandlerData implements Serializable, Comparable<CodeButtonHandlerData> {
    private final static int FILE_VERSION = 5;
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
//  PRESENTLY (as of 10/18/18) these are ONLY used by the edit routines to TEMPORARILY get a copy.  The
//  data is NEVER stored anywhere.  I say this because "_mUniqueID" MUST have another unique number if it is EVER
//  stored anywhere!  For example: take the source # and add 5,000,000 to it each time.  Even copies of copies would
//  get unique numbers!  If the user ever creates 5,000,000 objects, they must be GOD!
    public CodeButtonHandlerData deepCopy() {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(this);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return (CodeButtonHandlerData)objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) { return null;}
    }

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
    public int                  _mFileVersion;
//  Data used by the runtime (JMRI) and Editor systems:
    public String               _mCodeButtonInternalSensor;
    public String               _mOSSectionOccupiedExternalSensor;              // Required
    public String               _mOSSectionOccupiedExternalSensor2;             // Optional
    public int                  _mOSSectionSwitchSlavedToUniqueID;
    public int                  _mGUIColumnNumber;
    public boolean              _mGUIGeneratedAtLeastOnceAlready;
    public int                  _mCodeButtonDelayTime;
//  Signal Direction Indicators:
    public boolean              _mSIDI_Enabled;
    public String               _mSIDI_LeftInternalSensor;
    public String               _mSIDI_NormalInternalSensor;
    public String               _mSIDI_RightInternalSensor;
    public int                  _mSIDI_CodingTimeInMilliseconds;
    public int                  _mSIDI_TimeLockingTimeInMilliseconds;
    public String               _mSIDI_LeftRightTrafficSignalsCSVList;
    public String               _mSIDI_RightLeftTrafficSignalsCSVList;
//  Signal Direction Lever:
    public boolean              _mSIDL_Enabled;
    public String               _mSIDL_LeftInternalSensor;
    public String               _mSIDL_NormalInternalSensor;
    public String               _mSIDL_RightInternalSensor;
//  Switch Direction Indicators:
    public boolean              _mSWDI_Enabled;
    public String               _mSWDI_NormalInternalSensor;
    public String               _mSWDI_ReversedInternalSensor;
    public String               _mSWDI_ExternalTurnout;
    public int                  _mSWDI_CodingTimeInMilliseconds;
    public boolean              _mSWDI_FeedbackDifferent;
    public TURNOUT_TYPE         _mSWDI_GUITurnoutType;
    public boolean              _mSWDI_GUITurnoutLeftHand;
    public boolean              _mSWDI_GUICrossoverLeftHand;
//  Switch Direction Lever:
    public boolean              _mSWDL_Enabled;
    public String               _mSWDL_InternalSensor;
//  Call On:
    public boolean              _mCO_Enabled;
    public String               _mCO_CallOnToggleInternalSensor;
    public String               _mCO_GroupingsListString;
//  Traffic Locking:
    public boolean              _mTRL_Enabled;
    public String               _mTRL_LeftTrafficLockingRulesSSVList;
    public String               _mTRL_RightTrafficLockingRulesSSVList;
//  Turnout Locking:
    public boolean              _mTUL_Enabled;
    public String               _mTUL_DispatcherInternalSensorLockToggle;
    public String               _mTUL_ExternalTurnout;
    public boolean              _mTUL_ExternalTurnoutFeedbackDifferent;
    public String               _mTUL_DispatcherInternalSensorUnlockedIndicator;
    public boolean              _mTUL_NoDispatcherControlOfSwitch;
    public boolean              _mTUL_ndcos_WhenLockedSwitchStateIsClosed;
    public LOCK_IMPLEMENTATION  _mTUL_LockImplementation;
    public String               _mTUL_AdditionalExternalTurnout1;
    public boolean              _mTUL_AdditionalExternalTurnout1FeedbackDifferent;
    public String               _mTUL_AdditionalExternalTurnout2;
    public boolean              _mTUL_AdditionalExternalTurnout2FeedbackDifferent;
    public String               _mTUL_AdditionalExternalTurnout3;
    public boolean              _mTUL_AdditionalExternalTurnout3FeedbackDifferent;
//  Indication Locking (Signals):
    public boolean              _mIL_Enabled;
    public String               _mIL_ListOfCSVSignalNames;

    public void upgradeSelf() {
        for (int oldVersion = _mFileVersion; oldVersion < FILE_VERSION; oldVersion++) {
            switch(oldVersion) {
                case 0:     // 0->1: Get rid of ALL Traffic locking stuff.  Incompatible with prior version.
                case 1:     // 1->2: Get rid of ALL Traffic locking stuff.  Incompatible with prior version.
                case 2:     // 2->3: Get rid of ALL Traffic locking stuff.  Incompatible with prior version.
                case 3:     // 3->4: Get rid of ALL Traffic locking stuff.  Incompatible with prior version.
                    _mTRL_Enabled = false;
                    _mTRL_LeftTrafficLockingRulesSSVList = "";
                    _mTRL_RightTrafficLockingRulesSSVList = "";
                    break;
                default:
                    break;
            }
        }
        _mFileVersion = FILE_VERSION;       // Now at this version
    }

/*  When I change a variable name but want to keep the contents, I need to
    pre-process the file BEFORE I turn it over to serialization.  That is done
    here:

    NOTE:

    This is ALWAYS done BEFORE the normal "upgradeSelf"!  And if it matches a version to change,
    it ALWAYS increments the file version by one!  Therefore "upgradeSelf" will see one greater!
    So if you want to do BOTH, then you need to increase file version by 2, and insure that the
    first increment is processed by this:
*/
    private final static String FILE_VERSION_STRING = "<string>_mFileVersion</string>"; // NOI18N
    private final static String LESS_THAN_SIGN = "<";                                   // NOI18N
    private static final String TEMPORARY_EXTENSION = ".xmlTMP";                        // NOI18N

//  Regarding "@SuppressFBWarnings": My attitude is that if the input file is screwed up, do nothing!:
    @SuppressFBWarnings(value = "NP_IMMEDIATE_DEREFERENCE_OF_READLINE", justification = "I'm already catching 'NullPointerException', it's ok!")
    static public void preprocessingUpgradeSelf(String filename) {
//  First, get the existing _mFileVersion from the file to see if we need to work on it:
        int fileVersion = -1;       // Indicate none found.
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filename))) {
            String aLine;
            while (!(aLine = bufferedReader.readLine()).contains(FILE_VERSION_STRING)) {}   // Skip to the line IF it exists.
            bufferedReader.readLine();  // Ignore <void method="set">
            bufferedReader.readLine();  // Ignore <object idref="CodeButtonHandlerData18"/>
            aLine = bufferedReader.readLine().trim();  // Get something like <int>4</int>
            if (aLine.startsWith(INT_START_STRING)) {
                aLine = aLine.substring(5);     // Get rid of it.
                fileVersion = Integer.parseInt(aLine.substring(0, aLine.indexOf(LESS_THAN_SIGN)));
            }
        } catch (IOException | NumberFormatException | NullPointerException e) {}
        if (fileVersion < 0) return;    // Safety: Nothing found, ignore it (though we should have found and parsed it!)
        switch (fileVersion) {
            case 4:
                upgradeVersion4FileTo5(filename);
                break;
            default:
                break;
        }
    }

    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", justification = "Any problems, I don't care, it's too late by this point")
    static private void upgradeVersion4FileTo5(String filename) {
        String temporaryFilename = ProjectsCommonSubs.changeExtensionTo(filename, TEMPORARY_EXTENSION);
        (new File(temporaryFilename)).delete();   // Just delete it for safety before we start:
        boolean hadAChange = false;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filename)); BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(temporaryFilename))) {
            String aLine = null;
            while ((aLine = bufferedReader.readLine()) != null) { // Not EOF:
                if ((aLine = checkFileVersion(bufferedReader, bufferedWriter, aLine, "4", "5")) == null) { hadAChange = true; continue; } // Was processed.
                if ((aLine = checkForRefactor(bufferedWriter, aLine, "_mSWDI_ActualTurnout",                      "_mSWDI_ExternalTurnout")) == null) { hadAChange = true; continue; }  // NOI18N Was processed.
                if ((aLine = checkForRefactor(bufferedWriter, aLine, "_mTUL_ActualTurnout",                       "_mTUL_ExternalTurnout")) == null) { hadAChange = true; continue; }  // NOI18N Was processed.
                if ((aLine = checkForRefactor(bufferedWriter, aLine, "_mTUL_ActualTurnoutFeedbackDifferent",      "_mTUL_ExternalTurnoutFeedbackDifferent")) == null) { hadAChange = true; continue; }  // NOI18N Was processed.
                if ((aLine = checkForRefactor(bufferedWriter, aLine, "_mTUL_AdditionalTurnout1",                  "_mTUL_AdditionalExternalTurnout1")) == null) { hadAChange = true; continue; }  // NOI18N Was processed.
                if ((aLine = checkForRefactor(bufferedWriter, aLine, "_mTUL_AdditionalTurnout1FeedbackDifferent", "_mTUL_AdditionalExternalTurnout1FeedbackDifferent")) == null) { hadAChange = true; continue; }  // NOI18N Was processed.
                if ((aLine = checkForRefactor(bufferedWriter, aLine, "_mTUL_AdditionalTurnout2",                  "_mTUL_AdditionalExternalTurnout2")) == null) { hadAChange = true; continue; }  // NOI18N Was processed.
                if ((aLine = checkForRefactor(bufferedWriter, aLine, "_mTUL_AdditionalTurnout2FeedbackDifferent", "_mTUL_AdditionalExternalTurnout2FeedbackDifferent")) == null) { hadAChange = true; continue; }  // NOI18N Was processed.
                if ((aLine = checkForRefactor(bufferedWriter, aLine, "_mTUL_AdditionalTurnout3",                  "_mTUL_AdditionalExternalTurnout3")) == null) { hadAChange = true; continue; }  // NOI18N Was processed.
                if ((aLine = checkForRefactor(bufferedWriter, aLine, "_mTUL_AdditionalTurnout3FeedbackDifferent", "_mTUL_AdditionalExternalTurnout3FeedbackDifferent")) == null) { hadAChange = true; continue; }  // NOI18N Was processed.
                writeLine(bufferedWriter, aLine);
            }
//  Regarding commented out code (due to SpotBugs):
//  I'm a safety "nut".  I will do such things in case other code is someday inserted
//  between the above "check for != null" and here.  But to satisfy SpotBugs:
            if (/*aLine == null && */hadAChange) { // Do the two step:
                bufferedReader.close();
                bufferedWriter.close();
                File oldFile = new File(filename);
                oldFile.delete();                   // Delete existing old file.
                (new File(temporaryFilename)).renameTo(oldFile);    // Rename temporary filename to proper final file.
            }
        } catch (IOException e) {}  // Any other error(s) just cleans up:
        (new File(temporaryFilename)).delete();        // If we get here, just clean up.
    }

/*
    Returns:    null if we processed it or it was the wrong format, and in either case WROTE the line(s) out indicating that we handled it.
        or
                The original aLine passed and NOTHING written, so that other(s) can check it further.
*/
    private final static String INT_START_STRING = "<int>"; // NOI18N
    private final static String INT_END_STRING = "</int>";  // NOI18N
    static private String checkFileVersion(BufferedReader bufferedReader, BufferedWriter bufferedWriter, String aLine, String oldVersion, String newVersion) throws IOException {
        if (aLine.contains(FILE_VERSION_STRING)) {
            writeLine(bufferedWriter, aLine);
            writeLine(bufferedWriter, bufferedReader.readLine());   // Ignore <void method="set">
            writeLine(bufferedWriter, bufferedReader.readLine());   // Ignore <object idref="CodeButtonHandlerData18"/>
            aLine = bufferedReader.readLine();  // Get something like <int>4</int>
            if (aLine != null) {
                int intStart = aLine.indexOf(INT_START_STRING + oldVersion + INT_END_STRING);
                if (intStart >= 0) { // Found, replace:
                    writeLine(bufferedWriter, aLine.substring(0, intStart) + INT_START_STRING + newVersion + INT_END_STRING);
                } else {
                    writeLine(bufferedWriter, aLine);
                }
            }
            return null;
        }
        return aLine;   // Line wasn't for us!
    }

    private final static String STRING_START_STRING = "<string>";   // NOI18N
    private final static String STRING_END_STRING = "</string>";    // NOI18N
    static private String checkForRefactor(BufferedWriter bufferedWriter, String aLine, String oldName, String newName) throws IOException {
        int intStart = aLine.indexOf(STRING_START_STRING + oldName + STRING_END_STRING);
        if (intStart >= 0) { // Found, replace:
            writeLine(bufferedWriter, aLine.substring(0, intStart) + STRING_START_STRING + newName + STRING_END_STRING);
            return null;
        }
        return aLine;
    }

    static private void writeLine(BufferedWriter bufferedWriter, String aLine) throws IOException {
        bufferedWriter.write(aLine); bufferedWriter.newLine();
    }
}
