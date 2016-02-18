//JmriSRCPThrottleServer.java
package jmri.jmris.srcp;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.io.IOException;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Throttle;
import jmri.ThrottleManager;
import jmri.ThrottleListener;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface between the JMRI Throttles and an SRCP network connection
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class JmriSRCPThrottleServer extends jmri.jmris.AbstractThrottleServer {

    private static final Logger log = LoggerFactory.getLogger(JmriSRCPThrottleServer.class);

    public JmriSRCPThrottleServer() {
       super();
    }

    /*
     * Protocol Specific Functions
     */
    public void sendStatus() throws IOException {
        TimeStampedOutput.writeTimestamp(output, "499 ERROR unspecified error\n\r");
    }

    /*
     * send the status of the specified throttle address on the specified bus
     * @param bus bus number.
     * @param address locomoitve address.
     */
    public void sendStatus(int bus, int address) throws IOException {
        log.debug("send Status called with bus {} and address {}", bus, address);

        /* translate the bus into a system connection memo */
        java.util.List<SystemConnectionMemo> list = jmri.InstanceManager.getList(SystemConnectionMemo.class);
        SystemConnectionMemo memo = null;
        try {
            memo = list.get(bus - 1);
        } catch (java.lang.IndexOutOfBoundsException obe) {
            TimeStampedOutput.writeTimestamp(output, "412 ERROR wrong value\n\r");
            return;
        }

        /* request the throttle for this particular locomotive address */

        /* the notifyThrottleFound callback function continues the operation */ 
    }

    public void sendErrorStatus() throws IOException{
        TimeStampedOutput.writeTimestamp(output, "499 ERROR unspecified error\n\r");
    }

    public void parsecommand(String statusString) throws JmriException, IOException {
    }

}
