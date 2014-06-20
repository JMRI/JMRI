// SerialDriverAdapter.java

package jmri.jmrix.tams.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.tams.TamsMessage;
import jmri.jmrix.tams.TamsPortController;
import jmri.jmrix.tams.TamsTrafficController;
import jmri.jmrix.tams.TamsSystemConnectionMemo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.IOException;
import jmri.jmrix.tams.TamsReply;

/**
 * MRC simulator
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @author			Paul Bender, Copyright (C) 2009
 * @author 			Daniel Boudreau Copyright (C) 2010
 * @version			$Revision: 24776 $
 */
public class SimulatorAdapter extends TamsPortController implements
		jmri.jmrix.SerialPortAdapter, Runnable {

	// private control members
	private boolean opened = false;
	private Thread sourceThread;
	
	// streams to share with user class
	private DataOutputStream pout = null; // this is provided to classes who want to write to us
	private DataInputStream pin = null; // this is provided to class who want data from us

	// internal ends of the pipes
	private DataOutputStream outpipe = null; // feed pin
	private DataInputStream inpipe = null; // feed pout
	    
    public SimulatorAdapter (){
        super();
        adaptermemo = new TamsSystemConnectionMemo();
    }

    @Override
    public TamsSystemConnectionMemo getSystemConnectionMemo() {
    	return adaptermemo;
	}

    public void dispose(){
        if (adaptermemo!=null)
            adaptermemo.dispose();
        adaptermemo = null;
    }
	
	public String openPort(String portName, String appName) {
		try {
			PipedOutputStream tempPipeI = new PipedOutputStream();
			pout = new DataOutputStream(tempPipeI);
			inpipe = new DataInputStream(new PipedInputStream(tempPipeI));
			PipedOutputStream tempPipeO = new PipedOutputStream();
			outpipe = new DataOutputStream(tempPipeO);
			pin = new DataInputStream(new PipedInputStream(tempPipeO));
		} catch (java.io.IOException e) {
			log.error("init (pipe): Exception: " + e.toString());
		}
		opened = true;
		return null; // indicates OK return
	}

	/**
	 * set up all of the other objects to simulate operation with an MRC command
	 * station.
	 */
	public void configure() {
        TamsTrafficController tc = new TamsTrafficController();
        tc.connectPort(this);
        adaptermemo.setTamsTrafficController(tc);
        tc.setAdapterMemo(adaptermemo);
        //tc.connectPort(this);     
		                
        adaptermemo.configureManagers();
        //tc.setCabNumber(2);
		jmri.jmrix.tams.ActiveFlag.setActive();

		// start the simulator
		sourceThread = new Thread(this);
		sourceThread.setName("Tams Simulator");
		sourceThread.setPriority(Thread.MIN_PRIORITY);
		sourceThread.start();
        //tc.startThreads();
	}

	// base class methods for the TamsPortController interface
	public DataInputStream getInputStream() {
		if (!opened || pin == null) {
			log.error("getInputStream called before load(), stream not available");
		}
		return pin;
	}

	public DataOutputStream getOutputStream() {
		if (!opened || pout == null) {
			log.error("getOutputStream called before load(), stream not available");
		}
		return pout;
	}

	public boolean status() {
		return opened;
	}

	/**
	 * Get an array of valid baud rates.
	 */
	public String[] validBaudRates() {
		log.debug("validBaudRates should not have been invoked");
		return null;
	}

	public String getCurrentBaudRate() {
		return "";
	}

	public void run() { // start a new thread
		// this thread has one task.  It repeatedly reads from the input pipe
		// and writes an appropriate response to the output pipe.  This is the heart
		// of the TAMS command station simulation.
        // report status?
        if (log.isInfoEnabled()) 
            log.info("TAMS Simulator Started");     
        int cab = 1;
		while (true) {
			try{
                synchronized(this){
                    wait(50);
                }
			}catch (Exception e){

			}
            TamsMessage m = readMessage();
            TamsReply r;
			if (log.isDebugEnabled()) {
				StringBuffer buf = new StringBuffer();
				buf.append("Tams Simulator Thread received message: ");
				if (m != null) {
					for (int i = 0; i < m.getNumDataElements(); i++)
						buf.append(Integer.toHexString(0xFF & m.getElement(i)) + " ");
				} else {
					buf.append("null message buffer");
				}
				log.debug(buf.toString());
			}
			if (m != null) {
                //if(m.isReplyExpected()){
                    r = generateReply(m);
                    writeReply(r);
                //}
				if (log.isDebugEnabled() && r != null) {
					StringBuffer buf = new StringBuffer();
					buf.append("Tams Simulator Thread sent reply: ");
					for (int i = 0; i < r.getNumDataElements(); i++)
						buf.append(Integer.toHexString(0xFF & r.getElement(i)) + " ");
					log.debug(buf.toString());
				}
			}
		}
	}

	// readMessage reads one incoming message from the buffer
	private TamsMessage readMessage() {
		TamsMessage msg = null;
		try {
            if(inpipe.available()>0)
                msg = loadChars();
		} catch (java.io.IOException e) {

		}
		return (msg);
	}

	/**
	 * Get characters from the input source.
	 *
	 * @returns filled message 
	 * @throws IOException when presented by the input source.
	 */
	private TamsMessage loadChars() throws java.io.IOException {
		int nchars;
		byte[] rcvBuffer = new byte[32];

		nchars = inpipe.read(rcvBuffer, 0, 32);
		//log.debug("new message received");
		TamsMessage msg = new TamsMessage(nchars);

		for (int i = 0; i < nchars; i++) {
			msg.setElement(i, rcvBuffer[i] & 0xFF);
		}
		return msg;
	}

	// generateReply is the heart of the simulation.  It translates an 
	// incoming TamsMessage into an outgoing TamsReply.
	private TamsReply generateReply(TamsMessage m) {
		TamsReply reply = new TamsReply();
        int i = 0;
        log.debug("Rec " + m.toString());
        if(m.toString().startsWith("xY")){
            reply.setElement(i++, 0x00);
        } else if(m.toString().startsWith("xSR")){
            reply.setElement(i++, 0x53);
            reply.setElement(i++, 0x52);
            reply.setElement(i++, 0x20);
            reply.setElement(i++, m.getElement(3));
        } else if (m.getElement(0)==0x99){// && m.getElement(1)==0x53 && m.getElement(2)=0x52 && m.getElement(3)==0x30){
            reply.setElement(i++, 0x55); reply.setElement(i++, 0x55);
            reply.setElement(i++, 0xAA); reply.setElement(i++, 0xAA);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00); 
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
            reply.setElement(i++, 0x00); reply.setElement(i++, 0x00);
        }
        reply.setElement(i++, 0x0d);
        reply.setElement(i++, 0x5d);
		return reply;
	}

	private void writeReply(TamsReply r) {
		if(r == null)
			return;
		for (int i = 0; i < r.getNumDataElements(); i++){
			try {
				outpipe.writeByte((byte) r.getElement(i));
			} catch (java.io.IOException ex) {
			}
		}
		try {
			outpipe.flush();
		} catch (java.io.IOException ex) {
		}
	}
    
	static Logger log = LoggerFactory
			.getLogger(SimulatorAdapter.class.getName());

}
