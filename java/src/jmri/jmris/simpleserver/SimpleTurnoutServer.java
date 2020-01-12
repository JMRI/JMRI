package jmri.jmris.simpleserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import jmri.InstanceManager;
import jmri.Turnout;
import jmri.jmris.AbstractTurnoutServer;
import jmri.jmris.JmriConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Server interface between the JMRI turnout manager and a network
 * connection
 *
 * @author Paul Bender Copyright (C) 2010
 */
public class SimpleTurnoutServer extends AbstractTurnoutServer {

    private DataOutputStream output;
    private JmriConnection connection;

    public SimpleTurnoutServer(JmriConnection connection) {
        this.connection = connection;
    }

    public SimpleTurnoutServer(DataInputStream inStream, DataOutputStream outStream) {

        output = outStream;
    }


    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(String turnoutName, int Status) throws IOException {
        addTurnoutToList(turnoutName);
        if (Status == Turnout.THROWN) {
            this.sendMessage("TURNOUT " + turnoutName + " THROWN\n");
        } else if (Status == Turnout.CLOSED) {
            this.sendMessage("TURNOUT " + turnoutName + " CLOSED\n");
        } else {
            //  unknown state
            this.sendMessage("TURNOUT " + turnoutName + " UNKNOWN\n");
        }
    }

    @Override
    public void sendErrorStatus(String turnoutName) throws IOException {
        this.sendMessage("TURNOUT ERROR\n");
    }

    @Override
    public void parseStatus(String statusString) throws jmri.JmriException, java.io.IOException {
        int index;
        index = statusString.indexOf(" ") + 1;
        log.debug(statusString);
        if (statusString.contains("THROWN")) {
            if (log.isDebugEnabled()) {
                log.debug("Setting Turnout THROWN");
            }
            // create turnout if it does not exist since throwTurnout() no longer does so
            this.initTurnout(statusString.substring(index, statusString.indexOf(" ", index + 1)));
            throwTurnout(statusString.substring(index, statusString.indexOf(" ", index + 1)));
        } else if (statusString.contains("CLOSED")) {
            if (log.isDebugEnabled()) {
                log.debug("Setting Turnout CLOSED");
            }
            // create turnout if it does not exist since closeTurnout() no longer does so
            this.initTurnout(statusString.substring(index, statusString.indexOf(" ", index + 1)));
            closeTurnout(statusString.substring(index, statusString.indexOf(" ", index + 1)));
        } else {
            // default case, return status for this turnout
            try {
                sendStatus(statusString.substring(index),
                    InstanceManager.turnoutManagerInstance().provideTurnout(statusString.substring(index)).getKnownState());
            } catch (IllegalArgumentException ex) {
                log.warn("Failed to provide Turnout \"{}\" in parseStatus", statusString.substring(index));
            }
        }
    }

    private void sendMessage(String message) throws IOException {
        if (this.output != null) {
            this.output.writeBytes(message);
        } else {
            this.connection.sendMessage(message);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SimpleTurnoutServer.class);
}
