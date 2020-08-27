package jmri.jmrit.ctc;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.Manager;
import jmri.jmrit.ctc.ctcserialdata.*;
import jmri.jmrit.ctc.editor.code.*;

/**
 * Start the CtcManager and register with the instance and configuration managers.
 * <p>
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

    public int getXMLOrder() {
        return Manager.CTCDATA;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CtcManager.class);
}
