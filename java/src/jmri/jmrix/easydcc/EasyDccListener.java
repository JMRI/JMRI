package jmri.jmrix.easydcc;

/**
 * Defines the interface for listening to traffic on the EasyDcc communications
 * link.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2004
 */
public interface EasyDccListener extends jmri.jmrix.AbstractMRListener {

    void message(EasyDccMessage m);

    void reply(EasyDccReply m);

}
