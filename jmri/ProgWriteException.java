/** 
 * ProgWriteException.java
 *
 * Description:		<describe the ProgWriteException class here>
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */


// Represents a failure to write when programming

// No ACK is not a failure if the implementation does not expect to see one

package jmri;


public class ProgWriteException extends ProgrammerException {
	public ProgWriteException(String s) { super(s); }
	public ProgWriteException() {}

}


/* @(#)ProgWriteException.java */
