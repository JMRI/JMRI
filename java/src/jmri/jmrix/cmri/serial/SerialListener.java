package jmri.jmrix.cmri.serial;

/**
 * Listener interface to be notified about serial C/MRI traffic
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public interface SerialListener extends jmri.jmrix.AbstractMRListener {

    public void message(SerialMessage m);

    public void reply(SerialReply m);
}
