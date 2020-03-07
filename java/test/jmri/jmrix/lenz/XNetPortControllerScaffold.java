package jmri.jmrix.lenz;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of XNetPortController that eases
 * checking whether data was forwarded or not.
 * 
 * @author	Bob Jacobsen Copyright (C) 2006, 2015
 */
public class XNetPortControllerScaffold extends XNetSimulatorPortController {

    private final static Logger log = LoggerFactory.getLogger(XNetPortControllerScaffold.class);

    /*@Override
    public java.util.Vector<String> getPortNames() {
        return null;
    }*/

    @Override
    public String openPort(String portName, String appName) {
        return null;
    }

    @Override
    public void configure() {
    }

    @Override
    public String getCurrentPortName() {
         return("testport");
    }

    @Override
    public String[] validBaudRates() {
        return new String[] {};
    }

    @Override
    public int[] validBaudNumbers() {
        return new int[] {};
    }

    PipedInputStream otempIPipe;
    PipedOutputStream otempOPipe;
    
    PipedInputStream itempIPipe;
    PipedOutputStream itempOPipe;
    
    public XNetPortControllerScaffold() throws Exception {
        otempIPipe = new PipedInputStream(200);
        tostream = new DataInputStream(otempIPipe);
        otempOPipe = new PipedOutputStream(otempIPipe);
        ostream = new DataOutputStream(otempOPipe);

        itempIPipe = new PipedInputStream(200);
        istream = new DataInputStream(itempIPipe);
        itempOPipe = new PipedOutputStream(itempIPipe);
        tistream = new DataOutputStream(itempOPipe);
    }

    public void flush() {
        try { 
            ostream.flush();
            otempOPipe.flush();
        
            tistream.flush();
            itempOPipe.flush();

            jmri.util.JUnitUtil.releaseThread(this);

        } catch (Exception e) {
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
    public DataInputStream tostream;

    /**
     * Tests write to this.
     */
    public DataOutputStream tistream;
    /**
     * The traffic controller can read test data from this.
     */
    DataInputStream istream;

}
