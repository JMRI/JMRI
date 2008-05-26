// SRCPListener.java

package jmri.jmrix.srcp;

/**
 * Defines the interface for listening to traffic on the SRCP
 * communications link.
 *
 * @author		Bob Jacobsen  Copyright (C) 2001, 2004, 2008
 * @version		$Revision: 1.1 $
 */
public interface SRCPListener extends jmri.jmrix.AbstractMRListener {
    public void message(SRCPMessage m);
    public void reply(SRCPReply m);
}

/* @(#)SRCPListener.java */
