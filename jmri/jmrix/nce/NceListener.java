// NceListener.java

package jmri.jmrix.nce;

/**
 * Defines the interface to an NCE protocol handling class
 *
 * @author		Bob Jacobsen  Copyright (C) 2001
 * @version		$Revision: 1.2 $
 */
public interface NceListener extends jmri.jmrix.AbstractMRListener {
	public void message(NceMessage m);
	public void reply(NceReply m);
}


/* @(#)NceListener.java */
