//SimplePowerServer.java
package jmri.jmris.simpleserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jetty.websocket.WebSocket.Connection;

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
    private Connection connection;
    static Logger log = LoggerFactory.getLogger(SimplePowerServer.class.getName());

    public SimplePowerServer(DataInputStream inStream, DataOutputStream outStream) {
        output = outStream;
        mgrOK();
    }

    public SimplePowerServer(Connection cnctn) {
    	this.connection = cnctn;
    	mgrOK();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(int Status) throws IOException {
        if (Status == PowerManager.ON) {
            this.sendStatus("POWER ON\n");
        } else if (Status == PowerManager.OFF) {
            this.sendStatus("POWER OFF\n");
        } else {
            this.sendStatus("POWER UNKNOWN\n");
        }
    }

    @Override
    public void sendErrorStatus() throws IOException {
        this.sendStatus("POWER ERROR\n");
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
    
    public void sendStatus(String status) throws IOException {
    	if (this.output != null) {
    		this.output.writeBytes(status);
    	} else {
    		this.connection.sendMessage(status);
    	}
    }
}
