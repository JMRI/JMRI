/** 
 * Turnout.java
 *
 * Description:		<describe the Turnout interface here>
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri;


public interface Turnout {

	// user identification
	public String id();
	
	// state is a parameter; both closed and thrown is possible!
	public static final int CLOSED    = 0x02;
	public static final int THROWN    = 0x04;

	// state known
	
	// state commanded
	
		
}


/* @(#)Turnout.java */
