// InternalSensorManager.java

package jmri.managers;

import jmri.AbstractSensorManager;
import jmri.AbstractSensor;
import jmri.Sensor;

/**
 * Implementation of the InternalSensorManager interface.
 * @author			Bob Jacobsen Copyright (C) 2001, 2003, 2006
 * @version			$Revision: 1.1 $
 */
public class InternalSensorManager extends AbstractSensorManager {

    /**
     * Create an internal (dummy) sensor object
     * @return new null
     */
    protected Sensor createNewSensor(String systemName, String userName) {
        return new AbstractSensor(systemName, userName){
            public void requestUpdateFromLayout(){}
        };
    }
	
	public char systemLetter() { return 'I'; }
	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(InternalSensorManager.class.getName());
}

/* @(#)InternalSensorManager.java */
