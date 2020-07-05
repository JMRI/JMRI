package jmri.jmrix;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Represents a failure during the configuration of a serial port, typically via
 * a NetworkPortAdapter interface.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
@API(status = EXPERIMENTAL)
public class NetworkConfigException extends jmri.JmriException {

    public NetworkConfigException(String s) {
        super(s);
    }

    public NetworkConfigException() {
    }

}
