package jmri.jmrix.srcp;

/**
 * Defines the interface for listening to traffic on the SRCP communications
 * link.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2004, 2008
 */
public interface SRCPListener extends jmri.jmrix.AbstractMRListener {

    public void message(SRCPMessage m);

    public void reply(SRCPReply m);

    public void reply(jmri.jmrix.srcp.parser.SimpleNode n);
}


