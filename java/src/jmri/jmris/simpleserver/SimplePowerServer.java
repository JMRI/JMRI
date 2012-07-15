//SimplePowerServer.java
package jmri.jmris.simpleserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import jmri.PowerManager;
import jmri.jmris.AbstractPowerServer;

/**
 * Simple Server interface between the JMRI power manager and a network
 * connection
 *
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision$
 */
public class SimplePowerServer extends AbstractPowerServer {

    private DataOutputStream output;

    public SimplePowerServer(DataInputStream inStream, DataOutputStream outStream) {
        output = outStream;
        mgrOK();
    }


    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(int Status) throws IOException {
        if (Status == PowerManager.ON) {
            output.writeBytes("POWER ON\n");
        } else if (Status == PowerManager.OFF) {
            output.writeBytes("POWER OFF\n");
        } else {
            output.writeBytes("POWER UNKNOWN\n");
        }
    }

    @Override
    public void sendErrorStatus() throws IOException {
        output.writeBytes("POWER ERROR\n");
    }

    @Override
    public void parseStatus(String statusString) throws jmri.JmriException {
        if (statusString.contains("ON")) {
            if (log.isDebugEnabled()) {
                log.debug("Setting Power ON");
            }
            setOnStatus();
        } else if (statusString.contains("OFF")) {
            if (log.isDebugEnabled()) {
                log.debug("Setting Power OFF");
            }
            setOffStatus();
        }
    }
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SimplePowerServer.class.getName());
}
