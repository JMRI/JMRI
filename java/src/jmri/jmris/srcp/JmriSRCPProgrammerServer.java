package jmri.jmris.srcp;

import java.io.DataOutputStream;
import java.io.IOException;
import jmri.ProgListener;
import jmri.jmris.AbstractProgrammerServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SRCP interface between the JMRI service mode programmer and a network
 * connection
 *
 * @author Paul Bender Copyright (C) 2012
 */
public class JmriSRCPProgrammerServer extends AbstractProgrammerServer {

    private DataOutputStream output;

    public JmriSRCPProgrammerServer(DataOutputStream outStream) {
        super();
        output = outStream;
    }


    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(int CV, int value, int status) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("sendStatus called for CV " + CV
                    + " with value " + value + " and status " + status);
        }
        if (status == ProgListener.OK) {
            TimeStampedOutput.writeTimestamp(output, "100 INFO 1 SM " + CV + " CV " + value + "\n\r");
        } else {
            TimeStampedOutput.writeTimestamp(output, "416 ERROR no data\n\r");
        }
    }

    @Override
    public void sendNotAvailableStatus() throws IOException {
        TimeStampedOutput.writeTimestamp(output, "499 ERROR unspecified error\n");
    }

    @Override
    public void parseRequest(String statusString) throws jmri.JmriException, java.io.IOException {
    }

    private final static Logger log = LoggerFactory.getLogger(JmriSRCPProgrammerServer.class);

}
