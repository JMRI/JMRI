// AbstractSensorManager.java

package jmri;

import java.util.Hashtable;


/**
 * Abstract base implementation of the SensorManager interface
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.3 $
 */
public abstract class AbstractSensorManager implements SensorManager{

	// abstract methods to be provided by subclasses
	public abstract Sensor newSensor(String systemName, String userName);

	// abstract methods to be extended by subclasses
	// to free resources when no longer used
	public void dispose() throws JmriException {
		_tsys.clear();
		_tuser.clear();
	}

	// implemented methods
	protected Hashtable _tsys = new Hashtable();   // stores known Sensor instances by system name
	protected Hashtable _tuser = new Hashtable();   // stores known Sensor instances by user name

	public Sensor getBySystemName(String key) {
		return (Sensor)_tsys.get(key);
	}

	public Sensor getByUserName(String key) {
		return (Sensor)_tuser.get(key);
	}

}


/* @(#)AbstractTurnoutManager.java */
