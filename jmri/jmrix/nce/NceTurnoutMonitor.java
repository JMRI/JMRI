//NceTournoutMonitor.java

package jmri.jmrix.nce;

import com.sun.java.util.collections.List;

import jmri.InstanceManager;
import jmri.Turnout;



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
 * to the JMRI turnout (accessory) state and if a discrepancy is discovered,
 * the JMRI turnout state is modified to match the CS.       
 * 
 *  
 * @author Daniel Boudreau (C) 2007
 * @version     $Revision: 1.5 $
 */

public class NceTurnoutMonitor extends Thread implements NceListener{

    // scope constants
    private static final int CS_ACCY_MEMORY = 0xEC00; 	// Address of start of CS accessory memory 
    private static final int NUM_BLOCK = 16;            // maximum number of memory blocks
    private static final int BLOCK_LEN = 16;            // number of bytes in a block
    private static final int REPLY_LEN = BLOCK_LEN;		// number of bytes read

    // object state
    private int currentBlock;							// used as state in scan over active blocks
    private int numTurnouts;							//number of turnouts loaded by JMRI
    private int savedNumTurnouts = -1;					//current number of turnouts being polled, -1 forces init
    private int numActiveBlocks = 0;
    
    // cached work fields
    boolean [] newTurnouts = new boolean [NUM_BLOCK];	//used to sync poll turnout memory
    boolean [] activeBlock = new boolean [NUM_BLOCK];	//When true there are active turnouts in the memory block
    byte [] csAccMemCopy = new byte [256];				//Copy of NCE CS accessory memory
    
    // debug final
    static final boolean debugTurnoutMonitor = false;	//Control verbose debug
        
    public NceMessage pollMessage() {
    	
    	if (NceMessage.getCommandOptions() < NceMessage.OPTION_2006 ){return null;}

        // First, rescan the defined turnouts.  It's tedious to do this on every request
        // for poll, so eventually this should be handled by a listener to the TurnoutManager
        // that updates only when the list of defined Turnouts changes, but for now this 
        // works.
                      
        List turnoutSysNameList = InstanceManager.turnoutManagerInstance().getSystemNameList();
        numTurnouts = turnoutSysNameList.size();
        
        // See if the number of turnouts now differs from the last scan.
        // This is not entirely reliable in the face of turnouts being deleted, as one can come and
        // another go leaving the total unchanged, but deleting is a recent addition known to be
        // unreliable at runtime.
        if (numTurnouts != savedNumTurnouts) {
 
            // Skip doing this again until number changed
            savedNumTurnouts = numTurnouts;
            if (numTurnouts==0) return null;  // no work!
            
            // Determine what turnouts have been defined and what blocks have active turnouts
            for (int block = 0; block < NUM_BLOCK; block++){
            	
            	newTurnouts[block] = true;			// Block may be active, but new turnouts may have been loaded	
            	if (activeBlock[block] == false) {  // no need to scan once known to be active

                    for (int i = 0; i < 128; i++) { // Check 128 turnouts per block 
                        int addr = 1 + i + (block*128);
                        Turnout mControlTurnout = InstanceManager.turnoutManagerInstance().getBySystemName("NT"+addr);
                        if (mControlTurnout != null){
                            activeBlock[block] = true;	//turnout found, block is active forever
                            numActiveBlocks++;
                            break; 						// don't check rest of block					
                        }
                    }
                }
            
            }
        }     
               
        // Note that not all Turnouts are NCE Turnouts; it's possible
        // to get here with no work if others are in use, e.g. IT1'
        if (numActiveBlocks<=0) {
            return null; // to avoid immediate infinite loop
        }
        
        // now try to build a poll message if there are any defined turnouts to scan
        while (true) { // will break out when next block to poll is found
            // move to next possible block
            currentBlock++;
            if (currentBlock >= NUM_BLOCK) currentBlock = 0;
            
            if (activeBlock[currentBlock]){
                if (debugTurnoutMonitor && log.isDebugEnabled()) log.debug("found turnouts block "+ currentBlock );

                // Read NCE CS memory                    
                int nceAccAddress = CS_ACCY_MEMORY+currentBlock*BLOCK_LEN;
                byte [] bl = NceBinaryCommand.accMemoryRead(nceAccAddress);
                NceMessage m = NceMessage.createBinaryMessage(bl, REPLY_LEN);
                return m;
            }
        }
    }
    
    public void message(NceMessage m){
        if (log.isDebugEnabled()) {
            log.debug("unexpected message" );
        }	
    }
    
    public void reply(NceReply r) {
        if (r.getNumDataElements()== REPLY_LEN) {
            
            if (log.isDebugEnabled() & debugTurnoutMonitor == true ) {
                log.debug("memory poll reply received for memory block " + currentBlock + ": " + r.toString());
            }
            
            // Compare NCE CS memory to local copy, change state if necessary
            // 128 turnouts checked per NCE CS memory read
            for (int j = 0; j < REPLY_LEN; j++) { 					// byte index
                byte recMemByte  = (byte)r.getElement(j);			// CS memory byte
     
                if (recMemByte != csAccMemCopy [j + currentBlock*BLOCK_LEN] || newTurnouts[currentBlock] == true){
                    
                    csAccMemCopy [j + currentBlock*BLOCK_LEN] = recMemByte;	// load copy into local memory
                    for (int i = 0; i < 8; i++){ 					// search this byte for active turnouts
                        
                        int addr = 1 + i + j*8 + (currentBlock*128);
                        NceTurnout rControlTurnout = (NceTurnout) InstanceManager.turnoutManagerInstance().getBySystemName("NT"+addr);
                        if (rControlTurnout != null){
                            
                            int tState = rControlTurnout.getKnownState();
                            int tFeedBack = rControlTurnout.getFeedbackMode();
                            if (debugTurnoutMonitor && log.isDebugEnabled()) {
                                log.debug("turnout exists NT"+addr+" state: " +tState + " Feed back mode: " + tFeedBack);
                            }
                            
                            // Keep JMRI panel in sync with NCE CS only if feedback mode is DIRECT
                            if (tFeedBack==Turnout.DIRECT) {
                                
                                //	Show the byte read from NCE CS
                                
                                if (debugTurnoutMonitor && log.isDebugEnabled()) {
                                    log.debug("memory byte: " + Integer.toHexString(recMemByte & 0xFF));
                                }	
                                
                                // test for closed or thrown
                                int accThrown = (recMemByte >> i) & 0x01;
                                if (accThrown > 0 & tState != Turnout.THROWN){
                                    if (log.isDebugEnabled()) {
                                        log.debug("turnout discrepency, need to THROW turnout NT" + addr);
                                    }
                                    // change JMRI's knowledge of the turnout state to match observed
                                    rControlTurnout.setKnownStateFromCS(Turnout.THROWN);
                                }
                                
                                if (accThrown == 0 & tState != Turnout.CLOSED) {
                                    if (log.isDebugEnabled()) {
                                        log.debug("turnout discrepency, need to CLOSE turnout NT" + addr);
                                    }	
                                   // change JMRI's knowledge of the turnout state to match observed
                                    rControlTurnout.setKnownStateFromCS(Turnout.CLOSED);
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
            newTurnouts[currentBlock] = false;
        }
        else log.warn("wrong number of read bytes for memory poll" );
    }
        
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceTurnoutMonitor.class.getName());	
    
}
/* @(#)NceTurnoutMonitor.java */


