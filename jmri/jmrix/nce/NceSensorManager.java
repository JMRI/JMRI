// NceSensorManager.java

package jmri.jmrix.nce;

import jmri.JmriException;
import jmri.Sensor;

/**
 * Manage the NCE-specific Sensor implementation.
 *
 * System names are "NSnnn", where nnn is the sensor number without padding.
 *
 * @author			Bob Jacobsen Copyright (C) 2003
 * @version			$Revision: 1.1 $
 */
public class NceSensorManager extends jmri.AbstractSensorManager {
    
    // ABC implementations
    
    // to free resources when no longer used
    public void dispose() throws JmriException {
    }
    
    public Sensor newSensor(String systemName, String userName) { return null; }
    
    // NCE-specific methods

    // ctor has to register for  events
    public NceSensorManager() {
        super();
    }
    
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceSensorManager.class.getName());
}

/* @(#)NceTurnoutManager.java */
