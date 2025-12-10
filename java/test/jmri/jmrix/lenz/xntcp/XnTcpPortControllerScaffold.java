package jmri.jmrix.lenz.xntcp;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.*;

import jmri.util.JUnitUtil;

/**
 * Implementation of XnTcpAdapter that eases
 * checking whether data was forwarded or not
 *
 * @author Bob Jacobsen Copyright (C) 2006, 2015
 */
class XnTcpPortControllerScaffold extends XnTcpAdapter {

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(XnTcpPortControllerScaffold.class);

    public java.util.Vector<String> getPortNames() {
        return null;
    }

    public String openPort(String portName, String appName) {
        return null;
    }

    @Override
    public void configure() {
    }

    public String[] validBaudRates() {
        return new String[] {};
    }

    public int[] validBaudNumbers() {
        return new int[] {};
    }

    private PipedInputStream otempIPipe;
    private PipedOutputStream otempOPipe;

    private PipedInputStream itempIPipe;
    private PipedOutputStream itempOPipe;

    protected XnTcpPortControllerScaffold() {
        assertDoesNotThrow( () -> {
            otempIPipe = new PipedInputStream(200);
            tostream = new DataInputStream(otempIPipe);
            otempOPipe = new PipedOutputStream(otempIPipe);
            ostream = new DataOutputStream(otempOPipe);

            itempIPipe = new PipedInputStream(200);
            istream = new DataInputStream(itempIPipe);
            itempOPipe = new PipedOutputStream(itempIPipe);
            tistream = new DataOutputStream(itempOPipe);
        });
    }

    public void flush() {
        try {
            ostream.flush();
            otempOPipe.flush();

            tistream.flush();
            itempOPipe.flush();

            JUnitUtil.waitFor(JUnitUtil.WAITFOR_DEFAULT_DELAY);

        } catch (IOException e) {
            log.error("Exception during flush", e);
        }
    }

    /**
     * Returns the InputStream from the port.
     */
    @Override
    public DataInputStream getInputStream() {
        return istream;
    }

    /**
     * Returns the outputStream to the port.
     */
    @Override
    public DataOutputStream getOutputStream() {
        return ostream;
    }

    /**
     * Check that this object is ready to operate.
     */
    @Override
    public boolean status() {
        return true;
    }

    @Override
    public boolean okToSend() {
        return true;
    }

    @Override
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
