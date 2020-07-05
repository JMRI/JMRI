package jmri.jmrix.marklin;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Define the interface for listening to traffic on the Marklin communications
 * link.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
@API(status = EXPERIMENTAL)
public interface MarklinListener extends jmri.jmrix.AbstractMRListener {

    public void message(MarklinMessage m);

    public void reply(MarklinReply m);
}
