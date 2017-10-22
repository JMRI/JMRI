package jmri.jmrix.sprog;

/**
 * Define the interface for listening to traffic on the NCE communications
 * link.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 */
public interface SprogListener extends java.util.EventListener {

    public void notifyMessage(SprogMessage m);

    public void notifyReply(SprogReply m);

}
