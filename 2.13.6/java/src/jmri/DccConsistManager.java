/**
 * DccConsistManager.java
 *
 * Description:           The Default Consist Manager which uses the 
 *                        DccConsist class for the consists it builds
 *
 * @author                Paul Bender Copyright (C) 2003
 * @version               $Revision$
 */


package jmri;

import java.util.Hashtable;
import java.util.ArrayList;

import jmri.DccConsist;
import jmri.ConsistListener;

public class DccConsistManager implements ConsistManager{

	private Hashtable<DccLocoAddress, DccConsist> ConsistTable = null;

	private ArrayList<DccLocoAddress> ConsistList = null;

	public DccConsistManager(){
	      ConsistTable = new Hashtable<DccLocoAddress, DccConsist>();
	      ConsistList = new ArrayList<DccLocoAddress>();
	}

	/**
	 *    Find a Consist with this consist address, and return it.
	 **/
	public Consist getConsist(DccLocoAddress address){
		if(ConsistTable.containsKey(address)) {
			return(ConsistTable.get(address));
		} else {
			DccConsist consist;
			consist = new DccConsist(address);
			ConsistTable.put(address,consist);
		 	ConsistList.add(address);
			return(consist);
		}
	   }
	
	// remove the old Consist
	public void delConsist(DccLocoAddress address){
		(ConsistTable.get(address)).dispose();
		ConsistTable.remove(address);
		ConsistList.remove(address);
	}

	/**
         *    This implementation does NOT support Command Station 
	 *    consists, so return false.
         **/
        public boolean isCommandStationConsistPossible() { return false; }

        /**
         *    Does a CS consist require a seperate consist address?
	 *    This implemenation does not support Command Station 
	 *    consists, so return false
         **/
        public boolean csConsistNeedsSeperateAddress() { return false; }

	/**
  	 *  Return the list of consists we know about.
	 **/
	public ArrayList<DccLocoAddress> getConsistList() { return ConsistList; }

	public String decodeErrorCode(int ErrorCode){
		StringBuffer buffer = new StringBuffer("");
		if ((ErrorCode & ConsistListener.NotImplemented) != 0)
					buffer.append("Not Implemented ");
		if ((ErrorCode & ConsistListener.OPERATION_SUCCESS) != 0)
					buffer.append("Operation Completed Successfully ");
		if ((ErrorCode & ConsistListener.CONSIST_ERROR) != 0)
					buffer.append("Consist Error ");
		if ((ErrorCode & ConsistListener.LOCO_NOT_OPERATED) != 0)
					buffer.append("Address not controled by this device.");
		if ((ErrorCode & ConsistListener.ALREADY_CONSISTED) != 0)
					buffer.append("Locomotive already consisted");
		if ((ErrorCode & ConsistListener.NOT_CONSISTED) != 0)
					buffer.append("Locomotive Not Consisted ");
		if ((ErrorCode & ConsistListener.NONZERO_SPEED) != 0)
					buffer.append("Speed Not Zero ");
		if ((ErrorCode & ConsistListener.NOT_CONSIST_ADDR) != 0)
					buffer.append("Address Not Conist Address ");
		if ((ErrorCode & ConsistListener.DELETE_ERROR) != 0)
					buffer.append("Delete Error ");
		if ((ErrorCode & ConsistListener.STACK_FULL) != 0)
					buffer.append("Stack Full ");

		String  retval = buffer.toString();
		if (retval.equals(""))
		   return "Unknown Status Code: " + ErrorCode;
		else return retval;
	}


}
