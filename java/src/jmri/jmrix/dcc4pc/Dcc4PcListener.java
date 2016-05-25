// Dcc4PcListener.java
package jmri.jmrix.dcc4pc;

/**
 * Defines the interface for listening to traffic on the DCC4PC communications
 * link.
 *
 * @author	Kevin Dickerson Copyright (C) 2001
 * @author	Bob Jacobsen Copyright (C) 2012
 * @version	$Revision: 18133 $
 */
public interface Dcc4PcListener extends jmri.jmrix.AbstractMRListener {

    public void message(Dcc4PcMessage m);

    public void reply(Dcc4PcReply m);

    public void handleTimeout(Dcc4PcMessage m);

    public void processingData();
}

/* @(#)Dcc4PcListener.java */
