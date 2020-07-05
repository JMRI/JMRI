package jmri.jmrix.tams;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Defines the interface for listening to traffic on the Tams communications
 * link.
 *
 * Based on work by Bob Jacobsen
 *
 * @author Kevin Dickerson Copyright (C) 2012
 */
@API(status = EXPERIMENTAL)
public interface TamsListener extends jmri.jmrix.AbstractMRListener {

    public void message(TamsMessage m);

    public void reply(TamsReply m);
}


