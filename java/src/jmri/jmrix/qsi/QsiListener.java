package jmri.jmrix.qsi;

/**
 * Defines the interface for listening to traffic on the QSI communications
 * link.
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 */
public interface QsiListener extends java.util.EventListener {

    public void message(QsiMessage m);

    public void reply(QsiReply m);

}
