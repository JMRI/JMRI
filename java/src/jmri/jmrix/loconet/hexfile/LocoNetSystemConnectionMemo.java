// LocoNetSystemConnectionMemo.java

package jmri.jmrix.loconet.hexfile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Lightweight class to denote that a system is active,
 * and provide general information.
 * <p>
 * Objects of specific subtypes are registered
 * in the instance manager to activate their
 * particular system.
 *
 * @author		Kevin Dickerson  Copyright (C) 2010
 * @version             $Revision: 22821 $
 */
@SuppressWarnings(value="NM_SAME_SIMPLE_NAME_AS_SUPERCLASS",justification="This is ineffect the same as its super class")
public class LocoNetSystemConnectionMemo extends jmri.jmrix.loconet.LocoNetSystemConnectionMemo {
    
    public jmri.jmrix.loconet.LnSensorManager getSensorManager() { 
        if (getDisabled())
            return null;
        if (sensorManager == null){
            sensorManager = new jmri.jmrix.loconet.hexfile.LnSensorManager(getLnTrafficController(), getSystemPrefix());
        }
        
        return /*(jmri.jmrix.loconet.LnSensorManager)*/ sensorManager;
    }

    static Logger log = LoggerFactory.getLogger(LocoNetSystemConnectionMemo.class.getName());   
}


/* @(#)LocoNetSystemConnectionMemo.java */
