package jmri.jmrix.sprog;

/**
 * Define the interface for listening to traffic on the Sprog communications
 * link. Based on {@link jmri.jmrix.nce.NceListener}
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 */
public interface SprogListener extends java.util.EventListener {

    public void notifyMessage(SprogMessage m);

    public void notifyReply(SprogReply m);

}
