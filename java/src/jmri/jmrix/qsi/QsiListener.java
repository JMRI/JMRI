package jmri.jmrix.qsi;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Defines the interface for listening to traffic on the QSI communications
 * link.
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
@API(status = EXPERIMENTAL)
public interface QsiListener extends java.util.EventListener {

    public void message(QsiMessage m);

    public void reply(QsiReply m);

}
