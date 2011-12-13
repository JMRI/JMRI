/**
 * LocoNetConsistManager.java
 *
 * Description:           Consist Manager for use with the
 *                        LocoNetConsist class for the consists it builds
 *
 * @author                Paul Bender Copyright (C) 2011
 * @version               $Revision$
 */


package jmri.jmrix.loconet;

import jmri.Consist;
import jmri.DccLocoAddress;

public class LocoNetConsistManager extends jmri.jmrix.AbstractConsistManager implements jmri.ConsistManager {
    
    private LocoNetSystemConnectionMemo memo=null;

    /**
     *  Constructor - call the constructor for the superclass, and 
     *  initilize the consist reader thread, which retrieves consist 
     *  information from the command station
     **/
    public LocoNetConsistManager(LocoNetSystemConnectionMemo lm){
        super();
	this.memo=lm;
    }
    
    /**
     *    This implementation does support command station assisted
     *     consists, so return true.
     **/
    public boolean isCommandStationConsistPossible() { return true; }
    
    /**
     *    Does a CS consist require a seperate consist address?
     **/
    public boolean csConsistNeedsSeperateAddress() { return false; }
    
    /**
     *    Add a new LocoNetConsist with the given address to 
     *    consistTable/consistList
     */
    public Consist addConsist(DccLocoAddress address){ 
        LocoNetConsist consist;
        consist = new LocoNetConsist(address,memo);
        consistTable.put(address,consist);
        consistList.add(address);
        return consist;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocoNetConsistManager.class.getName());
    
}
