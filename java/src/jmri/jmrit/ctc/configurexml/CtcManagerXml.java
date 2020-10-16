package jmri.jmrit.ctc.configurexml;

import java.lang.reflect.Field;
import java.util.*;

import jmri.*;
import jmri.jmrit.ctc.*;
import jmri.jmrit.ctc.editor.code.*;
import jmri.jmrit.ctc.ctcserialdata.*;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the functionality for persistence of the CTC tool data.  The data is stored
 * in the PanelPro data xml file using the standard Load/Store process.
 *
 * @author Dave Sand Copyright (c) 2020
 */
public class CtcManagerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {
    CtcManager cm = InstanceManager.getDefault(CtcManager.class);

    public CtcManagerXml() {
    }

    /**
     * Implementation for storing the contents of the CTC configuration.
     *
     * @param o Object to store, of type CtcManager
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element ctcdata = new Element("ctcdata");
        setStoreElementClass(ctcdata);

        ctcdata.addContent(storeProperties(cm));
        ctcdata.addContent(storeOtherData(cm));
        for (CodeButtonHandlerData cbhd : cm.getCTCSerialData().getCodeButtonHandlerDataArrayList()) {
            Element cbhdElement = new Element("ctcCodeButtonData");

            cbhdElement.addContent(storeInt("UniqueID", cbhd._mUniqueID));
            cbhdElement.addContent(storeInt("SwitchNumber", cbhd._mSwitchNumber));
            cbhdElement.addContent(storeInt("SignalEtcNumber", cbhd._mSignalEtcNumber));
            cbhdElement.addContent(storeInt("GUIColumnNumber", cbhd._mGUIColumnNumber));

            // Code section
            cbhdElement.addContent(storeSensor("CodeButtonInternalSensor", cbhd._mCodeButtonInternalSensor));
            cbhdElement.addContent(storeSensor("OSSectionOccupiedExternalSensor", cbhd._mOSSectionOccupiedExternalSensor));
            cbhdElement.addContent(storeSensor("OSSectionOccupiedExternalSensor2", cbhd._mOSSectionOccupiedExternalSensor2));
            cbhdElement.addContent(storeInt("OSSectionSwitchSlavedToUniqueID", cbhd._mOSSectionSwitchSlavedToUniqueID));
            cbhdElement.addContent(storeBoolean("GUIGeneratedAtLeastOnceAlready", cbhd._mGUIGeneratedAtLeastOnceAlready));
            cbhdElement.addContent(storeInt("CodeButtonDelayTime", cbhd._mCodeButtonDelayTime));

            // SIDI section
            cbhdElement.addContent(storeBoolean("SIDI_Enabled", cbhd._mSIDI_Enabled));
            cbhdElement.addContent(storeSensor("SIDI_LeftInternalSensor", cbhd._mSIDI_LeftInternalSensor));
            cbhdElement.addContent(storeSensor("SIDI_NormalInternalSensor", cbhd._mSIDI_NormalInternalSensor));
            cbhdElement.addContent(storeSensor("SIDI_RightInternalSensor", cbhd._mSIDI_RightInternalSensor));
            cbhdElement.addContent(storeInt("SIDI_CodingTimeInMilliseconds", cbhd._mSIDI_CodingTimeInMilliseconds));
            cbhdElement.addContent(storeInt("SIDI_TimeLockingTimeInMilliseconds", cbhd._mSIDI_TimeLockingTimeInMilliseconds));
            cbhdElement.addContent(storeString("SIDI_TrafficDirection", cbhd._mSIDI_TrafficDirection.toString()));
            cbhdElement.addContent(storeSignalList("SIDI_LeftRightTrafficSignals", cbhd._mSIDI_LeftRightTrafficSignals));
            cbhdElement.addContent(storeSignalList("SIDI_RightLeftTrafficSignals", cbhd._mSIDI_RightLeftTrafficSignals));

            // SIDL section
            cbhdElement.addContent(storeBoolean("SIDL_Enabled", cbhd._mSIDL_Enabled));
            cbhdElement.addContent(storeSensor("SIDL_LeftInternalSensor", cbhd._mSIDL_LeftInternalSensor));
            cbhdElement.addContent(storeSensor("SIDL_NormalInternalSensor", cbhd._mSIDL_NormalInternalSensor));
            cbhdElement.addContent(storeSensor("SIDL_RightInternalSensor", cbhd._mSIDL_RightInternalSensor));

            // SWDI section
            cbhdElement.addContent(storeBoolean("SWDI_Enabled", cbhd._mSWDI_Enabled));
            cbhdElement.addContent(storeSensor("SWDI_NormalInternalSensor", cbhd._mSWDI_NormalInternalSensor));
            cbhdElement.addContent(storeSensor("SWDI_ReversedInternalSensor", cbhd._mSWDI_ReversedInternalSensor));
            cbhdElement.addContent(storeTurnout("SWDI_ExternalTurnout", cbhd._mSWDI_ExternalTurnout));
            cbhdElement.addContent(storeInt("SWDI_CodingTimeInMilliseconds", cbhd._mSWDI_CodingTimeInMilliseconds));
            cbhdElement.addContent(storeBoolean("SWDI_FeedbackDifferent", cbhd._mSWDI_FeedbackDifferent));
            cbhdElement.addContent(storeInt("SWDI_GUITurnoutType", cbhd._mSWDI_GUITurnoutType.getInt()));
            cbhdElement.addContent(storeBoolean("SWDI_GUITurnoutLeftHand", cbhd._mSWDI_GUITurnoutLeftHand));
            cbhdElement.addContent(storeBoolean("SWDI_GUICrossoverLeftHand", cbhd._mSWDI_GUICrossoverLeftHand));

            // SWDL section
            cbhdElement.addContent(storeBoolean("SWDL_Enabled", cbhd._mSWDL_Enabled));
            cbhdElement.addContent(storeSensor("SWDL_InternalSensor", cbhd._mSWDL_InternalSensor));

            // CO section
            cbhdElement.addContent(storeBoolean("CO_Enabled", cbhd._mCO_Enabled));
            cbhdElement.addContent(storeSensor("CO_CallOnToggleInternalSensor", cbhd._mCO_CallOnToggleInternalSensor));
            cbhdElement.addContent(storeCallOnList("CO_GroupingsList", cbhd._mCO_GroupingsList));

            // TRL section
            cbhdElement.addContent(storeBoolean("TRL_Enabled", cbhd._mTRL_Enabled));
            cbhdElement.addContent(storeTRLRules("TRL_LeftRules", cbhd._mTRL_LeftTrafficLockingRules));
            cbhdElement.addContent(storeTRLRules("TRL_RightRules", cbhd._mTRL_RightTrafficLockingRules));

            // TUL section
            cbhdElement.addContent(storeBoolean("TUL_Enabled", cbhd._mTUL_Enabled));
            cbhdElement.addContent(storeSensor("TUL_DispatcherInternalSensorLockToggle", cbhd._mTUL_DispatcherInternalSensorLockToggle));
            cbhdElement.addContent(storeTurnout("TUL_ExternalTurnout", cbhd._mTUL_ExternalTurnout));
            cbhdElement.addContent(storeBoolean("TUL_ExternalTurnoutFeedbackDifferent", cbhd._mTUL_ExternalTurnoutFeedbackDifferent));
            cbhdElement.addContent(storeSensor("TUL_DispatcherInternalSensorUnlockedIndicator", cbhd._mTUL_DispatcherInternalSensorUnlockedIndicator));
            cbhdElement.addContent(storeBoolean("TUL_NoDispatcherControlOfSwitch", cbhd._mTUL_NoDispatcherControlOfSwitch));
            cbhdElement.addContent(storeBoolean("TUL_ndcos_WhenLockedSwitchStateIsClosed", cbhd._mTUL_ndcos_WhenLockedSwitchStateIsClosed));
            cbhdElement.addContent(storeBoolean("TUL_GUI_IconsEnabled", cbhd._mTUL_GUI_IconsEnabled));
            cbhdElement.addContent(storeInt("TUL_LockImplementation", cbhd._mTUL_LockImplementation.getInt()));
            cbhdElement.addContent(storeTULAdditionalTurnouts("TUL_AdditionalExternalTurnouts", cbhd));

            // IL section
            cbhdElement.addContent(storeBoolean("IL_Enabled", cbhd._mIL_Enabled));
            cbhdElement.addContent(storeSignalList("IL_Signals", cbhd._mIL_Signals));

            ctcdata.addContent(cbhdElement);
        }

        return (ctcdata);
    }

    /**
     * Subclass provides implementation to create the correct top element,
     * including the type information. Default implementation is to use the
     * local class here.
     *
     * @param ctcdata The top-level element being created
     */
    public void setStoreElementClass(Element ctcdata) {
        ctcdata.setAttribute("class", "jmri.jmrit.ctc.configurexml.CtcManagerXml");
    }

    Element storeProperties(CtcManager cm) {
        ProgramProperties pp = cm.getProgramProperties();

        Element properties = new Element("ctcProperties");
        properties.addContent(storeString("CodeButtonInternalSensorPattern", pp._mCodeButtonInternalSensorPattern));
        properties.addContent(storeInt("SIDI_CodingTimeInMilliseconds", pp._mSIDI_CodingTimeInMilliseconds));
        properties.addContent(storeString("SIDI_LeftInternalSensorPattern", pp._mSIDI_LeftInternalSensorPattern));
        properties.addContent(storeString("SIDI_NormalInternalSensorPattern", pp._mSIDI_NormalInternalSensorPattern));
        properties.addContent(storeString("SIDI_RightInternalSensorPattern", pp._mSIDI_RightInternalSensorPattern));
        properties.addContent(storeInt("SIDI_TimeLockingTimeInMilliseconds", pp._mSIDI_TimeLockingTimeInMilliseconds));
        properties.addContent(storeString("SIDL_LeftInternalSensorPattern", pp._mSIDL_LeftInternalSensorPattern));
        properties.addContent(storeString("SIDL_NormalInternalSensorPattern", pp._mSIDL_NormalInternalSensorPattern));
        properties.addContent(storeString("SIDL_RightInternalSensorPattern", pp._mSIDL_RightInternalSensorPattern));
        properties.addContent(storeInt("SWDI_CodingTimeInMilliseconds", pp._mSWDI_CodingTimeInMilliseconds));
        properties.addContent(storeString("SWDI_NormalInternalSensorPattern", pp._mSWDI_NormalInternalSensorPattern));
        properties.addContent(storeString("SWDI_ReversedInternalSensorPattern", pp._mSWDI_ReversedInternalSensorPattern));
        properties.addContent(storeString("SWDL_InternalSensorPattern", pp._mSWDL_InternalSensorPattern));
        properties.addContent(storeString("CO_CallOnToggleInternalSensorPattern", pp._mCO_CallOnToggleInternalSensorPattern));
        properties.addContent(storeString("TUL_DispatcherInternalSensorLockTogglePattern", pp._mTUL_DispatcherInternalSensorLockTogglePattern));
        properties.addContent(storeString("TUL_DispatcherInternalSensorUnlockedIndicatorPattern", pp._mTUL_DispatcherInternalSensorUnlockedIndicatorPattern));
        properties.addContent(storeInt("CodeButtonDelayTime", pp._mCodeButtonDelayTime));

        return properties;
    }

    Element storeOtherData(CtcManager cm) {
        OtherData od = cm.getOtherData();

        Element otherData = new Element("ctcOtherData");

        otherData.addContent(storeString("CtcVersion", OtherData.CTC_VERSION));

//  Fleeting:
        otherData.addContent(storeSensor("FleetingToggleInternalSensor", od._mFleetingToggleInternalSensor));
        otherData.addContent(storeBoolean("DefaultFleetingEnabled", od._mDefaultFleetingEnabled));

//  Global startup:
        otherData.addContent(storeBoolean("TUL_EnabledAtStartup", od._mTUL_EnabledAtStartup));
        otherData.addContent(storeInt("SignalSystemType", od._mSignalSystemType.getInt()));
        otherData.addContent(storeInt("TUL_SecondsToLockTurnouts", od._mTUL_SecondsToLockTurnouts));

//  Next unique # for each created Column:
        otherData.addContent(storeInt("NextUniqueNumber", od._mNextUniqueNumber));

//  CTC Debugging:
        otherData.addContent(storeSensor("CTCDebugSystemReloadInternalSensor", od._mCTCDebugSystemReloadInternalSensor));
        otherData.addContent(storeSensor("CTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor", od._mCTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor));

//  GUI design:
        otherData.addContent(storeInt("GUIDesign_NumberOfEmptyColumnsAtEnd", od._mGUIDesign_NumberOfEmptyColumnsAtEnd));
        otherData.addContent(storeInt("GUIDesign_CTCPanelType", od._mGUIDesign_CTCPanelType.getRadioGroupValue()));
        otherData.addContent(storeBoolean("GUIDesign_BuilderPlate", od._mGUIDesign_BuilderPlate));
        otherData.addContent(storeInt("GUIDesign_SignalsOnPanel", od._mGUIDesign_SignalsOnPanel.getRadioGroupValue()));
        otherData.addContent(storeBoolean("GUIDesign_FleetingToggleSwitch", od._mGUIDesign_FleetingToggleSwitch));
        otherData.addContent(storeBoolean("GUIDesign_AnalogClockEtc", od._mGUIDesign_AnalogClockEtc));
        otherData.addContent(storeBoolean("GUIDesign_ReloadCTCSystemButton", od._mGUIDesign_ReloadCTCSystemButton));
        otherData.addContent(storeBoolean("GUIDesign_CTCDebugOnToggle", od._mGUIDesign_CTCDebugOnToggle));
        otherData.addContent(storeBoolean("GUIDesign_CreateTrackPieces", od._mGUIDesign_CreateTrackPieces));
        otherData.addContent(storeInt("GUIDesign_VerticalSize", od._mGUIDesign_VerticalSize.getRadioGroupValue()));
        otherData.addContent(storeBoolean("GUIDesign_OSSectionUnknownInconsistentRedBlink", od._mGUIDesign_OSSectionUnknownInconsistentRedBlink));
        otherData.addContent(storeBoolean("GUIDesign_TurnoutsOnPanel", od._mGUIDesign_TurnoutsOnPanel));

        return otherData;
    }

    // **** Create elements for simple objects ****

    Element storeString(String elementName, String elementValue) {
        Element element = new Element(elementName);
        element.setText(elementValue);
        return element;
    }

    Element storeInt(String elementName, int elementValue) {
        Element element = new Element(elementName);
        element.setText(String.valueOf(elementValue));
        return element;
    }

    Element storeBoolean(String elementName, boolean elementValue) {
        Element element = new Element(elementName);
        element.setText(elementValue == true ? "true" : "false");
        return element;
    }

    Element storeSensor(String elementName, NBHSensor sensor) {
        Element element = new Element(elementName);
        if (sensor != null) {
            element.setText(sensor.getHandleName());
        }
        return element;
    }

    Element storeSignal(String elementName, NBHSignal signal) {
        Element element = new Element(elementName);
        if (signal != null) {
            element.setText(signal.getHandleName());
        }
        return element;
    }

    Element storeTurnout(String elementName, NBHTurnout turnout) {
        Element element = new Element(elementName);
        if (turnout != null) {
            element.setText(turnout.getHandleName());
        }
        return element;
    }

    Element storeBlock(String elementName, NamedBeanHandle<Block> block) {
        Element element = new Element(elementName);
        if (block != null) {
            element.setText(block.getName());
        }
        return element;
    }

    // **** Create elements for ArrayList objects ****

    Element storeSensorList(String elementName, List<NBHSensor> sensors) {
        Element element = new Element(elementName);
        sensors.forEach(sensor -> {
            element.addContent(storeSensor("sensor", sensor));
        });
        return element;
    }

    Element storeSignalList(String elementName, List<NBHSignal> signals) {
        Element element = new Element(elementName);
        signals.forEach(signal -> {
            element.addContent(storeSignal("signal", signal));
        });
        return element;
    }

    Element storeCallOnList(String elementName, List<CallOnData> callOnList) {
        Element element = new Element(elementName);
        callOnList.forEach(row -> {
            Element groupEntry = new Element("CO_GroupEntry");
            groupEntry.addContent(storeSignal("ExternalSignal", row._mExternalSignal));
            groupEntry.addContent(storeString("SignalFacingDirection", row._mSignalFacingDirection));
            groupEntry.addContent(storeString("SignalAspectToDisplay", row._mSignalAspectToDisplay));
            groupEntry.addContent(storeSensor("CalledOnExternalSensor", row._mCalledOnExternalSensor));
            groupEntry.addContent(storeBlock("ExternalBlock", row._mExternalBlock));
            groupEntry.addContent(storeSensorList("SwitchIndicators", row._mSwitchIndicators));
            element.addContent(groupEntry);
        });
        return element;
    }

    Element storeTRLRules(String elementName, List<TrafficLockingData> trlList) {
        Element element = new Element(elementName);
        trlList.forEach(row -> {
            Element ruleEntry = new Element("TRL_TrafficLockingRule");
            ruleEntry.addContent(storeString("UserRuleNumber", row._mUserRuleNumber));
            ruleEntry.addContent(storeString("RuleEnabled", row._mRuleEnabled));
            ruleEntry.addContent(storeString("DestinationSignalOrComment", row._mDestinationSignalOrComment));

            ruleEntry.addContent(storeTRLSwitches("switches", row._mSwitchAlignments));

            ruleEntry.addContent(storeSensorList("OccupancyExternalSensors", row._mOccupancyExternalSensors));
            ruleEntry.addContent(storeSensorList("OptionalExternalSensors", row._mOptionalExternalSensors));
            element.addContent(ruleEntry);
        });
        return element;
    }

    Element storeTRLSwitches(String elementName, List<TrafficLockingData.TRLSwitch> trlSwitches) {
        Element element = new Element(elementName);
        trlSwitches.forEach(trlSwitch -> {
            Element elSwitch = new Element("switch");
            elSwitch.addContent(storeString("UserText", trlSwitch._mUserText));
            elSwitch.addContent(storeString("SwitchAlignment", trlSwitch._mSwitchAlignment));
            elSwitch.addContent(storeInt("UniqueID", trlSwitch._mUniqueID));
            element.addContent(elSwitch);
        });
        return element;
    }

    Element storeTULAdditionalTurnouts(String elementName, CodeButtonHandlerData cbhd) {
        Element element = new Element(elementName);

        Element elementRow = createAdditionalTurnoutEntry(cbhd._mTUL_AdditionalExternalTurnout1, cbhd._mTUL_AdditionalExternalTurnout1FeedbackDifferent);
        if (elementRow != null) element.addContent(elementRow);
        elementRow = createAdditionalTurnoutEntry(cbhd._mTUL_AdditionalExternalTurnout2, cbhd._mTUL_AdditionalExternalTurnout2FeedbackDifferent);
        if (elementRow != null) element.addContent(elementRow);
        elementRow = createAdditionalTurnoutEntry(cbhd._mTUL_AdditionalExternalTurnout3, cbhd._mTUL_AdditionalExternalTurnout3FeedbackDifferent);
        if (elementRow != null) element.addContent(elementRow);

        return element;
    }

    Element createAdditionalTurnoutEntry(NBHTurnout turnout, boolean turnoutFeedback) {
        Element element = null;
        if (turnout.valid()) {
            element = new Element("TUL_AdditionalExternalTurnoutEntry");
            element.addContent(storeTurnout("TUL_AdditionalExternalTurnout", turnout));
            element.addContent(storeBoolean("TUL_AdditionalExternalTurnoutFeedbackDifferent", turnoutFeedback));
        }
        return element;
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Implementation for loading the contents of the CTC configuration.
     *
     * @param sharedCtcData Element to loaded.
     * @param perNodeCtcData Element to loaded (same as sharedCtcData).
     * @return true for successful load.
     */
    @Override
    public boolean load(Element sharedCtcData, Element perNodeCtcData) {
        List<Element> ctcList = sharedCtcData.getChildren();

        for (Element lvl1 : ctcList) {
            if (lvl1.getName().equals("ctcProperties")) {
                loadProperties(cm, lvl1);
                continue;
            }
            if (lvl1.getName().equals("ctcOtherData")) {
                loadOtherData(cm, lvl1);
                continue;
            }
            if (lvl1.getName().equals("ctcCodeButtonData")) {
                // Create basic CodeButtonHandlerData
    log.debug("------------- CBHD ------------");
                int _mUniqueID = loadInt(lvl1.getChild("UniqueID"));
                int _mSwitchNumber = loadInt(lvl1.getChild("SwitchNumber"));
                int _mSignalEtcNumber = loadInt(lvl1.getChild("SignalEtcNumber"));
                int _mGUIColumnNumber = loadInt(lvl1.getChild("GUIColumnNumber"));

                // Create a new CodeButtonHandlerData via CodeButtonHandlerDataRoutines which sets default values and empty NBH... objects
                CodeButtonHandlerData cbhd = CodeButtonHandlerDataRoutines.createNewCodeButtonHandlerData(
                        _mUniqueID, _mSwitchNumber, _mSignalEtcNumber, _mGUIColumnNumber, cm.getProgramProperties());
                cm.getCTCSerialData().addCodeButtonHandlerData(cbhd);

    log.debug("------------- Code ------------");

                // Code section
                cbhd._mCodeButtonInternalSensor = loadSensor(lvl1.getChild("CodeButtonInternalSensor"), true);
                cbhd._mOSSectionOccupiedExternalSensor = loadSensor(lvl1.getChild("OSSectionOccupiedExternalSensor"), false);
                cbhd._mOSSectionOccupiedExternalSensor2 = loadSensor(lvl1.getChild("OSSectionOccupiedExternalSensor2"), false);
                cbhd._mOSSectionSwitchSlavedToUniqueID = loadInt(lvl1.getChild("OSSectionSwitchSlavedToUniqueID"));
                cbhd._mGUIGeneratedAtLeastOnceAlready = loadBoolean(lvl1.getChild("GUIGeneratedAtLeastOnceAlready"));
                cbhd._mCodeButtonDelayTime = loadInt(lvl1.getChild("CodeButtonDelayTime"));
    log.debug("------------- SIDI ------------");

                // SIDI section
                cbhd._mSIDI_Enabled = loadBoolean(lvl1.getChild("SIDI_Enabled"));
                cbhd._mSIDI_LeftInternalSensor = loadSensor(lvl1.getChild("SIDI_LeftInternalSensor"), true);
                cbhd._mSIDI_NormalInternalSensor = loadSensor(lvl1.getChild("SIDI_NormalInternalSensor"), true);
                cbhd._mSIDI_RightInternalSensor = loadSensor(lvl1.getChild("SIDI_RightInternalSensor"), true);
                cbhd._mSIDI_CodingTimeInMilliseconds = loadInt(lvl1.getChild("SIDI_CodingTimeInMilliseconds"));
                cbhd._mSIDI_TimeLockingTimeInMilliseconds = loadInt(lvl1.getChild("SIDI_TimeLockingTimeInMilliseconds"));
                cbhd._mSIDI_TrafficDirection = CodeButtonHandlerData.TRAFFIC_DIRECTION.valueOf(loadString(lvl1.getChild("SIDI_TrafficDirection")));
                cbhd._mSIDI_LeftRightTrafficSignals = getSignalList(lvl1.getChild("SIDI_LeftRightTrafficSignals"));
                cbhd._mSIDI_RightLeftTrafficSignals = getSignalList(lvl1.getChild("SIDI_RightLeftTrafficSignals"));

    log.debug("------------- SIDL ------------");
                // SIDL section
                cbhd._mSIDL_Enabled = loadBoolean(lvl1.getChild("SIDL_Enabled"));
                cbhd._mSIDL_LeftInternalSensor = loadSensor(lvl1.getChild("SIDL_LeftInternalSensor"), true);
                cbhd._mSIDL_NormalInternalSensor = loadSensor(lvl1.getChild("SIDL_NormalInternalSensor"), true);
                cbhd._mSIDL_RightInternalSensor = loadSensor(lvl1.getChild("SIDL_RightInternalSensor"), true);

    log.debug("------------- SWDI ------------");
                // SWDI section
                cbhd._mSWDI_Enabled = loadBoolean(lvl1.getChild("SWDI_Enabled"));
                cbhd._mSWDI_NormalInternalSensor = loadSensor(lvl1.getChild("SWDI_NormalInternalSensor"), true);
                cbhd._mSWDI_ReversedInternalSensor = loadSensor(lvl1.getChild("SWDI_ReversedInternalSensor"), true);
                cbhd._mSWDI_ExternalTurnout = loadTurnout(lvl1.getChild("SWDI_ExternalTurnout"), lvl1.getChild("SWDI_FeedbackDifferent"));
                cbhd._mSWDI_CodingTimeInMilliseconds = loadInt(lvl1.getChild("SWDI_CodingTimeInMilliseconds"));
                cbhd._mSWDI_FeedbackDifferent = loadBoolean(lvl1.getChild("SWDI_FeedbackDifferent"));
                cbhd._mSWDI_GUITurnoutType = CodeButtonHandlerData.TURNOUT_TYPE.getTurnoutType(loadInt(lvl1.getChild("SWDI_GUITurnoutType")));
                cbhd._mSWDI_GUITurnoutLeftHand = loadBoolean(lvl1.getChild("SWDI_GUITurnoutLeftHand"));
                cbhd._mSWDI_GUICrossoverLeftHand = loadBoolean(lvl1.getChild("SWDI_GUICrossoverLeftHand"));

    log.debug("------------- SWDL ------------");
                // SWDL section
                cbhd._mSWDL_Enabled = loadBoolean(lvl1.getChild("SWDL_Enabled"));
                cbhd._mSWDL_InternalSensor = loadSensor(lvl1.getChild("SWDL_InternalSensor"), true);

    log.debug("-------------  CO  ------------");
                // CO section
                cbhd._mCO_Enabled = loadBoolean(lvl1.getChild("CO_Enabled"));
                cbhd._mCO_CallOnToggleInternalSensor = loadSensor(lvl1.getChild("CO_CallOnToggleInternalSensor"), true);
                cbhd._mCO_GroupingsList = getCallOnList(lvl1.getChild("CO_GroupingsList"));

    log.debug("------------- TRL  ------------");
                // TRL section
                cbhd._mTRL_Enabled = loadBoolean(lvl1.getChild("TRL_Enabled"));
                cbhd._mTRL_LeftTrafficLockingRules = getTrafficLocking(lvl1.getChild("TRL_LeftRules"));
                cbhd._mTRL_RightTrafficLockingRules = getTrafficLocking(lvl1.getChild("TRL_RightRules"));

    log.debug("------------- TUL  ------------");
                // TUL section
                cbhd._mTUL_Enabled = loadBoolean(lvl1.getChild("TUL_Enabled"));
                cbhd._mTUL_DispatcherInternalSensorLockToggle = loadSensor(lvl1.getChild("TUL_DispatcherInternalSensorLockToggle"), true);
                cbhd._mTUL_ExternalTurnout = loadTurnout(lvl1.getChild("TUL_ExternalTurnout"), lvl1.getChild("TUL_ExternalTurnoutFeedbackDifferent"));
                cbhd._mTUL_ExternalTurnoutFeedbackDifferent = loadBoolean(lvl1.getChild("TUL_ExternalTurnoutFeedbackDifferent"));
                cbhd._mTUL_DispatcherInternalSensorUnlockedIndicator = loadSensor(lvl1.getChild("TUL_DispatcherInternalSensorUnlockedIndicator"), true);
                cbhd._mTUL_NoDispatcherControlOfSwitch = loadBoolean(lvl1.getChild("TUL_NoDispatcherControlOfSwitch"));
                cbhd._mTUL_ndcos_WhenLockedSwitchStateIsClosed = loadBoolean(lvl1.getChild("TUL_ndcos_WhenLockedSwitchStateIsClosed"));
                cbhd._mTUL_GUI_IconsEnabled = loadBoolean(lvl1.getChild("TUL_GUI_IconsEnabled"));
                cbhd._mTUL_LockImplementation = CodeButtonHandlerData.LOCK_IMPLEMENTATION.getLockImplementation(loadInt(lvl1.getChild("TUL_LockImplementation")));
                loadAdditionalTurnouts(lvl1.getChild("TUL_AdditionalExternalTurnouts"), cbhd);

    log.debug("-------------  IL  ------------");
                // IL section
                cbhd._mIL_Enabled = loadBoolean(lvl1.getChild("IL_Enabled"));
                cbhd._mIL_Signals = getSignalList(lvl1.getChild("IL_Signals"));

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
        }
        convertCallOnSensorNamesToNBHSensors(cm);
        return true;
    }

    /**
     * Load the ProgramProperties class.
     * @param cm The CTC manager.
     * @param el The "ctcProperties" element.
     */
    void loadProperties(CtcManager cm, Element el) {
        ProgramProperties pp = cm.getProgramProperties();

        pp._mCodeButtonInternalSensorPattern = loadString(el.getChild("CodeButtonInternalSensorPattern"));
        pp._mSIDI_CodingTimeInMilliseconds = loadInt(el.getChild("SIDI_CodingTimeInMilliseconds"));
        pp._mSIDI_LeftInternalSensorPattern = loadString(el.getChild("SIDI_LeftInternalSensorPattern"));
        pp._mSIDI_NormalInternalSensorPattern = loadString(el.getChild("SIDI_NormalInternalSensorPattern"));
        pp._mSIDI_RightInternalSensorPattern = loadString(el.getChild("SIDI_RightInternalSensorPattern"));
        pp._mSIDI_TimeLockingTimeInMilliseconds = loadInt(el.getChild("SIDI_TimeLockingTimeInMilliseconds"));
        pp._mSIDL_LeftInternalSensorPattern = loadString(el.getChild("SIDL_LeftInternalSensorPattern"));
        pp._mSIDL_NormalInternalSensorPattern = loadString(el.getChild("SIDL_NormalInternalSensorPattern"));
        pp._mSIDL_RightInternalSensorPattern = loadString(el.getChild("SIDL_RightInternalSensorPattern"));
        pp._mSWDI_CodingTimeInMilliseconds = loadInt(el.getChild("SWDI_CodingTimeInMilliseconds"));
        pp._mSWDI_NormalInternalSensorPattern = loadString(el.getChild("SWDI_NormalInternalSensorPattern"));
        pp._mSWDI_ReversedInternalSensorPattern = loadString(el.getChild("SWDI_ReversedInternalSensorPattern"));
        pp._mSWDL_InternalSensorPattern = loadString(el.getChild("SWDL_InternalSensorPattern"));
        pp._mCO_CallOnToggleInternalSensorPattern = loadString(el.getChild("CO_CallOnToggleInternalSensorPattern"));
        pp._mTUL_DispatcherInternalSensorLockTogglePattern = loadString(el.getChild("TUL_DispatcherInternalSensorLockTogglePattern"));
        pp._mTUL_DispatcherInternalSensorUnlockedIndicatorPattern = loadString(el.getChild("TUL_DispatcherInternalSensorUnlockedIndicatorPattern"));
        pp._mCodeButtonDelayTime = loadInt(el.getChild("CodeButtonDelayTime"));

// Debugging aid -- not active due to SpotBugs
//             log.debug("ProgramProperties:");
//             List<Field> fields = Arrays.asList(ProgramProperties.class.getFields());
//             fields.forEach(field -> {
//                 try {
//                     log.info("    ProgramProperties: fld = {}, val = {}", field.getName(), field.get(pp));
//                 } catch (Exception ex) {
//                     log.info("    ProgramProperties list exception: {}", ex.getMessage());
//                 }
//             });
    }

    /**
     * Load the OtherData class.
     * @param cm The CTC manager.
     * @param el The "ctcOtherData" element.
     */
    void loadOtherData(CtcManager cm, Element el) {
        OtherData od = cm.getOtherData();

        String xmlVersion = loadString(el.getChild("CtcVersion"));
        xmlVersion = xmlVersion == null ? "v2.0" : xmlVersion;   // v2.0 is the initial version
        if (!xmlVersion.equals(OtherData.CTC_VERSION)) {
            log.warn("Update from version {} to version {} required", xmlVersion, OtherData.CTC_VERSION);
        }

//  Fleeting:
        od._mFleetingToggleInternalSensor = loadSensor(el.getChild("FleetingToggleInternalSensor"), true);
        od._mDefaultFleetingEnabled = loadBoolean(el.getChild("DefaultFleetingEnabled"));

//  Global startup:
        od._mTUL_EnabledAtStartup = loadBoolean(el.getChild("TUL_EnabledAtStartup"));
        od._mSignalSystemType = OtherData.SIGNAL_SYSTEM_TYPE.getSignalSystemType(loadInt(el.getChild("SignalSystemType")));
        od._mTUL_SecondsToLockTurnouts = loadInt(el.getChild("TUL_SecondsToLockTurnouts"));

//  Next unique # for each created Column:
        od._mNextUniqueNumber = loadInt(el.getChild("NextUniqueNumber"));

//  CTC Debugging:
        od._mCTCDebugSystemReloadInternalSensor = loadSensor(el.getChild("CTCDebugSystemReloadInternalSensor"), true);
        od._mCTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor = loadSensor(el.getChild("CTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor"), true);

//  GUI design:
        od._mGUIDesign_NumberOfEmptyColumnsAtEnd = loadInt(el.getChild("GUIDesign_NumberOfEmptyColumnsAtEnd"));
        od._mGUIDesign_CTCPanelType = OtherData.CTC_PANEL_TYPE.getRadioGroupValue(loadInt(el.getChild("GUIDesign_CTCPanelType")));
        od._mGUIDesign_BuilderPlate = loadBoolean(el.getChild("GUIDesign_BuilderPlate"));
        od._mGUIDesign_SignalsOnPanel = OtherData.SIGNALS_ON_PANEL.getRadioGroupValue(loadInt(el.getChild("GUIDesign_SignalsOnPanel")));
        od._mGUIDesign_FleetingToggleSwitch = loadBoolean(el.getChild("GUIDesign_FleetingToggleSwitch"));
        od._mGUIDesign_AnalogClockEtc = loadBoolean(el.getChild("GUIDesign_AnalogClockEtc"));
        od._mGUIDesign_ReloadCTCSystemButton = loadBoolean(el.getChild("GUIDesign_ReloadCTCSystemButton"));
        od._mGUIDesign_CTCDebugOnToggle = loadBoolean(el.getChild("GUIDesign_CTCDebugOnToggle"));
        od._mGUIDesign_CreateTrackPieces = loadBoolean(el.getChild("GUIDesign_CreateTrackPieces"));
        od._mGUIDesign_VerticalSize = OtherData.VERTICAL_SIZE.getRadioGroupValue(loadInt(el.getChild("GUIDesign_VerticalSize")));
        od._mGUIDesign_OSSectionUnknownInconsistentRedBlink = loadBoolean(el.getChild("GUIDesign_OSSectionUnknownInconsistentRedBlink"));
        od._mGUIDesign_TurnoutsOnPanel = loadBoolean(el.getChild("GUIDesign_TurnoutsOnPanel"));

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

    String loadString(Element element) {
        String newString = null;
        if (element != null) {
            newString = element.getValue();
        }
        return newString;
    }

    int loadInt(Element element) {
        int newInt = 0;
        if (element != null) {
            try {
                newInt = Integer.parseInt(element.getValue());
            } catch (NumberFormatException ex) {
                log.warn("loadInt format exception: element = {}, value = {}", element.getName(), element.getValue());
            }
        }
        return newInt;
    }

    boolean loadBoolean(Element element) {
        boolean newBoolean = false;
        if (element != null) {
            newBoolean = element.getValue().equals("true") ? true : false;
        }
        return newBoolean;
    }

    NBHSensor loadSensor(Element element, boolean isInternal) {
        NBHSensor sensor = null;
        if (element != null && element.getValue() != null && !element.getValue().isEmpty()) {
            String sensorName = element.getValue();
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

    NBHSignal loadSignal(Element element) {
        NBHSignal signal = null;
        if (element != null && element.getValue() != null && !element.getValue().isEmpty()) {
            String signalName  = element.getValue();
            signal = cm.getNBHSignal(signalName);
            if (signal == null) {
                signal = new NBHSignal(element.getValue());
            }
        } else {
            signal = new NBHSignal("");
        }
        return signal;
    }

    NBHTurnout loadTurnout(Element element, Element feedback) {
        NBHTurnout turnout = null;
        boolean feedBack = loadBoolean(feedback);
        if (element != null && element.getValue() != null && !element.getValue().isEmpty()) {
            String turnoutName = element.getValue();

            turnout = cm.getNBHTurnout(turnoutName);
            if (turnout == null) {
                turnout = new NBHTurnout("CtcManagerXml", "", element.getValue(), element.getValue(), feedBack);
            }
        } else {
            turnout = new NBHTurnout("CtcManagerXml", "Empty NBHTurnout", "");
        }
        return turnout;
    }

    NamedBeanHandle<Block> loadBlock(Element element) {
        NamedBeanHandle<Block> blockHandle = null;
        if (element != null && element.getValue() != null && !element.getValue().isEmpty()) {
            blockHandle = cm.getBlock(element.getValue());
            if (blockHandle == null) {
                Block block = InstanceManager.getDefault(BlockManager.class).getBlock(element.getValue());
                if (block != null) {
                    blockHandle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(element.getValue(), block);
                    cm.putBlock(element.getValue(), blockHandle);
                }
            }
        }
        return blockHandle;
    }

    // **** Load ArrayList objects ****

    ArrayList<NBHSensor> getSensorList(Element element) {
        ArrayList<NBHSensor> sensorList = new ArrayList<>();
        if (element != null) {
            for (Element el : element.getChildren()) {
                NBHSensor sensor = loadSensor(el, false);
                sensorList.add(sensor);
            }
        }
        return sensorList;
    }

    ArrayList<NBHSignal> getSignalList(Element element) {
        ArrayList<NBHSignal> signalList = new ArrayList<>();
        if (element != null) {
            for (Element el : element.getChildren()) {
                NBHSignal signal = loadSignal(el);
                signalList.add(signal);
            }
        }
        return signalList;
    }

    ArrayList<CallOnData> getCallOnList(Element element) {
        ArrayList<CallOnData> callOnList = new ArrayList<>();
        if (element != null) {
            for (Element elCallOn : element.getChildren()) {
                CallOnData cod = new CallOnData();
                cod._mExternalSignal = loadSignal(elCallOn.getChild("ExternalSignal"));
                cod._mSignalFacingDirection = loadString(elCallOn.getChild("SignalFacingDirection"));
                cod._mSignalAspectToDisplay = loadString(elCallOn.getChild("SignalAspectToDisplay"));
                cod._mCalledOnExternalSensor = loadSensor(elCallOn.getChild("CalledOnExternalSensor"), false);
                cod._mExternalBlock = loadBlock(elCallOn.getChild("ExternalBlock"));
                cod._mSwitchIndicators = new ArrayList<>();
                cod._mSwitchIndicatorNames = getCallOnSensorNames(elCallOn.getChild("SwitchIndicators"));
                callOnList.add(cod);
            }
        }
        return callOnList;
    }

    ArrayList<String> getCallOnSensorNames(Element element) {
        ArrayList<String> sensorList = new ArrayList<>();
        if (element != null) {
            for (Element el : element.getChildren()) {
                sensorList.add(el.getValue());
            }
        }
        return sensorList;
    }

    void convertCallOnSensorNamesToNBHSensors(CtcManager cm) {
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

    ArrayList<TrafficLockingData> getTrafficLocking(Element element) {
        ArrayList<TrafficLockingData> trlData = new ArrayList<>();
        if (element != null) {
            for (Element elRule : element.getChildren()) {
                TrafficLockingData trl = new TrafficLockingData();
                trl._mUserRuleNumber = loadString(elRule.getChild("UserRuleNumber"));
                trl._mRuleEnabled = loadString(elRule.getChild("RuleEnabled"));
                trl._mDestinationSignalOrComment = loadString(elRule.getChild("DestinationSignalOrComment"));

                trl._mSwitchAlignments = getTRLSwitchList(elRule.getChild("switches"));

                trl._mOccupancyExternalSensors = getSensorList(elRule.getChild("OccupancyExternalSensors"));
                trl._mOptionalExternalSensors = getSensorList(elRule.getChild("OptionalExternalSensors"));
                trlData.add(trl);
            }
        }
        return trlData;
    }

    ArrayList<TrafficLockingData.TRLSwitch> getTRLSwitchList(Element element) {
        ArrayList<TrafficLockingData.TRLSwitch> trlSwitches = new ArrayList<>();
        if (element != null) {
            for (Element elSwitch : element.getChildren()) {
                String userText = loadString(elSwitch.getChild("UserText"));
                if (userText != null && !userText.isEmpty()) {
                    TrafficLockingData.TRLSwitch newSwitch = new TrafficLockingData.TRLSwitch(
                            userText,
                            loadString(elSwitch.getChild("SwitchAlignment")),
                            loadInt(elSwitch.getChild("UniqueID")));
                    trlSwitches.add(newSwitch);
                }
            }
        }
        return trlSwitches;
    }

    void loadAdditionalTurnouts(Element element, CodeButtonHandlerData cbhd) {      // TUL_AdditionalExternalTurnouts
        if (element != null) {
            int rowNumber = 0;
            for (Element elTurnout : element.getChildren()) {       // TUL_AdditionalExternalTurnoutEntry
                rowNumber++;
                NBHTurnout turnout = loadTurnout(elTurnout.getChild("TUL_AdditionalExternalTurnout"), elTurnout.getChild("TUL_AdditionalExternalTurnoutFeedbackDifferent"));
                boolean feedback = loadBoolean(elTurnout.getChild("TUL_AdditionalExternalTurnoutFeedbackDifferent"));

                if (rowNumber == 1) {
                    cbhd._mTUL_AdditionalExternalTurnout1 = turnout;
                    cbhd._mTUL_AdditionalExternalTurnout1FeedbackDifferent = feedback;
                }
                if (rowNumber == 2) {
                    cbhd._mTUL_AdditionalExternalTurnout2 = turnout;
                    cbhd._mTUL_AdditionalExternalTurnout2FeedbackDifferent = feedback;
                }
                if (rowNumber == 3) {
                    cbhd._mTUL_AdditionalExternalTurnout3 = turnout;
                    cbhd._mTUL_AdditionalExternalTurnout3FeedbackDifferent = feedback;
                }
            }
        }
    }

    @Override
    public int loadOrder() {
        return InstanceManager.getDefault(jmri.jmrit.ctc.CtcManager.class).getXMLOrder();
    }

    private final static Logger log = LoggerFactory.getLogger(CtcManagerXml.class);
}
