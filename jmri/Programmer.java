/** 
 * Programmer.java
 *
 * Description:		<describe the Programmer interface here>
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri;

import jmri.ProgListener;

public interface Programmer  {

	// mode e.g. register, direct, paged
	public static final int REGISTERMODE = 11;
	public static final int PAGEMODE     = 12;
	public static final int DIRECTMODE   = 13;
	public static final int ADDRESSMODE  = 14;
	
	// write CV
	public void writeCV(int CV, int val, int mode, ProgListener p) throws ProgrammerException;
	
	// read CV
	public void readCV(int CV, int mode, ProgListener p) throws ProgrammerException;
	
	// error handling on request is via exceptions
	// results are returned via the ProgListener callback
	
	// special case for CV18/19 double write?	
	// access to direct mode bit operations?
	// programming on the main / ops mode?
	
	// in use? By who?
	
	// support for more than one programmer? For getting one?
	
	
}


/* @(#)Programmer.java */
