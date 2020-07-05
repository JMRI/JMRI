package jmri.jmrix.cmri.serial;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Listener interface to be notified about serial C/MRI traffic
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
@API(status = EXPERIMENTAL)
public interface SerialListener extends jmri.jmrix.AbstractMRListener {

    public void message(SerialMessage m);

    public void reply(SerialReply m);
}
