/** 
 * InstanceManager.java
 *
 * Description:		static members for locating various interfaces
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri;
import jmri.Programmer;
import jmri.PowerManager;
import jmri.SensorManager;
import jmri.TurnoutManager;

public class InstanceManager {

	static public PowerManager powerManagerInstance()  { return _powerManager; }

	static public Programmer programmerInstance()  { return _programmer; }
	
	static public SensorManager sensorManagerInstance()  { return _sensorManager; }

	static public TurnoutManager turnoutManagerInstance()  { return _turnoutManager; }

	
	static private PowerManager _powerManager = null;
	static public void setPowerManager(PowerManager p) { 
		if (p!=_powerManager && p!=null && log.isInfoEnabled()) log.info("PowerManager instance is being replaced: "+p);
		_powerManager = p; 
	}

	static private Programmer _programmer = null;
	static public void setProgrammer(Programmer p) {
		if (p!=_programmer && p!=null && log.isInfoEnabled()) log.info("Programmer instance is being replaced: "+p);
		_programmer = p; 
	}

	static private SensorManager _sensorManager = null;
	static public void setSensorManager(SensorManager p) {
		if (p!=_sensorManager && p!=null && log.isInfoEnabled()) log.info("SensorManager instance is being replaced: "+p);
		_sensorManager = p; 
	}

	static private TurnoutManager _turnoutManager = null;
	static public void setTurnoutManager(TurnoutManager p) {
		if (p!=_turnoutManager && p!=null && log.isInfoEnabled()) log.info("TurnoutManager instance is being replaced: "+p);
		_turnoutManager = p; 
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(InstanceManager.class.getName());

}


/* @(#)InstanceManager.java */
