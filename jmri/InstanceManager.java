/** 
 * InstanceManager.java
 *
 * Description:		static members for locating various interfaces
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri;
import jmri.Programmer;

public class InstanceManager {

	static public Programmer programmerInstance()  { return _programmer; }
	
	
	static private Programmer _programmer = null;
	static public void setProgrammer(Programmer p) { _programmer = p; }

}


/* @(#)InstanceManager.java */
