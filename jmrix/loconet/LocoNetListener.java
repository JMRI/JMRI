/** 
 * LocoNetListener.java
 *
 * Description:		<describe the LocoNetListener class here>
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.loconet;


public interface LocoNetListener extends java.util.EventListener{
	public void message(LocoNetMessage m);
}


/* @(#)LocoNetListener.java */
