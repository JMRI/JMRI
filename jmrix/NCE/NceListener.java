/** 
 * NceListener.java
 *
 * Description:		<describe the NceListener class here>
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.nce;


public interface NceListener extends java.util.EventListener{
	public void message(NceMessage m);
	public void reply(NceReply m);
}


/* @(#)NceListener.java */
