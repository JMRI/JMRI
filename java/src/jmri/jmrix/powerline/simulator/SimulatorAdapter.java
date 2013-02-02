// SerialDriverAdapter.java

package jmri.jmrix.powerline.simulator;

import org.apache.log4j.Logger;
import jmri.jmrix.powerline.SerialSystemConnectionMemo;
import jmri.jmrix.powerline.SerialPortController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Implement simulator for powerline serial systems
 * <P>
 * System names are "PLnnn", where nnn is the bit number without padding.
 *
 * This is based on the NCE simulator.
 * 
 * @author	Dave Duchamp Copyright (C) 2004
 * @author	Bob Jacobsen Copyright (C) 2006, 2007, 2008
 * Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 * @version	$Revision$
 */
public class SimulatorAdapter extends SerialPortController implements
		jmri.jmrix.SerialPortAdapter, Runnable {

	// private control members
	private boolean opened = false;
	private Thread sourceThread;
	
	// streams to share with user class
	private DataOutputStream pout = null; // this is provided to classes who want to write to us
	private DataInputStream pin = null; // this is provided to class who want data from us

	// internal ends of the pipes
	@SuppressWarnings("unused")
	private DataOutputStream outpipe = null; // feed pin
	@SuppressWarnings("unused")
	private DataInputStream inpipe = null; // feed pout
	
	
    public SimulatorAdapter (){
        super();
        adaptermemo = new SpecificSystemConnectionMemo();
    }

    @Override
    public SerialSystemConnectionMemo getSystemConnectionMemo() {
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
	 * set up all of the other objects to simulate operation with an command
	 * station.
	 */
	public void configure() {
		SpecificTrafficController tc = new SpecificTrafficController(adaptermemo);
		
        // connect to the traffic controller
        adaptermemo.setTrafficController(tc);
        tc.setAdapterMemo(adaptermemo);     
        adaptermemo.configureManagers();
        tc.connectPort(this);
        
        // Configure the form of serial address validation for this connection
        adaptermemo.setSerialAddress(new jmri.jmrix.powerline.SerialAddress(adaptermemo));

		jmri.jmrix.powerline.ActiveFlag.setActive();

		// start the simulator
		sourceThread = new Thread(this);
		sourceThread.setName("Powerline Simulator");
		sourceThread.setPriority(Thread.MIN_PRIORITY);
		sourceThread.start();
	}

	// base class methods for the PortController interface
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
		// of the command station simulation.
        // report status?
        if (log.isInfoEnabled()) 
            log.info("Powerline Simulator Started");     
		while (true) {
			try{
				wait(100);
			}catch (Exception e){

			}
			/*
			SerialMessage m = readMessage();
			if (log.isDebugEnabled()) {
				StringBuffer buf = new StringBuffer();
				buf.append("Powerline Simulator Thread received message: ");
				for (int i = 0; i < m.getNumDataElements(); i++)
					buf.append(Integer.toHexString(0xFF & m.getElement(i)) + " ");
				log.debug(buf.toString());
			}
			if (m != null) {
				SerialReply r = generateReply(m);
				writeReply(r);
				if (log.isDebugEnabled() && r != null) {
					StringBuffer buf = new StringBuffer();
					buf.append("Powerline Simulator Thread sent reply: ");
					for (int i = 0; i < r.getNumDataElements(); i++)
						buf.append(Integer.toHexString(0xFF & r.getElement(i)) + " ");
					log.debug(buf.toString());
				}
			}
			*/
		}
	}
//
//	// readMessage reads one incoming message from the buffer
//	private SerialMessage readMessage() {
//		SerialMessage msg = null;
//		try {
//			msg = loadChars();
//		} catch (java.io.IOException e) {
//
//		}
//		return (msg);
//	}
//
//	/**
//	 * Get characters from the input source.
//	 *
//	 * @returns filled message 
//	 * @throws IOException when presented by the input source.
//	 */
//	private SerialMessage loadChars() throws java.io.IOException {
//		int nchars;
//		byte[] rcvBuffer = new byte[32];
//
//		nchars = inpipe.read(rcvBuffer, 0, 32);
//		//log.debug("new message received");
//		SerialMessage msg = new SerialMessage(nchars);
//
//		for (int i = 0; i < nchars; i++) {
//			msg.setElement(i, rcvBuffer[i] & 0xFF);
//		}
//		return msg;
//	}
//
//	// generateReply is the heart of the simulation.  It translates an 
//	// incoming Message into an outgoing Reply.
//	private SerialReply generateReply(SerialMessage m) {
//		SerialReply reply = new SerialReply(adaptermemo.getTrafficController());
//		return reply;
//	}
//
//	private void writeReply(SerialReply r) {
//		if(r == null)
//			return;
//		for (int i = 0; i < r.getNumDataElements(); i++){
//			try {
//				outpipe.writeByte((byte) r.getElement(i));
//			} catch (java.io.IOException ex) {
//			}
//		}
//		try {
//			outpipe.flush();
//		} catch (java.io.IOException ex) {
//		}
//	}
		
	static Logger log = org.apache.log4j.Logger
			.getLogger(SimulatorAdapter.class.getName());

}
