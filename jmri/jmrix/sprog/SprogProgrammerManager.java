/* NceProgrammerManager.java */

package jmri.jmrix.sprog;

import jmri.DefaultProgrammerManager;
import jmri.Programmer;

/**
 * Extend DefaultProgrammerManager to provide programmers for SPROG systems
 *
 * @see         jmri.ProgrammerManager
 * @author	Andrew crosland Copyright (C) 2001
 * @version	$Revision: 1.1 $
 */
public class SprogProgrammerManager  extends DefaultProgrammerManager {

    public static final int SERVICE = 0;
    public static final int OPS = 1;

    private Programmer localProgrammer;
    private int mode;

    public SprogProgrammerManager(Programmer serviceModeProgrammer) {
        super(serviceModeProgrammer);
        localProgrammer = serviceModeProgrammer;
        this.mode = SERVICE;
    }

    public SprogProgrammerManager(Programmer serviceModeProgrammer, int mode) {
        super(serviceModeProgrammer);
        localProgrammer = serviceModeProgrammer;
        this.mode = mode;
    }

    /**
     * Classic SPROG is service mode only
     * SPROG Command Station is Ops mode only
     * @return true
     */
    public boolean isOpsModePossible() {
      if (mode == OPS) {return true;}
      else return false;
    }

    public boolean isServiceModePossible() {
      if (mode == SERVICE) {return true;}
      else return false;
    }

    public Programmer getOpsModeProgrammer(boolean pLongAddress, int pAddress) {
        return new SprogOpsModeProgrammer(pAddress, pLongAddress);
    }

    public Programmer reserveOpsModeProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    };
    public void releaseOopsModeProgrammer(Programmer p) {}
}


/* @(#)SprogProgrammerManager.java */
