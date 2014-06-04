// SerialDriverAdapter.java

package jmri.jmrix.nce.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.nce.NceBinaryCommand;
import jmri.jmrix.nce.NceReply;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceCmdStationMemory;
import jmri.jmrix.nce.NcePortController;
import jmri.jmrix.nce.NceTrafficController;
import jmri.jmrix.nce.NceTurnoutMonitor;
import jmri.jmrix.nce.NceSystemConnectionMemo;

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
 * Commands 0xAD to 0xBF are not used and return '0'
 * <p>
 * Errors returned:
 * '0'= command not supported
 * '1'= loco address out of range
 * '2'= cab address out of range
 * '3'= data out of range
 * '4'= byte count out of range
 * '!'= command completed successfully
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
 * 0x9A aaaa<8 data bytes> Write 8 bytes to aaaa (1) 		!			<br>
 * 0x9B yy 			Return status of AIU yy (short form of command 0x8A)
 *											(2) <current hi byte><current lo byte><br>
 * 0x9C xx 			Execute macro number xx 	(1) 		!, 0,3		<br>
 * 0x9D aaaa 		Read 1 byte from aaaa 		(1) 1 byte				<br>
 * 0x9E 			Enter programming track mode(1) 		!=success 3=short circuit	<br>
 * 0x9F 			Exit programming track mode (1) 		!=success	<br>
 * 0xA0 aaaa xx 	Program CV aa with data xx in paged mode 			<br>
 * 												(1) !=success 0=program track	<br>
 * 0xA1 aaaa 		Read CV aaaa in paged mode  Note: cv data followed by !
 * 					for OK. 0xFF followed by 3 for can't read CV (2) !, 0,3	<br>
 * 0xA2<4 data bytes> Locomotive control command (1) 			!,1		<br>
 * 0xA3<3 bytepacket> Queue 3 byte packet to TRK _Q (replaces any packet with
 * same address if it exists)					(1) 			!,1		<br>
 * 0xA4<4 byte packet> Queue 4 byte packet to TRK _Q (1) 		!,1		<br>
 * 0xA5<5 byte packet> Queue 5 byte packet to TRK _Q (1) 		!,1		<br>
 * 0xA6 rr dd 		Program register rr with dd 	(1) 	!=success	0=no program track <br>
 * 0xA7 rr 			Read register rr. Note: cv data followed by ! for OK.
 * 					0xFF followed by 3 for can't read CV (2) 	!,3	0=no program track <br>
 * 0xA8 aaaa dd 	Program CV aaaa with dd in direct mode.	(1) !=success 0=no program track<br>
 * 0xA9 aaaa 		Read CV aaaa in direct mode. Note: cv data followed by !
 * 					for OK. 0xFF followed by 3 for can't read CV (2) !,3<br>
 * 0xAA 			Return software revision number. Format: VV.MM.mm 
 * 													(3) 3 data bytes	<br>
 * 0xAB 			Perform soft reset of command station (like cycling power)
 * 					(0) Returns nothing									<br>
 * 0xAC 			Perform hard reset of command station. Reset to factory
 * 					defaults (Note: will change baud rate to 9600)(0) Returns nothing<br>
 * 0xAD <4 data bytes>Accy/signal and macro commands 	(1) 	!,1 		<br>
 * 
 * <P>
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @author			Paul Bender, Copyright (C) 2009
 * @author 			Daniel Boudreau Copyright (C) 2010
 * @version			$Revision$
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
	
	// Simulator responses
	char NCE_OKAY = '!';
	char NCE_ERROR = '0';
	char NCE_LOCO_OUT_OF_RANGE = '1';
	char NCE_CAB_OUT_OF_RANGE = '2';
	char NCE_DATA_OUT_OF_RANGE = '3';
	char NCE_BYTE_OUT_OF_RANGE = '4';
    
    public SimulatorAdapter (){
        super();
        adaptermemo = new NceSystemConnectionMemo();
    }

    @Override
    public NceSystemConnectionMemo getSystemConnectionMemo() {
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
	 * set up all of the other objects to simulate operation with an NCE command
	 * station.
	 */
	public void configure() {
        NceTrafficController tc = new NceTrafficController();
        adaptermemo.setNceTrafficController(tc);
        tc.setAdapterMemo(adaptermemo);
        tc.connectPort(this);     
		
		// setting binary mode
        adaptermemo.configureCommandStation(NceTrafficController.OPTION_2006);
        tc.setCmdGroups(NceTrafficController.CMDS_MEM |
        		NceTrafficController.CMDS_AUI_READ |
        		NceTrafficController.CMDS_PROGTRACK |
        		NceTrafficController.CMDS_OPS_PGM |
        		NceTrafficController.CMDS_USB |
        		NceTrafficController.CMDS_NOT_USB |
        		NceTrafficController.CMDS_CLOCK |
        		NceTrafficController.CMDS_ALL_SYS);
        tc.setUsbSystem(NceTrafficController.USB_SYSTEM_NONE);
                
        adaptermemo.configureManagers();
        
		jmri.jmrix.nce.ActiveFlag.setActive();

		// start the simulator
		sourceThread = new Thread(this);
		sourceThread.setName("Nce Simulator");
		sourceThread.setPriority(Thread.MIN_PRIORITY);
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

	public void run() { // start a new thread
		// this thread has one task.  It repeatedly reads from the input pipe
		// and writes an appropriate response to the output pipe.  This is the heart
		// of the NCE command station simulation.
        // report status?
        if (log.isInfoEnabled()) 
            log.info("NCE Simulator Started");     
		while (true) {
			try{
				wait(100);
			}catch (Exception e){

			}
			NceMessage m = readMessage();
			if (log.isDebugEnabled()) {
				StringBuffer buf = new StringBuffer();
				buf.append("Nce Simulator Thread received message: ");
				for (int i = 0; i < m.getNumDataElements(); i++)
					buf.append(Integer.toHexString(0xFF & m.getElement(i)) + " ");
				log.debug(buf.toString());
			}
			if (m != null) {
				NceReply r = generateReply(m);
				writeReply(r);
				if (log.isDebugEnabled() && r != null) {
					StringBuffer buf = new StringBuffer();
					buf.append("Nce Simulator Thread sent reply: ");
					for (int i = 0; i < r.getNumDataElements(); i++)
						buf.append(Integer.toHexString(0xFF & r.getElement(i)) + " ");
					log.debug(buf.toString());
				}
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
		byte[] rcvBuffer = new byte[32];

		nchars = inpipe.read(rcvBuffer, 0, 32);
		//log.debug("new message received");
		NceMessage msg = new NceMessage(nchars);

		for (int i = 0; i < nchars; i++) {
			msg.setElement(i, rcvBuffer[i] & 0xFF);
		}
		return msg;
	}

	// generateReply is the heart of the simulation.  It translates an 
	// incoming NceMessage into an outgoing NceReply.
	private NceReply generateReply(NceMessage m) {
		NceReply reply = new NceReply(adaptermemo.getNceTrafficController());
		int command = m.getElement(0);
		if (command < 0x80) 					// NOTE: NCE command station does not respond to
			return null;						// command less than 0x80 (times out)
		if (command < 0x80 || command > 0xBF){	// Command is out of range
			reply.setElement(0, NCE_ERROR);		// Nce command not supported
			return reply;	
		}
		switch (command) {
		case NceBinaryCommand.SW_REV_CMD:		// Get Eprom revision
			reply.setElement(0, 0x06); 			// Send Eprom revision 6 2 1
			reply.setElement(1, 0x02);
			reply.setElement(2, 0x01);
			break;
		case NceBinaryCommand.READ_CLOCK_CMD:	// Read clock
			reply.setElement(0, 0x12);			// Return fixed time
			reply.setElement(1, 0x30);
			break;
		case NceBinaryCommand.READ_AUI4_CMD:	// Read AUI 4 byte response
			reply.setElement(0, 0x00);			// fixed data for now
			reply.setElement(1, 0x00);			// fixed data for now
			reply.setElement(2, 0x00);			// fixed data for now
			reply.setElement(3, 0x00);			// fixed data for now
			break;
		case NceBinaryCommand.DUMMY_CMD:		// Dummy instruction
			reply.setElement(0, NCE_OKAY);		// return ! CR LF
			reply.setElement(1, 0x0D);
			reply.setElement(2, 0x0A);
			break;
		case NceBinaryCommand.READ16_CMD:		// Read 16 bytes
			readMemory(m, reply, 16);
			break;
		case NceBinaryCommand.READ_AUI2_CMD:	// Read AUI 2 byte response
			reply.setElement(0, 0x00);			// fixed data for now
			reply.setElement(1, 0x00);			// fixed data for now
			break;
		case NceBinaryCommand.READ1_CMD:		// Read 1 bytes
			readMemory(m, reply, 1);
			break;
		case NceBinaryCommand.WRITE1_CMD:		// Write 1 bytes
			writeMemory(m, reply, 1, false);
			break;
		case NceBinaryCommand.WRITE2_CMD:		// Write 2 bytes
			writeMemory(m, reply, 2, false);
			break;
		case NceBinaryCommand.WRITE4_CMD:		// Write 4 bytes
			writeMemory(m, reply, 4, false);
			break;
		case NceBinaryCommand.WRITE8_CMD:		// Write 8 bytes
			writeMemory(m, reply, 8, false);
			break;
		case NceBinaryCommand.WRITEn_CMD:		// Write n bytes
			writeMemory(m, reply, m.getElement(3), true);
			break;
		case NceBinaryCommand.ACC_CMD:			// accessory command
			accessoryCommand(m, reply);
			break;
		case NceMessage.READ_DIR_CV_CMD:
			reply.setElement(0, 172);			// dummy data
			//reply.setElement(1,NCE_DATA_OUT_OF_RANGE);  // forces fail
			reply.setElement(1,NCE_OKAY);  // forces succeed
			break;
		case NceMessage.READ_PAGED_CV_CMD:
			reply.setElement(0, 172);			// dummy data
			//reply.setElement(1,NCE_DATA_OUT_OF_RANGE);  // forces fail
			reply.setElement(1,NCE_OKAY);  // forces succeed
			break;
		case NceMessage.READ_REG_CMD:
			reply.setElement(0, 172);			// dummy data
			//reply.setElement(1,NCE_DATA_OUT_OF_RANGE);  // forces fail
			reply.setElement(1,NCE_OKAY);  // forces succeed
			break;
		default:
			reply.setElement(0, NCE_OKAY); 		// Nce okay reply!
		}
		return reply;
	}

	private void writeReply(NceReply r) {
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
		
	private byte[] turnoutMemory = new byte[256];
	private byte[] macroMemory = new byte[256*20+16];	// and a little padding
	private byte[] consistMemory = new byte[256*6+16];	// and a little padding
	
	/* Read NCE memory.  This implementation simulates reading the NCE
	 * command station memory.  There are three memory blocks that are
	 * supported, turnout status, macros, and consists.  The turnout status
	 * memory is 256 bytes and starts at memory address 0xEC00. The macro memory
	 * is 256*20 or 5120 bytes and starts at memory address 0xC800. The consist
	 * memory is 256*6 or 1536 bytes and starts at memory address 0xF500.
	 * 
	 */
	private NceReply readMemory (NceMessage m, NceReply reply, int num){
		if (num>16){
			log.error("Nce read memory command was greater than 16");
			return null;
		}
		int nceMemoryAddress = getNceAddress(m);
		if (nceMemoryAddress >= NceTurnoutMonitor.CS_ACCY_MEMORY && nceMemoryAddress < NceTurnoutMonitor.CS_ACCY_MEMORY+256){
			log.debug("Reading turnout memory: "+Integer.toHexString(nceMemoryAddress));
			int offset = m.getElement(2);
			for (int i=0; i<num; i++)
				reply.setElement(i, turnoutMemory[offset+i]);
			return reply;
		}
		if (nceMemoryAddress >= NceCmdStationMemory.CabMemorySerial.CS_CONSIST_MEM && nceMemoryAddress < NceCmdStationMemory.CabMemorySerial.CS_CONSIST_MEM+256*6){
			log.debug("Reading consist memory: "+Integer.toHexString(nceMemoryAddress));
			int offset = nceMemoryAddress - NceCmdStationMemory.CabMemorySerial.CS_CONSIST_MEM;
			for (int i=0; i<num; i++)
				reply.setElement(i, consistMemory[offset+i]);
			return reply;
		}
		if (nceMemoryAddress >= NceCmdStationMemory.CabMemorySerial.CS_MACRO_MEM && nceMemoryAddress < NceCmdStationMemory.CabMemorySerial.CS_MACRO_MEM+256*20){
			log.debug("Reading macro memory: "+Integer.toHexString(nceMemoryAddress));
			int offset = nceMemoryAddress-NceCmdStationMemory.CabMemorySerial.CS_MACRO_MEM;
			log.debug("offset:"+offset);
			for (int i=0; i<num; i++)
				reply.setElement(i, macroMemory[offset+i]);
			return reply;
		}
		for (int i=0; i<num; i++)
			reply.setElement(i, 0x00);			// default fixed data
		return reply;
	}
	
	private NceReply writeMemory (NceMessage m, NceReply reply, int num, boolean skipbyte){
		if (num>16){
			log.error("Nce write memory command was greater than 16");
			return null;
		}
		int nceMemoryAddress = getNceAddress(m);
		int byteDataBegins = 3;
		if (skipbyte)
			byteDataBegins++;
		if (nceMemoryAddress >= NceTurnoutMonitor.CS_ACCY_MEMORY && nceMemoryAddress < NceTurnoutMonitor.CS_ACCY_MEMORY+256){
			log.debug("Writing turnout memory: "+Integer.toHexString(nceMemoryAddress));
			int offset = m.getElement(2);
			for (int i=0; i<num; i++)
				turnoutMemory[offset+i] = (byte)m.getElement(i+byteDataBegins);
		}
		if (nceMemoryAddress >= NceCmdStationMemory.CabMemorySerial.CS_CONSIST_MEM && nceMemoryAddress < NceCmdStationMemory.CabMemorySerial.CS_CONSIST_MEM+256*6){
			log.debug("Writing consist memory: "+Integer.toHexString(nceMemoryAddress));
			int offset = nceMemoryAddress-NceCmdStationMemory.CabMemorySerial.CS_CONSIST_MEM;
			for (int i=0; i<num; i++)
				consistMemory[offset+i] = (byte)m.getElement(i+byteDataBegins);
		}
		if (nceMemoryAddress >= NceCmdStationMemory.CabMemorySerial.CS_MACRO_MEM && nceMemoryAddress < NceCmdStationMemory.CabMemorySerial.CS_MACRO_MEM+256*20){
			log.debug("Writing macro memory: "+Integer.toHexString(nceMemoryAddress));
			int offset = nceMemoryAddress-NceCmdStationMemory.CabMemorySerial.CS_MACRO_MEM;
			log.debug("offset:"+offset);
			for (int i=0; i<num; i++)
				macroMemory[offset+i] = (byte)m.getElement(i+byteDataBegins);
		}
		reply.setElement(0, NCE_OKAY); 		// Nce okay reply!
		return reply;
	}
	
	private int getNceAddress(NceMessage m){
		int addr = m.getElement(1);
		addr = addr * 256;
		addr = addr + m.getElement(2);
		return addr;
	}
	
	private NceReply accessoryCommand(NceMessage m, NceReply reply){
		if (m.getElement(3) == 0x03 || m.getElement(3) == 0x04){		// 0x03 = close, 0x04 = throw
			String operation = "close";
			if (m.getElement(3) == 0x04)
				operation = "throw";
			int nceAccessoryAddress = getNceAddress(m);
			log.debug("Accessory command "+operation+" NT"+nceAccessoryAddress);
			if (nceAccessoryAddress > 2044){
				log.error("Turnout address greater than 2044, address: "+nceAccessoryAddress );
				return null;
			}
			int bit = (nceAccessoryAddress-1) & 0x07;
			int setMask = 0x01;
			for (int i=0; i<bit; i++){
				setMask = setMask<<1;
			}
			int clearMask = 0x0FFF - setMask;
			//log.debug("setMask:"+Integer.toHexString(setMask)+" clearMask:"+Integer.toHexString(clearMask));
			int offset = (nceAccessoryAddress-1)>>3;
			int read = turnoutMemory[offset];
			byte write = (byte)(read & clearMask & 0xFF);

			if (operation.equals("close"))
				write = (byte)(write + setMask);	// set bit if closed
			turnoutMemory[offset] = write;
			//log.debug("wrote:"+Integer.toHexString(write)); 
		}
		reply.setElement(0, NCE_OKAY); 		// Nce okay reply!
		return reply;
	}

	static Logger log = LoggerFactory
			.getLogger(SimulatorAdapter.class.getName());

}
