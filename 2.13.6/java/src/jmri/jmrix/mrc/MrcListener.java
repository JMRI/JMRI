// MrcListener.java

package jmri.jmrix.mrc;

/**
 * Defines the interface for listening to traffic on the MRC
 * communications link.
 *
 * @author		Bob Jacobsen  Copyright (C) 2001, 2004
 * @version		$Revision$
 */
public interface MrcListener extends jmri.jmrix.AbstractMRListener {
    public void message(MrcMessage m);
    public void reply(MrcReply m);
}

/* @(#)MrcListener.java */
