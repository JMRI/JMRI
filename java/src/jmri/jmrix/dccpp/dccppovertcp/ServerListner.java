package jmri.jmrix.dccpp.dccppovertcp;

/**
 * Implementation of the DCCppOverTcp Server Server Protocol
 *
 * @author Alex Shepherd Copyright (C) 2006
 */
public interface ServerListner {

    void notifyServerStateChanged(Server s);

    void notifyClientStateChanged(Server s);
}
