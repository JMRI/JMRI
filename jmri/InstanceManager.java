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

public class InstanceManager {

	static public PowerManager powerManagerInstance()  { return _powerManager; }

	static public Programmer programmerInstance()  { return _programmer; }
	
	
	static private PowerManager _powerManager = null;
	static public void setPowerManager(PowerManager p) { _powerManager = p; }

	static private Programmer _programmer = null;
	static public void setProgrammer(Programmer p) { _programmer = p; }
}


/* @(#)InstanceManager.java */
