// SprogVersionListener.java
package jmri.jmrix.sprog.update;

/**
 * Defines the interface for listening to SPROG version replies.
 *
 * @author	Andrew Crosland Copyright (C) 2012
 * @version	$Revision: $
 */
public interface SprogVersionListener extends java.util.EventListener {

    public void notifyVersion(SprogVersion v);

}

/* @(#)SprogVersionListener.java */
