/**
 * XNetConsistManager.java
 *
 * Description:           Consist Manager for use with the
 *                        XNetConsist class for the consists it builds
 *
 * @author                Paul Bender Copyright (C) 2004
 * @version               $Revision: 2.4 $
 */


package jmri.jmrix.lenz;

import jmri.Consist;
import jmri.ConsistManager;
import jmri.DccLocoAddress;

import java.util.Enumeration;

import com.sun.java.util.collections.Hashtable;
import com.sun.java.util.collections.ArrayList;

public class XNetConsistManager extends jmri.jmrix.AbstractConsistManager implements jmri.ConsistManager {

	/**
         *    This implementation does support andvanced consists, so 
	 *    return true.
         **/
        public boolean isCommandStationConsistPossible() { return true; }

        /**
         *    Does a CS consist require a seperate consist address?
	 *    CS consist addresses are assigned by the command station, so 
	 *    no consist address is needed, so return false
         **/
        public boolean csConsistNeedsSeperateAddress() { return false; }

	/**
	 *    Add a new XNetConsist with the given address to ConsistTable/ConsistList
	 */
	public Consist addConsist(DccLocoAddress address){ 
		        XNetConsist consist;
                        consist = new XNetConsist(address);
                        ConsistTable.put(address,consist);
                        ConsistList.add(address);
                        return((Consist)consist);
	}

}
