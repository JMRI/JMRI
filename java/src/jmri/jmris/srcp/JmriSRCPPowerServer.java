package jmri.jmris.srcp;

import java.io.IOException;
import java.io.OutputStream;

import jmri.PowerManager;
import jmri.jmris.AbstractPowerServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SRCP interface between the JMRI power manager and a network connection
 *
 * @author Paul Bender Copyright (C) 2010
 */
public class JmriSRCPPowerServer extends AbstractPowerServer {

    private OutputStream output;

    public JmriSRCPPowerServer(OutputStream outStream) {
        super();
        output = outStream;
        mgrOK();
    }


    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(int Status) throws IOException {
        if (Status == PowerManager.ON) {
            output.write("100 INFO 0 POWER ON\n\r".getBytes());
        } else if (Status == PowerManager.OFF) {
            output.write("100 INFO 0 POWER OFF\n\r".getBytes());
        } else {
            // power unknown
            output.write("411 ERROR unknown value\n\r".getBytes());
        }
    }

    @Override
    public void sendErrorStatus() throws IOException {
        output.write("499 ERROR unspecified error\n\r".getBytes());
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

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent ev) {
        try {
            // send updates, but only if the status is ON or OFF.
            if (p.getPower() == PowerManager.ON || p.getPower() == PowerManager.OFF) {
                sendStatus(p.getPower());
            }
        } catch (IOException ie2) {
            // silently ignore
        }
    }
    private static final Logger log = LoggerFactory.getLogger(JmriSRCPPowerServer.class);

}
