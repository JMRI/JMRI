package jmri.jmrix.tmcc;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Listener interface to be notified about serial TMCC traffic.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2006
 */
@API(status = EXPERIMENTAL)
public interface SerialListener extends jmri.jmrix.AbstractMRListener {

    public void message(SerialMessage m);

    public void reply(SerialReply m);

}
