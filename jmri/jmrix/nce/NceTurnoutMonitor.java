//NceTournoutMonitor.java

package jmri.jmrix.nce;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Turnout;
import jmri.jmrix.AbstractMRReply;



/**
 * 
 * Polls NCE Command Station for turnout discrepancies
 * 
 * This implementation reads the NCE Command Station (CS) memory
 * that stores the state of all accessories thrown by cabs or though
 * the comm port using the new binary switch command.  The accessory
 * states are storied in 256 byte array starting at address 0xEC00.
 * 
 * byte 0,   bit 0 = ACCY 1,    bit 1 = ACCY 2
 * byte 1,   bit 0 = ACCY 9,    bit 1 = ACCY 10
 * 
 * byte 255, bit 0 = ACCY 2041, bit 3 = ACCY 2044 (last valid addr)
 * 
 * Block reads (16 bytes) of the NCE CS memory are performed to 
 * minimize impact to the NCE CS.  Data from the CS is then compared 
 * to the JMRI switch state and if a discrepancy is discovered, the 
 * JMRI switch state is modified to match the CS.       
 * 
 *  
 * @author Daniel Boudreau (C) 2007
 * @version     $Revision: 1.1 $
 */

public class NceTurnoutMonitor extends Thread implements NceListener{
	
    public NceTurnoutMonitor() {
        // connect to the TrafficManager
        tc = NceTrafficController.instance();
        tc.addNceListener(this);
    }
	
	static final int POLL_RATE  = 1000; 				//(msec)maximum time to read all 256 bytes of CS memory
	static final int BLOCK_POLL_RATE = POLL_RATE/16; 	//Wait time between block reads
	static final int CS_ACCY_MEMORY = 0xEC00; 			//Start of CS accessory memory 
	static boolean accPollEnabled = false;
	public static boolean accPollOff;					//allow memory polling to be disabled
	private final int pollTimeout = 5000;				//in case of lost response
	private boolean awaitingReply = false;				//used to determine if reply lost 
	private boolean waiting = false;					//used to signal read message sent
	private int memoryBlock;							//there are 16 x 256 byte memory blocks
	private int address = CS_ACCY_MEMORY;				//NCE accessory memory address  
	private final int REPLY_LEN = 16;					//number of bytes read
	protected Turnout mControlTurnout = null;
	static boolean [] activeBlock = new boolean [16];	//When true there are active turnouts in the memory block
	static boolean debugTurnoutMonitor = true;			//Control verbose debug
	
	public void run (){
		
		accPollEnabled = true;
	
		while (!accPollOff){
			
			address = CS_ACCY_MEMORY;
			
			// Determine if any turnouts have been loaded
						
			for (memoryBlock = 0; memoryBlock < 16; memoryBlock++){
				
				if (activeBlock[memoryBlock] == false){
					
					for (int j = 0; j < REPLY_LEN; j++) { 	// byte index
						
						 for (int i = 0; i < 8; i++){ 		// bit index
						
							int addr = 1 + i + j*8 + (memoryBlock*128);
						
							mControlTurnout = InstanceManager.turnoutManagerInstance().getBySystemName("NT"+addr);
		                         
							if (mControlTurnout != null){
								
								activeBlock[memoryBlock] = true;
								i = 8;								//Break out of loop
								j = REPLY_LEN;						//Break out of loop
								
							}
							
						 }
						 
					}
					
				}
			
				if (activeBlock[memoryBlock] == true){
									
					if (log.isDebugEnabled() & debugTurnoutMonitor == true) log.debug("found turnouts block "+ memoryBlock );
			
					// Read NCE CS memory
								
					byte [] bl = NceBinaryCommand.accMemoryRead(address);
					NceMessage m = NceMessage.createBinaryMessage(bl, REPLY_LEN);
				
					// This code always waits for the pollTimeout to expire, synchronized needed to prevent exceptions?
				
					synchronized (this) {
						if (log.isDebugEnabled()) {
							log.debug("NCE accessory memory read " + Integer.toHexString(address));
						}
						waiting = false;
						awaitingReply = true;
						
// Need help here to get recieve message, currently using listener.  			
					
						
//						NceTrafficController.instance().sendNceMessage(m, reply);
					
						NceTrafficController.instance().sendNceMessage(m, null);
					
						try {
							wait(pollTimeout);
						} 
						catch (InterruptedException e) {if (log.isDebugEnabled())
															{log.debug("exception in wait e="  +e.toString());
															}
						}
					}
					if (awaitingReply) {
						log.warn("timeout waiting for NCE accessory memory read");
					
					}
				
					//	Wait to minimize load on NCE system 
				
				}
				try	{
					Thread.sleep (BLOCK_POLL_RATE);
				} 
				catch (InterruptedException e)	{if (log.isDebugEnabled())
														{log.debug("exception in wait e="  +e.toString());
														}
				}
				
				address = address + REPLY_LEN;
			}
		}
	}
	
	NceTrafficController tc = null;
	
	public void message(NceMessage m){
		
		if (awaitingReply == true){
		
			if (m.getElement(0) == NceBinaryCommand.READ_CMD){
				waiting = true;
	
				if (log.isDebugEnabled()& debugTurnoutMonitor == true) {
					log.debug("expect memory poll reply" );
					
				}	
			}
		}	
	}
		
	public void reply(NceReply r) {

		if (waiting){

			awaitingReply = false;
			waiting = false;

			if (r.getNumDataElements()== REPLY_LEN) {

				if (log.isDebugEnabled() & debugTurnoutMonitor == true ) {
					log.debug("memory poll reply received for memory block " + memoryBlock + ": " + r.toString());
				}

				// Compare NCE CS memory to turnout state, change state if necessary
				// 128 turnouts checked per NCE CS memory read

				activeBlock[memoryBlock] = false;			//if turnouts purged, stop reading memory block

				byte recMemByte;							// CS memory byte

				for (int j = 0; j < REPLY_LEN; j++) { 		// byte index

					for (int i = 0; i < 8; i++){ 			// bit index

						int addr = 1 + i + j*8 + (memoryBlock*128);

						mControlTurnout = InstanceManager.turnoutManagerInstance().getBySystemName("NT"+addr);

						if (mControlTurnout != null){

							activeBlock[memoryBlock] = true;	// continue reading this block
							int tState = mControlTurnout.getKnownState();
							int tFeedBack = mControlTurnout.getFeedbackMode();

							if (log.isDebugEnabled()& debugTurnoutMonitor == true) {
								log.debug("turnout exists NT"+addr+" state: " +tState + " Feed back mode: " + tFeedBack);
							}

							// Keep JMRI panel in sync with NCE CS only if feedback mode is DIRECT

							if (tFeedBack==Turnout.DIRECT) { 

								recMemByte  = (byte) r.getElement(j);
								
								//	Show the byte read from NCE CS

								if (log.isDebugEnabled() & debugTurnoutMonitor == true) {
									log.debug("memory byte: " + Integer.toHexString(recMemByte & 0xFF));
								}	

								// test for closed or thrown

								int accThrown = (recMemByte >> i) & 0x01;

								if (accThrown > 0 & tState != Turnout.THROWN){

									if (log.isDebugEnabled()) {
										log.debug("turnout discrepency, need to THROW turnout NT" + addr);

										// throw turnout, unfortunately the throw command also goes to the layout

										mControlTurnout.setCommandedState(Turnout.THROWN);

									}
								}

								if (accThrown == 0 & tState != Turnout.CLOSED){

									if (log.isDebugEnabled()) {
										log.debug("turnout discrepency, need to CLOSE turnout NT" + addr);

										// close turnout, unfortunately the close command also goes to the layout

										mControlTurnout.setCommandedState(Turnout.CLOSED);

									}	
								}									

							}

						}

						else {
							//
							//if (log.isDebugEnabled()) log.debug("turnout does not exist NT"+addr);
							//
						}
					}
				}
			}

			else log.warn("wrong number of read bytes for memory poll" );

		}
	}


	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceTurnoutMonitor.class.getName());	
	
}
/* @(#)NceTurnoutMonitor.java */

