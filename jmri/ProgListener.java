/** 
 * ProgListener.java
 *
 * Description:		Interface to be implemented by classes using Programmer.
 *                  This allows a Programmer object to return delayed status
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			
 */


// Note that callbacks will be in the Swing execution thread

package jmri;


public interface ProgListener extends java.util.EventListener{
	public void programmingOpReply(int value, int status);
	
	// these values are bit coded
	public final int OK 			= 0;
	public final int UnknownError 	= 1;
	public final int NoAck			= 2;
	public final int NoLocoDetected = 4;
	public final int ProgrammerBusy = 8;
	public final int NotImplemented = 0x10;
	public final int UserAborted    = 0x20;
		
}


/* @(#)LocoNetListener.java */
