package jmri.jmrix.loconet.hexfile;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.JmriException;
import jmri.Sensor;
import jmri.jmrix.loconet.LnSensor;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "This is ineffect the same as its super class")
/**
 * Manage the LocoNet-specific Sensor implementation via a LocoNet
 * hexfile emulator.
 * <p>
 * System names are "LSnnn", where L is the user-configurable system prefix,
 * nnn is the sensor number without padding.
 *
 * @author Kevin Dickerson Copyright (C) 2001
 */
public class LnSensorManager extends jmri.jmrix.loconet.LnSensorManager {

    public LnSensorManager(LocoNetSystemConnectionMemo memo) {
        super(memo);
    }

    // LocoNet-specific methods
    @Override
    public Sensor createNewSensor(String systemName, String userName) {
        Sensor s = new LnSensor(systemName, userName, tc, getSystemPrefix());
        if (defaultSensorState != Sensor.UNKNOWN) {
            try {
                s.setKnownState(defaultSensorState);
            } catch (JmriException e) {
                log.warn("Error setting state: " + e);
            }
        }
        return s;
    }

    int defaultSensorState = Sensor.UNKNOWN;

    public void setDefaultSensorState(String state) {
        if (state.equals(Bundle.getMessage("SensorStateInactive"))) {
            defaultSensorState = Sensor.INACTIVE;
        } else if (state.equals(Bundle.getMessage("SensorStateActive"))) {
            defaultSensorState = Sensor.ACTIVE;
        } else {
            defaultSensorState = Sensor.UNKNOWN;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LnSensorManager.class);

}
