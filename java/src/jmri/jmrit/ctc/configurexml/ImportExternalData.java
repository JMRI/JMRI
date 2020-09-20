package jmri.jmrit.ctc.configurexml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import java.lang.reflect.Field;
import java.util.*;

import jmri.jmrit.XmlFile;
import jmri.util.FileUtil;
import jmri.*;
import jmri.jmrit.ctc.*;
import jmri.jmrit.ctc.editor.code.*;
import jmri.jmrit.ctc.ctcserialdata.*;

import org.jdom2.Element;
import org.jdom2.JDOMException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Import the old ProgramProperties.xml and CTCSystem.xml files.
 * <pre>
 * java
 *   object - new OtherData
 *     void - "getField"
 *       string - field name
 *       void - "set"
 *         object - parent ref
 *         boolean/string/int - value
 *   object - ArrayList
 *     void - "add"
 *       object - new CodeButtonHandlerData
 *         void - "getField"
 *           string - field name
 *           void - "set"
 *             object - parent ref
 *             boolean/string/int - value
 * etc.
 * </pre>
 *
 * @author Dave Sand Copyright (c) 2020
 */
public class ImportExternalData {
    static final CtcManager cm = InstanceManager.getDefault(CtcManager.class);
    static ArrayList<HashMap<String, String>> sections = new ArrayList<>();
    static HashMap<String, String> fields;
    static boolean hasOtherData = false;
    static int cbdhCount = 0;

    public static void loadExternalData() {
        // Load ProgramProperties
        cm.getProgramProperties().importExternalProgramProperties();

        // Convert the CTCSystem.xml to an ArrayList of HashMaps containing field names and values.
        loadCTCSystemContent();

        // Process the field content
        doDataLoading();

        // Rename data files
        if (!CTCFiles.renameFile("ProgramProperties.xml", "OldProgramProperties.xml")) {
            log.error("Rename failed for ProgramProperties.xml");
        }
        if (!CTCFiles.renameFile("CTCSystem.xml", "OldCTCSystem.xml")) {
            log.error("Rename failed for CTCSystem.xml");
        }
    }

    public static void loadCTCSystemContent() {
        // Get the XML file for the OtherData and CodeButtonHandlerData content
        CTCSystemFile x = new CTCSystemFile();
        File file = x.getFile();

        // Parse the XMLExport format
        try {
            Element root = x.rootFromFile(file);
            for (Element level1 : root.getChildren()) {
                if (level1.getAttributeValue("class").contains("OtherData")) {
                    // This will be the OtherData section
                    fields = new HashMap<>();
                    for (Element level2 : level1.getChildren()) {
                        getField(level2);
                    }
                    sections.add(fields);
                    hasOtherData = true;
                    continue;
                }
                if (level1.getAttributeValue("class").contains("ArrayList")) {
                    // This will be the CodeButtonHandleData section
                    for (Element level2 : level1.getChildren()) {
                        Element level3 = level2.getChild("object");

                        // This is were a new CodeButtonHandleData starts
                        fields = new HashMap<>();
                        for (Element level4 : level3.getChildren()) {
                            getField(level4);
                        }
                        sections.add(fields);
                        cbdhCount++;
                    }
                }
            }
        } catch (JDOMException ex) {
            log.error("File invalid: {}", ex);  // NOI18N
            return;
        } catch (IOException ex) {
            log.error("Error reading file: {}", ex);  // NOI18N
            return;
        }
    }

    static void getField(Element element) {
        String fieldName = element.getChild("string").getValue();

        Element children = element.getChild("void");
        for (Element child : children.getChildren()) {
            switch (child.getName()) {
                case "object":
                    break;
                case "string":
                case "int":
                case "boolean":
                    fields.put(fieldName, child.getValue());
                    break;
                default:
                    log.error("++++  unknown type +++++: {}, {}", fieldName, child.getValue());
                    break;
            }
        }
    }

    public static class CTCSystemFile extends XmlFile {
        public File getFile() {
            return CTCFiles.getFile("CTCSystem.xml");
        }
    }

    static void doDataLoading() {
        int index = 0;
        if (hasOtherData) {
            loadOtherData(sections.get(index));
            index++;
        }

        for (int idx = index; idx < sections.size(); idx++) {
            loadCodeButtonHandlerData(sections.get(idx));
        }
        convertCallOnSensorNamesToNBHSensors();
    }



    static void loadCodeButtonHandlerData(HashMap<String, String> fieldList) {
    log.debug("------------- CBHD ------------");
        String value = fieldList.get("_mUniqueID");
        int _mUniqueID = loadInt(value);

        value = fieldList.get("_mSwitchNumber");
        int _mSwitchNumber = loadInt(value);

        value = fieldList.get("_mSignalEtcNumber");
        int _mSignalEtcNumber = loadInt(value);

        value = fieldList.get("_mGUIColumnNumber");
        int _mGUIColumnNumber = loadInt(value);

        // Create a new CodeButtonHandlerData via CodeButtonHandlerDataRoutines which sets default values and empty NBH... objects
        CodeButtonHandlerData cbhd = CodeButtonHandlerDataRoutines.createNewCodeButtonHandlerData(
                _mUniqueID, _mSwitchNumber, _mSignalEtcNumber, _mGUIColumnNumber, cm.getProgramProperties());
        cm.getCTCSerialData().addCodeButtonHandlerData(cbhd);


    log.debug("------------- Code ------------");

        // Code section
        value = fieldList.get("_mCodeButtonInternalSensor");
        if (value != null) cbhd._mCodeButtonInternalSensor = loadSensor(value, true);

        value = fieldList.get("_mOSSectionOccupiedExternalSensor");
        if (value != null) cbhd._mOSSectionOccupiedExternalSensor = loadSensor(value, false);

        value = fieldList.get("_mOSSectionOccupiedExternalSensor2");
        if (value != null) cbhd._mOSSectionOccupiedExternalSensor2 = loadSensor(value, false);

        value = fieldList.get("_mOSSectionSwitchSlavedToUniqueID");
        if (value != null) cbhd._mOSSectionSwitchSlavedToUniqueID = loadInt(value);

        value = fieldList.get("_mGUIGeneratedAtLeastOnceAlready");
        if (value != null) cbhd._mGUIGeneratedAtLeastOnceAlready = loadBoolean(value);

        value = fieldList.get("_mCodeButtonDelayTime");
        if (value != null) cbhd._mCodeButtonDelayTime = loadInt(value);
    log.debug("------------- SIDI ------------");

        // SIDI section
        value = fieldList.get("_mSIDI_Enabled");
        if (value != null) cbhd._mSIDI_Enabled = loadBoolean(value);

        value = fieldList.get("_mSIDI_LeftInternalSensor");
        if (value != null) cbhd._mSIDI_LeftInternalSensor = loadSensor(value, true);

        value = fieldList.get("_mSIDI_NormalInternalSensor");
        if (value != null) cbhd._mSIDI_NormalInternalSensor = loadSensor(value, true);

        value = fieldList.get("_mSIDI_RightInternalSensor");
        if (value != null) cbhd._mSIDI_RightInternalSensor = loadSensor(value, true);

        value = fieldList.get("_mSIDI_CodingTimeInMilliseconds");
        if (value != null) cbhd._mSIDI_CodingTimeInMilliseconds = loadInt(value);

        value = fieldList.get("_mSIDI_TimeLockingTimeInMilliseconds");
        if (value != null) cbhd._mSIDI_TimeLockingTimeInMilliseconds = loadInt(value);

        value = fieldList.get("_mSIDI_TrafficDirection");
        if (value != null) cbhd._mSIDI_TrafficDirection = CodeButtonHandlerData.TRAFFIC_DIRECTION.valueOf(loadString(value));

        value = fieldList.get("_mSIDI_LeftRightTrafficSignalsCSVList");
        if (value != null) cbhd._mSIDI_LeftRightTrafficSignals = getSignalList(value);

        value = fieldList.get("_mSIDI_RightLeftTrafficSignalsCSVList");
        if (value != null) cbhd._mSIDI_RightLeftTrafficSignals = getSignalList(value);

    log.debug("------------- SIDL ------------");
        // SIDL section
        value = fieldList.get("_mSIDL_Enabled");
        if (value != null) cbhd._mSIDL_Enabled = loadBoolean(value);

        value = fieldList.get("_mSIDL_LeftInternalSensor");
        if (value != null) cbhd._mSIDL_LeftInternalSensor = loadSensor(value, true);

        value = fieldList.get("_mSIDL_NormalInternalSensor");
        if (value != null) cbhd._mSIDL_NormalInternalSensor = loadSensor(value, true);

        value = fieldList.get("_mSIDL_RightInternalSensor");
        if (value != null) cbhd._mSIDL_RightInternalSensor = loadSensor(value, true);

    log.debug("------------- SWDI ------------");
        // SWDI section
        value = fieldList.get("_mSWDI_Enabled");
        if (value != null) cbhd._mSWDI_Enabled = loadBoolean(value);

        value = fieldList.get("_mSWDI_NormalInternalSensor");
        if (value != null) cbhd._mSWDI_NormalInternalSensor = loadSensor(value, true);

        value = fieldList.get("_mSWDI_ReversedInternalSensor");
        if (value != null) cbhd._mSWDI_ReversedInternalSensor = loadSensor(value, true);

        value = fieldList.get("_mSWDI_FeedbackDifferent");
        if (value != null) cbhd._mSWDI_FeedbackDifferent = loadBoolean(value);

        value = fieldList.get("_mSWDI_ExternalTurnout");
        if (value != null) cbhd._mSWDI_ExternalTurnout = loadTurnout(value, cbhd._mSWDI_FeedbackDifferent);

        value = fieldList.get("_mSWDI_CodingTimeInMilliseconds");
        if (value != null) cbhd._mSWDI_CodingTimeInMilliseconds = loadInt(value);

        value = fieldList.get("_mSWDI_GUITurnoutType");
        if (value != null) cbhd._mSWDI_GUITurnoutType = CodeButtonHandlerData.TURNOUT_TYPE.getTurnoutType(loadInt(value));

        value = fieldList.get("_mSWDI_GUITurnoutLeftHand");
        if (value != null) cbhd._mSWDI_GUITurnoutLeftHand = loadBoolean(value);

        value = fieldList.get("_mSWDI_GUICrossoverLeftHand");
        if (value != null) cbhd._mSWDI_GUICrossoverLeftHand = loadBoolean(value);

    log.debug("------------- SWDL ------------");
        // SWDL section
        value = fieldList.get("_mSWDL_Enabled");
        if (value != null) cbhd._mSWDL_Enabled = loadBoolean(value);

        value = fieldList.get("_mSWDL_InternalSensor");
        if (value != null) cbhd._mSWDL_InternalSensor = loadSensor(value, true);

    log.debug("-------------  CO  ------------");
        // CO section
        value = fieldList.get("_mCO_Enabled");
        if (value != null) cbhd._mCO_Enabled = loadBoolean(value);

        value = fieldList.get("_mCO_CallOnToggleInternalSensor");
        if (value != null) cbhd._mCO_CallOnToggleInternalSensor = loadSensor(value, true);

        value = fieldList.get("_mCO_GroupingsListString");
        if (value != null) cbhd._mCO_GroupingsList = getCallOnList(value);

    log.debug("------------- TRL  ------------");
        // TRL section
        value = fieldList.get("_mTRL_Enabled");
        if (value != null) cbhd._mTRL_Enabled = loadBoolean(value);

        value = fieldList.get("_mTRL_LeftTrafficLockingRulesSSVList");
        if (value != null) cbhd._mTRL_LeftTrafficLockingRules = getTrafficLocking(value);

        value = fieldList.get("_mTRL_RightTrafficLockingRulesSSVList");
        if (value != null) cbhd._mTRL_RightTrafficLockingRules = getTrafficLocking(value);

    log.debug("------------- TUL  ------------");
        // TUL section
        value = fieldList.get("_mTUL_Enabled");
        if (value != null) cbhd._mTUL_Enabled = loadBoolean(value);

        value = fieldList.get("_mTUL_DispatcherInternalSensorLockToggle");
        if (value != null) cbhd._mTUL_DispatcherInternalSensorLockToggle = loadSensor(value, true);

        value = fieldList.get("_mTUL_ExternalTurnoutFeedbackDifferent");
        if (value != null) cbhd._mTUL_ExternalTurnoutFeedbackDifferent = loadBoolean(value);

        value = fieldList.get("_mTUL_ExternalTurnout");
        if (value != null) cbhd._mTUL_ExternalTurnout = loadTurnout(value, cbhd._mTUL_ExternalTurnoutFeedbackDifferent);

        value = fieldList.get("_mTUL_DispatcherInternalSensorUnlockedIndicator");
        if (value != null) cbhd._mTUL_DispatcherInternalSensorUnlockedIndicator = loadSensor(value, true);

        value = fieldList.get("_mTUL_NoDispatcherControlOfSwitch");
        if (value != null) cbhd._mTUL_NoDispatcherControlOfSwitch = loadBoolean(value);

        value = fieldList.get("_mTUL_ndcos_WhenLockedSwitchStateIsClosed");
        if (value != null) cbhd._mTUL_ndcos_WhenLockedSwitchStateIsClosed = loadBoolean(value);

        value = fieldList.get("_mTUL_LockImplementation");
        if (value != null) cbhd._mTUL_LockImplementation = CodeButtonHandlerData.LOCK_IMPLEMENTATION.getLockImplementation(loadInt(value));

        value = fieldList.get("_mTUL_AdditionalExternalTurnout1");
        if (value != null) {
            boolean feedback = loadBoolean(fieldList.get("_mTUL_AdditionalExternalTurnout1FeedbackDifferent"));
            cbhd._mTUL_AdditionalExternalTurnout1 = loadTurnout(value, feedback);
            cbhd._mTUL_AdditionalExternalTurnout1FeedbackDifferent = feedback;
        }

        value = fieldList.get("_mTUL_AdditionalExternalTurnout2");
        if (value != null) {
            boolean feedback = loadBoolean(fieldList.get("_mTUL_AdditionalExternalTurnout2FeedbackDifferent"));
            cbhd._mTUL_AdditionalExternalTurnout2 = loadTurnout(value, feedback);
            cbhd._mTUL_AdditionalExternalTurnout2FeedbackDifferent = feedback;
        }

        value = fieldList.get("_mTUL_AdditionalExternalTurnout3");
        if (value != null) {
            boolean feedback = loadBoolean(fieldList.get("_mTUL_AdditionalExternalTurnout3FeedbackDifferent"));
            cbhd._mTUL_AdditionalExternalTurnout3 = loadTurnout(value, feedback);
            cbhd._mTUL_AdditionalExternalTurnout3FeedbackDifferent = feedback;
        }

    log.debug("-------------  IL  ------------");
        // IL section
        value = fieldList.get("_mIL_Enabled");
        if (value != null) cbhd._mIL_Enabled = loadBoolean(value);

        value = fieldList.get("_mIL_ListOfCSVSignalNames");
        if (value != null) cbhd._mIL_Signals = getSignalList(value);

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
     * @param fieldList The "ctcOtherData" fields.
     */
    static void loadOtherData(HashMap<String, String> fieldList) {
        OtherData od = cm.getOtherData();
        String value;

//  Fleeting:
        value = fieldList.get("_mFleetingToggleInternalSensor");
        if (value != null) od._mFleetingToggleInternalSensor = loadSensor(value, true);

        value = fieldList.get("_mDefaultFleetingEnabled");
        if (value != null) od._mDefaultFleetingEnabled = loadBoolean(value);

//  Global startup:
        value = fieldList.get("_mTUL_EnabledAtStartup");
        if (value != null) od._mTUL_EnabledAtStartup = loadBoolean(value);

        value = fieldList.get("_mSignalSystemType");
        if (value != null) od._mSignalSystemType = OtherData.SIGNAL_SYSTEM_TYPE.getSignalSystemType(loadInt(value));

        value = fieldList.get("_mTUL_SecondsToLockTurnouts");
        if (value != null) od._mTUL_SecondsToLockTurnouts = loadInt(value);

//  Next unique # for each created Column:
        value = fieldList.get("_mNextUniqueNumber");
        if (value != null) od._mNextUniqueNumber = loadInt(value);

//  CTC Debugging:
        value = fieldList.get("_mCTCDebugSystemReloadInternalSensor");
        if (value != null) od._mCTCDebugSystemReloadInternalSensor = loadSensor(value, true);

        value = fieldList.get("_mCTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor");
        if (value != null) od._mCTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor = loadSensor(value, true);

//  GUI design:
        value = fieldList.get("_mGUIDesign_NumberOfEmptyColumnsAtEnd");
        if (value != null) od._mGUIDesign_NumberOfEmptyColumnsAtEnd = loadInt(value);

        value = fieldList.get("_mGUIDesign_CTCPanelType");
        if (value != null) od._mGUIDesign_CTCPanelType = OtherData.CTC_PANEL_TYPE.getRadioGroupValue(loadInt(value));

        value = fieldList.get("_mGUIDesign_BuilderPlate");
        if (value != null) od._mGUIDesign_BuilderPlate = loadBoolean(value);

        value = fieldList.get("_mGUIDesign_SignalsOnPanel");
        if (value != null) od._mGUIDesign_SignalsOnPanel = OtherData.SIGNALS_ON_PANEL.getRadioGroupValue(loadInt(value));

        value = fieldList.get("_mGUIDesign_FleetingToggleSwitch");
        if (value != null) od._mGUIDesign_FleetingToggleSwitch = loadBoolean(value);

        value = fieldList.get("_mGUIDesign_AnalogClockEtc");
        if (value != null) od._mGUIDesign_AnalogClockEtc = loadBoolean(value);

        value = fieldList.get("_mGUIDesign_ReloadCTCSystemButton");
        if (value != null) od._mGUIDesign_ReloadCTCSystemButton = loadBoolean(value);

        value = fieldList.get("_mGUIDesign_CTCDebugOnToggle");
        if (value != null) od._mGUIDesign_CTCDebugOnToggle = loadBoolean(value);

        value = fieldList.get("_mGUIDesign_CreateTrackPieces");
        if (value != null) od._mGUIDesign_CreateTrackPieces = loadBoolean(value);

        value = fieldList.get("_mGUIDesign_VerticalSize");
        if (value != null) od._mGUIDesign_VerticalSize = OtherData.VERTICAL_SIZE.getRadioGroupValue(loadInt(value));

        value = fieldList.get("_mGUIDesign_OSSectionUnknownInconsistentRedBlink");
        if (value != null) od._mGUIDesign_OSSectionUnknownInconsistentRedBlink = loadBoolean(value);

        value = fieldList.get("_mGUIDesign_TurnoutsOnPanel");
        if (value != null) od._mGUIDesign_TurnoutsOnPanel = loadBoolean(value);

// Debugging aid -- not active due to SpotBugs
//         log.info("OtherData:");
//         List<Field> fields = Arrays.asList(OtherData.class.getFields());
//         fields.forEach(field -> {
//             try {
//                 log.info("    OtherData: fld = {}, type = {}, val = {}", field.getName(), field.getType(), field.get(od));
//             } catch (Exception ex) {
//                 log.info("    OtherData list exception: {}", ex.getMessage());
//             }
//         });
    }

    // **** Load simple objects ****

    static String loadString(String value) {
        String newString = null;
        if (value != null) {
            newString = value;
        }
        return newString;
    }

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

    static boolean loadBoolean(String value) {
        boolean newBoolean = false;
        if (value != null) {
            newBoolean = value.equals("true") ? true : false;
        }
        return newBoolean;
    }

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
            cod._mSignalFacingDirection = loadString(entry._mSignalFacingDirection);
            cod._mSignalAspectToDisplay = loadString(entry._mSignalAspectToDisplay);
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
            trl._mUserRuleNumber = loadString(entry._mUserRuleNumber);
            trl._mRuleEnabled = loadString(entry._mRuleEnabled);
            trl._mDestinationSignalOrComment = loadString(entry._mDestinationSignalOrComment);

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
