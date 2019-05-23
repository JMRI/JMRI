package jmri.jmrix.ecos;

/**
 * Defines the interface for listening to traffic on the ECoS communications
 * link.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public interface EcosListener extends jmri.jmrix.AbstractMRListener {

    public void message(EcosMessage m);

    public void reply(EcosReply m);

}
