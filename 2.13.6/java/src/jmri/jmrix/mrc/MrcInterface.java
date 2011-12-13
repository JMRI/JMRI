// MrcInterface.java

package jmri.jmrix.mrc;

/**
 * Layout interface, similar to command station
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision$
 */
public interface MrcInterface {

	public void addMrcListener( MrcListener l);
	public void removeMrcListener( MrcListener l);

	boolean status();   // true if the implementation is operational

	void sendMrcMessage(MrcMessage m, MrcListener l);  // 2nd arg gets the reply
}


/* @(#)MrcInterface.java */
