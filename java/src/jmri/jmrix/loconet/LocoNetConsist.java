/**
 *  LocoNetConsist.java
 *
 * This is the Consist definition for a consist on a LocoNet system.
 * it uses the LocoNet specific commands to build a consist.
 *
 * @author                      Paul Bender Copyright (C) 2011
 * @version                     $Revision$
 */

package jmri.jmrix.loconet;

import org.apache.log4j.Logger;
import jmri.Consist;
import jmri.ConsistListener;
import jmri.DccLocoAddress;
import jmri.ThrottleListener;

import java.util.ArrayList;

public class LocoNetConsist extends jmri.DccConsist implements SlotListener,ThrottleListener {

	private SlotManager slotManager=null;
	private LnTrafficController trafficController=null;
	private jmri.jmrix.AbstractThrottleManager throttleManager=null;
	private LocoNetSlot leadSlot=null;

	private ArrayList<DccLocoAddress> needToWrite=null;

	// State Machine states
	final static int IDLESTATE=0;
	final static int LEADREQUESTSTATE=1;
	final static int LINKSTAGEONESTATE=2;
	final static int LINKSTAGETWOSTATE=4;
	final static int LINKSTAGETHREESTATE=8;
	final static int UNLINKSTAGEONESTATE=16;
	

	private int consistRequestState = IDLESTATE;

	// Initialize a consist for the specific address
        // the Default consist type for loconet is a Command
        // Station Consist. 
	public LocoNetConsist(int address,LocoNetSystemConnectionMemo lm) {
		super(address);
		this.slotManager=lm.getSlotManager();
		this.trafficController=lm.getLnTrafficController();
		this.throttleManager=(jmri.jmrix.AbstractThrottleManager)lm.getThrottleManager();
		consistRequestState = LEADREQUESTSTATE;
	        ConsistType=Consist.CS_CONSIST;
		needToWrite=new ArrayList<DccLocoAddress>();
	        throttleManager.requestThrottle(ConsistAddress,this);
	}

	// Initialize a consist for the specific address
        // the Default consist type for loconet is a Command
        // Station Consist. 
	public LocoNetConsist(DccLocoAddress address,LocoNetSystemConnectionMemo lm) {
		super(address);
		this.slotManager=lm.getSlotManager();
		this.trafficController=lm.getLnTrafficController();
		this.throttleManager=(jmri.jmrix.AbstractThrottleManager)lm.getThrottleManager();
		consistRequestState = LEADREQUESTSTATE;
	        ConsistType=Consist.CS_CONSIST;
		needToWrite=new ArrayList<DccLocoAddress>();
	        throttleManager.requestThrottle(ConsistAddress,this);
	}

	// Clean Up local storage
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
	 * On LocoNet systems, All addresses can be used in a Universal Consist 
	 * and only 0 is not allowed in Advanced Consists.
	 */
	public boolean isAddressAllowed(DccLocoAddress address) {
		if(ConsistType==Consist.CS_CONSIST) return true;
		else if(address.getNumber()!=0) return(true);
		else return(false);
	}

	/* is there a size limit for this consist?
	 * For LocoNet returns -1 (no limit) for 
         * both CS and Advanced Consists
	 * return 0 for any other consist type.
   	 */
	public int sizeLimit(){
	   if(ConsistType==ADVANCED_CONSIST) {
		return -1;
	   } else if(ConsistType==CS_CONSIST) {
		return -1;
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
	   log.debug("consist " +ConsistAddress +" obtaining direction for " +address +" Consist List Size " +ConsistList.size());
	   if(ConsistType==ADVANCED_CONSIST || ConsistType == CS_CONSIST) {
		if(address==ConsistAddress) return true;
		if(ConsistList.contains(address)) {
		  Boolean Direction = ConsistDir.get(address);
		  return( Direction.booleanValue());
                } else return(true);
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
                if(ConsistDir.containsKey(LocoAddress)) {
                   ConsistDir.remove(LocoAddress);
                }
		ConsistDir.put(LocoAddress,Direction);
	}	

	/*
     	 * Method for removing an address from the internal consist list object.
	 */
	private synchronized void removeFromConsistList(DccLocoAddress LocoAddress){
		ConsistDir.remove(LocoAddress);
		ConsistList.remove(LocoAddress);
	}

        /*
	 * Add a Locomotive to a Consist
	 *  @param address is the Locomotive address to add to the locomotive
	 *  @param directionNormal is True if the locomotive is traveling 
         *        the same direction as the consist, or false otherwise.
         */
	@Override
	public synchronized void add(DccLocoAddress LocoAddress, boolean directionNormal) {
	      if(LocoAddress==ConsistAddress){
		// this is required for command station consists on LocoNet.
		addToConsistList(LocoAddress,directionNormal);
		notifyConsistListeners(LocoAddress,ConsistListener.OPERATION_SUCCESS);	   	
	      }	
	      else if(ConsistType==ADVANCED_CONSIST) {
		    if(ConsistList.contains(LocoAddress)){
		       // we are changing the direction, so remove first, 
                       // then add
                       removeFromAdvancedConsist(LocoAddress); 
                    }
	            addToConsistList(LocoAddress,directionNormal);
		    if(leadSlot==null || consistRequestState!=IDLESTATE)
			needToWrite.add(LocoAddress);
		    else
	               addToAdvancedConsist(LocoAddress, directionNormal);
	      } else if(ConsistType==CS_CONSIST) {
		    if(ConsistList.contains(LocoAddress)){
		       // we are changing the direction, so remove first, 
                       // then add
                       removeFromCSConsist(LocoAddress); 
                    }
		    addToConsistList(LocoAddress,directionNormal);
		    if(leadSlot==null || consistRequestState!=IDLESTATE)
			needToWrite.add(LocoAddress);
		    else
		       addToCSConsist(LocoAddress,directionNormal);
	      } else {
		log.error("Consist Type Not Supported");
		notifyConsistListeners(LocoAddress,ConsistListener.NotImplemented);
	      }
	}
	
	private synchronized void delayedAdd() {
	      DccLocoAddress LocoAddress=needToWrite.get(0);
	      if(ConsistType==ADVANCED_CONSIST) {
	               addToAdvancedConsist(LocoAddress, getLocoDirection(LocoAddress));
	      } else if(ConsistType==CS_CONSIST) {
		       addToCSConsist(LocoAddress,getLocoDirection(LocoAddress));
	      }
	      needToWrite.remove(LocoAddress);
	}

        /*
         * Restore a Locomotive to a Consist, but don't write to
         * the command station.  This is used for restoring the consist
         * from a file or adding a consist read from the command station.
         *  @param address is the Locomotive address to add to the locomotive
         *  @param directionNormal is True if the locomotive is traveling
         *        the same direction as the consist, or false otherwise.
         */
	@Override
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
	@Override
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
		consistRequestState=LINKSTAGEONESTATE;
	        throttleManager.requestThrottle(LocoAddress,this);
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
	        slotManager.slotFromLocoAddress(LocoAddress.getNumber(),this);	
		consistRequestState=UNLINKSTAGEONESTATE;
	}

        /*
	 *  Add a Locomotive to a LocoNet Universal Consist.
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
	        throttleManager.requestThrottle(LocoAddress,this);
		// skip right to stage 2, we do not need to status edit.	
                consistRequestState=LINKSTAGETWOSTATE;
	}

        /*
	 *  Remove a Locomotive from a LocoNet Universal Consist.
	 *  @param address is the Locomotive address to add to the locomotive
         */
	public synchronized void removeFromCSConsist(DccLocoAddress LocoAddress) {
 		if (log.isDebugEnabled()) log.debug("Remove Locomotive " +  
                                                    LocoAddress.toString() + 
                                                    " from Standard Consist " +
                                                    ConsistAddress.toString() + 
                                                    ".");
	        slotManager.slotFromLocoAddress(LocoAddress.getNumber(),this);	
                consistRequestState=UNLINKSTAGEONESTATE;
	}

	/*
	 * create and send a message to link two slots
	 * @param lead is the slot which is the leader
         * @param follow is the slot which will follow the leader
         */
	private void linkSlots(LocoNetSlot lead, LocoNetSlot follow){
           if(lead!=follow) {
	     LocoNetMessage msg = new LocoNetMessage(4);
	     msg.setOpCode(LnConstants.OPC_LINK_SLOTS);
	     msg.setElement(1,follow.getSlot());		
	     msg.setElement(2,lead.getSlot());		
             trafficController.sendLocoNetMessage(msg);	   	
           }
	   consistRequestState=IDLESTATE;
	   if(needToWrite.size()!=0) delayedAdd();
	}

	/*
	 * create and send a message to unlink two slots
	 * @param lead is the slot which is the leader
         * @param follow is the slot which was following the leader
         */
	private void unlinkSlots(LocoNetSlot lead, LocoNetSlot follow){
           if(lead!=follow) {
	     LocoNetMessage msg = new LocoNetMessage(4);
	     msg.setOpCode(LnConstants.OPC_UNLINK_SLOTS);
	     msg.setElement(1,follow.getSlot());	
	     msg.setElement(2,lead.getSlot());		
             trafficController.sendLocoNetMessage(msg);	   	
           }
	   consistRequestState=IDLESTATE;
	   if(needToWrite.size()!=0) delayedAdd();
	}

	private void setDirection(LocoNetThrottle t){
	   log.debug("consist " +ConsistAddress +" set direction for " + t.getLocoAddress());
	    // send a command to set the direction
	    // of the locomotive in the slot.
            Boolean directionNormal=getLocoDirection((DccLocoAddress) t.getLocoAddress());
	    if(directionNormal) t.setIsForward(leadSlot.isForward());
		else t.setIsForward(!leadSlot.isForward());

	    consistRequestState=LINKSTAGETWOSTATE;
	}


	private void setSlotModeAdvanced(LocoNetSlot s){
            // set the slot so that it can be an advanced consist
	    int oldstatus=s.slotStatus();
	    int newstatus=oldstatus|LnConstants.STAT1_SL_SPDEX;
	    trafficController.sendLocoNetMessage(s.writeStatus(newstatus));
	}

	// slot listener interface functions
	public void notifyChangedSlot(LocoNetSlot s) {
		log.debug("Notified slot " +s.getSlot() +
                          " changed with mode " +consistRequestState + 
                          " slot consist state: " + 
                          LnConstants.CONSIST_STAT(s.consistStatus()) );
		switch(consistRequestState){
		   case LEADREQUESTSTATE:
			leadSlot=s;
			consistRequestState=IDLESTATE;
			break;
		   case LINKSTAGEONESTATE:	
			s.addSlotListener(this);
			setSlotModeAdvanced(s);
			consistRequestState=LINKSTAGETWOSTATE;
			break;
	           case LINKSTAGETWOSTATE:
			linkSlots(leadSlot,s);
			break;
		   case UNLINKSTAGEONESTATE:
			unlinkSlots(leadSlot,s);
			break;
		   default:
			s.removeSlotListener(this);
		        notifyConsistListeners(new DccLocoAddress(s.locoAddr(),
				 throttleManager.canBeLongAddress(s.locoAddr())),
				ConsistListener.OPERATION_SUCCESS);	   	
			if(needToWrite.size()!=0) delayedAdd();
	                else 
                          consistRequestState=IDLESTATE;
		}
	}

	// Throttle listener interface functions
	public void notifyThrottleFound(jmri.DccThrottle t)
	{
		log.debug("notified Throttle " +t.getLocoAddress() +" found with mode "+consistRequestState);
	     try {
	        if(consistRequestState==LEADREQUESTSTATE) {
		  ((LocoNetThrottle)t).setIsForward(true);
		  leadSlot=((LocoNetThrottle) t).getLocoNetSlot();
	          consistRequestState=IDLESTATE;
		  if(needToWrite.size()!=0) delayedAdd();
	        } else {
		  LocoNetSlot tempSlot=((LocoNetThrottle) t).getLocoNetSlot();
		  tempSlot.addSlotListener(this);
                  if(consistRequestState==LINKSTAGEONESTATE){
                     notifyChangedSlot(tempSlot);
		     setDirection(((LocoNetThrottle) t));
                     consistRequestState=LINKSTAGETWOSTATE;
                  }
		  else setDirection(((LocoNetThrottle) t));
	       }
	     } catch (java.lang.ClassCastException cce) {
	       // if the simulator is in use, we will
               // get a ClassCastException. 
	       if(consistRequestState==LEADREQUESTSTATE) {
		 t.setIsForward(true);
	         consistRequestState=IDLESTATE;
		 if(needToWrite.size()!=0) delayedAdd();
	       } else {
		 setDirection(((LocoNetThrottle) t));
	       }
                
             }
	}

	public void notifyFailedThrottleRequest(DccLocoAddress address, String reason){
	    notifyConsistListeners(address,
		ConsistListener.CONSIST_ERROR);	   	
	    removeFromConsistList(address);
	    consistRequestState=IDLESTATE;
	}

	static Logger log = Logger.getLogger(LocoNetConsist.class.getName());

}
