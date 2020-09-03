package jmri.jmrit.ctc;

import java.util.HashMap;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.Manager;
import jmri.jmrit.ctc.*;
import jmri.jmrit.ctc.ctcserialdata.*;
import jmri.jmrit.ctc.editor.code.*;

/**
 * Start the CtcManager and register with the instance and configuration managers.
 * <ul>
 *   <li>Create/provide the ProgramProperties instance</li>
 *   <li>Create/provide the CTCSerialData instance</li>
 *   <li>Provide the OtherData instance</li>
 * </ul>
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class CtcManager implements InstanceManagerAutoDefault {

    ProgramProperties programProperties = null;
    CTCSerialData ctcSerialData = null;
    HashMap<String, NBHSensor> nbhSensors = new HashMap<>();
    HashMap<String, NBHSignal> nbhSignals = new HashMap<>();
    HashMap<String, NBHTurnout> nbhTurnouts = new HashMap<>();


    public CtcManager() {
        InstanceManager.setDefault(CtcManager.class, this);
        InstanceManager.getOptionalDefault(ConfigureManager.class).ifPresent(cm -> {
            cm.registerConfig(this, getXMLOrder());
        });
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
        log.info("sensor put = {} -- {}", name, nbh);
        if (oldSensor != null) {
            log.info("---- duplicate sensor: {} -- {}", name, nbh);
            // cleanup after replace
        }
        // Set veto listener
    }

    public NBHSignal getNBHSignal(String name) {
        // check for new names
        return nbhSignals.get(name);
    }

    public void putNBHSignal(String name, NBHSignal nbh) {
        NBHSignal oldSignal = nbhSignals.put(name, nbh);
        log.info("signal put = {} -- {}", name, nbh);
        if (oldSignal != null) {
            log.info("---- duplicate signal: {} -- {}", name, nbh);
            // cleanup after replace
        }
        // Set veto listener
    }

    public NBHTurnout getNBHTurnout(String name) {
        // check for new names
        return nbhTurnouts.get(name);
    }

    public void putNBHTurnout(String name, NBHTurnout nbh) {
        NBHTurnout oldTurnout = nbhTurnouts.put(name, nbh);
        log.info("turnout put = {} -- {}", name, nbh);
        if (oldTurnout != null) {
            log.info("---- duplicate turnout: {} -- {}", name, nbh);
            // cleanup after replace
        }
        // Set veto listener
    }

    public int getXMLOrder() {
        return Manager.CTCDATA;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CtcManager.class);
}
