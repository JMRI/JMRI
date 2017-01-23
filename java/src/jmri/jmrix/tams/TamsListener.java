package jmri.jmrix.tams;

/**
 * Defines the interface for listening to traffic on the Tams communications
 * link.
 *
 * Based on work by Bob Jacobsen
 *
 * @author	Kevin Dickerson Copyright (C) 2012
 */
public interface TamsListener extends jmri.jmrix.AbstractMRListener {

    public void message(TamsMessage m);

    public void reply(TamsReply m);
}


