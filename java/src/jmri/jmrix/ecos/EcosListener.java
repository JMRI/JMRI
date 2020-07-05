package jmri.jmrix.ecos;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Defines the interface for listening to traffic on the ECoS communications
 * link.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
@API(status = EXPERIMENTAL)
public interface EcosListener extends jmri.jmrix.AbstractMRListener {

    public void message(EcosMessage m);

    public void reply(EcosReply m);

}
