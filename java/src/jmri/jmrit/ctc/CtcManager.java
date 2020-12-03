package jmri.jmrit.ctc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jmri.Block;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.Manager;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.NamedBeanUsageReport;
import jmri.jmrit.ctc.ctcserialdata.*;
import jmri.jmrit.ctc.editor.code.*;

/**
 * Start the CtcManager and register with the instance and configuration managers.
 * <ul>
 *   <li>Create/provide the ProgramProperties instance</li>
 *   <li>Create/provide the CTCSerialData instance</li>
 *   <li>Provide the OtherData instance</li>
 *   <li>Provide hash maps of beans used by CTC</li>
 *   <li>Veto deletes for beans used by CTC</li>
 * </ul>
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class CtcManager implements InstanceManagerAutoDefault, java.beans.VetoableChangeListener {

    ProgramProperties programProperties = null;
    CTCSerialData ctcSerialData = null;
    HashMap<String, NBHSensor> nbhSensors = new HashMap<>();
    HashMap<String, NBHSignal> nbhSignals = new HashMap<>();
    HashMap<String, NBHTurnout> nbhTurnouts = new HashMap<>();
    HashMap<String, NamedBeanHandle<Block>> blocks = new HashMap<>();

    // Search results
    NBHSensor  foundSensor;
    NBHSignal  foundSignal;
    NBHTurnout foundTurnout;
    Block      foundBlock;

    List<NamedBeanUsageReport> usageReport;

    public CtcManager() {
        InstanceManager.setDefault(CtcManager.class, this);
        InstanceManager.getOptionalDefault(ConfigureManager.class).ifPresent(cm -> {
            cm.registerConfig(this, getXMLOrder());
        });
        InstanceManager.getDefault(jmri.SensorManager.class).addVetoableChangeListener(this);
        InstanceManager.getDefault(jmri.SignalHeadManager.class).addVetoableChangeListener(this);
        InstanceManager.getDefault(jmri.SignalMastManager.class).addVetoableChangeListener(this);
        InstanceManager.getDefault(jmri.TurnoutManager.class).addVetoableChangeListener(this);
        InstanceManager.getDefault(jmri.BlockManager.class).addVetoableChangeListener(this);
        log.debug("CtcManager started");  // NOI18N
    }

    public ProgramProperties getProgramProperties() {
        if (programProperties == null) {
            programProperties = new ProgramProperties();
        }
        return programProperties;
    }

    public ProgramProperties newProgramProperties() {
        programProperties = new ProgramProperties();
        return programProperties;
    }

    public CTCSerialData getCTCSerialData() {
        if (ctcSerialData == null) {
            ctcSerialData = new CTCSerialData();
        }
        return ctcSerialData;
    }

    public CTCSerialData newCTCSerialData() {
        ctcSerialData = new CTCSerialData();

        nbhSensors.clear();
        nbhSignals.clear();
        nbhTurnouts.clear();
        blocks.clear();

        return ctcSerialData;
    }

    public OtherData getOtherData() {
        if (ctcSerialData == null) {
            ctcSerialData = getCTCSerialData();
        }
        return ctcSerialData.getOtherData();
    }

    public NBHSensor getNBHSensor(String name) {
        // check for new names
        return nbhSensors.get(name);
    }

    public void putNBHSensor(String name, NBHSensor nbh) {
        NBHSensor oldSensor = nbhSensors.put(name, nbh);
        log.debug("sensor put = {} -- {}", name, nbh);  // NOI18N
        if (oldSensor != null) {
            log.debug("---- duplicate sensor: {} -- {}", name, nbh);  // NOI18N
        }
    }

    public NBHSignal getNBHSignal(String name) {
        // check for new names
        return nbhSignals.get(name);
    }

    public void putNBHSignal(String name, NBHSignal nbh) {
        NBHSignal oldSignal = nbhSignals.put(name, nbh);
        log.debug("signal put = {} -- {}", name, nbh);  // NOI18N
        if (oldSignal != null) {
            log.debug("---- duplicate signal: {} -- {}", name, nbh);  // NOI18N
        }
    }

    public NBHTurnout getNBHTurnout(String name) {
        // check for new names
        return nbhTurnouts.get(name);
    }

    public void putNBHTurnout(String name, NBHTurnout nbh) {
        NBHTurnout oldTurnout = nbhTurnouts.put(name, nbh);
        log.debug("turnout put = {} -- {}", name, nbh);  // NOI18N
        if (oldTurnout != null) {
            log.debug("---- duplicate turnout: {} -- {}", name, nbh);  // NOI18N
        }
    }

    public NamedBeanHandle<Block> getBlock(String name) {
        // check for new names
        return blocks.get(name);
    }

    public void putBlock(String name, NamedBeanHandle<Block> block) {
        NamedBeanHandle<Block> oldBlock = blocks.put(name, block);
        log.debug("block put = {} -- {}", name, block);  // NOI18N
        if (oldBlock != null) {
            log.debug("---- duplicate block: {} -- {}", name, block);  // NOI18N
        }
    }

    public int getXMLOrder() {
        return Manager.CTCDATA;
    }

    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        jmri.NamedBean nb = (jmri.NamedBean) evt.getOldValue();
        if ("CanDelete".equals(evt.getPropertyName())) { // NOI18N
            if (findNBHforBean(nb)) {
                java.beans.PropertyChangeEvent e = new java.beans.PropertyChangeEvent(this, "DoNotDelete", null, null);  // NOI18N
                throw new java.beans.PropertyVetoException(getVetoDetails(nb), e);
            }
        }
    }

    String getVetoDetails(NamedBean nb) {
        StringBuilder sb = new StringBuilder();
        sb.append(Bundle.getMessage("CtcManagerDeleteVetoed", nb.getBeanType()));  // NOI18N
        for (NamedBeanUsageReport report : getUsageReport(nb)) {
            sb.append(Bundle.getMessage("VetoDetailLine", report.usageData));    // NOI18N
        }
        return sb.toString();
    }

    boolean findNBHforBean(NamedBean nb) {
        if (nb == null) return false;
        boolean found = false;
        foundSensor = null;
        foundSignal = null;
        foundTurnout = null;
        foundBlock = null;

        if (nb instanceof jmri.Sensor) {
            for (NBHSensor sensor : nbhSensors.values()) {
                if (nb.equals(sensor.getBean())) {
                    foundSensor = sensor;
                    found = true;
                    break;
                }
            }
        }

        if (nb instanceof jmri.SignalHead || nb instanceof jmri.SignalMast) {
            for (NBHSignal signal : nbhSignals.values()) {
                if (nb.equals(signal.getBean())) {
                    foundSignal = signal;
                    found = true;
                    break;
                }
            }
        }

        if (nb instanceof jmri.Turnout) {
            for (NBHTurnout turnout : nbhTurnouts.values()) {
                if (nb.equals(turnout.getBean())) {
                    foundTurnout = turnout;
                    found = true;
                    break;
                }
            }
        }

        if (nb instanceof Block) {
            for (NamedBeanHandle<Block> block : blocks.values()) {
                if (nb.equals(block.getBean())) {
                    foundBlock = block.getBean();
                    found = true;
                    break;
                }
            }
        }
        return found;
    }

    public List<NamedBeanUsageReport> getUsageReport(NamedBean bean) {
        usageReport = new ArrayList<>();
        if (findNBHforBean(bean)) {
            // Other data
            if (getOtherData()._mFleetingToggleInternalSensor.equals(foundSensor) ||
                    getOtherData()._mCTCDebugSystemReloadInternalSensor.equals(foundSensor) ||
                    getOtherData()._mCTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor.equals(foundSensor)) {
                usageReport.add(new NamedBeanUsageReport("CtcWhereUsedOther", Bundle.getMessage("WhereUsedOther")));  // NOI18N
            }

            // O.S. Sections
            getCTCSerialData().getCodeButtonHandlerDataArrayList().forEach(cbhd -> {
                getCodeButtonHandleDataUsage(cbhd);
            });
        }
        return usageReport;
    }

    void getCodeButtonHandleDataUsage(CodeButtonHandlerData cbhd) {
        String osName = cbhd.myShortStringNoComma();

        // CB Sensors
        if (cbhd._mCodeButtonInternalSensor.equals(foundSensor) ||
                cbhd._mOSSectionOccupiedExternalSensor.equals(foundSensor) ||
                cbhd._mOSSectionOccupiedExternalSensor2.equals(foundSensor)) {
            usageReport.add(new NamedBeanUsageReport("CtcWhereUsedCBHD", Bundle.getMessage("WhereUsedSensor", osName, "CB")));  // NOI18N
        }

        // SIDI Sensors
        if (cbhd._mSIDI_LeftInternalSensor.equals(foundSensor) ||
                cbhd._mSIDI_NormalInternalSensor.equals(foundSensor) ||
                cbhd._mSIDI_RightInternalSensor.equals(foundSensor)) {
            usageReport.add(new NamedBeanUsageReport("CtcWhereUsedCBHD", Bundle.getMessage("WhereUsedSensor", osName, "SIDI")));  // NOI18N
        }

        // SIDI Signals
        cbhd._mSIDI_LeftRightTrafficSignals.forEach(signal -> {
            if (signal.equals(foundSignal)) {
                usageReport.add(new NamedBeanUsageReport("CtcWhereUsedCBHD", Bundle.getMessage("WhereUsedSignal", osName, "SIDI")));  // NOI18N
            }
        });
        cbhd._mSIDI_RightLeftTrafficSignals.forEach(signal -> {
            if (signal.equals(foundSignal)) {
                usageReport.add(new NamedBeanUsageReport("CtcWhereUsedCBHD", Bundle.getMessage("WhereUsedSignal", osName, "SIDI")));  // NOI18N
            }
        });

        // SIDL Sensors
        if (cbhd._mSIDL_LeftInternalSensor.equals(foundSensor) ||
                cbhd._mSIDL_NormalInternalSensor.equals(foundSensor) ||
                cbhd._mSIDL_RightInternalSensor.equals(foundSensor)) {
            usageReport.add(new NamedBeanUsageReport("CtcWhereUsedCBHD", Bundle.getMessage("WhereUsedSensor", osName, "SIDL")));  // NOI18N
        }

        // SWDI Sensors
        if (cbhd._mSWDI_NormalInternalSensor.equals(foundSensor) ||
                cbhd._mSWDI_ReversedInternalSensor.equals(foundSensor)) {
            usageReport.add(new NamedBeanUsageReport("CtcWhereUsedCBHD", Bundle.getMessage("WhereUsedSensor", osName, "SWDI")));  // NOI18N
        }

        // SWDI Turnout
        if (cbhd._mSWDI_ExternalTurnout.equals(foundTurnout)) {
            usageReport.add(new NamedBeanUsageReport("CtcWhereUsedCBHD", Bundle.getMessage("WhereUsedTurnout", osName, "SWDI")));  // NOI18N
        }

        // SWDL Sensor
        if (cbhd._mSWDL_InternalSensor.equals(foundSensor)) {
            usageReport.add(new NamedBeanUsageReport("CtcWhereUsedCBHD", Bundle.getMessage("WhereUsedSensor", osName, "SWDL")));  // NOI18N
        }

        callOnDataUsage(cbhd, osName);
        traffficLockingDataUsage(cbhd, osName);

        // TUL Sensors
        if (cbhd._mTUL_DispatcherInternalSensorLockToggle.equals(foundSensor) ||
                cbhd._mTUL_DispatcherInternalSensorUnlockedIndicator.equals(foundSensor)) {
            usageReport.add(new NamedBeanUsageReport("CtcWhereUsedCBHD", Bundle.getMessage("WhereUsedSensor", osName, "TUL")));  // NOI18N
        }

        // TUL Turnouts
        if (cbhd._mTUL_ExternalTurnout.equals(foundTurnout) ||
                cbhd._mTUL_AdditionalExternalTurnout1.equals(foundTurnout) ||
                cbhd._mTUL_AdditionalExternalTurnout2.equals(foundTurnout) ||
                cbhd._mTUL_AdditionalExternalTurnout3.equals(foundTurnout)) {
            usageReport.add(new NamedBeanUsageReport("CtcWhereUsedCBHD", Bundle.getMessage("WhereUsedTurnout", osName, "TUL")));  // NOI18N
        }

        // IL Signals
        cbhd._mIL_Signals.forEach(signal -> {
            if (signal.equals(foundSignal)) {
                usageReport.add(new NamedBeanUsageReport("CtcWhereUsedCBHD", Bundle.getMessage("WhereUsedSignal", osName, "IL")));  // NOI18N
            }
        });
    }

    void callOnDataUsage(CodeButtonHandlerData cbhd, String osName) {
        // CO Sensor
        if (cbhd._mCO_CallOnToggleInternalSensor.equals(foundSensor)) {
            usageReport.add(new NamedBeanUsageReport("CtcWhereUsedCBHD", Bundle.getMessage("WhereUsedSensor", osName, "CO")));  // NOI18N
        }
        cbhd._mCO_GroupingsList.forEach(row -> {
            // Sensor
            if (row._mCalledOnExternalSensor.equals(foundSensor)) {
                usageReport.add(new NamedBeanUsageReport("CtcWhereUsedCBHD", Bundle.getMessage("WhereUsedSensor", osName, "CO")));  // NOI18N
            }

            // Signal
            if (row._mExternalSignal.equals(foundSignal)) {
                usageReport.add(new NamedBeanUsageReport("CtcWhereUsedCBHD", Bundle.getMessage("WhereUsedSignal", osName, "CO")));  // NOI18N
            }

            // Block
            if (row._mExternalBlock != null && row._mExternalBlock.getBean().equals(foundBlock)) {
                usageReport.add(new NamedBeanUsageReport("CtcWhereUsedCBHD", Bundle.getMessage("WhereUsedBlock", osName, "CO")));  // NOI18N
            }

            // Switch indicator sensors
            row._mSwitchIndicators.forEach(sw -> {
                if (sw.equals(foundSensor)) {
                    usageReport.add(new NamedBeanUsageReport("CtcWhereUsedCBHD", Bundle.getMessage("WhereUsedSensor", osName, "CO")));  // NOI18N
                }
            });
        });
    }

    void traffficLockingDataUsage(CodeButtonHandlerData cbhd, String osName) {
        cbhd._mTRL_LeftTrafficLockingRules.forEach(rule -> {
            traffficLockingRuleDataUsage(rule, osName);
        });

        cbhd._mTRL_RightTrafficLockingRules.forEach(rule -> {
            traffficLockingRuleDataUsage(rule, osName);
        });
    }

    void traffficLockingRuleDataUsage(TrafficLockingData rule, String osName) {
        // Signal -- _mDestinationSignalOrComment
        if (foundSignal != null) {
            if (rule._mDestinationSignalOrComment.equals(foundSignal.getHandleName())) {
                usageReport.add(new NamedBeanUsageReport("CtcWhereUsedCBHD", Bundle.getMessage("WhereUsedSignal", osName, "TRL")));  // NOI18N
            }
        }

        // Occupancy sensors
        for (NBHSensor sensor : rule._mOccupancyExternalSensors) {
            if (sensor.equals(foundSensor)) {
                usageReport.add(new NamedBeanUsageReport("CtcWhereUsedCBHD", Bundle.getMessage("WhereUsedSensor", osName, "TRL")));  // NOI18N
                break;
            }
        }

        // Optional sensors
        for (NBHSensor sensor : rule._mOptionalExternalSensors) {
            if (sensor.equals(foundSensor)) {
                usageReport.add(new NamedBeanUsageReport("CtcWhereUsedCBHD", Bundle.getMessage("WhereUsedSensor", osName, "TRL")));  // NOI18N
                break;
            }
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CtcManager.class);
}
