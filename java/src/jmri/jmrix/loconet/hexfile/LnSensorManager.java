// LnSensorManager.java

package jmri.jmrix.loconet.hexfile;

import jmri.jmrix.loconet.LnSensor;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.Sensor;
import jmri.JmriException;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
@SuppressWarnings(value="NM_SAME_SIMPLE_NAME_AS_SUPERCLASS",justification="This is ineffect the same as its super class")
/**
 * Manage the LocoNet-specific Sensor implementation.
 *
 * System names are "LSnnn", where nnn is the sensor number without padding.
 *
 * @author			Kevin Dickerson Copyright (C) 2001
 * @version			$Revision: 22821 $
 */

public class LnSensorManager extends jmri.jmrix.loconet.LnSensorManager implements LocoNetListener {

    public LnSensorManager(LnTrafficController tc, String prefix) {
        super(tc, prefix);
    }
    

    // LocoNet-specific methods

    public Sensor createNewSensor(String systemName, String userName) {
        Sensor s = new LnSensor(systemName, userName, tc, prefix);
        if(defaultSensorState!=Sensor.UNKNOWN){
            try {
                s.setKnownState(defaultSensorState);
            } catch (JmriException e) { log.warn("Error setting state: "+e); }
        }
        return s;
    }
    
    int defaultSensorState = Sensor.UNKNOWN;
    
    public void setDefaultSensorState(String state){
        if (state.equals(Bundle.getMessage("SensorStateInactive")))
            defaultSensorState=Sensor.INACTIVE;
        else if (state.equals(Bundle.getMessage("SensorStateActive")))
            defaultSensorState=Sensor.ACTIVE;
        else
            defaultSensorState = Sensor.UNKNOWN;
    }
    
     static Logger log = LoggerFactory.getLogger(LnSensorManager.class.getName());   
}

/* @(#)LnSensorManager.java */
