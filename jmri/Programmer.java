/** 
 * Programmer.java
 *
 * Description:		<describe the Programmer interface here>
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri;


public interface Programmer  {

	// mode e.g. register, direct, paged - returns false if not available
	public boolean mode(int m) throws ProgrammerException;
	
	public static final int REGISTERMODE = 11;
	public static final int PAGEMODE     = 12;
	public static final int DIRECTMODE   = 13;
	public static final int ADDRESSMODE  = 14;
	
	// write CV  (returns boolean for ACK, not exception)
	public boolean writeCV(int CV, int val) throws ProgrammerException;
	
	// read CV
	public int readCV(int CV) throws ProgrammerException;

	// locomotive detected present on track
	public boolean locoPresent() throws ProgrammerException;
	
	// error handling is via exceptions
	// special case for CV18/19 double write?
	
	// access to direct mode bit operations?
	
	// programming on the main / ops mode?
	
	// in use? By who?
	
	// support for more than one? For getting one?
	
	
}


/* @(#)Turnout.java */
