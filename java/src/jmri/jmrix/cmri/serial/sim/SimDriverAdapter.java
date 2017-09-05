package jmri.jmrix.cmri.serial.sim;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.serial.SerialTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Extends the serialdriver.SimDriverAdapter class to act as simulated
 * connection.
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2008, 2011
 */
@SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
        justification = "Access to 'self' OK until multiple instance pattern installed")
public class SimDriverAdapter extends jmri.jmrix.cmri.serial.serialdriver.SerialDriverAdapter {

    @Override
    public String openPort(String portName, String appName) {
            // don't even try to get port

        // get and save stream
        serialStream = null;

        opened = true;

        return null; // normal operation
    }

    /**
     * Can the port accept additional characters? Yes, always
     */
    @Override
    public boolean okToSend() {
        return true;
    }

    /**
     * set up all of the other objects to operate connected to this port
     */
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "Access to 'self' OK until multiple instance pattern installed")

    @Override
    public void configure() {
        // install a traffic controller that doesn't time out
        SerialTrafficController tc = new SerialTrafficController() {
            // timeout doesn't do anything
            @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
                    justification = "only until multi-connect update done")
            @Override
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage m, jmri.jmrix.AbstractMRListener l) {
            }
        };

        // connect to the traffic controller
        tc.connectPort(this);
        ((CMRISystemConnectionMemo)getSystemConnectionMemo()).setTrafficController(tc);
        ((CMRISystemConnectionMemo)getSystemConnectionMemo()).configureManagers();

    }

    // base class methods for the SerialPortController interface
    @Override
    public DataInputStream getInputStream() {
        try {
            return new DataInputStream(new java.io.PipedInputStream(new java.io.PipedOutputStream()));
        } catch (Exception e) {
            return null;
        }
        //return new DataInputStream(serialStream);
    }

    @Override
    public DataOutputStream getOutputStream() {
        return new DataOutputStream(new java.io.OutputStream() {
            @Override
            public void write(int b) throws java.io.IOException {
            }
        });
    }

    @Override
    public boolean status() {
        return opened;
    }

    /**
     * Local method to do specific port configuration
     */
    @Override
    protected void setSerialPort() throws UnsupportedCommOperationException {
    }

    @Override
    public String[] validBaudRates() {
        return Arrays.copyOf(validSpeeds, validSpeeds.length);
    }

    /**
     * Set the baud rate.
     */
    @Override
    public void configureBaudRate(String rate) {
        log.debug("configureBaudRate: " + rate);
        selectedSpeed = rate;
        super.configureBaudRate(rate);
    }

    // private control members
    private boolean opened = false;
    InputStream serialStream = null;

    /**
     * @deprecated JMRI Since 4.5.1 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static public jmri.jmrix.cmri.serial.serialdriver.SerialDriverAdapter instance() {
        if (mInstance == null) {
            mInstance = new SimDriverAdapter();
        }
        return mInstance;
    }
    static SimDriverAdapter mInstance;

    private final static Logger log = LoggerFactory.getLogger(SimDriverAdapter.class);

}
