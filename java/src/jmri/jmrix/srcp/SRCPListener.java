package jmri.jmrix.srcp;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Defines the interface for listening to traffic on the SRCP communications
 * link.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2004, 2008
 */
@API(status = EXPERIMENTAL)
public interface SRCPListener extends jmri.jmrix.AbstractMRListener {

    public void message(SRCPMessage m);

    public void reply(SRCPReply m);

    public void reply(jmri.jmrix.srcp.parser.SimpleNode n);
}


