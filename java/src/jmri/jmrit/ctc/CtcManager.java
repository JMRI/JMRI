package jmri.jmrit.ctc;

import java.util.HashMap;
import jmri.Block;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.Manager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
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
        log.debug("CtcManager started");
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
        log.debug("sensor put = {} -- {}", name, nbh);
        if (oldSensor != null) {
            log.debug("---- duplicate sensor: {} -- {}", name, nbh);
            // cleanup after replace an existing sensor may have a different name
        }
//         nbh.getBean().addPropertyChangeListener(this, nbh.getHandleName(), "somethng");
//                         _turnout.getBean().addPropertyChangeListener(this, getName(), "Route " + getDisplayName() + " Output Turnout");

    }

    public NBHSignal getNBHSignal(String name) {
        // check for new names
        return nbhSignals.get(name);
    }

    public void putNBHSignal(String name, NBHSignal nbh) {
        NBHSignal oldSignal = nbhSignals.put(name, nbh);
        log.debug("signal put = {} -- {}", name, nbh);
        if (oldSignal != null) {
            log.debug("---- duplicate signal: {} -- {}", name, nbh);
            // cleanup after replace
        }
    }

    public NBHTurnout getNBHTurnout(String name) {
        // check for new names
        return nbhTurnouts.get(name);
    }

    public void putNBHTurnout(String name, NBHTurnout nbh) {
        NBHTurnout oldTurnout = nbhTurnouts.put(name, nbh);
        log.debug("turnout put = {} -- {}", name, nbh);
        if (oldTurnout != null) {
            log.debug("---- duplicate turnout: {} -- {}", name, nbh);
            // cleanup after replace
        }
    }

    public NamedBeanHandle<Block> getBlock(String name) {
        // check for new names
        return blocks.get(name);
    }

    public void putBlock(String name, NamedBeanHandle<Block> block) {
        NamedBeanHandle<Block> oldBlock = blocks.put(name, block);
        log.debug("block put = {} -- {}", name, block);
        if (oldBlock != null) {
            log.debug("---- duplicate block: {} -- {}", name, block);
            // cleanup after replace
        }
    }

    public int getXMLOrder() {
        return Manager.CTCDATA;
    }

    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        jmri.NamedBean nb = (jmri.NamedBean) evt.getOldValue();
        boolean found = false;
        if ("CanDelete".equals(evt.getPropertyName())) { // NOI18N

            if (nb instanceof jmri.Sensor) {
                for (NBHSensor sensor : nbhSensors.values()) {
                    if (nb.equals(sensor.getBean())) {
                        found = true;
                        break;
                    }
                }
            }

            if (nb instanceof jmri.SignalHead || nb instanceof jmri.SignalMast) {
                for (NBHSignal signal : nbhSignals.values()) {
                    if (nb.equals(signal.getBean())) {
                        found = true;
                        break;
                    }
                }
            }

            if (nb instanceof jmri.Turnout) {
                for (NBHTurnout turnout : nbhTurnouts.values()) {
                    if (nb.equals(turnout.getBean())) {
                        found = true;
                        break;
                    }
                }
            }

            if (nb instanceof Block) {
                for (NamedBeanHandle<Block> block : blocks.values()) {
                    if (nb.equals(block.getBean())) {
                        found = true;
                        break;
                    }
                }
            }

            if (found) {
                java.beans.PropertyChangeEvent e = new java.beans.PropertyChangeEvent(this, "DoNotDelete", null, null);  // NOI18N
                throw new java.beans.PropertyVetoException(Bundle.getMessage("CtcManagerDeleteVetoed", nb.getBeanType()), e);   // NOI18N
            }
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CtcManager.class);
}
