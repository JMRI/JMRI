// SpeedoListener.java
package jmri.jmrix.bachrus;

/**
 * Defines the interface for listening to traffic on the NCE communications
 * link.
 *
 * @author	Andrew Crosland Copyright (C) 2010
 * @version	$Revision$
 */
public interface SpeedoListener extends java.util.EventListener {

    public void reply(SpeedoReply m);
}

/* @(#)SpeedoListener.java */
