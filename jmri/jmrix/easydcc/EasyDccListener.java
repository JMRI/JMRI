// EasyDccListener.java

package jmri.jmrix.easydcc;

/**
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			$Revision: 1.2 $
 */

public interface EasyDccListener extends java.util.EventListener{
	public void message(EasyDccMessage m);
	public void reply(EasyDccReply m);
}


/* @(#)EasyDccListener.java */
