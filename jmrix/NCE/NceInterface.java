/** 
 * NceInterface.java
 *
 * Description:		<describe the NceInterface class here>
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.nce;


public interface NceInterface {

	public void addNceListener( NceListener l);
	public void removeNceListener( NceListener l);
	
	boolean status();   // true if the implementation is operational

	void sendNceMessage(NceMessage m, NceListener l);  // 2nd arg gets the reply
}


/* @(#)NceInterface.java */
