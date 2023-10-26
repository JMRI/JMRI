package jmri.jmrix.oaktree;

/**
 * Listener interface to be notified about serial traffic.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2006
 */
public interface SerialListener extends jmri.jmrix.AbstractMRListener {

    void message(SerialMessage m);

    void reply(SerialReply m);

}
