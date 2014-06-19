/* MrcProgrammerManager.java */

package jmri.jmrix.mrc;

import jmri.managers.DefaultProgrammerManager;
import jmri.Programmer;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers for MRC systems
 *
 * @see         jmri.ProgrammerManager
 * @author	Bob Jacobsen Copyright (C) 2002
 * @author	Ken Cameron Copyright (C) 2014
 * @author  Kevin Dickerson Copyright (C) 2014
 * @version	$Revision: 23001 $
 */
public class MrcProgrammerManager  extends DefaultProgrammerManager {
	
	MrcTrafficController tc;

    public MrcProgrammerManager(Programmer serviceModeProgrammer, MrcSystemConnectionMemo memo) {
        super(serviceModeProgrammer, memo);
    	this.tc = memo.getMrcTrafficController();
    }

    /**
     * Works with command station to provide Ops Mode, so say it works
     * @return true
     */
    public boolean isAddressedModePossible() {return true;}
    
    /**
	 * Works with PH command station to provide Service Mode and USB connect to
	 * PowerCab.
	 * 
	 * @return true if not USB connect to SB3,PowerPro,SB5
	 */
    public boolean isGlobalProgrammerAvailable() {
		return true;
	}


    public Programmer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new MrcOpsModeProgrammer(tc, pAddress, pLongAddress);
    }

    public Programmer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }
}


/* @(#)MrcProgrammerManager.java */
