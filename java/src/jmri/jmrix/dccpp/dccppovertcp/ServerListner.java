package jmri.jmrix.dccpp.dccppovertcp;

/**
 * Implementation of the DCCppOverTcp Server Server Protocol
 *
 * @author Alex Shepherd Copyright (C) 2006
 */
public interface ServerListner {

    public void notifyServerStateChanged(Server s);

    public void notifyClientStateChanged(Server s);
}
