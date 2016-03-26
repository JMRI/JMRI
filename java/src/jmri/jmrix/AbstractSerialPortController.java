// AbstractSerialPortController.java
package jmri.jmrix;

import gnu.io.CommPortIdentifier;
import java.util.Enumeration;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide an abstract base for *PortController classes.
 * <P>
 * This is complicated by the lack of multiple inheritance. SerialPortAdapter is
 * an Interface, and its implementing classes also inherit from various
 * PortController types. But we want some common behaviours for those, so we put
 * them here.
 *
 * @see jmri.jmrix.SerialPortAdapter
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002
 * @version	$Revision$
 */
abstract public class AbstractSerialPortController extends AbstractPortController implements SerialPortAdapter {

    protected AbstractSerialPortController(SystemConnectionMemo connectionMemo) {
        super(connectionMemo);
    }

    /**
     * Standard error handling for port-busy case
     */
    public String handlePortBusy(gnu.io.PortInUseException p, String portName, Logger log) {
        log.error(portName + " port is in use: " + p.getMessage());
        /*JOptionPane.showMessageDialog(null, "Port is in use",
         "Error", JOptionPane.ERROR_MESSAGE);*/
        ConnectionStatus.instance().setConnectionState(portName, ConnectionStatus.CONNECTION_DOWN);
        return portName + " port is in use";
    }

    /**
     * Standard error handling for port-not-found case
     */
    public String handlePortNotFound(gnu.io.NoSuchPortException p, String portName, Logger log) {
        log.error("Serial port " + portName + " not found");
        /*JOptionPane.showMessageDialog(null, "Serial port "+portName+" not found",
         "Error", JOptionPane.ERROR_MESSAGE);*/
        ConnectionStatus.instance().setConnectionState(portName, ConnectionStatus.CONNECTION_DOWN);
        return portName + " not found";
    }

    public void connect() throws Exception {
        openPort(mPort, "JMRI app");
    }

    public void setPort(String port) {
        mPort = port;
    }
    protected String mPort = null;

    public String getCurrentPortName() {
        if (mPort == null) {
            if (getPortNames() == null) {
                //This shouldn't happen but in the tests for some reason this happens
                log.error("Port names returned as null");
                return null;
            }
            if (getPortNames().size() <= 0) {
                log.error("No usable ports returned");
                return null;
            }
            return null;
            // return (String)getPortNames().elementAt(0);
        }
        return mPort;
    }

    /**
     * Set the baud rate. This records it for later.
     */
    public void configureBaudRate(String rate) {
        mBaudRate = rate;
    }
    protected String mBaudRate = null;

    public String getCurrentBaudRate() {
        if (mBaudRate == null) {
            return validBaudRates()[0];
        }
        return mBaudRate;
    }

    /**
     * Get an array of valid baud rates as integers. This allows subclasses to
     * change the arrays of speeds.
     *
     * This method need not be reimplemented unless the subclass is using
     * currentBaudNumber, which requires it.
     */
    public int[] validBaudNumber() {
        log.error("default validBaudNumber implementation should not be used");
        new Exception().printStackTrace();
        return null;
    }

    /**
     * Convert a baud rate string to a number.
     *
     * Uses the validBaudNumber and validBaudRates methods to do this.
     *
     * @return -1 if no match (configuration system should prevent this)
     */
    public int currentBaudNumber(String currentBaudRate) {
        String[] rates = validBaudRates();
        int[] numbers = validBaudNumber();

        // return if arrays invalid
        if (numbers == null) {
            log.error("numbers array null in currentBaudNumber");
            return -1;
        }
        if (rates == null) {
            log.error("rates array null in currentBaudNumber");
            return -1;
        }
        if (numbers.length < 1 || (numbers.length != rates.length)) {
            log.error("arrays wrong length in currentBaudNumber: " + numbers.length + "," + rates.length);
            return -1;
        }

        // find the baud rate value, configure comm options
        for (int i = 0; i < numbers.length; i++) {
            if (rates[i].equals(currentBaudRate)) {
                return numbers[i];
            }
        }

        // no match
        log.error("no match to (" + currentBaudRate + ") in currentBaudNumber");
        return -1;
    }

    Vector<String> portNameVector = null;

    @SuppressWarnings("unchecked")
    public Vector<String> getPortNames() {
        //reloadDriver(); // Refresh the list of communication ports
        // first, check that the comm package can be opened and ports seen
        portNameVector = new Vector<String>();
        Enumeration<CommPortIdentifier> portIDs = CommPortIdentifier.getPortIdentifiers();
        // find the names of suitable ports
        while (portIDs.hasMoreElements()) {
            CommPortIdentifier id = portIDs.nextElement();
            // filter out line printers 
            if (id.getPortType() != CommPortIdentifier.PORT_PARALLEL) // accumulate the names in a vector
            {
                portNameVector.addElement(id.getName());
            }
        }
        return portNameVector;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    /**
     * This is called when a connection is initially lost. It closes the client
     * side socket connection, resets the open flag and attempts a reconnection.
     */
    public void recover() {
        if (!allowConnectionRecovery) {
            return;
        }
        opened = false;
        try {
            closeConnection();
        } catch (Exception e) {
        }
        reconnect();
    }

    /*Each serial port adapter should handle this and it should be abstract.
     However this is in place until all the other code has been refactored */

    protected void closeConnection() throws Exception {
        System.out.println("crap Called");
    }

    /*Each port adapter shoudl handle this and it should be abstract.
     However this is in place until all the other code has been refactored */
    protected void resetupConnection() {
    }

    /**
     * Attempts to reconnect to a failed Server
     */
    public void reconnect() {
        // If the connection is already open, then we shouldn't try a re-connect.
        if (opened && !allowConnectionRecovery) {
            return;
        }
        reconnectwait thread = new reconnectwait();
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            log.error("Unable to join to the reconnection thread " + e.getMessage());
        }
        if (!opened) {
            log.error("Failed to re-establish connectivity");
        } else {
            log.info("Reconnected to " + getCurrentPortName());
            resetupConnection();
        }
    }

    class reconnectwait extends Thread {

        public final static int THREADPASS = 0;
        public final static int THREADFAIL = 1;
        int _status;

        public int status() {
            return _status;
        }

        public reconnectwait() {
            _status = THREADFAIL;
        }

        @SuppressWarnings("unchecked")
        public void run() {
            boolean reply = true;
            int count = 0;
            int secondCount = 0;
            while (reply) {
                safeSleep(reconnectinterval, "Waiting");
                count++;
                try {
                    log.error("Retrying Connection attempt " + secondCount + "-" + count);
                    Enumeration<CommPortIdentifier> portIDs = CommPortIdentifier.getPortIdentifiers();
                    while (portIDs.hasMoreElements()) {
                        CommPortIdentifier id = portIDs.nextElement();
                        // filter out line printers
                        if (id.getPortType() != CommPortIdentifier.PORT_PARALLEL) // accumulate the names in a vector
                        {
                            if (id.getName().equals(mPort)) {
                                log.info(mPort + " port has reappeared as being valid trying to reconnect");
                                openPort(mPort, "jmri");
                            }
                        }
                    }
                } catch (Exception e) {
                }
                reply = !opened;
                if (count >= retryAttempts) {
                    log.error("Unable to reconnect after " + count + " Attempts, increasing duration of retries");
                    //retrying but with twice the retry interval.
                    reconnectinterval = reconnectinterval * 2;
                    count = 0;
                    secondCount++;
                }
                if (secondCount >= 10) {
                    log.error("Giving up on reconnecting after 100 attempts to reconnect");
                    reply = false;
                }
            }
            if (!opened) {
                log.error("Failed to re-establish connectivity");
            } else {
                log.error("Reconnected to " + getCurrentPortName());
                resetupConnection();
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractSerialPortController.class.getName());

}
