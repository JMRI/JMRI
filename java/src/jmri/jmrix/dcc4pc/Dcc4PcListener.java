package jmri.jmrix.dcc4pc;

/**
 * Defines the interface for listening to traffic on the DCC4PC communications
 * link.
 *
 * @author Kevin Dickerson Copyright (C) 2001
 * @author Bob Jacobsen Copyright (C) 2012
 * 
 */
public interface Dcc4PcListener extends jmri.jmrix.AbstractMRListener {

    void message(Dcc4PcMessage m);

    void reply(Dcc4PcReply m);

    void handleTimeout(Dcc4PcMessage m);
}
