/* EasyDccProgrammerManager.java */

package jmri.jmrix.easydcc;

import jmri.*;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers for EasyDcc systems
 *
 * @see         jmri.ProgrammerManager
 * @author	Bob Jacobsen Copyright (C) 2002
 * @version	$Revision: 1.1 $
 */
public class EasyDccProgrammerManager  extends DefaultProgrammerManager {

    private Programmer localProgrammer;

    public EasyDccProgrammerManager(Programmer serviceModeProgrammer) {
        super(serviceModeProgrammer);
        localProgrammer = serviceModeProgrammer;

    }

    /**
     * Works with command station to provide Ops Mode, so say it works
     * @returns true
     */
    public boolean isOpsModePossible() {return true;}

    public Programmer getOpsModeProgrammer(boolean pLongAddress, int pAddress) {
        return new EasyDccOpsModeProgrammer(pAddress, pLongAddress);
    }

    public Programmer reserveOpsModeProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    };
    public void releaseOopsModeProgrammer(Programmer p) {}
}


/* @(#)EasyDccProgrammerManager.java */
