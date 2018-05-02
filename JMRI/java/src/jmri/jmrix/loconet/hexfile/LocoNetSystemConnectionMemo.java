package jmri.jmrix.loconet.hexfile;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Kevin Dickerson Copyright (C) 2010
 */
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "This is ineffect the same as its super class")
public class LocoNetSystemConnectionMemo extends jmri.jmrix.loconet.LocoNetSystemConnectionMemo {

    @Override
    public jmri.jmrix.loconet.LnSensorManager getSensorManager() {
        if (getDisabled()) {
            return null;
        }
        if (sensorManager == null) {
            sensorManager = new jmri.jmrix.loconet.hexfile.LnSensorManager(getLnTrafficController(), getSystemPrefix());
        }

        return /*(jmri.jmrix.loconet.LnSensorManager)*/ sensorManager;
    }
}
