// SprogListener.java
package jmri.jmrix.sprog;

/**
 * Defines the interface for listening to traffic on the NCE communications
 * link.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision$
 */
public interface SprogListener extends java.util.EventListener {

    public void notifyMessage(SprogMessage m);

    public void notifyReply(SprogReply m);
}

/* @(#)SprogListener.java */
