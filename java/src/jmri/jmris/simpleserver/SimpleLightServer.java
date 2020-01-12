package jmri.jmris.simpleserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import jmri.JmriException;
import jmri.Light;
import jmri.jmris.AbstractLightServer;
import jmri.jmris.JmriConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Server interface between the JMRI light manager and a network
 * connection
 *
 * @author Paul Bender Copyright (C) 2010
 */
public class SimpleLightServer extends AbstractLightServer {

    private DataOutputStream output = null;
    private JmriConnection connection = null;

    public SimpleLightServer(JmriConnection connection) {
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
            initLight(statusString.substring(index, statusString.indexOf(" ", index + 1)));
            lightOn(statusString.substring(index, statusString.indexOf(" ", index + 1)));
        } else if (statusString.contains("OFF")) {
            if (log.isDebugEnabled()) {
                log.debug("Setting Light OFF");
            }
            initLight(statusString.substring(index, statusString.indexOf(" ", index + 1)));
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

    private final static Logger log = LoggerFactory.getLogger(SimpleLightServer.class);
}
