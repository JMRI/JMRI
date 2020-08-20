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
        CtcManager cm = InstanceManager.getDefault(CtcManager.class);
        Element ctcdata = new Element("ctcdata");
        setStoreElementClass(ctcdata);

        ctcdata.addContent(storeProperties(cm));
        ctcdata.addContent(storeOtherData(cm));
        for (CodeButtonHandlerData cbhd : cm.getCTCSerialData().getCodeButtonHandlerDataArrayList()) {
            Element cbhdElement = new Element("ctcCodeButtonData");

            cbhdElement.addContent(storeInt("UniqueID", cbhd._mUniqueID));
            cbhdElement.addContent(storeInt("SwitchNumber", cbhd._mSwitchNumber));
            cbhdElement.addContent(storeInt("SignalEtcNumber", cbhd._mSignalEtcNumber));
            cbhdElement.addContent(storeInt("FileVersion", cbhd._mFileVersion));
            cbhdElement.addContent(storeInt("GUIColumnNumber", cbhd._mGUIColumnNumber));

            // Code section
            cbhdElement.addContent(storeString("CodeButtonInternalSensor", cbhd._mCodeButtonInternalSensor));
            cbhdElement.addContent(storeString("OSSectionOccupiedExternalSensor", cbhd._mOSSectionOccupiedExternalSensor));
            cbhdElement.addContent(storeString("OSSectionOccupiedExternalSensor2", cbhd._mOSSectionOccupiedExternalSensor2));
            cbhdElement.addContent(storeInt("OSSectionSwitchSlavedToUniqueID", cbhd._mOSSectionSwitchSlavedToUniqueID));
            cbhdElement.addContent(storeBoolean("GUIGeneratedAtLeastOnceAlready", cbhd._mGUIGeneratedAtLeastOnceAlready));
            cbhdElement.addContent(storeInt("CodeButtonDelayTime", cbhd._mCodeButtonDelayTime));

            // SIDI section
            cbhdElement.addContent(storeBoolean("SIDI_Enabled", cbhd._mSIDI_Enabled));
            cbhdElement.addContent(storeString("SIDI_LeftInternalSensor", cbhd._mSIDI_LeftInternalSensor));
            cbhdElement.addContent(storeString("SIDI_NormalInternalSensor", cbhd._mSIDI_NormalInternalSensor));
            cbhdElement.addContent(storeString("SIDI_RightInternalSensor", cbhd._mSIDI_RightInternalSensor));
            cbhdElement.addContent(storeInt("SIDI_CodingTimeInMilliseconds", cbhd._mSIDI_CodingTimeInMilliseconds));
            cbhdElement.addContent(storeInt("SIDI_TimeLockingTimeInMilliseconds", cbhd._mSIDI_TimeLockingTimeInMilliseconds));
            cbhdElement.addContent(storeSignalList("SIDI_LeftRightTrafficSignals", cbhd._mSIDI_LeftRightTrafficSignals));
            cbhdElement.addContent(storeSignalList("SIDI_RightLeftTrafficSignals", cbhd._mSIDI_RightLeftTrafficSignals));

            // SIDL section
            cbhdElement.addContent(storeBoolean("SIDL_Enabled", cbhd._mSIDL_Enabled));
            cbhdElement.addContent(storeString("SIDL_LeftInternalSensor", cbhd._mSIDL_LeftInternalSensor));
            cbhdElement.addContent(storeString("SIDL_NormalInternalSensor", cbhd._mSIDL_NormalInternalSensor));
            cbhdElement.addContent(storeString("SIDL_RightInternalSensor", cbhd._mSIDL_RightInternalSensor));

            // SWDI section
            cbhdElement.addContent(storeBoolean("SWDI_Enabled", cbhd._mSWDI_Enabled));
            cbhdElement.addContent(storeString("SWDI_NormalInternalSensor", cbhd._mSWDI_NormalInternalSensor));
            cbhdElement.addContent(storeString("SWDI_ReversedInternalSensor", cbhd._mSWDI_ReversedInternalSensor));
            cbhdElement.addContent(storeString("SWDI_ExternalTurnout", cbhd._mSWDI_ExternalTurnout));
            cbhdElement.addContent(storeInt("SWDI_CodingTimeInMilliseconds", cbhd._mSWDI_CodingTimeInMilliseconds));
            cbhdElement.addContent(storeBoolean("SWDI_FeedbackDifferent", cbhd._mSWDI_FeedbackDifferent));
            cbhdElement.addContent(storeInt("SWDI_GUITurnoutType", cbhd._mSWDI_GUITurnoutType.getInt()));
            cbhdElement.addContent(storeBoolean("SWDI_GUITurnoutLeftHand", cbhd._mSWDI_GUITurnoutLeftHand));
            cbhdElement.addContent(storeBoolean("SWDI_GUICrossoverLeftHand", cbhd._mSWDI_GUICrossoverLeftHand));

            // SWDL section
            cbhdElement.addContent(storeBoolean("SWDL_Enabled", cbhd._mSWDL_Enabled));
            cbhdElement.addContent(storeString("SWDL_InternalSensor", cbhd._mSWDL_InternalSensor));

            // CO section
            cbhdElement.addContent(storeBoolean("CO_Enabled", cbhd._mCO_Enabled));
            cbhdElement.addContent(storeString("CO_CallOnToggleInternalSensor", cbhd._mCO_CallOnToggleInternalSensor));
            cbhdElement.addContent(storeCallOnList("CO_GroupingsList", cbhd._mCO_GroupingsList));

            // TRL section
            cbhdElement.addContent(storeBoolean("TRL_Enabled", cbhd._mTRL_Enabled));
            cbhdElement.addContent(storeTRLRules("TRL_LeftRules", cbhd._mTRL_LeftTrafficLockingRules));
            cbhdElement.addContent(storeTRLRules("TRL_RightRules", cbhd._mTRL_RightTrafficLockingRules));

            // TUL section
            cbhdElement.addContent(storeBoolean("TUL_Enabled", cbhd._mTUL_Enabled));
            cbhdElement.addContent(storeString("TUL_DispatcherInternalSensorLockToggle", cbhd._mTUL_DispatcherInternalSensorLockToggle));
            cbhdElement.addContent(storeString("TUL_ExternalTurnout", cbhd._mTUL_ExternalTurnout));
            cbhdElement.addContent(storeBoolean("TUL_ExternalTurnoutFeedbackDifferent", cbhd._mTUL_ExternalTurnoutFeedbackDifferent));
            cbhdElement.addContent(storeString("TUL_DispatcherInternalSensorUnlockedIndicator", cbhd._mTUL_DispatcherInternalSensorUnlockedIndicator));
            cbhdElement.addContent(storeBoolean("TUL_NoDispatcherControlOfSwitch", cbhd._mTUL_NoDispatcherControlOfSwitch));
            cbhdElement.addContent(storeBoolean("TUL_ndcos_WhenLockedSwitchStateIsClosed", cbhd._mTUL_ndcos_WhenLockedSwitchStateIsClosed));
            cbhdElement.addContent(storeInt("TUL_LockImplementation", cbhd._mTUL_LockImplementation.getInt()));
            cbhdElement.addContent(storeTULAdditionalTurnouts("TUL_AdditionalExternalTurnouts", cbhd._mTUL_AdditionalExternalTurnouts));

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
        properties.addContent(storeBoolean("NoMoreReservedCharactersWarning", pp._mNoMoreReservedCharactersWarning));

        return properties;
    }

    Element storeOtherData(CtcManager cm) {
        OtherData od = cm.getOtherData();

        Element otherData = new Element("ctcOtherData");

        otherData.addContent(storeInt("FileVersion", od._mFileVersion));

//  Fleeting:
        otherData.addContent(storeString("FleetingToggleInternalSensor", od._mFleetingToggleInternalSensor));
        otherData.addContent(storeBoolean("DefaultFleetingEnabled", od._mDefaultFleetingEnabled));

//  Global startup:
        otherData.addContent(storeBoolean("TUL_EnabledAtStartup", od._mTUL_EnabledAtStartup));
        otherData.addContent(storeInt("SignalSystemType", od._mSignalSystemType.getInt()));
        otherData.addContent(storeInt("TUL_SecondsToLockTurnouts", od._mTUL_SecondsToLockTurnouts));

//  Next unique # for each created Column:
        otherData.addContent(storeInt("NextUniqueNumber", od._mNextUniqueNumber));

//  CTC Debugging:
        otherData.addContent(storeString("CTCDebugSystemReloadInternalSensor", od._mCTCDebugSystemReloadInternalSensor));
        otherData.addContent(storeString("CTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor", od._mCTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor));

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
            NamedBeanHandle handle = sensor.getBeanHandle();
            element.setText(handle.getName());
        }
        return element;
    }

    Element storeSignal(String elementName, NBHSignal signal) {
        Element element = new Element(elementName);
        if (signal != null) {
            NamedBeanHandle handle = (NamedBeanHandle) signal.getBeanHandle();
            element.setText(handle.getName());
        }
        return element;
    }

    Element storeBlock(String elementName, Block block) {
        Element element = new Element(elementName);
        if (block != null) {
            element.setText(block.getDisplayName());
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

    Element storeTULAdditionalTurnouts(String elementName, List<CodeButtonHandlerData.AdditionalTurnout> tulTurnouts) {
        Element element = new Element(elementName);
        tulTurnouts.forEach(tulTurnout -> {
            Element additionalTurnout = new Element("TUL_AdditionalExternalTurnoutEntry");
            additionalTurnout.addContent(storeString("TUL_AdditionalExternalTurnout", tulTurnout._mTUL_AdditionalExternalTurnout));
            additionalTurnout.addContent(storeBoolean("TUL_AdditionalExternalTurnoutFeedbackDifferent", tulTurnout._mTUL_AdditionalExternalTurnoutFeedbackDifferent));
            element.addContent(additionalTurnout);
        });
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
        CtcManager cm = InstanceManager.getDefault(CtcManager.class);
        List<Element> ctcList = sharedCtcData.getChildren();

        for (Element lvl1 : ctcList) {
            if (lvl1.getName() == "ctcProperties") {
                loadProperties(cm, lvl1);
                continue;
            }
            if (lvl1.getName() == "ctcOtherData") {
                loadOtherData(cm, lvl1);
                continue;
            }
            if (lvl1.getName() == "ctcCodeButtonData") {
                // Create basic CodeButtonHandlerData
                int _mUniqueID = loadInt(lvl1.getChild("UniqueID"));
                int _mSwitchNumber = loadInt(lvl1.getChild("SwitchNumber"));
                int _mSignalEtcNumber = loadInt(lvl1.getChild("SignalEtcNumber"));
                int _mGUIColumnNumber = loadInt(lvl1.getChild("GUIColumnNumber"));
                int _mFileVersion = loadInt(lvl1.getChild("FileVersion"));

                CodeButtonHandlerData cbhd = new CodeButtonHandlerData(_mUniqueID, _mSwitchNumber, _mSignalEtcNumber, _mGUIColumnNumber);
                cm.getCTCSerialData().addCodeButtonHandlerData(cbhd);

                // Code section
                cbhd._mCodeButtonInternalSensor = loadString(lvl1.getChild("CodeButtonInternalSensor"));
                cbhd._mOSSectionOccupiedExternalSensor = loadString(lvl1.getChild("OSSectionOccupiedExternalSensor"));
                cbhd._mOSSectionOccupiedExternalSensor2 = loadString(lvl1.getChild("OSSectionOccupiedExternalSensor2"));
                cbhd._mOSSectionSwitchSlavedToUniqueID = loadInt(lvl1.getChild("OSSectionSwitchSlavedToUniqueID"));
                cbhd._mGUIGeneratedAtLeastOnceAlready = loadBoolean(lvl1.getChild("GUIGeneratedAtLeastOnceAlready"));
                cbhd._mCodeButtonDelayTime = loadInt(lvl1.getChild("CodeButtonDelayTime"));

                // SIDI section
                cbhd._mSIDI_Enabled = loadBoolean(lvl1.getChild("SIDI_Enabled"));
                cbhd._mSIDI_LeftInternalSensor = loadString(lvl1.getChild("SIDI_LeftInternalSensor"));
                cbhd._mSIDI_NormalInternalSensor = loadString(lvl1.getChild("SIDI_NormalInternalSensor"));
                cbhd._mSIDI_RightInternalSensor = loadString(lvl1.getChild("SIDI_RightInternalSensor"));
                cbhd._mSIDI_CodingTimeInMilliseconds = loadInt(lvl1.getChild("SIDI_CodingTimeInMilliseconds"));
                cbhd._mSIDI_TimeLockingTimeInMilliseconds = loadInt(lvl1.getChild("SIDI_TimeLockingTimeInMilliseconds"));
                cbhd._mSIDI_LeftRightTrafficSignals = getSignalList(lvl1.getChild("SIDI_LeftRightTrafficSignals"));
                cbhd._mSIDI_RightLeftTrafficSignals = getSignalList(lvl1.getChild("SIDI_RightLeftTrafficSignals"));

                // SIDL section
                cbhd._mSIDL_Enabled = loadBoolean(lvl1.getChild("SIDL_Enabled"));
                cbhd._mSIDL_LeftInternalSensor = loadString(lvl1.getChild("SIDL_LeftInternalSensor"));
                cbhd._mSIDL_NormalInternalSensor = loadString(lvl1.getChild("SIDL_NormalInternalSensor"));
                cbhd._mSIDL_RightInternalSensor = loadString(lvl1.getChild("SIDL_RightInternalSensor"));

                // SWDI section
                cbhd._mSWDI_Enabled = loadBoolean(lvl1.getChild("SWDI_Enabled"));
                cbhd._mSWDI_NormalInternalSensor = loadString(lvl1.getChild("SWDI_NormalInternalSensor"));
                cbhd._mSWDI_ReversedInternalSensor = loadString(lvl1.getChild("SWDI_ReversedInternalSensor"));
                cbhd._mSWDI_ExternalTurnout = loadString(lvl1.getChild("SWDI_ExternalTurnout"));
                cbhd._mSWDI_CodingTimeInMilliseconds = loadInt(lvl1.getChild("SWDI_CodingTimeInMilliseconds"));
                cbhd._mSWDI_FeedbackDifferent = loadBoolean(lvl1.getChild("SWDI_FeedbackDifferent"));
                cbhd._mSWDI_GUITurnoutType = CodeButtonHandlerData.TURNOUT_TYPE.getTurnoutType(loadInt(lvl1.getChild("SWDI_GUITurnoutType")));
                cbhd._mSWDI_GUITurnoutLeftHand = loadBoolean(lvl1.getChild("SWDI_GUITurnoutLeftHand"));
                cbhd._mSWDI_GUICrossoverLeftHand = loadBoolean(lvl1.getChild("SWDI_GUICrossoverLeftHand"));

                // SWDL section
                cbhd._mSWDL_Enabled = loadBoolean(lvl1.getChild("SWDL_Enabled"));
                cbhd._mSWDL_InternalSensor = loadString(lvl1.getChild("SWDL_InternalSensor"));

                // CO section
                cbhd._mCO_Enabled = loadBoolean(lvl1.getChild("CO_Enabled"));
                cbhd._mCO_CallOnToggleInternalSensor = loadString(lvl1.getChild("CO_CallOnToggleInternalSensor"));
                cbhd._mCO_GroupingsList = getCallOnList(lvl1.getChild("CO_GroupingsList"));

                // TRL section
                cbhd._mTRL_Enabled = loadBoolean(lvl1.getChild("TRL_Enabled"));
                cbhd._mTRL_LeftTrafficLockingRules = getTrafficLocking(lvl1.getChild("TRL_LeftRules"));
                cbhd._mTRL_RightTrafficLockingRules = getTrafficLocking(lvl1.getChild("TRL_RightRules"));

                // TUL section
                cbhd._mTUL_Enabled = loadBoolean(lvl1.getChild("TUL_Enabled"));
                cbhd._mTUL_DispatcherInternalSensorLockToggle = loadString(lvl1.getChild("TUL_DispatcherInternalSensorLockToggle"));
                cbhd._mTUL_ExternalTurnout = loadString(lvl1.getChild("TUL_ExternalTurnout"));
                cbhd._mTUL_ExternalTurnoutFeedbackDifferent = loadBoolean(lvl1.getChild("TUL_ExternalTurnoutFeedbackDifferent"));
                cbhd._mTUL_DispatcherInternalSensorUnlockedIndicator = loadString(lvl1.getChild("TUL_DispatcherInternalSensorUnlockedIndicator"));
                cbhd._mTUL_NoDispatcherControlOfSwitch = loadBoolean(lvl1.getChild("TUL_NoDispatcherControlOfSwitch"));
                cbhd._mTUL_ndcos_WhenLockedSwitchStateIsClosed = loadBoolean(lvl1.getChild("TUL_ndcos_WhenLockedSwitchStateIsClosed"));
                cbhd._mTUL_LockImplementation = CodeButtonHandlerData.LOCK_IMPLEMENTATION.getLockImplementation(loadInt(lvl1.getChild("TUL_LockImplementation")));
                cbhd._mTUL_AdditionalExternalTurnouts = getAdditionalTurnouts(lvl1.getChild("TUL_AdditionalExternalTurnouts"));

                // IL section
                cbhd._mIL_Enabled = loadBoolean(lvl1.getChild("IL_Enabled"));
                cbhd._mIL_Signals = getSignalList(lvl1.getChild("IL_SignalNames"));

                if (log.isDebugEnabled()) {
                    log.info("CodeButtonHandlerData, {}/{}:", _mSwitchNumber, _mSignalEtcNumber);
                    List<Field> fields = Arrays.asList(CodeButtonHandlerData.class.getFields());
                    fields.forEach(field -> {
                        try {
                            log.info("    CBHD: fld = {}, type = {}, val = {}", field.getName(), field.getType(), field.get(cbhd));
                        } catch (Exception ex) {
                            log.info("    CBHD list exception: {}", ex.getMessage());
                        }
                    });
                }
            }
        }
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
        pp._mNoMoreReservedCharactersWarning = loadBoolean(el.getChild("NoMoreReservedCharactersWarning"));

        if (log.isDebugEnabled()) {
            log.debug("ProgramProperties:");
            List<Field> fields = Arrays.asList(ProgramProperties.class.getFields());
            fields.forEach(field -> {
                try {
                    log.info("    ProgramProperties: fld = {}, val = {}", field.getName(), field.get(pp));
                } catch (Exception ex) {
                    log.info("    ProgramProperties list exception: {}", ex.getMessage());
                }
            });
        }
    }

    /**
     * Load the OtherData class.
     * @param cm The CTC manager.
     * @param el The "ctcOtherData" element.
     */
    void loadOtherData(CtcManager cm, Element el) {
        OtherData od = cm.getOtherData();

        od._mFileVersion = loadInt(el.getChild("FileVersion"));

//  Fleeting:
        od._mFleetingToggleInternalSensor = loadString(el.getChild("FleetingToggleInternalSensor"));
        od._mDefaultFleetingEnabled = loadBoolean(el.getChild("DefaultFleetingEnabled"));

//  Global startup:
        od._mTUL_EnabledAtStartup = loadBoolean(el.getChild("TUL_EnabledAtStartup"));
        od._mSignalSystemType = OtherData.SIGNAL_SYSTEM_TYPE.getSignalSystemType(loadInt(el.getChild("SignalSystemType")));
        od._mTUL_SecondsToLockTurnouts = loadInt(el.getChild("TUL_SecondsToLockTurnouts"));

//  Next unique # for each created Column:
        od._mNextUniqueNumber = loadInt(el.getChild("NextUniqueNumber"));

//  CTC Debugging:
        od._mCTCDebugSystemReloadInternalSensor = loadString(el.getChild("CTCDebugSystemReloadInternalSensor"));
        od._mCTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor = loadString(el.getChild("CTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor"));

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

        if (log.isDebugEnabled()) {
            log.info("OtherData:");
            List<Field> fields = Arrays.asList(OtherData.class.getFields());
            fields.forEach(field -> {
                try {
                    log.info("    OtherData: fld = {}, type = {}, val = {}", field.getName(), field.getType(), field.get(od));
                } catch (Exception ex) {
                    log.info("    OtherData list exception: {}", ex.getMessage());
                }
            });
        }
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

    NBHSensor loadSensor(Element element) {
        NBHSensor sensor = null;
        if (element != null && element.getValue() != null && !element.getValue().isEmpty()) {
            sensor = new NBHSensor("CtcManagerXml", "", element.getValue(), element.getValue(), false);
        }
        return sensor;
    }

    NBHSignal loadSignal(Element element) {
        NBHSignal signal = null;
        if (element != null && element.getValue() != null && !element.getValue().isEmpty()) {
            signal = new NBHSignal(element.getValue());
        }
        return signal;
    }

    NBHTurnout loadTurnout(Element element) {
        NBHTurnout turnout = null;
        if (element != null && element.getValue() != null && !element.getValue().isEmpty()) {
            turnout = new NBHTurnout("CtcManagerXml", "", element.getValue(), element.getValue(), false);
        }
        return turnout;
    }

    Block loadBlock(Element element) {
        Block block = null;
        if (element != null && element.getValue() != null && !element.getValue().isEmpty()) {
            block = InstanceManager.getDefault(BlockManager.class).getBlock(element.getValue());
        }
        return block;
    }

    // **** Load ArrayList objects ****

    ArrayList<NBHSensor> getSensorList(Element element) {
        ArrayList<NBHSensor> sensorList = new ArrayList<>();
        if (element != null) {
            for (Element el : element.getChildren()) {
                NBHSensor sensor = loadSensor(el);
                if (sensor != null) {
                    sensorList.add(sensor);
                }
            }
        }
        return sensorList;
    }

    ArrayList<NBHSignal> getSignalList(Element element) {
        ArrayList<NBHSignal> signalList = new ArrayList<>();
        if (element != null) {
            for (Element el : element.getChildren()) {
                NBHSignal signal = loadSignal(el);
                if (signal != null) {
                    signalList.add(signal);
                }
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
                cod._mCalledOnExternalSensor = loadSensor(elCallOn.getChild("CalledOnExternalSensor"));
                cod._mExternalBlock = loadBlock(elCallOn.getChild("ExternalBlock"));
                cod._mSwitchIndicators = getSensorList(elCallOn.getChild("SwitchIndicators"));
                callOnList.add(cod);
            }
        }
        return callOnList;
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

    ArrayList<CodeButtonHandlerData.AdditionalTurnout> getAdditionalTurnouts(Element element) {
        ArrayList<CodeButtonHandlerData.AdditionalTurnout> tulTurnouts = new ArrayList<>();
        if (element != null) {
            for (Element elTurnout : element.getChildren()) {
                String turnout = loadString(elTurnout.getChild("TUL_AdditionalExternalTurnout"));
                if (turnout != null && !turnout.isEmpty()) {
                    CodeButtonHandlerData.AdditionalTurnout newAdditionalTurnout = new CodeButtonHandlerData.AdditionalTurnout(
                            turnout, loadBoolean(elTurnout.getChild("TUL_AdditionalExternalTurnoutFeedbackDifferent")));
                    tulTurnouts.add(newAdditionalTurnout);
                }
            }
        }
        return tulTurnouts;
    }

    @Override
    public int loadOrder() {
        return InstanceManager.getDefault(jmri.jmrit.ctc.CtcManager.class).getXMLOrder();
    }

    private final static Logger log = LoggerFactory.getLogger(CtcManagerXml.class);
}
