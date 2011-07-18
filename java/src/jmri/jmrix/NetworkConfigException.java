// NetworkConfigException.java

package jmri.jmrix;

/**
 * Represents a failure during the configuration of a serial
 * port, typically via a NetworkPortAdapter interface.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version			$Revision$
 */
public class NetworkConfigException extends jmri.JmriException {
    public NetworkConfigException(String s) { super(s); }
    public NetworkConfigException() {}

}


/* @(#)NetworkConfigException.java */
