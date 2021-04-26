package jmri.jmrit.ctc.configurexml;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.lang.reflect.Field;
import java.util.*;

// import jmri.jmrit.XmlFile;
import jmri.util.FileUtil;
import jmri.*;
import jmri.jmrit.ctc.*;
import jmri.jmrit.ctc.editor.code.*;
import jmri.jmrit.ctc.ctcserialdata.*;

/**
 * The external data was created using XMLEncoder.  The import process changes the
 * class names in the xml file and then loads the temporary classes using XMLDecoder.
 * The content from the temporary classes is then transferred and converted to the
 * real classes.
 *
 * @author Dave Sand Copyright (c) 2020
 */
public class ImportExternalData {

    static final CtcManager cm = InstanceManager.getDefault(CtcManager.class);
    static ImportOtherData _mImportOtherData;
    static ArrayList<ImportCodeButtonHandlerData> _mImportCodeButtonHandlerDataArrayList = new ArrayList<>();

    private static final String CTC_FILE_NAME = "CTCSystem.xml";        // NOI18N
    private static final String PROG_PROPS = "ProgramProperties.xml";   // NOI18N
    private static final String TEMPORARY_EXTENSION = ".xmlTMP";        // NOI18N
    private static final String SAVED_PREFIX = "V1_Save_";              // NOI18N

    public static void loadExternalData() {
        // Make an initial backup of the CTCSystem.xml file.  This only occurs on the very first run
        // This backup can be used to run version 1 CTC.
        String backName = SAVED_PREFIX + CTC_FILE_NAME;
        if (!CTCFiles.fileExists(backName)) {
            if (!CTCFiles.copyFile(CTC_FILE_NAME, backName, false)) {
                log.warn("Unable to make version 1 copy: source = {}, backup = {}", CTC_FILE_NAME, backName);
                return;
            }
        }
        backName = SAVED_PREFIX + PROG_PROPS;
        if (!CTCFiles.fileExists(backName)) {
            if (!CTCFiles.copyFile(PROG_PROPS, backName, false)) {
                log.warn("Unable to make version 1 copy: source = {}, backup = {}", PROG_PROPS, backName);
                return;
            }
        }

        cm.getProgramProperties().importExternalProgramProperties();    // Load ProgramProperties
        if (loadCTCSystemContent()) {   // Load the CTCSystem.xml file into special classes
            doDataLoading();            // Process the content
        }

        // Delete tempoary data files
        if (!CTCFiles.deleteFile(CTC_FILE_NAME)) {
            log.error("Delete failed for old CTCSystem.xml");
        }
        if (!CTCFiles.deleteFile(PROG_PROPS)) {
            log.error("Delete failed for old ProgramProperties.xml");
        }
    }

    @SuppressWarnings("unchecked") // See below comments:
    public static boolean loadCTCSystemContent() {
        String fullName = CTCFiles.getFullName(CTC_FILE_NAME);
        ImportCodeButtonHandlerData.preprocessingUpgradeSelf(fullName);     // WHOLE FILE operations FIRST.
        try {
            convertClassNameReferences(CTC_FILE_NAME);   // Change the class references
        } catch (Exception ex) {
            log.error("Exception occurred converting the class names in CTCSystem.xml: ex = {}", ex.getMessage());
            return false;
        }

        try {
            try (XMLDecoder xmlDecoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(fullName)))) {
                _mImportOtherData = (ImportOtherData) xmlDecoder.readObject();
                // triggers unchecked warning
                _mImportCodeButtonHandlerDataArrayList = (ArrayList<ImportCodeButtonHandlerData>) xmlDecoder.readObject(); // Type safety: Unchecked cast from Object to ArrayList<>
            }
        } catch (IOException e) {
            log.debug("Unable to read {}", CTC_FILE_NAME, e); // debug because missing file is not error
        }
        if (_mImportOtherData == null) {
            log.error("---------  Import failed");
            return false;
        }

        // Give each object a chance to upgrade itself BEFORE anything uses it:
        _mImportOtherData.upgradeSelf();
        for (ImportCodeButtonHandlerData codeButtonHandlerData : _mImportCodeButtonHandlerDataArrayList) {
            codeButtonHandlerData.upgradeSelf();
        }

        return true;
    }

    @SuppressFBWarnings(value = "OS_OPEN_STREAM_EXCEPTION_PATH", justification = "Low risk due to rare usage")
    static private void convertClassNameReferences(String fileName) throws Exception {
        String temporaryFilename = fileName + TEMPORARY_EXTENSION;
        int errors = 0;
        if (!CTCFiles.deleteFile(temporaryFilename)) errors++;   // Just delete it for safety before we start:
        BufferedReader bufferedReader = new BufferedReader(new FileReader(CTCFiles.getFullName(fileName)));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(CTCFiles.getFullName(temporaryFilename)));
        String aLine = null;
        while ((aLine = bufferedReader.readLine()) != null) { // Not EOF:
            aLine = aLine.replaceFirst("jmri.jmrit.ctc.ctcserialdata.", "jmri.jmrit.ctc.configurexml.Import");
            writeLine(bufferedWriter, aLine);
        }
        bufferedReader.close();
        bufferedWriter.close();
        if (!CTCFiles.deleteFile(fileName)) errors++;   // Just delete it for safety before we start:
        if (!CTCFiles.renameFile(temporaryFilename, fileName, false)) errors++;   // Just delete it for safety before we start:
        log.debug("convertClassNameReferences: errors = {}", errors);
     }

    static private void writeLine(BufferedWriter bufferedWriter, String aLine) throws IOException {
        bufferedWriter.write(aLine); bufferedWriter.newLine();
    }

    static void doDataLoading() {
        loadOtherData();
        _mImportCodeButtonHandlerDataArrayList.forEach(imp -> {
            loadCodeButtonHandlerData(imp);
        });
        convertCallOnSensorNamesToNBHSensors();
    }

    static void loadCodeButtonHandlerData(ImportCodeButtonHandlerData oldCBHD) {
    log.debug("------------- Create CBHD ------------");
        int _mUniqueID = oldCBHD._mUniqueID;
        int _mSwitchNumber = oldCBHD._mSwitchNumber;
        int _mSignalEtcNumber = oldCBHD._mSignalEtcNumber;
        int _mGUIColumnNumber = oldCBHD._mGUIColumnNumber;

        // Create a new CodeButtonHandlerData via CodeButtonHandlerDataRoutines which sets default values and empty NBH... objects
        CodeButtonHandlerData cbhd = CodeButtonHandlerDataRoutines.createNewCodeButtonHandlerData(
                _mUniqueID, _mSwitchNumber, _mSignalEtcNumber, _mGUIColumnNumber, cm.getProgramProperties());
        cm.getCTCSerialData().addCodeButtonHandlerData(cbhd);

    log.debug("------------- Code ------------");

        // Code section
        cbhd._mCodeButtonInternalSensor = loadSensor(oldCBHD._mCodeButtonInternalSensor, true);
        cbhd._mOSSectionOccupiedExternalSensor = loadSensor(oldCBHD._mOSSectionOccupiedExternalSensor, false);
        cbhd._mOSSectionOccupiedExternalSensor2 = loadSensor(oldCBHD._mOSSectionOccupiedExternalSensor2, false);
        cbhd._mOSSectionSwitchSlavedToUniqueID = oldCBHD._mOSSectionSwitchSlavedToUniqueID;
        cbhd._mGUIGeneratedAtLeastOnceAlready = oldCBHD._mGUIGeneratedAtLeastOnceAlready;
        cbhd._mCodeButtonDelayTime = oldCBHD._mCodeButtonDelayTime;

    log.debug("------------- SIDI ------------");

        // SIDI section
        cbhd._mSIDI_Enabled = oldCBHD._mSIDI_Enabled;
        cbhd._mSIDI_LeftInternalSensor = loadSensor(oldCBHD._mSIDI_LeftInternalSensor, true);
        cbhd._mSIDI_NormalInternalSensor = loadSensor(oldCBHD._mSIDI_NormalInternalSensor, true);
        cbhd._mSIDI_RightInternalSensor = loadSensor(oldCBHD._mSIDI_RightInternalSensor, true);
        cbhd._mSIDI_CodingTimeInMilliseconds = oldCBHD._mSIDI_CodingTimeInMilliseconds;
        cbhd._mSIDI_TimeLockingTimeInMilliseconds = oldCBHD._mSIDI_TimeLockingTimeInMilliseconds;
        cbhd._mSIDI_LeftRightTrafficSignals = getSignalList(oldCBHD._mSIDI_LeftRightTrafficSignalsCSVList);
        cbhd._mSIDI_RightLeftTrafficSignals = getSignalList(oldCBHD._mSIDI_RightLeftTrafficSignalsCSVList);

        // Set the traffic direction based on indicator sensors.
        String trafficDirection = "BOTH";
        if (oldCBHD._mSIDI_LeftInternalSensor.isEmpty() && !oldCBHD._mSIDI_RightInternalSensor.isEmpty()) {
            trafficDirection = "RIGHT";
        } else if (!oldCBHD._mSIDI_LeftInternalSensor.isEmpty() && oldCBHD._mSIDI_RightInternalSensor.isEmpty()) {
            trafficDirection = "LEFT";
        }
        cbhd._mSIDI_TrafficDirection = CodeButtonHandlerData.TRAFFIC_DIRECTION.valueOf(trafficDirection);

    log.debug("------------- SIDL ------------");
        // SIDL section
        cbhd._mSIDL_Enabled = oldCBHD._mSIDL_Enabled;
        cbhd._mSIDL_LeftInternalSensor = loadSensor(oldCBHD._mSIDL_LeftInternalSensor, true);
        cbhd._mSIDL_NormalInternalSensor = loadSensor(oldCBHD._mSIDL_NormalInternalSensor, true);
        cbhd._mSIDL_RightInternalSensor = loadSensor(oldCBHD._mSIDL_RightInternalSensor, true);

    log.debug("------------- SWDI ------------");
        // SWDI section
        cbhd._mSWDI_Enabled = oldCBHD._mSWDI_Enabled;
        cbhd._mSWDI_NormalInternalSensor = loadSensor(oldCBHD._mSWDI_NormalInternalSensor, true);
        cbhd._mSWDI_ReversedInternalSensor = loadSensor(oldCBHD._mSWDI_ReversedInternalSensor, true);
        cbhd._mSWDI_FeedbackDifferent = oldCBHD._mSWDI_FeedbackDifferent;
        cbhd._mSWDI_ExternalTurnout = loadTurnout(oldCBHD._mSWDI_ExternalTurnout, oldCBHD._mSWDI_FeedbackDifferent);
        cbhd._mSWDI_CodingTimeInMilliseconds = oldCBHD._mSWDI_CodingTimeInMilliseconds;
        cbhd._mSWDI_GUITurnoutType = CodeButtonHandlerData.TURNOUT_TYPE.valueOf(oldCBHD._mSWDI_GUITurnoutType.toString());
        cbhd._mSWDI_GUITurnoutLeftHand = oldCBHD._mSWDI_GUITurnoutLeftHand;
        cbhd._mSWDI_GUICrossoverLeftHand = oldCBHD._mSWDI_GUICrossoverLeftHand;

    log.debug("------------- SWDL ------------");
        // SWDL section
        cbhd._mSWDL_Enabled = oldCBHD._mSWDL_Enabled;
        cbhd._mSWDL_InternalSensor = loadSensor(oldCBHD._mSWDL_InternalSensor, true);

    log.debug("-------------  CO  ------------");
        // CO section
        cbhd._mCO_Enabled = oldCBHD._mCO_Enabled;
        cbhd._mCO_CallOnToggleInternalSensor = loadSensor(oldCBHD._mCO_CallOnToggleInternalSensor, true);
        cbhd._mCO_GroupingsList = getCallOnList(oldCBHD._mCO_GroupingsListString);

    log.debug("------------- TRL  ------------");
        // TRL section
        cbhd._mTRL_Enabled = oldCBHD._mTRL_Enabled;
        cbhd._mTRL_LeftTrafficLockingRules = getTrafficLocking(oldCBHD._mTRL_LeftTrafficLockingRulesSSVList);
        cbhd._mTRL_RightTrafficLockingRules = getTrafficLocking(oldCBHD._mTRL_RightTrafficLockingRulesSSVList);

    log.debug("------------- TUL  ------------");
        // TUL section
        cbhd._mTUL_Enabled = oldCBHD._mTUL_Enabled;
        cbhd._mTUL_DispatcherInternalSensorLockToggle = loadSensor(oldCBHD._mTUL_DispatcherInternalSensorLockToggle, true);
        cbhd._mTUL_ExternalTurnoutFeedbackDifferent = oldCBHD._mTUL_ExternalTurnoutFeedbackDifferent;
        cbhd._mTUL_ExternalTurnout = loadTurnout(oldCBHD._mTUL_ExternalTurnout, oldCBHD._mTUL_ExternalTurnoutFeedbackDifferent);
        cbhd._mTUL_DispatcherInternalSensorUnlockedIndicator = loadSensor(oldCBHD._mTUL_DispatcherInternalSensorUnlockedIndicator, true);
        cbhd._mTUL_NoDispatcherControlOfSwitch = oldCBHD._mTUL_NoDispatcherControlOfSwitch;
        cbhd._mTUL_ndcos_WhenLockedSwitchStateIsClosed = oldCBHD._mTUL_ndcos_WhenLockedSwitchStateIsClosed;

        // Use the lock toggle to set the GUI icons boolean
        cbhd._mTUL_GUI_IconsEnabled = cbhd._mTUL_DispatcherInternalSensorLockToggle.valid() ? true : false;

        cbhd._mTUL_LockImplementation = CodeButtonHandlerData.LOCK_IMPLEMENTATION.valueOf(oldCBHD._mTUL_LockImplementation.toString());

        cbhd._mTUL_AdditionalExternalTurnout1 = loadTurnout(oldCBHD._mTUL_AdditionalExternalTurnout1, oldCBHD._mTUL_AdditionalExternalTurnout1FeedbackDifferent);
        cbhd._mTUL_AdditionalExternalTurnout2 = loadTurnout(oldCBHD._mTUL_AdditionalExternalTurnout2, oldCBHD._mTUL_AdditionalExternalTurnout2FeedbackDifferent);
        cbhd._mTUL_AdditionalExternalTurnout3 = loadTurnout(oldCBHD._mTUL_AdditionalExternalTurnout3, oldCBHD._mTUL_AdditionalExternalTurnout3FeedbackDifferent);

        cbhd._mTUL_AdditionalExternalTurnout1FeedbackDifferent = oldCBHD._mTUL_AdditionalExternalTurnout1FeedbackDifferent;
        cbhd._mTUL_AdditionalExternalTurnout2FeedbackDifferent = oldCBHD._mTUL_AdditionalExternalTurnout2FeedbackDifferent;
        cbhd._mTUL_AdditionalExternalTurnout3FeedbackDifferent = oldCBHD._mTUL_AdditionalExternalTurnout3FeedbackDifferent;

    log.debug("-------------  IL  ------------");
        // IL section
        cbhd._mIL_Enabled = oldCBHD._mIL_Enabled;
        cbhd._mIL_Signals = getSignalList(oldCBHD._mIL_ListOfCSVSignalNames);

// Debugging aid -- not active due to SpotBugs
//                 log.info("CodeButtonHandlerData, {}/{}:", _mSwitchNumber, _mSignalEtcNumber);
//                 List<Field> fields = Arrays.asList(CodeButtonHandlerData.class.getFields());
//                 fields.forEach(field -> {
//                     try {
//                         log.info("    CBHD: fld = {}, type = {}, val = {}", field.getName(), field.getType(), field.get(cbhd));
//                     } catch (Exception ex) {
//                         log.info("    CBHD list exception: {}", ex.getMessage());
//                     }
//                 });
    }

    /**
     * Load the OtherData class.
     */
    static void loadOtherData() {
        OtherData od = cm.getOtherData();

//  Fleeting:
        od._mFleetingToggleInternalSensor = loadSensor(_mImportOtherData._mFleetingToggleInternalSensor, true);
        od._mDefaultFleetingEnabled = _mImportOtherData._mDefaultFleetingEnabled;

//  Global startup:
        od._mTUL_EnabledAtStartup = _mImportOtherData._mTUL_EnabledAtStartup;
        od._mSignalSystemType = OtherData.SIGNAL_SYSTEM_TYPE.valueOf(_mImportOtherData._mSignalSystemType.toString());
        od._mTUL_SecondsToLockTurnouts = _mImportOtherData._mTUL_SecondsToLockTurnouts;

//  Next unique # for each created Column:
        od._mNextUniqueNumber = _mImportOtherData._mNextUniqueNumber;

//  CTC Debugging:
        od._mCTCDebugSystemReloadInternalSensor = loadSensor(_mImportOtherData._mCTCDebugSystemReloadInternalSensor, true);
        od._mCTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor = loadSensor(_mImportOtherData._mCTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor, true);

//  GUI design:
        od._mGUIDesign_NumberOfEmptyColumnsAtEnd = _mImportOtherData._mGUIDesign_NumberOfEmptyColumnsAtEnd  ;
        od._mGUIDesign_CTCPanelType = OtherData.CTC_PANEL_TYPE.valueOf(_mImportOtherData._mGUIDesign_CTCPanelType.toString());
        od._mGUIDesign_BuilderPlate = _mImportOtherData._mGUIDesign_BuilderPlate  ;
        od._mGUIDesign_SignalsOnPanel = OtherData.SIGNALS_ON_PANEL.valueOf(_mImportOtherData._mGUIDesign_SignalsOnPanel.toString());
        od._mGUIDesign_FleetingToggleSwitch = _mImportOtherData._mGUIDesign_FleetingToggleSwitch  ;
        od._mGUIDesign_AnalogClockEtc = _mImportOtherData._mGUIDesign_AnalogClockEtc  ;
        od._mGUIDesign_ReloadCTCSystemButton = _mImportOtherData._mGUIDesign_ReloadCTCSystemButton  ;
        od._mGUIDesign_CTCDebugOnToggle = _mImportOtherData._mGUIDesign_CTCDebugOnToggle  ;
        od._mGUIDesign_CreateTrackPieces = _mImportOtherData._mGUIDesign_CreateTrackPieces  ;
        od._mGUIDesign_VerticalSize = OtherData.VERTICAL_SIZE.valueOf(_mImportOtherData._mGUIDesign_VerticalSize.toString());
        od._mGUIDesign_OSSectionUnknownInconsistentRedBlink = _mImportOtherData._mGUIDesign_OSSectionUnknownInconsistentRedBlink  ;
        od._mGUIDesign_TurnoutsOnPanel = _mImportOtherData._mGUIDesign_TurnoutsOnPanel  ;

// Debugging aid -- not active due to SpotBugs
//         log.info("OtherData:");
//         List<Field> fields = Arrays.asList(ImportOtherData.class.getFields());
//         fields.forEach(field -> {
//             try {
//                 log.info("    OtherData: fld = {}, type = {}, val = {}", field.getName(), field.getType(), field.get(_mImportOtherData));
//             } catch (Exception ex) {
//                 log.info("    OtherData list exception: {}", ex.getMessage());
//             }
//         });
    }

    // **** Load simple objects ****

//     static String loadString(String value) {
//         String newString = null;
//         if (value != null) {
//             newString = value;
//         }
//         return newString;
//     }

    static int loadInt(String value) {
        int newInt = 0;
        if (value != null) {
            try {
                newInt = Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                log.warn("loadInt format exception: value = {}", value);
            }
        }
        return newInt;
    }

//     static boolean loadBoolean(String value) {
//         boolean newBoolean = false;
//         if (value != null) {
//             newBoolean = value.equals("true") ? true : false;
//         }
//         return newBoolean;
//     }

    static NBHSensor loadSensor(String value, boolean isInternal) {
        NBHSensor sensor = null;
        if (value != null && !value.isEmpty()) {
            String sensorName = value;
            sensor = cm.getNBHSensor(sensorName);
            if (sensor == null) {
                if (isInternal) {
                    sensor = new NBHSensor("CtcManagerXml", "create internal = ", sensorName, sensorName);
                } else {
                    sensor = new NBHSensor("CtcManagerXml", "create standard = ", sensorName, sensorName, false);
                }
            }
        } else {
            sensor = new NBHSensor("CtcManagerXml", "", "Empty NBHSensor", "", true);
        }
        return sensor;
    }

    static NBHSignal loadSignal(String signalName) {
        NBHSignal signal = null;
        if (signalName != null && !signalName.isEmpty()) {
            signal = cm.getNBHSignal(signalName);
            if (signal == null) {
                signal = new NBHSignal(signalName);
            }
        } else {
            signal = new NBHSignal("");
        }
        return signal;
    }

    static NBHTurnout loadTurnout(String value, boolean feedback) {
        NBHTurnout turnout = null;
        if (value != null && !value.isEmpty()) {
            String turnoutName = value;

            turnout = cm.getNBHTurnout(turnoutName);
            if (turnout == null) {
                turnout = new NBHTurnout("CtcManagerXml", "", value, value, feedback);
            }
        } else {
            turnout = new NBHTurnout("CtcManagerXml", "Empty NBHTurnout", "");
        }
        return turnout;
    }

    static NamedBeanHandle<Block> loadBlock(String value) {
        NamedBeanHandle<Block> blockHandle = null;
        if (value != null && !value.isEmpty()) {
            blockHandle = cm.getBlock(value);
            if (blockHandle == null) {
                Block block = InstanceManager.getDefault(BlockManager.class).getBlock(value);
                if (block != null) {
                    blockHandle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(value, block);
                    cm.putBlock(value, blockHandle);
                }
            }
        }
        return blockHandle;
    }

    // **** Load ArrayList objects ****

    static ArrayList<NBHSignal> getSignalList(String value) {
        ArrayList<NBHSignal> signalList = new ArrayList<>();
        if (value != null) {
            for (String signalName : ProjectsCommonSubs.getArrayListFromCSV(value)) {
                NBHSignal signal = loadSignal(signalName);
                signalList.add(signal);
            }
        }
        return signalList;
    }

    static ArrayList<CallOnData> getCallOnList(String value) {
        ArrayList<CallOnData> callOnList = new ArrayList<>();
        for (String csvString : ProjectsCommonSubs.getArrayListFromSSV(value)) {
            CallOnData cod = new CallOnData();

            CallOnEntry entry = new CallOnEntry(csvString);
            cod._mExternalSignal = loadSignal(entry._mExternalSignal);
            cod._mSignalFacingDirection = entry._mSignalFacingDirection;
            cod._mSignalAspectToDisplay = entry._mSignalAspectToDisplay;
            cod._mCalledOnExternalSensor = loadSensor(entry._mCalledOnExternalSensor, false);
            cod._mExternalBlock = loadBlock(entry._mExternalBlock);

            cod._mSwitchIndicators = new ArrayList<>();
            cod._mSwitchIndicatorNames = getCallOnSensorNames(entry);

            callOnList.add(cod);
        }
        return callOnList;
    }

    static ArrayList<String> getCallOnSensorNames(CallOnEntry entry) {
        ArrayList<String> sensorList = new ArrayList<>();
        if (!entry._mSwitchIndicator1.isEmpty()) sensorList.add(entry._mSwitchIndicator1);
        if (!entry._mSwitchIndicator2.isEmpty()) sensorList.add(entry._mSwitchIndicator2);
        if (!entry._mSwitchIndicator3.isEmpty()) sensorList.add(entry._mSwitchIndicator3);
        if (!entry._mSwitchIndicator4.isEmpty()) sensorList.add(entry._mSwitchIndicator4);
        if (!entry._mSwitchIndicator5.isEmpty()) sensorList.add(entry._mSwitchIndicator5);
        if (!entry._mSwitchIndicator6.isEmpty()) sensorList.add(entry._mSwitchIndicator6);
        return sensorList;
    }

    static void convertCallOnSensorNamesToNBHSensors() {
        for (CodeButtonHandlerData cbhd : cm.getCTCSerialData().getCodeButtonHandlerDataArrayList()) {
            for (CallOnData cod : cbhd._mCO_GroupingsList) {
                for (String sensorName : cod._mSwitchIndicatorNames) {
                    NBHSensor sensor = cm.getNBHSensor(sensorName);
                    if (sensor != null) {
                        cod._mSwitchIndicators.add(sensor);
                    }
                }
            }
        }
    }

    static ArrayList<TrafficLockingData> getTrafficLocking(String value) {
        ArrayList<TrafficLockingData> trlData = new ArrayList<>();
        for (String csvString : ProjectsCommonSubs.getArrayListFromSSV(value)) {
            TrafficLockingData trl = new TrafficLockingData();

            TrafficLockingEntry entry = new TrafficLockingEntry(csvString);
            trl._mUserRuleNumber = entry._mUserRuleNumber;
            trl._mRuleEnabled = entry._mRuleEnabled;
            trl._mDestinationSignalOrComment = entry._mDestinationSignalOrComment;

            trl._mSwitchAlignments = getTRLSwitchList(entry);

            trl._mOccupancyExternalSensors = getTRLSensorList(entry, true);
            trl._mOptionalExternalSensors = getTRLSensorList(entry, false);

            trlData.add(trl);
        }
        return trlData;
    }

    static ArrayList<TrafficLockingData.TRLSwitch> getTRLSwitchList(TrafficLockingEntry entry) {
        ArrayList<TrafficLockingData.TRLSwitch> trlSwitches = new ArrayList<>();
        if (!entry._mUserText1.isEmpty()) trlSwitches.add(createTRLSwitch(entry._mUserText1, entry._mSwitchAlignment1, entry._mUniqueID1));
        if (!entry._mUserText2.isEmpty()) trlSwitches.add(createTRLSwitch(entry._mUserText2, entry._mSwitchAlignment2, entry._mUniqueID2));
        if (!entry._mUserText3.isEmpty()) trlSwitches.add(createTRLSwitch(entry._mUserText3, entry._mSwitchAlignment3, entry._mUniqueID3));
        if (!entry._mUserText4.isEmpty()) trlSwitches.add(createTRLSwitch(entry._mUserText4, entry._mSwitchAlignment4, entry._mUniqueID4));
        if (!entry._mUserText5.isEmpty()) trlSwitches.add(createTRLSwitch(entry._mUserText5, entry._mSwitchAlignment5, entry._mUniqueID5));
        return trlSwitches;
    }

    static TrafficLockingData.TRLSwitch createTRLSwitch(String text, String alignment, String id) {
        return new TrafficLockingData.TRLSwitch(text, alignment, loadInt(id));
    }

    static ArrayList<NBHSensor> getTRLSensorList(TrafficLockingEntry entry, boolean occupancy) {
        ArrayList<NBHSensor> sensorList = new ArrayList<>();
        if (occupancy) {
            if (!entry._mOccupancyExternalSensor1.isEmpty()) sensorList.add(loadSensor(entry._mOccupancyExternalSensor1, false));
            if (!entry._mOccupancyExternalSensor2.isEmpty()) sensorList.add(loadSensor(entry._mOccupancyExternalSensor2, false));
            if (!entry._mOccupancyExternalSensor3.isEmpty()) sensorList.add(loadSensor(entry._mOccupancyExternalSensor3, false));
            if (!entry._mOccupancyExternalSensor4.isEmpty()) sensorList.add(loadSensor(entry._mOccupancyExternalSensor4, false));
            if (!entry._mOccupancyExternalSensor5.isEmpty()) sensorList.add(loadSensor(entry._mOccupancyExternalSensor5, false));
            if (!entry._mOccupancyExternalSensor6.isEmpty()) sensorList.add(loadSensor(entry._mOccupancyExternalSensor6, false));
            if (!entry._mOccupancyExternalSensor7.isEmpty()) sensorList.add(loadSensor(entry._mOccupancyExternalSensor7, false));
            if (!entry._mOccupancyExternalSensor8.isEmpty()) sensorList.add(loadSensor(entry._mOccupancyExternalSensor8, false));
            if (!entry._mOccupancyExternalSensor9.isEmpty()) sensorList.add(loadSensor(entry._mOccupancyExternalSensor9, false));
        } else {
            if (!entry._mOptionalExternalSensor1.isEmpty()) sensorList.add(loadSensor(entry._mOptionalExternalSensor1, false));
            if (!entry._mOptionalExternalSensor2.isEmpty()) sensorList.add(loadSensor(entry._mOptionalExternalSensor2, false));
        }
        return sensorList;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImportExternalData.class);
}
