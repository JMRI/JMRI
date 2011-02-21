// InternalSensorManager.java

package jmri.jmrix.internal;

/**
 * Implementation of the InternalSensorManager interface.
 * @author			Bob Jacobsen Copyright (C) 2001, 2003, 2006
 * @version			$Revision: 1.1 $
 */
public class InternalSensorManager extends jmri.managers.InternalSensorManager {

    public InternalSensorManager(String prefix){
        super();
        this.prefix = prefix;
    }
        
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(InternalSensorManager.class.getName());
}

/* @(#)InternalSensorManager.java */
