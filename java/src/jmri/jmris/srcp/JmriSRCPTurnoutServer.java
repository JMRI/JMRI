package jmri.jmris.srcp;

import java.beans.PropertyChangeEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import jmri.InstanceManager;
import jmri.Turnout;
import jmri.jmris.AbstractTurnoutServer;
import jmri.jmrix.SystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SRCP Server interface between the JMRI Turnout manager and a network
 * connection
 *
 * @author Paul Bender Copyright (C) 2010-2013
 */
public class JmriSRCPTurnoutServer extends AbstractTurnoutServer {

    private DataOutputStream output;

    public JmriSRCPTurnoutServer(DataInputStream inStream, DataOutputStream outStream) {

        output = outStream;
    }

    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(String turnoutName, int Status) throws IOException {
        TimeStampedOutput.writeTimestamp(output, "499 ERROR unspecified error\n\r");
    }

    public void sendStatus(int bus, int address) throws IOException {
        log.debug("send Status called with bus {} and address {}", bus, address);
        java.util.List<SystemConnectionMemo> list = jmri.InstanceManager.getList(SystemConnectionMemo.class);
        SystemConnectionMemo memo = null;
        try {
            memo = list.get(bus - 1);
        } catch (java.lang.IndexOutOfBoundsException obe) {
            TimeStampedOutput.writeTimestamp(output, "412 ERROR wrong value\n\r");
            return;
        }
        String turnoutName = memo.getSystemPrefix()
                + "T" + address;
        try {
            // busy loop, wait for turnout to settle before continuing.
            while (InstanceManager.turnoutManagerInstance().provideTurnout(turnoutName).getKnownState() != InstanceManager.turnoutManagerInstance().provideTurnout(turnoutName).getCommandedState()) {
            }
            int Status = InstanceManager.turnoutManagerInstance().provideTurnout(turnoutName).getKnownState();
            if (Status == Turnout.THROWN) {
                TimeStampedOutput.writeTimestamp(output, "100 INFO " + bus + " GA " + address + " 1 0\n\r");
            } else if (Status == Turnout.CLOSED) {
                TimeStampedOutput.writeTimestamp(output, "100 INFO " + bus + " GA " + address + " 0 0\n\r");
            } else {
                //  unknown state
                TimeStampedOutput.writeTimestamp(output, "411 ERROR unknown value\n\r");
            }
        } catch (IllegalArgumentException ex) {
            log.warn("Failed to provide Turnout \"{}\" in sendStatus", turnoutName);
        }
    }

    @Override
    public void sendErrorStatus(String turnoutName) throws IOException {
        TimeStampedOutput.writeTimestamp(output, "499 ERROR unspecified error\n\r");
    }

    @Override
    public void parseStatus(String statusString) throws jmri.JmriException, java.io.IOException {
        TimeStampedOutput.writeTimestamp(output, "499 ERROR unspecified error\n\r");
    }

    /*
     * Initialize an SRCP server turnout. Constructs the system name 
     * string from the provided parameters.
     */
    public void initTurnout(int bus, int address, String protocol) throws jmri.JmriException, java.io.IOException {

        log.debug("init Turnout called with bus {} address {} and protocol {}", bus, address, protocol);
        java.util.List<SystemConnectionMemo> list = jmri.InstanceManager.getList(SystemConnectionMemo.class);
        SystemConnectionMemo memo;
        try {
            memo = list.get(bus - 1);
        } catch (java.lang.IndexOutOfBoundsException obe) {
            TimeStampedOutput.writeTimestamp(output, "412 ERROR wrong value\n\r");
            return;
        }
        String turnoutName = memo.getSystemPrefix()
                + "T" + address;
        // create turnout if it does not exist.
        this.initTurnout(turnoutName);
        TimeStampedOutput.writeTimestamp(output, "101 INFO " + bus + " GA " + address + " " + protocol + "\n\r");
    }

    /*
     * for SRCP, we're doing the parsing elsewhere, so we just need to build
     * the correct string from the provided compoents.
     */
    public void parseStatus(int bus, int address, int value) throws jmri.JmriException, java.io.IOException {

        log.debug("parse Status called with bus {} address {} and value {}", bus, address, value);
        java.util.List<SystemConnectionMemo> list = jmri.InstanceManager.getList(SystemConnectionMemo.class);
        SystemConnectionMemo memo;
        try {
            memo = list.get(bus - 1);
        } catch (java.lang.IndexOutOfBoundsException obe) {
            TimeStampedOutput.writeTimestamp(output, "412 ERROR wrong value\n\r");
            return;
        }
        String turnoutName = memo.getSystemPrefix()
                + "T" + address;
        // create turnout if it does not exist since closeTurnout() and throwTurnout() no longer do so
        //this.initTurnout(turnoutName);
        if (value == 1) {
            log.debug("Setting Turnout THROWN");
            throwTurnout(turnoutName);
        } else if (value == 0) {
            log.debug("Setting Turnout CLOSED");
            closeTurnout(turnoutName);
        }
        sendStatus(bus, address);
    }

    @Override
    protected TurnoutListener getListener(String turnoutName) {
        return new TurnoutListener(turnoutName);
    }

    class TurnoutListener extends AbstractTurnoutServer.TurnoutListener {

        TurnoutListener(String turnoutName) {
            super(turnoutName);
        }

        // update state as state of turnout changes
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            // If the Commanded State changes, show transition state as "<inconsistent>"
            if (e.getPropertyName().equals("KnownState")) {
                try {
                    String Name = ((jmri.Turnout) e.getSource()).getSystemName();
                    java.util.List<SystemConnectionMemo> List = jmri.InstanceManager.getList(SystemConnectionMemo.class);
                    int i = 0;
                    int address;
                    for (Object memo : List) {
                        String prefix = memo.getClass().getName();
                        if (Name.startsWith(prefix)) {
                            address = Integer.parseInt(Name.substring(prefix.length()));
                            sendStatus(i, address);
                            break;
                        }
                        i++;
                    }
                } catch (java.io.IOException ie) {
                    log.error("Error Sending Status");
                }
            }
        }
    }
    private final static Logger log = LoggerFactory.getLogger(JmriSRCPTurnoutServer.class);
}
