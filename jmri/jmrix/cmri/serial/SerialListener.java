/** 
 * SerialListener.java
 *
 * Description:		<describe the SerialListener class here>
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.cmri.serial;


public interface SerialListener extends java.util.EventListener{
	public void message(SerialMessage m);
	public void reply(SerialReply m);
}


/* @(#)SerialListener.java */
