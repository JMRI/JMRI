/* NceProgrammerManager.java */

package jmri.jmrix.sprog;

import jmri.jmrix.sprog.SprogConstants.SprogMode;
import jmri.managers.DefaultProgrammerManager;
import jmri.Programmer;

/**
 * Extend DefaultProgrammerManager to provide programmers for SPROG systems
 *
 * @see         jmri.ProgrammerManager
 * @author	Andrew crosland Copyright (C) 2001
 * @version	$Revision$
 */
public class SprogProgrammerManager  extends DefaultProgrammerManager {
	//private Programmer localProgrammer;
    private SprogMode mode;

    public SprogProgrammerManager(Programmer serviceModeProgrammer, SprogSystemConnectionMemo memo) {
        super(serviceModeProgrammer, memo);
        //localProgrammer = serviceModeProgrammer;
        this.mode = SprogMode.SERVICE;
    }

    public SprogProgrammerManager(Programmer serviceModeProgrammer, SprogMode mode, SprogSystemConnectionMemo memo) {
        super(serviceModeProgrammer, memo);
        //localProgrammer = serviceModeProgrammer;
        this.mode = mode;
    }

    /**
     * Classic SPROG is service mode only
     * SPROG Command Station is Ops mode only
     * @return true
     */
    public boolean isAddressedModePossible() {
      if (mode == SprogMode.OPS) {return true;}
      else return false;
    }

    public boolean isGlobalProgrammerAvailable() {
      if (mode == SprogMode.SERVICE) {return true;}
      else return false;
    }

    public Programmer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new SprogOpsModeProgrammer(pAddress, pLongAddress);
    }

    public Programmer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }
}


/* @(#)SprogProgrammerManager.java */
