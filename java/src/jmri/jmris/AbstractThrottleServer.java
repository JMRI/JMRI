//AbstractThrottleServer.java
package jmri.jmris;

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
 * Abstract interface between the JMRI Throttles and a network connection
 *
 * @author Paul Bender Copyright (C) 2015
 * @version $Revision: 23184 $
 */
abstract public class AbstractThrottleServer implements ThrottleListener {

    private static final Logger log = LoggerFactory.getLogger(AbstractThrottleServer.class);
    ArrayList throttleList;

    public AbstractThrottleServer() {
        throttleList=new ArrayList<>();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    abstract public void sendStatus() throws IOException;

    abstract public void sendErrorStatus() throws IOException;

    abstract public void parsecommand(String statusString) throws JmriException, IOException;

    // implementation of ThrottleListener
    public void notifyThrottleFound(DccThrottle t){
        throttleList.add(t);
    }

    public void notifyFailedThrottleRequest(DccLocoAddress address, String reason){
       try{
         sendErrorStatus();
       } catch(java.io.IOException ioe){
           //Something failed writing data to the port.
       }
    }
}
