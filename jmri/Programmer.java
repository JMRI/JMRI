/** 
 * Programmer.java
 *
 * Description:		<describe the Programmer interface here>
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri;

import jmri.ProgListener;
import java.beans.PropertyChangeListener;

public interface Programmer  {

	// mode e.g. register, direct, paged
	public static final int REGISTERMODE    = 11;
	public static final int PAGEMODE        = 21;
	public static final int DIRECTBITMODE   = 31;
	public static final int DIRECTBYTEMODE  = 32;
	public static final int ADDRESSMODE     = 41;
	
	// write CV
	public void writeCV(int CV, int val, ProgListener p) throws ProgrammerException;
	
	// read CV
	public void readCV(int CV, ProgListener p) throws ProgrammerException;
	
	/* 
	 * Mode is a property that can be set and queried for the
	 * programmer.  Notification is also possible...
	 */
	public void setMode(int mode);
	public int  getMode();
	 
	public void addPropertyChangeListener(PropertyChangeListener p);
	public void removePropertyChangeListener(PropertyChangeListener p);
	 
	// error handling on request is via exceptions
	// results are returned via the ProgListener callback
	
	// special case for CV18/19 double write?	
	// access to direct mode bit operations?
	// programming on the main / ops mode?
	
	// in use? By who?
	
	// support for more than one programmer? For getting one?
	
	
}


/* @(#)Programmer.java */
