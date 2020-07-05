package jmri.jmrix.ieee802154.xbee;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Listener interface to be notified about XBee traffic
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2006, 2007, 2008
 */
@API(status = EXPERIMENTAL)
public interface XBeeListener extends jmri.jmrix.AbstractMRListener {

    public void message(XBeeMessage m);

    public void reply(XBeeReply m);
}


