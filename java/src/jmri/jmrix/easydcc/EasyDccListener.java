package jmri.jmrix.easydcc;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Defines the interface for listening to traffic on the EasyDcc communications
 * link.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2004
 */
@API(status = EXPERIMENTAL)
public interface EasyDccListener extends jmri.jmrix.AbstractMRListener {

    public void message(EasyDccMessage m);

    public void reply(EasyDccReply m);

}
