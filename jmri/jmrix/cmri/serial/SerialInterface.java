/**
 * SerialInterface.java
 *
 * Description:		<describe the SerialInterface class here>
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Id: SerialInterface.java,v 1.1 2002-03-03 05:50:45 jacobsen Exp $
 */

package jmri.jmrix.cmri.serial;


public interface SerialInterface {

	public void addSerialListener( SerialListener l);
	public void removeSerialListener( SerialListener l);

	boolean status();   // true if the implementation is operational

	void sendSerialMessage(SerialMessage m, SerialListener l);  // 2nd arg gets the reply
}


/* @(#)SerialInterface.java */
