package jmri.jmrix.loconet.loconetovertcp;

/**
 * Implementation of the LocoNetOverTcp LbServer Server Protocol.
 *
 * @author Alex Shepherd Copyright (C) 2006
 */
public interface LnTcpServerListener {

    public void notifyServerStateChanged(LnTcpServer s);

    public void notifyClientStateChanged(LnTcpServer s);

}
