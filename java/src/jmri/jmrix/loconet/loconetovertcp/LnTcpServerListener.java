package jmri.jmrix.loconet.loconetovertcp;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Implementation of the LocoNetOverTcp LbServer Server Protocol.
 *
 * @author Alex Shepherd Copyright (C) 2006
 */
@API(status = EXPERIMENTAL)
public interface LnTcpServerListener {

    public void notifyServerStateChanged(LnTcpServer s);

    public void notifyClientStateChanged(LnTcpServer s);

}
