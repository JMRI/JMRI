/**
 * NceInterface.java
 *
 * Description:		<describe the NceInterface class here>
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Id: NceInterface.java,v 1.1 2002-02-28 23:57:35 jacobsen Exp $
 */

package jmri.jmrix.nce;


public interface NceInterface {

	public void addNceListener( NceListener l);
	public void removeNceListener( NceListener l);

	boolean status();   // true if the implementation is operational

	void sendNceMessage(NceMessage m, NceListener l);  // 2nd arg gets the reply
}


/* @(#)NceInterface.java */
