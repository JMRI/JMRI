package jmri.jmrix.cmri.serial.sim;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import jmri.jmrix.cmri.serial.SerialSensorManager;
import jmri.jmrix.cmri.serial.SerialTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends the serialdriver.SimDriverAdapter class to act as simulated
 * connection.
 *
 * @author	Bob Jacobsen Copyright (C) 2002, 2008, 2011
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
        justification = "Access to 'self' OK until multiple instance pattern installed")
public class SimDriverAdapter extends jmri.jmrix.cmri.serial.serialdriver.SerialDriverAdapter {

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
    public boolean okToSend() {
        return true;
    }

    /**
     * set up all of the other objects to operate connected to this port
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "Access to 'self' OK until multiple instance pattern installed")

    public void configure() {
        // install a traffic controller that doesn't time out
        new SerialTrafficController() {
            // timeout doesn't do anything
            @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
                    justification = "only until multi-connect update done")
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage m, jmri.jmrix.AbstractMRListener l) {
            }

            // and make this the instance
            {
                self = this;
            }
        };

        // connect to the traffic controller
        SerialTrafficController.instance().connectPort(this);

        jmri.InstanceManager.setTurnoutManager(jmri.jmrix.cmri.serial.SerialTurnoutManager.instance());
        jmri.InstanceManager.setLightManager(jmri.jmrix.cmri.serial.SerialLightManager.instance());

        SerialSensorManager s;
        jmri.InstanceManager.setSensorManager(s = jmri.jmrix.cmri.serial.SerialSensorManager.instance());
        SerialTrafficController.instance().setSensorManager(s);
        jmri.jmrix.cmri.serial.ActiveFlag.setActive();
    }

    // base class methods for the SerialPortController interface
    public DataInputStream getInputStream() {
        try {
            return new DataInputStream(new java.io.PipedInputStream(new java.io.PipedOutputStream()));
        } catch (Exception e) {
            return null;
        }
        //return new DataInputStream(serialStream);
    }

    public DataOutputStream getOutputStream() {
        return new DataOutputStream(new java.io.OutputStream() {
            public void write(int b) throws java.io.IOException {
            }
        });
    }

    public boolean status() {
        return opened;
    }

    /**
     * Local method to do specific port configuration
     */
    protected void setSerialPort() throws gnu.io.UnsupportedCommOperationException {
    }

    /**
     * Get an array of valid baud rates.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP") // OK to expose array instead of copy until Java 1.6
    public String[] validBaudRates() {
        return validSpeeds;
    }

    /**
     * Set the baud rate.
     */
    public void configureBaudRate(String rate) {
        log.debug("configureBaudRate: " + rate);
        selectedSpeed = rate;
        super.configureBaudRate(rate);
    }

    // private control members
    private boolean opened = false;
    InputStream serialStream = null;

    static public jmri.jmrix.cmri.serial.serialdriver.SerialDriverAdapter instance() {
        if (mInstance == null) {
            mInstance = new SimDriverAdapter();
        }
        return mInstance;
    }
    static SimDriverAdapter mInstance;

    private final static Logger log = LoggerFactory.getLogger(SimDriverAdapter.class.getName());

}
