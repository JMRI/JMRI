/**
 * SprogInterface.java
 *
 * Description:		<describe the SprogInterface class here>
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Id: SprogInterface.java,v 1.1 2003-01-27 05:24:00 jacobsen Exp $
 */

package jmri.jmrix.sprog;


public interface SprogInterface {

	public void addSprogListener( SprogListener l);
	public void removeSprogListener( SprogListener l);

	boolean status();   // true if the implementation is operational

	void sendSprogMessage(SprogMessage m, SprogListener l);  // 2nd arg gets the reply
}


/* @(#)SprogInterface.java */
