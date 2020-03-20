package jmri.jmris.srcp;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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

    private OutputStream output;

    public JmriSRCPProgrammerServer(OutputStream outStream) {
        super();
        output = outStream;
    }


    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(int CV, int value, int status) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("sendStatus called for CV {} with value {} and status {}",CV,value,status);
        }
        if (status == ProgListener.OK) {
            output.write(("100 INFO 1 SM " + CV + " CV " + value + "\n\r").getBytes());
        } else {
            output.write("416 ERROR no data\n\r".getBytes());
        }
    }

    @Override
    public void sendNotAvailableStatus() throws IOException {
        output.write("499 ERROR unspecified error\n".getBytes());
    }

    @Override
    public void parseRequest(String statusString) throws jmri.JmriException, java.io.IOException {
    }

    private static final Logger log = LoggerFactory.getLogger(JmriSRCPProgrammerServer.class);

}
