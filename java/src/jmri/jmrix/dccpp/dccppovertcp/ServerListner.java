package jmri.jmrix.dccpp.dccppovertcp;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Implementation of the DCCppOverTcp Server Server Protocol
 *
 * @author Alex Shepherd Copyright (C) 2006
 */
@API(status = EXPERIMENTAL)
public interface ServerListner {

    public void notifyServerStateChanged(Server s);

    public void notifyClientStateChanged(Server s);
}
