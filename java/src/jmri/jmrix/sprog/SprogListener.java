package jmri.jmrix.sprog;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Define the interface for listening to traffic on the Sprog communications
 * link. Based on {@link jmri.jmrix.nce.NceListener}
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
@API(status = EXPERIMENTAL)
public interface SprogListener extends java.util.EventListener {

    public void notifyMessage(SprogMessage m);

    public void notifyReply(SprogReply m);

}
