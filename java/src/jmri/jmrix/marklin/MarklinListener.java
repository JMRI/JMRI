// MarklinListener.java
package jmri.jmrix.marklin;

/**
 * Defines the interface for listening to traffic on the Marklin communications
 * link.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision: 17977 $
 */
public interface MarklinListener extends jmri.jmrix.AbstractMRListener {

    public void message(MarklinMessage m);

    public void reply(MarklinReply m);
}

/* @(#)MarklinListener.java */
