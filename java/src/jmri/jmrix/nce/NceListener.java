package jmri.jmrix.nce;

/**
 * Defines the interface for listening to traffic on the NCE communications
 * link.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public interface NceListener extends jmri.jmrix.AbstractMRListener {

    void message(NceMessage m);

    void reply(NceReply m);
}


