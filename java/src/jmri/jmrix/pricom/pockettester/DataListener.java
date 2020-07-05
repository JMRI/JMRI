package jmri.jmrix.pricom.pockettester;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Receive notification when data arrives from a Pocket Tester.
 * <p>
 * You register this listener with a DataSource object
 *
 * @see jmri.jmrix.pricom.pockettester.DataSource
 *
 * @author Bob Jacobsen Copyright (C) 2005
 */
@API(status = EXPERIMENTAL)
public interface DataListener {

    public void asciiFormattedMessage(String m);

    // public void rawMessage(String m);
}


