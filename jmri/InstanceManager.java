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
	static public void setPowerManager(PowerManager p) { _powerManager = p; }

	static private Programmer _programmer = null;
	static public void setProgrammer(Programmer p) { _programmer = p; }

	static private SensorManager _sensorManager = null;
	static public void setSensorManager(SensorManager p) { _sensorManager = p; }

	static private TurnoutManager _turnoutManager = null;
	static public void setTurnoutManager(TurnoutManager s) { _turnoutManager = s; }
}


/* @(#)InstanceManager.java */
