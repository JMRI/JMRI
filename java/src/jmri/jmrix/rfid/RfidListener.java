package jmri.jmrix.rfid;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Listener interface to be notified about serial traffic
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2006, 2007, 2008
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
@API(status = EXPERIMENTAL)
public interface RfidListener extends jmri.jmrix.AbstractMRListener {

    public void message(RfidMessage m);

    public void reply(RfidReply m);
}
