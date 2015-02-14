/** 
 * SensorAddress.java
 *
 * Description:		Object to handle "user" and "system" sensor addresses
 *                  SensorManager is primary consumer of these
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri;


public class SensorAddress extends Address {

	public SensorAddress(String system, String user) {
		super(system, user);
	}
	
	/**
	 * both names are the same in this ctor
	 */
	public SensorAddress(String name) {
		super(name);
	}
}


/* @(#)SensorAddress.java */
