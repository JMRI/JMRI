package jmri.jmris.simpleserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.jmris.AbstractTurnoutServer;
import jmri.jmris.JmriConnection;

/**
 * Simple Server interface between the JMRI turnout manager and a network
 * connection
 *
 * @author Paul Bender Copyright (C) 2010
 */
public class SimpleTurnoutServer extends AbstractTurnoutServer {

    private static final String TURNOUT = "TURNOUT ";
    private DataOutputStream output;
    private JmriConnection connection;

    public SimpleTurnoutServer(JmriConnection connection){
        this.connection = connection;
    }

    public SimpleTurnoutServer(DataInputStream inStream,DataOutputStream outStream){
        output = outStream;
    }


    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(String turnoutName, int Status) throws IOException {
        addTurnoutToList(turnoutName);
        switch (Status) {
            case Turnout.THROWN:
                this.sendMessage(TURNOUT + turnoutName + " THROWN\n");
                break;
            case Turnout.CLOSED:
                this.sendMessage(TURNOUT + turnoutName + " CLOSED\n");
                break;
            default: //  unknown state
                this.sendMessage(TURNOUT + turnoutName + " UNKNOWN\n");
                break;
        }
    }

    @Override
    public void sendErrorStatus(String turnoutName) throws IOException {
        this.sendMessage("TURNOUT ERROR\n");
    }

    @Override
    public void parseStatus(String statusString) throws jmri.JmriException, java.io.IOException {        
        
        String turnoutName = statusString.split(" ")[1];
        log.debug("status: {}", statusString);
        if (statusString.contains("THROWN")) {
            log.debug("Setting Turnout THROWN");
            // create turnout if it does not exist since throwTurnout() no longer does so
            this.initTurnout(turnoutName);
            throwTurnout(turnoutName);
        } else if (statusString.contains("CLOSED")) {
            log.debug("Setting Turnout CLOSED");
            // create turnout if it does not exist since closeTurnout() no longer does so
            this.initTurnout(turnoutName);
            closeTurnout(turnoutName);
        } else {
            // default case, return status for this turnout
            try {
                sendStatus(turnoutName,
                    InstanceManager.turnoutManagerInstance().provideTurnout(turnoutName).getKnownState());
            } catch (IllegalArgumentException ex) {
                log.warn("Failed to provide Turnout \"{}\" in parseStatus", turnoutName);
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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SimpleTurnoutServer.class);
}
