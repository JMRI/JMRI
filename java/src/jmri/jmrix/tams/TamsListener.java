package jmri.jmrix.tams;

/**
 * Defines the interface for listening to traffic on the Tams communications
 * link.
 *
 * Based on work by Bob Jacobsen
 *
 * @author Kevin Dickerson Copyright (C) 2012
 */
public interface TamsListener extends jmri.jmrix.AbstractMRListener {

    void message(TamsMessage m);

    void reply(TamsReply m);
}


