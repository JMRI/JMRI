package jmri.jmrix.xpa;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Defines the interface for listening to traffic sent to an XpressNet based
 * Command Station via an XPA and a modem.
 *
 * @author Paul Bender Copyright (C) 2004
 */
@API(status = EXPERIMENTAL)
public interface XpaListener extends java.util.EventListener {

    void message(XpaMessage m);

    void reply(XpaMessage m);
}
