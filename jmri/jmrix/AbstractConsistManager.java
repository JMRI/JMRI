/**
 * AbstractConsistManager.java
 *
 * Description:           an Abstract Consist Manager on top of which 
 *                        system specific consist managers can be built.
 *
 * @author                Paul Bender Copyright (C) 2004
 * @version               $Revision: 1.4 $
 */


package jmri.jmrix;

import java.util.Enumeration;

import com.sun.java.util.collections.Hashtable;
import com.sun.java.util.collections.ArrayList;

import jmri.Consist;
import jmri.ConsistListener;
import jmri.DccLocoAddress;

abstract public class AbstractConsistManager implements jmri.ConsistManager{

	protected Hashtable ConsistTable = null;

	protected ArrayList ConsistList = null;

	public AbstractConsistManager(){
	      ConsistTable = new Hashtable();
	      ConsistList = new ArrayList();
	}

	// Clean up Local Storage
	public void dispose() {
	}

	/**
	 *    Find a Consist with this consist address, and return it.
	 **/
	public Consist getConsist(DccLocoAddress address){
		if(ConsistTable.containsKey(address)) {
			return((Consist)ConsistTable.get(address));
		} else {
			return(addConsist(address));
		}
	   }

	/**
	 *     Add a new Consist with the given address to the ConsistTable/ConsistList
	 **/
	abstract public Consist addConsist(DccLocoAddress address);
	
	// remove the old Consist
	public void delConsist(DccLocoAddress address){
		((Consist)ConsistTable.get(address)).dispose();
		ConsistTable.remove(address);
		ConsistList.remove(address);
	}

	/**
         *    Does this implementation support a command station consist?
         **/
        abstract public boolean isCommandStationConsistPossible();

        /**
         *    Does a CS consist require a seperate consist address?
	 *    (or is the lead loco to be used for the consist address)
         **/
        abstract public boolean csConsistNeedsSeperateAddress();

	/**
  	 *  Return the list of consists we know about.
	 **/
	public ArrayList getConsistList() { return ConsistList; }

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
