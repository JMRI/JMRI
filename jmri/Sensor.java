/** 
 * Sensor.java
 *
 * Description:		<describe the Sensor interface here>
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri;


public interface Sensor {
	
	// user identification, unbound parameter
	public String getID();
	
	// states are parameters; both closed and thrown is possible!
	public static final int UNKNOWN      = 0x01;
	public static final int ACTIVE       = 0x02;
	public static final int INACTIVE     = 0x04;
	public static final int INCONSISTENT = 0x08;

	// known state on layout is a bound parameter -
	// always returns a answer, if need be the commanded state
	public int getKnownState();
	
	// request an update from the layout soft/hardware.  May not even
	// happen, and if it does it will happen later; listen for the result.
	public void requestUpdateFromLayout();
	
	// implementing classes will generally provide PropertyChangeListener
	// calls for KnownState
}


/* @(#)Sensor.java */
