/**
 *  XNetConsist.java
 *
 * This is the Consist definition for a consist on an XPresNet system.
 * it uses the XPressNet specific commands to build a consist.
 *
 * @author                      Paul Bender Copyright (C) 2004
 * @version                     $Revision: 2.3 $
 */

package jmri.jmrix.lenz;

import java.util.Enumeration;

import com.sun.java.util.collections.Hashtable;
import com.sun.java.util.collections.ArrayList;

import jmri.Consist;
import jmri.ConsistListener;

public class XNetConsist extends jmri.DccConsist implements XNetListener {

	// We need to wait for replies before completing consist 
	// operations
	private final int  IDLESTATE = 0;
	private final int ADDREQUESTSENTSTATE = 1;
	private final int REMOVEREQUESTSENTSTATE = 2;

	private int _state = IDLESTATE;

	private int _locoAddress = 0; // address for the last request
	private boolean _directionNormal = false; // direction of the last request

	// Initialize a consist for the specific address
        // the Default consist type is an advanced consist 
	public XNetConsist(int address) {
		super(address);
	 	// At construction, register for messages
        	XNetTrafficController.instance().addXNetListener(~0, this); 
	}

	// Clean Up local storage, and remove the XNetListener
	public void dispose() {
		super.dispose();
		XNetTrafficController.instance().removeXNetListener(~0,this);
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
		notifyConsistListeners(0,ConsistListener.NotImplemented);
	      }
	}

	/* is this address allowed?
	 * On Lenz systems, All addresses but 0 can be used in a consist 
	 * (Either and Advanced Consist or a Double Header).
	 */
	public boolean isAddressAllowed(int address) {
		if(address!=0) return(true);
		else return(false);
	}

	/* is there a size limit for this consist?
	 * For Lenz double headers, returns 2
	 * For Decoder Assisted Consists, returns -1 (no limit)
	 * return 0 for any other consist type.
   	 */
	public int sizeLimit(){
	   if(ConsistType==ADVANCED_CONSIST) {
		return -1;
	   } else if(ConsistType==CS_CONSIST) {
		return 2;
	   } else return 0;
	}

	// does the consist contain the specified address?
	public boolean contains(int address) {
	   if(ConsistType==ADVANCED_CONSIST || ConsistType == CS_CONSIST) {
		String Address= Integer.toString(address);
		return( (boolean) ConsistDir.contains(Address));
	   } else {
		log.error("Consist Type Not Supported");
		notifyConsistListeners(address,ConsistListener.NotImplemented);
	      }
	   return false;
	}

	// get the relative direction setting for a specific
	// locomotive in the consist
	public boolean getLocoDirection(int address) {
	   if(ConsistType==ADVANCED_CONSIST || ConsistType == CS_CONSIST) {
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
     	 * Method for adding an Address to the internal consist list object.
	 */
	private synchronized void addToConsistList(int LocoAddress, boolean directionNormal) {
		String Address= Integer.toString(LocoAddress);
	        Boolean Direction = new Boolean(directionNormal);
		if(!(ConsistList.contains(Address))) ConsistList.add(Address);
		ConsistDir.put(Address,Direction);
		if(ConsistType==CS_CONSIST && ConsistList.size()==2) {
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
	private synchronized void removeFromConsistList(int LocoAddress){
		String Address= Integer.toString(LocoAddress);
		ConsistDir.remove(Address);
		ConsistList.remove(Address);
		notifyConsistListeners(LocoAddress,ConsistListener.OPERATION_SUCCESS);	   	
	}

        /*
	 * Add a Locomotive to a Consist
	 *  @parm address is the Locomotive address to add to the locomotive
	 *  @parm directionNormal is True if the locomotive is traveling 
         *        the same direction as the consist, or false otherwise.
         */
	public synchronized void add(int LocoAddress,boolean directionNormal) {
	      if(ConsistType==ADVANCED_CONSIST) {
	         addToAdvancedConsist(LocoAddress, directionNormal);
		 // save the address for the check after we get a response 
		 // from the command station
		 _locoAddress = LocoAddress;
		 _directionNormal = directionNormal;
	      } else if(ConsistType==CS_CONSIST) {
		if(ConsistList.size()<2) {
		    // Lenz Double Headers require exactly 2 locomotives, so 
		    // wait for the second locomotive to be added to start
		    if(ConsistList.size()==1) {
	         	addToCSConsist(LocoAddress, directionNormal);		
		        // save the address for the check after we get a response 
		        // from the command station
		        _locoAddress = LocoAddress;
		        _directionNormal = directionNormal;
		    }  else if(ConsistList.size()<1) {
		       // we're going to just add this directly, since we 
		       // can't form the consist yet.
		       addToConsistList(LocoAddress,directionNormal);
		    }
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
	 *  Remove a Locomotive from this Consist
	 *  @parm address is the Locomotive address to add to the locomotive
         */
	public synchronized void remove(int LocoAddress) {
	      if(ConsistType==ADVANCED_CONSIST) {
	         removeFromAdvancedConsist(LocoAddress);		
		 // save the address for the check after we get a response 
		 // from the command station
		 _locoAddress = LocoAddress;
	      }
	      else if(ConsistType==CS_CONSIST) {
		 // Lenz Double Headers must be formed with EXACTLY 2 
		 // addresses, so if there are two addresses in the list, 
	         // we'll actually send the commands to remove the consist
		 if(ConsistList.size()==2) {
	         removeFromCSConsist(LocoAddress);
		 // save the address for the check after we get a response 
		 // from the command station
		 _locoAddress = LocoAddress;
		 } else {
		    // we just want to remove this from the list.
		    removeFromConsistList(LocoAddress);
		 }
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
	private synchronized void addToAdvancedConsist(int LocoAddress, boolean directionNormal) {
		// All we have to do here is create an apropriate XNetMessage, 
		// and send it.
		XNetMessage msg=XNetMessage.getAddLocoToConsistMsg(ConsistAddress,LocoAddress,directionNormal);
		XNetTrafficController.instance().sendXNetMessage(msg,this);
		_state=ADDREQUESTSENTSTATE;
	}

        /*
	 *  Remove a Locomotive from an Advanced Consist
	 *  @parm address is the Locomotive address to add to the locomotive
         */
	public synchronized void removeFromAdvancedConsist(int LocoAddress) {
		// All we have to do here is create an apropriate XNetMessage, 
		// and send it.
		XNetMessage msg=XNetMessage.getRemoveLocoFromConsistMsg(ConsistAddress,LocoAddress);
		XNetTrafficController.instance().sendXNetMessage(msg,this);
		 _state=REMOVEREQUESTSENTSTATE;
	}

        /*
	 *  Add a Locomotive to a Lenz Double Header
	 *  @parm address is the Locomotive address to add to the locomotive
	 *  @parm directionNormal is True if the locomotive is traveling 
         *        the same direction as the consist, or false otherwise.
         */
	private synchronized void addToCSConsist(int LocoAddress, boolean directionNormal) {
		// All we have to do here is create an apropriate XNetMessage, 
		// and send it.
		XNetMessage msg=XNetMessage.getBuildDoubleHeaderMsg(Integer.parseInt((String)ConsistList.get(0)),Integer.parseInt((String)ConsistList.get(1)));
		XNetTrafficController.instance().sendXNetMessage(msg,this);
		_state=ADDREQUESTSENTSTATE;
	}

        /*
	 *  Remove a Locomotive from a Lenz Double Header
	 *  @parm address is the Locomotive address to add to the locomotive
         */
	public synchronized void removeFromCSConsist(int LocoAddress) {
		// All we have to do here is create an apropriate XNetMessage, 
		// and send it.
		XNetMessage msg=XNetMessage.getDisolveDoubleHeaderMsg(Integer.parseInt((String)ConsistList.get(0)));
		XNetTrafficController.instance().sendXNetMessage(msg,this);
		_state=REMOVEREQUESTSENTSTATE; 
	}

	// Listeners for messages from the command station
	public synchronized void message(XNetReply l){
	   if(_state!=IDLESTATE) {
	   // we're waiting for a reply, so examine what we recieved
	   String text;
	   if (l.isOkMessage()) {
		if(_state==ADDREQUESTSENTSTATE) {
		   addToConsistList(_locoAddress,_directionNormal);
		} else if(_state==REMOVEREQUESTSENTSTATE) {
		   removeFromConsistList(_locoAddress);
		}
		_state=IDLESTATE;
	   } else if (l.getElement(0) == XNetConstants.CS_XpressNet_Error) {
                text = new String("XpressNet MU+DH error: ") ;
                switch(l.getElement(1)) {
                   case 0x81: text = text+ "Selected Locomotive has not been operated by this XPressNet device or address 0 selected";
			log.error(text);
			_state=IDLESTATE;
			notifyConsistListeners(_locoAddress,
					ConsistListener.CONSIST_ERROR |
					ConsistListener.LOCO_NOT_OPERATED);
                        break;
                   case 0x82: text = text+ "Selected Locomotive is being operated by another XPressNet device";
			log.error(text);
			_state=IDLESTATE;
			notifyConsistListeners(_locoAddress,
					ConsistListener.CONSIST_ERROR | 
					ConsistListener.LOCO_NOT_OPERATED);
                        break;
                   case 0x83: text = text+ "Selected Locomotive already in MU or DH";
			log.error(text);
			_state=IDLESTATE;
			notifyConsistListeners(_locoAddress,
					ConsistListener.CONSIST_ERROR |
					ConsistListener.ALREADY_CONSISTED);
                        break;
                   case 0x84: text = text+ "Unit selected for MU or DH has speed setting other than 0";
			log.error(text);
			_state=IDLESTATE;
			notifyConsistListeners(_locoAddress,
					ConsistListener.CONSIST_ERROR |
					ConsistListener.NONZERO_SPEED);
                        break;
                   case 0x85: text = text+ "Locomotive not in a MU";
			log.error(text);
			_state=IDLESTATE;
			notifyConsistListeners(_locoAddress,
					ConsistListener.CONSIST_ERROR |
					ConsistListener.NOT_CONSISTED);
			log.error(text);
                        break;
                   case 0x86: text = text+ "Locomotive address not a multi-unit base address";
			log.error(text);
			_state=IDLESTATE;
			notifyConsistListeners(_locoAddress,
					ConsistListener.CONSIST_ERROR |
					ConsistListener.NOT_CONSIST_ADDR);

			log.error(text);
                        break;
                   case 0x87: text = text+ "It is not possible to delete the locomotive";
			log.error(text);
			_state=IDLESTATE;
			notifyConsistListeners(_locoAddress,
					ConsistListener.CONSIST_ERROR |
					ConsistListener.DELETE_ERROR);
                        break;
                   case 0x88: text = text+ "The Command Station Stack is Full";
			log.error(text);
			_state=IDLESTATE;
			notifyConsistListeners(_locoAddress,
					ConsistListener.CONSIST_ERROR |
					ConsistListener.STACK_FULL );
			log.error(text);
                        break;
		   default: text = text+ "Unknown";
			log.error(text);
			_state=IDLESTATE;
			notifyConsistListeners(_locoAddress,
					ConsistListener.CONSIST_ERROR);
		   }
		}
	     }
	}

	public void message(XNetMessage l){
	}


	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetConsist.class.getName());

}
