// SerialListener.java

package jmri.jmrix.powerline;


/**
 * Listener interface to be notified about serial traffic
 *
 * @author			Bob Jacobsen  Copyright (C) 2001, 2006, 2007, 2008
 * @version			$Revision: 1.2 $
 */
abstract public interface SerialListener extends jmri.jmrix.AbstractMRListener {
    public void message(SerialMessage m);
    public void reply(SerialReply m);
}

/* @(#)SerialListener.java */
