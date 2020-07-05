package jmri.jmrix.dcc4pc;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Defines the interface for listening to traffic on the DCC4PC communications
 * link.
 *
 * @author Kevin Dickerson Copyright (C) 2001
 * @author Bob Jacobsen Copyright (C) 2012
 * 
 */
@API(status = EXPERIMENTAL)
public interface Dcc4PcListener extends jmri.jmrix.AbstractMRListener {

    public void message(Dcc4PcMessage m);

    public void reply(Dcc4PcReply m);

    public void handleTimeout(Dcc4PcMessage m);
}
