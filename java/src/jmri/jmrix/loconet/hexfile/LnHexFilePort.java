// LnHexFilePort.java

package jmri.jmrix.loconet.hexfile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.loconet.LnPortController;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * LnHexFilePort implements a LnPortController via a
 * ASCII-hex input file. See below for the file format
 * There are user-level controls for
 *      send next message
 *	how long to wait between messages
 *
 * An object of this class should run in a thread
 * of its own so that it can fill the output pipe as
 * needed.
 *
 *	The input file is expected to have one message per line. Each line
 *	can contain as many bytes as needed, each represented by two Hex characters
 *	and separated by a space. Variable whitespace is not (yet) supported
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision$
 */
public class LnHexFilePort extends LnPortController implements Runnable, jmri.jmrix.SerialPortAdapter {

    BufferedReader sFile = null;

    public LnHexFilePort() {
        super();
        try {
            PipedInputStream tempPipe = new PipedInputStream();
            pin = new DataInputStream(tempPipe);
            outpipe = new DataOutputStream(new PipedOutputStream(tempPipe));
            pout = outpipe;
        }
        catch (java.io.IOException e) {
            log.error("init (pipe): Exception: "+e.toString());
        }
        adaptermemo = new LocoNetSystemConnectionMemo();
    }
    
    /* load(File) fills the contents from a file */
    public void load(File file) {
        if (log.isDebugEnabled()) log.debug("file: "+file);

        // create the pipe stream for output, also store as the input stream if somebody wants to send
        // (This will emulate the LocoNet echo)
        try {
            sFile = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        } catch (Exception e) {
            log.error("load (pipe): Exception: "+e.toString());
        }
    }
    
    public void connect() throws Exception{
        jmri.jmrix.loconet.hexfile.HexFileFrame f
            = new jmri.jmrix.loconet.hexfile.HexFileFrame();
            
        f.setAdapter(this);
        try {
            f.initComponents();
        } catch (Exception ex) {
            //log.error("starting HexFileFrame exception: "+ex.toString());
        }
        f.configure();
    }

    public void run() { // invoked in a new thread
        while (true) {
            if (sFile!= null) {
                _running = true;
                // process the input file into the output side of pipe
                try {
                    String s;
                    byte bval;
                    int ival;
                    int len;
                    while ( (s = sFile.readLine()) != null) {
                        // this loop reads one line per turn
                        // ErrLog.msg(ErrLog.debugging,"LnHexFilePort","run","string=<"+s+">");
                        len = s.length();
                        for (int i=0; i<len; i+=3) {
                            // parse as hex into integer, then convert to byte
                            ival = Integer.valueOf(s.substring(i,i+2),16).intValue();
                            // send each byte to the output pipe (input to consumer)
                            bval = (byte) ival;
                            outpipe.writeByte(bval);
                        }
                        // finished that line, wait
                        Thread.sleep(delay);
                    }
                } catch (Exception e) {
                    log.error("run: Exception: "+e.toString());
                }
                // here we're done processing the file
                log.info("normal finish to file");
                sFile = null;
                _running = false;
            }
            // wait to be told there's another file
            try {
                Thread.sleep(Math.max(50, delay));  // wait at least 50 msec
            } catch (java.lang.InterruptedException e) {
                Thread.currentThread().interrupt(); // retain if needed later
                log.debug("woken from sleep");
            }
        }
    }

    /**
     * Provide a new message delay value, but 
     * don't allow it to go below 2 msec.
     */
    public void setDelay(int newDelay) {
        delay = Math.max(2,newDelay);
    }

    // base class methods
    public DataInputStream getInputStream() {
        if (pin == null)
            log.error("getInputStream: called before load(), stream not available");
        return pin;
    }

    public DataOutputStream getOutputStream(){
        if (pout == null) log.error("getOutputStream: called before load(), stream not available");
        return pout;
    }

    public boolean status() {return (pout!=null)&(pin!=null);}

    // to tell if we're currently putting out data
    public boolean running() { return _running; }

    // private data
    private boolean _running = false;

    // streams to share with user class
    private DataOutputStream pout = null; // this is provided to classes who want to write to us
    private DataInputStream pin = null;  // this is provided to class who want data from us

    // internal ends of the pipes
    private DataOutputStream outpipe = null;  // feed pin
    //private DataInputStream inpipe = null; // feed pout

    public boolean okToSend() { return true; }
    // define operation
    private int delay=100;  				// units are milliseconds; default is quiet a busy LocoNet

    public java.util.Vector<String> getPortNames() {
        log.error("getPortNames should not have been invoked");
        new Exception().printStackTrace();
        return null;
    }
    public String openPort(String portName, String appName) {
        log.error("openPort should not have been invoked");
        new Exception().printStackTrace();
        return null;
    }
    public void configure() {
        log.error("configure should not have been invoked");
        //new Exception().printStackTrace();
    }
    public String[] validBaudRates() {
        log.error("validBaudRates should not have been invoked");
        new Exception().printStackTrace();
        return null;
    }

    /**
     * Get an array of valid values for "option 3"; used to display valid options.
     * May not be null, but may have zero entries
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="EI_EXPOSE_REP") // OK to expose array instead of copy until Java 1.6
    public String[] validOption3() { return new String[]{"Normal", "Spread", "One Only", "Both"}; }

    /**
     * Get a String that says what Option 3 represents
     * May be an empty string, but will not be null
     */
    public String option3Name() { return "Turnout command handling: "; }

    /**
     * Set the third port option.  Only to be used after construction, but
     * before the openPort call
     */
    public void configureOption3(String value) {
        super.configureOption3(value);
    	log.debug("configureOption3: "+value);
        setTurnoutHandling(value);
    }
 
    public void dispose() {
        // leaves the LocoNet Packetizer (e.g. the simulated connection)
        // running.
        if(adaptermemo!=null)
            adaptermemo.dispose();
        adaptermemo = null;
        super.dispose();

    }
    
    public LocoNetSystemConnectionMemo getAdapterMemo() { return adaptermemo; }
    public LocoNetSystemConnectionMemo getSystemConnectionMemo() { return adaptermemo; }

    static Logger log = LoggerFactory.getLogger(LnHexFilePort.class.getName());
}

/* @(#)LnHexFilePort.java */
