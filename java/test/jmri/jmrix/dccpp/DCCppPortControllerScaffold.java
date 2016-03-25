package jmri.jmrix.dccpp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * DCCppPortControllerScaffold.java
 *
 * Description:	test implementation of DCCppPortController
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 * @author	Mark Underwood Copyright (C) 2015
 */
class DCCppPortControllerScaffold extends DCCppSimulatorPortController {

    public java.util.Vector<String> getPortNames() {
        return null;
    }

    public String openPort(String portName, String appName) {
        return null;
    }

    public void configure() {
    }

    public String[] validBaudRates() {
        return null;
    }

    protected DCCppPortControllerScaffold() throws Exception {
        PipedInputStream tempPipe;
        tempPipe = new PipedInputStream();
        tostream = new DataInputStream(tempPipe);
        ostream = new DataOutputStream(new PipedOutputStream(tempPipe));
        tempPipe = new PipedInputStream();
        istream = new DataInputStream(tempPipe);
        tistream = new DataOutputStream(new PipedOutputStream(tempPipe));
    }

    /**
     * Returns the InputStream from the port.
     */
    public DataInputStream getInputStream() {
        return istream;
    }

    /**
     * Returns the outputStream to the port.
     */
    public DataOutputStream getOutputStream() {
        return ostream;
    }

    /**
     * Check that this object is ready to operate.
     */
    public boolean status() {
        return true;
    }

    public boolean okToSend() {
        return true;
    }

    public void setOutputBufferEmpty(boolean s) {
    }

    /**
     * Traffic controller writes to this.
     */
    DataOutputStream ostream;
    /**
     * Can read test data from this.
     */
    DataInputStream tostream;

    /**
     * Tests write to this.
     */
    DataOutputStream tistream;
    /**
     * The traffic controller can read test data from this.
     */
    DataInputStream istream;

}
