/**
 *  DccConsist.java
 *
 * This is the Default DCC consist.
 * It utilizes the fact that IF a Command Station supports OpsMode 
 * Programing, you can write the consist information to CV19, so ANY 
 * Command Station that supports Ops Mode Programing can write this 
 * address to a Command Station that supports it.
 *
 * @author                      Paul Bender Copyright (C) 2003
 * @version                     $ Revision 1.00 $
 */

package jmri;

import jmri.ConsistListener;

import java.util.Enumeration;
import java.util.Vector;

import com.sun.java.util.collections.Hashtable;
import com.sun.java.util.collections.ArrayList;


public class DccConsist implements Consist, ProgListener{

	protected ArrayList ConsistList = null; // A List of Addresses in the consist

	protected Hashtable ConsistDir = null; // A Hash table 
                                        // containing the directions of 
					// each locomotive in the consist, 
					// keyed by Loco Address.

        protected int ConsistType = ADVANCED_CONSIST;

	protected int ConsistAddress = -1;

	// Initialize a consist for the specific address
        // the Default consist type is an advanced consist 
	public DccConsist(int address) {
		ConsistAddress = address;
		ConsistDir = new Hashtable();
		ConsistList = new ArrayList();
	}

	// Clean Up local Storage.
	public void dispose() {
	}

	// Set the Consist Type
	public void setConsistType(int consist_type){ 
	      if(consist_type==ADVANCED_CONSIST) {
		ConsistType = consist_type;
		return;
	      }
	      else {
		log.error("Consist Type Not Supported");
		notifyConsistListeners(0,ConsistListener.NotImplemented);
	      }
	}

	// get the Consist Type
	public int getConsistType(){ 
		return ConsistType;
	}

	// get the Consist Address
	public int getConsistAddress(){ 
		return ConsistAddress;
	}

	/* is this address allowed?
         * Since address 00 is an analog locomotive, we can't program CV19 
	 * to include it in a consist, but all other addresses are ok. 
         */
        public boolean isAddressAllowed(int address) {
                if(address!=0) return(true);
                else return(false);
        }

 	/* is there a size limit for this consist?
         * For Decoder Assisted Consists, returns -1 (no limit)  
         * return 0 for any other consist type.
         */
        public int sizeLimit(){
           if(ConsistType==ADVANCED_CONSIST) {
                return -1;
           } else return 0;
        }      


	// get a list of the locomotives in the consist
        public ArrayList getConsistList() { return ConsistList; }

	// does the consist contain the specified address?
	public boolean contains(int address) {
	   if(ConsistType==ADVANCED_CONSIST) {
		String Address= Integer.toString(address);
		return( (boolean) ConsistDir.contains(Address));
	   } else {
		log.error("Consist Type Not Supported");
		notifyConsistListeners(0,ConsistListener.NotImplemented);
	      }
	   return false;
	}

	// get the relative direction setting for a specific
	// locomotive in the consist
	public boolean getLocoDirection(int address) {
	   if(ConsistType==ADVANCED_CONSIST) {
		String Address= Integer.toString(address);
		Boolean Direction=(Boolean) ConsistDir.get(Address);
		return( Direction.booleanValue());
	   } else {
		log.error("Consist Type Not Supported");
		notifyConsistListeners(address,ConsistListener.NotImplemented);
	      }
	   return false;
	}

        /*
	 * Add a Locomotive to an Advanced Consist
	 *  @parm address is the Locomotive address to add to the locomotive
	 *  @parm directionNormal is True if the locomotive is traveling 
         *        the same direction as the consist, or false otherwise.
         */
	public void add(int LocoAddress,boolean directionNormal) {
	      if(ConsistType==ADVANCED_CONSIST) {
		 String Address= Integer.toString(LocoAddress);
	         Boolean Direction = new Boolean(directionNormal);
		 if(!(ConsistList.contains(Address))) ConsistList.add(Address);
		 ConsistDir.put(Address,Direction);
	         addToAdvancedConsist(LocoAddress, directionNormal);		
	      }
	      else {
		log.error("Consist Type Not Supported");
		notifyConsistListeners(LocoAddress,ConsistListener.NotImplemented);
	      }
	}

        /*
	 *  Remove a Locomotive from this Consist
	 *  @parm address is the Locomotive address to add to the locomotive
         */
	public void remove(int LocoAddress) {
	      if(ConsistType==ADVANCED_CONSIST) {
		 String Address= Integer.toString(LocoAddress);
		 ConsistDir.remove(Address);
		 ConsistList.remove(Address);
	         removeFromAdvancedConsist(LocoAddress);		
	      }
	      else {
		 log.error("Consist Type Not Supported");
		 notifyConsistListeners(LocoAddress,ConsistListener.NotImplemented);
	      }
	}


        /*
	 *  Add a Locomotive to an Advanced Consist
	 *  @parm address is the Locomotive address to add to the locomotive
	 *  @parm directionNormal is True if the locomotive is traveling 
         *        the same direction as the consist, or false otherwise.
         */
	private void addToAdvancedConsist(int LocoAddress, boolean directionNormal) {
		// Assume 2 digit address is a short address, and
		// 4 digits is a long address
		boolean isLongAddress=Integer.toString(LocoAddress).length()>2;

		Programmer opsProg = InstanceManager.programmerManagerInstance()
				    .getOpsModeProgrammer(isLongAddress,
							LocoAddress);
		if(directionNormal) {
			try {
				opsProg.writeCV(19,ConsistAddress,this);
			} catch(ProgrammerException e) {
			// Don't do anything with this yet
			}
		} else {
			try {
				opsProg.writeCV(19,ConsistAddress + 128 ,this);
			} catch(ProgrammerException e) {
			// Don't do anything with this yet
			}
		}

		InstanceManager.programmerManagerInstance()
                               .releaseOpsModeProgrammer(opsProg);
	}

        /*
	 *  Remove a Locomotive from an Advanced Consist
	 *  @parm address is the Locomotive address to add to the locomotive
         */
	public void removeFromAdvancedConsist(int LocoAddress) {
		// Assume 2 digit address is a short address, and
		// 4 digits is a long address
		boolean isLongAddress=Integer.toString(LocoAddress).length()>2;

		Programmer opsProg = InstanceManager.programmerManagerInstance()
				    .getOpsModeProgrammer(isLongAddress,
							LocoAddress);
		try {
			opsProg.writeCV(19,0,this);
		} catch(ProgrammerException e) {
			// Don't do anything with this yet
		}
		InstanceManager.programmerManagerInstance()
                               .releaseOpsModeProgrammer(opsProg);
	}

	// data member to hold the throttle listener objects
	final private Vector listeners = new Vector();
	
	/*
         * Add a Listener for consist events
         * @parm Listener is a consistListener object
         */
        public void addConsistListener(ConsistListener Listener){
		if(!listeners.contains(Listener))
			listeners.addElement(Listener);
	}
          
        /*
         * Remove a Listener for consist events
         * @parm Listener is a consistListener object
         */
        public void removeConsistListener(ConsistListener Listener){
		if(listeners.contains(Listener))
			listeners.removeElement(Listener);
	}

	/*
         * Notify all listener objects of a status change.
         * @parm LocoAddress is the address of any specific locomotive the
         *       status refers to.
         * @parm ErrorCode is the status code to send to the 
         *       consistListener objects
         */
        protected void notifyConsistListeners(int  LocoAddress, int ErrorCode){
 		// make a copy of the listener vector to  notify.
        	Vector v;
        	synchronized(this)
            	{
                 	v = (Vector) listeners.clone();
            	}
        	if (log.isDebugEnabled()) log.debug("Sending Status code: " +
						ErrorCode + " to "  + 
						v.size() + 
                                            	" listeners for Address "  
                                            	+ LocoAddress);
        	// forward to all listeners
        	int cnt = v.size();
        	for (int i=0; i < cnt; i++) {
            		ConsistListener client = (ConsistListener) v.elementAt(i);
            		client.consistReply(LocoAddress,ErrorCode);
        	}
	}

	// This class is to be registerd as a programmer listener, so we 
        // include the programmingOpReply() function
	public void programmingOpReply(int value, int status) {
		if(log.isDebugEnabled()) log.debug("Programming Operation reply recieved, value is " + value + " ,status is " +status);
		notifyConsistListeners(0,ConsistListener.OPERATION_SUCCESS);
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DccConsist.class.getName());

}
