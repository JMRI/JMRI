/** 
 * SprogListener.java
 *
 * Description:		<describe the SprogListener class here>
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			$Id: SprogListener.java,v 1.1 2003-01-27 05:24:00 jacobsen Exp $
 */

package jmri.jmrix.sprog;


public interface SprogListener extends java.util.EventListener{
	public void message(SprogMessage m);
	public void reply(SprogReply m);
}


/* @(#)SprogListener.java */
