package jmri.jmrix.jmriclient;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Defines the interface for listening to traffic on the JMRIClient
 * communications link.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2004, 2008
 */
@API(status = EXPERIMENTAL)
public interface JMRIClientListener extends jmri.jmrix.AbstractMRListener {

    public void message(JMRIClientMessage m);

    public void reply(JMRIClientReply m);

}
