/** 
 * Turnout.java
 *
 * Description:		<describe the Turnout interface here>
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri;


public interface Turnout {

	// user identification, unbound parameter
	public String getID();
	public void   setID(String s);
	
	// states are parameters; both closed and thrown is possible!
	public static final int UNKNOWN      = 0x01;
	public static final int CLOSED       = 0x02;
	public static final int THROWN       = 0x04;
	public static final int INCONSISTENT = 0x08;

	// known state on layout is a bound parameter -
	// always returns a answer, if need be the commanded state
	public int getKnownState();

	// state commanded is a bound parameter
	public void setCommandedState(int s) throws jmri.JmriException;
	public int getCommandedState();
	
	// feedbackType is an unbound parameter; many possible forms....
	public static final int NONE     = 0;  // only commanded state is known
	// UNKNOWN is also possible
	public static final int EXACT    = 2;  // both open and thrown actively sensed
	public static final int INDIRECT = 3;  // only one side directly sensed
	public static final int SENT     = 4;  // based on command seen on rails/bus
	
	public int getFeedbackType();
	
	// implementing classes will generally provide PropertyChangeListener
	// calls for KnownState and CommandedState
}


/* @(#)Turnout.java */
