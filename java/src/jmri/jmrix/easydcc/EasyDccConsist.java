/**
 *  EasyDccConsist.java
 *
 * This is the Consist definition for a consist on an EasyDCC system.
 * it uses the EasyDcc specific commands to build a consist.
 *
 * @author                      Paul Bender Copyright (C) 2006
 * @version                     $Revision$
 */

package jmri.jmrix.easydcc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.Consist;
import jmri.ConsistListener;
import jmri.DccLocoAddress;

public class EasyDccConsist extends jmri.DccConsist implements EasyDccListener {

	// Initialize a consist for the specific address
        // the Default consist type is an advanced consist 
	public EasyDccConsist(int address) {
		super(address);
	}

	// Initialize a consist for the specific address
        // the Default consist type is an advanced consist 
	public EasyDccConsist(DccLocoAddress address) {
		super(address);
	}

	// Clean Up local storag
	public void dispose() {
		super.dispose();
	}

	// Set the Consist Type
	public void setConsistType(int consist_type){ 
	      if(consist_type==Consist.ADVANCED_CONSIST) {
		ConsistType = consist_type;
		return;
	      } else if(consist_type==Consist.CS_CONSIST) {
		ConsistType = consist_type;
	      } else {
		log.error("Consist Type Not Supported");
		notifyConsistListeners(new DccLocoAddress(0,false),ConsistListener.NotImplemented);
	      }
	}

	/* is this address allowed?
	 * On EasyDCC systems, All addresses but 0 can be used in a consist 
	 * (Either an Advanced Consist or a Standard Consist).
	 */
	public boolean isAddressAllowed(DccLocoAddress address) {
		if(address.getNumber()!=0) return(true);
		else return(false);
	}

	/* is there a size limit for this consist?
	 * For EasyDcc Standard Consist, returns 8
	 * For Decoder Assisted Consists, returns -1 (no limit)
	 * return 0 for any other consist type.
   	 */
	public int sizeLimit(){
	   if(ConsistType==ADVANCED_CONSIST) {
		return -1;
	   } else if(ConsistType==CS_CONSIST) {
		return 8;
	   } else return 0;
	}

	// does the consist contain the specified address?
	public boolean contains(DccLocoAddress address) {
	   if(ConsistType==ADVANCED_CONSIST || ConsistType == CS_CONSIST) {
		return ConsistList.contains(address);
	   } else {
		log.error("Consist Type Not Supported");
		notifyConsistListeners(address,ConsistListener.NotImplemented);
	      }
	   return false;
	}

	// get the relative direction setting for a specific
	// locomotive in the consist
	public boolean getLocoDirection(DccLocoAddress address) {
	   if(ConsistType==ADVANCED_CONSIST || ConsistType == CS_CONSIST) {
		Boolean Direction = ConsistDir.get(address);
		return( Direction.booleanValue());
	   } else {
		log.error("Consist Type Not Supported");
		notifyConsistListeners(address,ConsistListener.NotImplemented);
	      }
	   return false;
	}

	/*
     	 * Method for adding an Address to the internal consist list object.
	 */
	private synchronized void addToConsistList(DccLocoAddress LocoAddress, boolean directionNormal) {
	        Boolean Direction = Boolean.valueOf(directionNormal);
		if(!(ConsistList.contains(LocoAddress))) 
					ConsistList.add(LocoAddress);
		ConsistDir.put(LocoAddress,Direction);
		if(ConsistType==CS_CONSIST && ConsistList.size()==8) {
		    notifyConsistListeners(LocoAddress,
				ConsistListener.OPERATION_SUCCESS | 
				ConsistListener.CONSIST_FULL);
	 	} else {
		    notifyConsistListeners(LocoAddress,
				ConsistListener.OPERATION_SUCCESS);
		}
	}	

	/*
     	 * Method for removing an address from the internal consist list object.
	 */
	private synchronized void removeFromConsistList(DccLocoAddress LocoAddress){
		ConsistDir.remove(LocoAddress);
		ConsistList.remove(LocoAddress);
		notifyConsistListeners(LocoAddress,ConsistListener.OPERATION_SUCCESS);	   	
	}

        /*
	 * Add a Locomotive to a Consist
	 *  @param address is the Locomotive address to add to the locomotive
	 *  @param directionNormal is True if the locomotive is traveling 
         *        the same direction as the consist, or false otherwise.
         */
	public synchronized void add(DccLocoAddress LocoAddress, boolean directionNormal) {
	      if(ConsistType==ADVANCED_CONSIST) {
		 addToConsistList(LocoAddress,directionNormal);
	         addToAdvancedConsist(LocoAddress, directionNormal);
	      } else if(ConsistType==CS_CONSIST) {
		 if(ConsistList.size()<8){
		    addToConsistList(LocoAddress,directionNormal);
		    addToCSConsist(LocoAddress,directionNormal);
                 } else {
                    notifyConsistListeners(LocoAddress,
                            ConsistListener.CONSIST_ERROR |
                            ConsistListener.CONSIST_FULL);
                 }
	      } else {
		log.error("Consist Type Not Supported");
		notifyConsistListeners(LocoAddress,ConsistListener.NotImplemented);
	      }
	}

        /*
         * Restore a Locomotive to an Advanced Consist, but don't write to
         * the command station.  This is used for restoring the consist
         * from a file or adding a consist read from the command station.
         *  @param address is the Locomotive address to add to the locomotive
         *  @param directionNormal is True if the locomotive is traveling
         *        the same direction as the consist, or false otherwise.
         */
 	public synchronized void restore(DccLocoAddress LocoAddress, boolean directionNormal) {
	      if(ConsistType==ADVANCED_CONSIST) {
		 addToConsistList(LocoAddress,directionNormal);
	      } else if(ConsistType==CS_CONSIST) {
		 addToConsistList(LocoAddress,directionNormal);
	      } else {
		log.error("Consist Type Not Supported");
		notifyConsistListeners(LocoAddress,ConsistListener.NotImplemented);
	      }
	}

        /*
	 *  Remove a Locomotive from this Consist
	 *  @param address is the Locomotive address to add to the locomotive
         */
	public synchronized void remove(DccLocoAddress LocoAddress) {
	      if(ConsistType==ADVANCED_CONSIST) {
	         removeFromAdvancedConsist(LocoAddress);		
		 removeFromConsistList(LocoAddress);
	      }
	      else if(ConsistType==CS_CONSIST) {
	         removeFromCSConsist(LocoAddress);
		 removeFromConsistList(LocoAddress);
	      }
	      else {
		 log.error("Consist Type Not Supported");
		 notifyConsistListeners(LocoAddress,ConsistListener.NotImplemented);
	      }
	}

        /*
	 *  Add a Locomotive to an Advanced Consist
	 *  @param address is the Locomotive address to add to the locomotive
	 *  @param directionNormal is True if the locomotive is traveling 
         *        the same direction as the consist, or false otherwise.
         */
	@Override
	protected synchronized void addToAdvancedConsist(DccLocoAddress LocoAddress, boolean directionNormal) {
 		if (log.isDebugEnabled()) log.debug("Add Locomotive " +  
                                                    LocoAddress.toString() + 
                                                    " to advanced consist " +
                                                    ConsistAddress.toString() + 
                                                    " With Direction Normal " +
                                                     directionNormal + ".");
                 // create the message and fill it,
                 byte[] contents = jmri.NmraPacket.consistControl(LocoAddress.getNumber(), 
                                                LocoAddress.isLongAddress(), 
                                                ConsistAddress.getNumber(),
                                                directionNormal);
                 EasyDccMessage msg = new EasyDccMessage(4+3*contents.length);
                 msg.setOpCode('S');
                 msg.setElement(1, ' ');
                 msg.setElement(2, '0');
                 msg.setElement(3, '5');
                 int j = 4;
                 for (int i=0; i<contents.length; i++) { 
                     msg.setElement(j++, ' ');
                     msg.addIntAsTwoHex(contents[i]&0xFF, j);
                     j = j+2;
                 }
        
        	// send it
        	EasyDccTrafficController.instance().sendEasyDccMessage(msg, this);
	}

        /*
	 *  Remove a Locomotive from an Advanced Consist
	 *  @param address is the Locomotive address to add to the locomotive
         */
	@Override
	protected synchronized void removeFromAdvancedConsist(DccLocoAddress LocoAddress) {
 		if (log.isDebugEnabled()) log.debug(" Remove Locomotive " +  
                                                    LocoAddress.toString() + 
                                                    " from advanced consist " +
                                                    ConsistAddress.toString());
                 // create the message and fill it,
                 byte[] contents = jmri.NmraPacket.consistControl(LocoAddress.getNumber(), 
                                                LocoAddress.isLongAddress(), 
                                                0,true);
                 EasyDccMessage msg = new EasyDccMessage(4+3*contents.length);
                 msg.setOpCode('S');
                 msg.setElement(1, ' ');
                 msg.setElement(2, '0');
                 msg.setElement(3, '5');
                 int j = 4;
                 for (int i=0; i<contents.length; i++) { 
                     msg.setElement(j++, ' ');
                     msg.addIntAsTwoHex(contents[i]&0xFF, j);
                     j = j+2;
                 }
        
        	// send it
        	EasyDccTrafficController.instance().sendEasyDccMessage(msg, this);
	}

        /*
	 *  Add a Locomotive to an EasyDCC Standard Consist.
	 *  @param address is the Locomotive address to add to the locomotive
	 *  @param directionNormal is True if the locomotive is traveling 
         *        the same direction as the consist, or false otherwise.
         */
	private synchronized void addToCSConsist(DccLocoAddress LocoAddress, boolean directionNormal) {
 		if (log.isDebugEnabled()) log.debug("Add Locomotive " +  
                                                    LocoAddress.toString() + 
                                                    " to Standard Consist " +
                                                    ConsistAddress.toString() + 
                                                    " With Direction Normal " +
                                                     directionNormal + ".");
		EasyDccMessage m;
		if(directionNormal){
			m=EasyDccMessage.getAddConsistNormal(ConsistAddress.getNumber(),LocoAddress);
		} else {
			m=EasyDccMessage.getAddConsistReverse(ConsistAddress.getNumber(),LocoAddress);
		}
		EasyDccTrafficController.instance().sendEasyDccMessage(m,this);
	}

        /*
	 *  Remove a Locomotive from an EasyDCC Standard Consist.
	 *  @param address is the Locomotive address to add to the locomotive
         */
	public synchronized void removeFromCSConsist(DccLocoAddress LocoAddress) {
 		if (log.isDebugEnabled()) log.debug("Remove Locomotive " +  
                                                    LocoAddress.toString() + 
                                                    " from Standard Consist " +
                                                    ConsistAddress.toString() + 
                                                    ".");
		EasyDccMessage m=EasyDccMessage.getSubtractConsist(ConsistAddress.getNumber(),LocoAddress);
		EasyDccTrafficController.instance().sendEasyDccMessage(m,this);		
	}

	// Listeners for messages from the command station
	public void message(EasyDccMessage m){
	  log.error("message received unexpectedly: " +m.toString());
	}

	public void reply(EasyDccReply r){
	  // There isn't anything meaningful coming back at this time.
	  if(log.isDebugEnabled()) log.debug("reply received unexpectedly: " +r.toString());
	}

	static Logger log = LoggerFactory.getLogger(EasyDccConsist.class.getName());

}
