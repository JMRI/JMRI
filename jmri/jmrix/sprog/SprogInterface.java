// SprogInterface.java

package jmri.jmrix.sprog;

/**
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.2 $
 */

public interface SprogInterface {

	public void addSprogListener( SprogListener l);
	public void removeSprogListener( SprogListener l);

	boolean status();   // true if the implementation is operational

	void sendSprogMessage(SprogMessage m, SprogListener l);  // 2nd arg gets the reply
}


/* @(#)SprogInterface.java */
