package jmri.jmrix.nce;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Defines the interface for listening to traffic on the NCE communications
 * link.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
@API(status = EXPERIMENTAL)
public interface NceListener extends jmri.jmrix.AbstractMRListener {

    public void message(NceMessage m);

    public void reply(NceReply m);
}


