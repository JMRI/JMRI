package jmri.jmrix.jmriclient;

/**
 * Defines the interface for listening to traffic on the JMRIClient
 * communications link.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2004, 2008
 */
public interface JMRIClientListener extends jmri.jmrix.AbstractMRListener {

    void message(JMRIClientMessage m);

    void reply(JMRIClientReply m);

}
