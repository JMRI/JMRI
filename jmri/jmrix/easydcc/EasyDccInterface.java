/**
 * EasyDccInterface.java
 *
 * Description:		<describe the EasyDccInterface class here>
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Id: EasyDccInterface.java,v 1.1 2002-03-23 07:28:30 jacobsen Exp $
 */

package jmri.jmrix.easydcc;


public interface EasyDccInterface {

	public void addEasyDccListener( EasyDccListener l);
	public void removeEasyDccListener( EasyDccListener l);

	boolean status();   // true if the implementation is operational

	void sendEasyDccMessage(EasyDccMessage m, EasyDccListener l);  // 2nd arg gets the reply
}


/* @(#)EasyDccInterface.java */
