// SerialDriverAdapter.java

package jmri.jmrix.nce.simulator;

import jmri.jmrix.nce.NceBinaryCommand;
import jmri.jmrix.nce.NceReply;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NcePortController;
import jmri.jmrix.nce.NceProgrammer;
import jmri.jmrix.nce.NceProgrammerManager;
import jmri.jmrix.nce.NceSensorManager;
import jmri.jmrix.nce.NceTrafficController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.IOException;

/**
 * The following was copied from the NCE Power Pro System Reference Manual.  It
 * provides the background for the various NCE command that are simulated
 * by this implementation.
 * <p> 
 * Implements a Command Station Simulator for the NCE system.
 * <p>
 * BINARY COMMAND SET
 * <p>
 * The RS-232 port binary commands are designed to work in a computer friendly way.
 * Command format is: <command number> <data> <data> ...
 * <p>
 * Commands range from 0x80 to 0xBF
 * <p>
 * Commands and formats supported:
 * Commands 0xAD to 0xBF are not used and return ‘0’
 * <p>
 * Errors returned:
 * ‘0’= command not supported
 * ’1'= loco address out of range
 * ’2'= cab address out of range
 * ’3'= data out of range
 * ’4'= byte count out of range
 * ’!’= command completed successfully
 * <p>
 * For a complete description of Binary Commands see: www.ncecorporation.com/pdf/
 * bincmds.pdf
 * <p>
 * 
 * Command			Description				#bytes rtn	Responses 		<br>
 * 0x80 			NOP, dummy instruction 	(1) 		!				<br>
 * 0x81 xx xx yy 	assign loco xxxx to cab cc (1) 		!, 1,2			<br>
 * 0x82 			read clock 				(2) <hours><minutes>		<br>
 * 0x83 			Clock stop 				(1) 		!				<br>
 * 0x84 			Clock start 			(1) 		!				<br>
 * 0x85 xx xx 		Set clock hr./min 		(1) 		!,3				<br>
 * 0x86 xx 			Set clock 12/24 		(1) 		!,3				<br>
 * 0x87 xx 			Set clock ratio 		(1) 		!,3				<br>
 * 0x88 xxxx 		Dequeue packet by loco addr (1) 	!, 1,2			<br>
 * 0x89 			Enable main trk, kill prog (1) 		!				<br>
 * 0x8A yy 			Return status of AIU yy (4) 					
 *	<current hi byte> <current lo byte> <change hi byte> <change lo byte> <br>
 * 0x8B 			Kill main trk, enable prog (1) 		!				<br>
 * 0x8C 			dummy inst. returns	"!" followed CR/LF(3) !0x0D, 0x0A <br>
 * 0x8D xxxx mm 	Set speed mode of loco xxxx to mode mm, 1=14, 2=28, 3=128
 * 									(1) 	!, 1,3<speed mode, 0 to 3>	<br>
 * 0x8E aaaa nn<16 data bytes>
 *					Write nn bytes, start at aaaa Must have 16 data bytes, pad
 * them out to 16 if necessary					(1) 		!,4			<br>
 * <P>
 * 0x8F aaaa 		Read 16 bytes, start at aaaa(16) 		16 bytes	<br>
 * 0x90 cc xx... 	Send 16 char message to Cab ccLCD line 3. xx = 16 ASCII char
 *												(1) 		! ,2		<br>
 * 0x91 cc xx 		Send 16 char message to cab cc LCD line 4. xx=16 ASCII
 * 												(1) 		!,2			<br>
 * 0x92 cc xx 		Send 8 char message to cab cc LCD line 2 right xx=8 char
 * 												(1) 		!,2			<br>
 * 0x93 ss<3 byte packet> Queue 3 byte packet to temp _Q send ss times
 *												(1) 		!			<br>
 * 0x94 ss<4 byte packet> Queue 4 byte packet to temp _Q send ss times
 * 												(1) 		!			<br>
 * 0x95 ss<5 byte packet> Queue 5 byte packet to temp_Q send ss times
 * 												(1) 		!			<br>
 * 0x96 ss<6 byte packet> Queue 6 byte packet to temp _Q send ss times
 * 												(1) 		!			<br>
 * 0x97 aaaa xx 	Write 1 byte to aaaa 		(1) 		!			<br>
 * 0x98 aaaa xx 	xxWrite 2 bytes to aaaa 	(1) 		!			<br>
 * 0x99 aaaa<4 data bytes> Write 4 bytes to aaaa (1) 		!			<br>
 * 
 * <P>
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @author			Paul Bender, Copyright (C) 2009
 * @author 			Daniel Boudreau Copyright (C) 2010
 * @version			$Revision: 1.1 $
 */
public class SimulatorAdapter extends NcePortController implements
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
	
	static SimulatorAdapter mInstance = null;
	static public SimulatorAdapter instance() {
		if (mInstance == null)
			mInstance = new SimulatorAdapter();
		return mInstance;
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
	 * set up all of the other objects to simulate operation with an NCE command
	 * station.
	 */
	public void configure() {
		// setting binary mode
		NceMessage.setCommandOptions(NceMessage.OPTION_2006);

		// connect to the traffic controller
		NceTrafficController.instance().connectPort(this);

		jmri.InstanceManager.setProgrammerManager(new NceProgrammerManager(
				new NceProgrammer()));

		jmri.InstanceManager.setPowerManager(new jmri.jmrix.nce.NcePowerManager());

		jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.nce.NceTurnoutManager());

		NceSensorManager s;
		jmri.InstanceManager.setSensorManager(s = new jmri.jmrix.nce.NceSensorManager());
		NceTrafficController.instance().setSensorManager(s);

		jmri.InstanceManager.setThrottleManager(new jmri.jmrix.nce.NceThrottleManager());

		jmri.InstanceManager.addClockControl(new jmri.jmrix.nce.NceClockControl());

		jmri.jmrix.nce.ActiveFlag.setActive();

		sourceThread = new Thread(this);
		sourceThread.setName("Nce Simulator");
		sourceThread.start();
	}

	// base class methods for the NcePortController interface
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

	String manufacturerName = jmri.jmrix.DCCManufacturerList.NCE;
	public String getManufacturer() {
		return manufacturerName;
	}

	public void setManufacturer(String manu) {
		manufacturerName = manu;
	}

	public void run() { // start a new thread
		// this thread has one task.  It repeatedly reads from the input pipe
		// and writes an appropriate response to the output pipe.  This is the heart
		// of the NCE command station simulation.
		if (log.isDebugEnabled())
			log.debug("Nce Simulator Thread Started");
		while (true) {
			NceMessage m = readMessage();
			if (log.isDebugEnabled()) {
				String f = "Nce Simulator Thread received message: ";
				for (int i = 0; i < m.getNumDataElements(); i++)
					f = f + Integer.toHexString(0xFF & m.getElement(i)) + " ";
				log.debug(f);
			}
			NceReply r = generateReply(m);
			writeReply(r);
			if (log.isDebugEnabled()) {
				String f = "Nce Simulator Thread sent reply: ";
				for (int i = 0; i < r.getNumDataElements(); i++)
					f = f + Integer.toHexString(0xFF & r.getElement(i)) + " ";
				log.debug(f);
			}
		}
	}

	// readMessage reads one incoming message from the buffer
	private NceMessage readMessage() {
		NceMessage msg = null;
		try {
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
	private NceMessage loadChars() throws java.io.IOException {
		int nchars;
		byte[] rcvBuffer = new byte[16];

		nchars = inpipe.read(rcvBuffer, 0, 16);
		log.debug("new message received");
		NceMessage msg = new NceMessage(nchars);

		for (int i = 0; i < nchars; i++) {
			msg.setElement(i, rcvBuffer[i] & 0xFF);
		}
		return msg;
	}

	// generateReply is the heart of the simulation.  It translates an 
	// incoming NceMessage into an outgoing NceReply.
	private NceReply generateReply(NceMessage m) {
		NceReply reply = new NceReply();
		int command = m.getElement(0);
		if (command < 0x80 || command > 0xBF){
			reply.setElement(0, 0 & 0xff);		// Nce command not supported
			return reply;	
		}
		switch (command) {
		case NceBinaryCommand.SW_REV_CMD:		// Get Eprom revision
			reply.setElement(0, 0x06 & 0xff); 	// Send Eprom revision 6 2 1
			reply.setElement(1, 0x02 & 0xff);
			reply.setElement(2, 0x01 & 0xff);
			break;
		case NceBinaryCommand.READ_CLOCK_CMD:	// Read clock
			reply.setElement(0, 0x12 & 0xff);	// Return fixed time
			reply.setElement(1, 0x30 & 0xff);
			break;
		case NceBinaryCommand.READ_AUI4_CMD:	// Read AUI 4 byte response
			reply.setElement(0, 0x00 & 0xff);	// fixed data for now
			reply.setElement(1, 0x00 & 0xff);	// fixed data for now
			reply.setElement(2, 0x00 & 0xff);	// fixed data for now
			reply.setElement(3, 0x00 & 0xff);	// fixed data for now
			break;
		case NceBinaryCommand.DUMMY_CMD:		// Dummy instruction
			reply.setElement(0, '!' & 0xff);	// return ! CR LF
			reply.setElement(1, 0x0D & 0xff);
			reply.setElement(2, 0x0A & 0xff);
			break;
		case NceBinaryCommand.READ16_CMD:		// Read 16 bytes
			for (int i=0; i<16; i++)
				reply.setElement(i, 0x00 & 0xff);	// fixed data for now
			break;
			
		default:
			reply.setElement(0, '!' & 0xff); 	// Nce okay reply!
		}
		return reply;
	}

	private void writeReply(NceReply r) {
		for (int i = 0; i < r.getNumDataElements(); i++)
			try {
				outpipe.writeByte((byte) r.getElement(i));
			} catch (java.io.IOException ex) {
			}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(SimulatorAdapter.class.getName());

}
