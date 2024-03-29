package jmri.jmrix.maple;

/**
 * Listener interface to be notified about traffic
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 */
public interface SerialListener extends jmri.jmrix.AbstractMRListener {

    void message(SerialMessage m);

    void reply(SerialReply m);
}


