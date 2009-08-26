/* CbusProgrammerManager.java */

package jmri.jmrix.can.cbus;

import jmri.managers.DefaultProgrammerManager;
import jmri.Programmer;

/**
 * Extend DefaultProgrammerManager to provide programmers for CBUS systems
 *
 * @see         jmri.ProgrammerManager
 * @author	Andrew crosland Copyright (C) 2009
 * @version	$Revision: 1.1 $
 */
public class CbusDccProgrammerManager  extends DefaultProgrammerManager {

    public static final int SERVICE = 0;
    public static final int OPS = 1;

    //private Programmer localProgrammer;
    private int mode;

    public CbusDccProgrammerManager(Programmer serviceModeProgrammer) {
        super(serviceModeProgrammer);
        //localProgrammer = serviceModeProgrammer;
        this.mode = SERVICE;
    }

    public CbusDccProgrammerManager(Programmer serviceModeProgrammer, int mode) {
        super(serviceModeProgrammer);
        //localProgrammer = serviceModeProgrammer;
        this.mode = mode;
    }

    /**
     * Classic SPROG is service mode only
     * SPROG Command Station is Ops mode only
     * @return true
     */
    public boolean isAddressedModePossible() {
      if (mode == OPS) {return true;}
      else return false;
    }

    public boolean isGlobalProgrammerAvailable() {
      if (mode == SERVICE) {return true;}
      else return false;
    }

    public Programmer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
//        return new CbusOpsModeProgrammer(pAddress, pLongAddress);
        return null;
    }

    public Programmer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CbusDccProgrammerManager.class.getName());
}


/* @(#)CbusDccProgrammerManager.java */
