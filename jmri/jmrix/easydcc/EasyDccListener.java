/** 
 * EasyDccListener.java
 *
 * Description:		<describe the EasyDccListener class here>
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			$Id: EasyDccListener.java,v 1.1 2002-03-23 07:28:30 jacobsen Exp $
 */

package jmri.jmrix.easydcc;


public interface EasyDccListener extends java.util.EventListener{
	public void message(EasyDccMessage m);
	public void reply(EasyDccReply m);
}


/* @(#)EasyDccListener.java */
