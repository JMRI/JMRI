package jmri.jmrix.cmri.serial.sim;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;

import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.serial.SerialTrafficController;
import jmri.util.ImmediatePipedOutputStream;

/**
 * Extends the serialdriver.SimDriverAdapter class to act as simulated
 * connection.
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2008, 2011
 */
public class SimDriverAdapter extends jmri.jmrix.cmri.serial.serialdriver.SerialDriverAdapter {

    @Override
    public String openPort(String portName, String appName) {
            // don't even try to get port

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
    @Override
    public void configure() {
        // install a traffic controller that doesn't time out
        SerialTrafficController tc = new SerialTrafficController() {
            // timeout doesn't do anything
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
            return new DataInputStream(new PipedInputStream(new ImmediatePipedOutputStream()));
        } catch (Exception e) {
            return null;
        }
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

    @Override
    public String getCurrentPortName(){
       return "";
    }

    // private control members
    private boolean opened = false;

}
