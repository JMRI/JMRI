//SimpleLightServer.java
package jmri.jmris.simpleserver;

import java.io.*;

import jmri.JmriException;
import jmri.Light;
import jmri.jmris.AbstractLightServer;
import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.WebSocket.Connection;

/**
 * Simple Server interface between the JMRI light manager and a network
 * connection
 *
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision$
 */
public class SimpleLightServer extends AbstractLightServer {

    private DataOutputStream output = null;
    private Connection connection = null;

    public SimpleLightServer(Connection connection) {
    	this.connection = connection;
    }
    
    public SimpleLightServer(DataInputStream inStream, DataOutputStream outStream) {

        output = outStream;
    }


    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(String lightName, int Status) throws IOException {
        if (Status == Light.ON) {
            this.sendMessage("LIGHT " + lightName + " ON\n");
        } else if (Status == Light.OFF) {
        	this.sendMessage("LIGHT " + lightName + " OFF\n");
        } else {
            //  unknown state
        	this.sendMessage("LIGHT " + lightName + " UNKNOWN\n");
        }
    }

    @Override
    public void sendErrorStatus(String lightName) throws IOException {
    	this.sendMessage("LIGHT ERROR\n");
    }

    @Override
    public void parseStatus(String statusString) throws JmriException, IOException {
        int index;
        index = statusString.indexOf(" ") + 1;
        if (statusString.contains("ON")) {
            if (log.isDebugEnabled()) {
                log.debug("Setting Light ON");
            }
            lightOn(statusString.substring(index, statusString.indexOf(" ", index + 1)));
        } else if (statusString.contains("OFF")) {
            if (log.isDebugEnabled()) {
                log.debug("Setting Light OFF");
            }
            lightOff(statusString.substring(index, statusString.indexOf(" ", index + 1)));
        }
    }
    
    private void sendMessage(String message) throws IOException {
    	if (this.output != null) {
    		this.output.writeBytes(message);
    	} else {
    		this.connection.sendMessage(message);
    	}
    }
    
    static Logger log = Logger.getLogger(SimpleLightServer.class.getName());
}
