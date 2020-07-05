package jmri.jmrix.bachrus;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Defines the interface for listening to traffic on the NCE communications
 * link.
 *
 * @author Andrew Crosland Copyright (C) 2010
 */
@API(status = EXPERIMENTAL)
public interface SpeedoListener extends java.util.EventListener {

    public void reply(SpeedoReply m);

}
