package jmri.jmris;

import java.io.IOException;
import jmri.InstanceManager;
import jmri.Programmer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract interface between the a JMRI Programmer and a network connection
 * <p>
 * Connects to default global programmer at construction time.
 *
 * @author Paul Bender Copyright (C) 2012
 */
abstract public class AbstractProgrammerServer implements jmri.ProgListener {

    private Programmer p = null;

    protected Programmer getProgrammer() {
        return p;
    }

    protected int lastCV = -1;

    public AbstractProgrammerServer() {
        if (InstanceManager.getNullableDefault(jmri.GlobalProgrammerManager.class) != null) {
            p = InstanceManager.getDefault(jmri.GlobalProgrammerManager.class).getGlobalProgrammer();
        } else {
            log.warn("no Service Mode ProgrammerManager configured, network programming disabled");
        }
    }

    /*
     * Protocol Specific Abstract Functions
     * @param CV CV number (in DCC terms)
     * @param value vale to read/write to CV
     * @param status Denotes the completion code from a programming operation
     */
    abstract public void sendStatus(int CV, int value, int status) throws IOException;

    abstract public void sendNotAvailableStatus() throws IOException;

    abstract public void parseRequest(String statusString) throws jmri.JmriException, java.io.IOException;

    public void writeCV(jmri.ProgrammingMode mode, int CV, int value) {
        if (p == null) {
            try {
                sendNotAvailableStatus();
            } catch (java.io.IOException ioe) {
                // Connection Terminated?
            }
            return;
        }
        lastCV = CV;
        try {
            p.setMode(mode); // need to check if mode is available
            p.writeCV(String.valueOf(CV), value, this);
        } catch (jmri.ProgrammerException ex) {
            //Send failure Status.
            try {
                sendNotAvailableStatus();
            } catch (java.io.IOException ioe) {
                // Connection Terminated?
            }
        }
    }

    public void readCV(jmri.ProgrammingMode mode, int CV) {
        if (p == null || !(p.getCanRead())) {
            try {
                sendNotAvailableStatus();
            } catch (java.io.IOException ioe) {
                // Connection Terminated?
            }
            return;
        }
        lastCV = CV;
        try {
            p.setMode(mode); // need to check if mode is available
            p.readCV(String.valueOf(CV), this);
        } catch (jmri.ProgrammerException ex) {
            //Send failure Status.
            try {
                sendNotAvailableStatus();
            } catch (java.io.IOException ioe) {
                // Connection Terminated?
            }
        }
    }

    /**
     * Receive a callback at the end of a programming operation.
     *
     * @param value  Value from a read operation, or value written on a write
     * @param status Denotes the completion code. Note that this is a bitwise
     *               combination of the various status coded defined in this
     *               interface.
     */
    @Override
    public void programmingOpReply(int value, int status) {
        if (log.isDebugEnabled()) {
            log.debug("programmingOpReply called with value " + value + " and status " + status);
        }
        try {
            sendStatus(lastCV, value, status);
        } catch (java.io.IOException ioe) {
            // Connection Terminated?
            if (log.isDebugEnabled()) {
                log.debug("Exception while sending reply");
            }
        }
    }

    public void dispose() {
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractProgrammerServer.class);

}
