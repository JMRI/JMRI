package jmri.jmris.srcp;

import java.io.DataOutputStream;
import java.io.IOException;
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

    private DataOutputStream output;

    public JmriSRCPPowerServer(DataOutputStream outStream) {
        output = outStream;
        mgrOK();
    }


    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(int Status) throws IOException {
        if (Status == PowerManager.ON) {
            TimeStampedOutput.writeTimestamp(output, "100 INFO 0 POWER ON\n\r");
        } else if (Status == PowerManager.OFF) {
            TimeStampedOutput.writeTimestamp(output, "100 INFO 0 POWER OFF\n\r");
        } else {
            // power unknown
            TimeStampedOutput.writeTimestamp(output, "411 ERROR unknown value\n\r");
        }
    }

    @Override
    public void sendErrorStatus() throws IOException {
        TimeStampedOutput.writeTimestamp(output, "499 ERROR unspecified error\n\r");
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
        } catch (jmri.JmriException ex) {
            try {
                sendErrorStatus();
            } catch (IOException ie) {
            }
        } catch (IOException ie2) {
        }
    }
    private final static Logger log = LoggerFactory.getLogger(JmriSRCPPowerServer.class);

}
