// SprogListener.java

package jmri.jmrix.sprog;

/**
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			$Revision: 1.2 $
 */

public interface SprogListener extends java.util.EventListener{
	public void message(SprogMessage m);
	public void reply(SprogReply m);
}


/* @(#)SprogListener.java */
