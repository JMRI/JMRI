/**
 * DccConsistManager.java
 *
 * Description:           The Default Consist Manager which uses the 
 *                        DccConsist class for the consists it builds
 *
 * @author                Paul Bender Copyright (C) 2003
 * @version               $ version 1.00 $
 */


package jmri;

import java.util.Enumeration;

import com.sun.java.util.collections.Hashtable;

import jmri.DccConsist;

public class DccConsistManager implements ConsistManager{

	Hashtable ConsistTable = null;

	public DccConsistManager(){
	      ConsistTable = new Hashtable();
	}

	// Clean up Local Storage
	public void dispose() {
	}

	/**
	 *    Find a Consist with this consist address, and return it.
	 **/
	public Consist getConsist(int address){
		String Address=Integer.toString(address);
		if(ConsistTable.contains(Address)) {
			return((Consist)ConsistTable.get(Address));
		} else {
			DccConsist consist;
			consist = new DccConsist(address);
			ConsistTable.put(Address,consist);
			return(consist);
		}
	   }
	
	// remove the old Consist
	public void delConsist(int address){
		String Address=Integer.toString(address);
		((DccConsist)ConsistTable.get(Address)).dispose();
		ConsistTable.remove(Address);
	}

	/**
         *    This implementation does NOT support andvanced consists, so 
	 *    return false.
         **/
        public boolean isCommandStationConsistPossible() { return false; }

}
