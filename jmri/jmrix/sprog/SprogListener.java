// SprogListener.java

package jmri.jmrix.sprog;

/**
 * Defines the interface for listening to traffic on the NCE
 * communications link.
 *
 * @author	Bob Jacobsen  Copyright (C) 2001
 * @version	$Revision: 1.3 $
 */

public interface SprogListener extends java.util.EventListener{
    public void message(SprogMessage m);
    public void reply(SprogReply m);
}

/* @(#)SprogListener.java */
