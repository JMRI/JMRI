/* NceProgrammerManager.java */

package jmri.jmrix.nce;

import jmri.DefaultProgrammerManager;
import jmri.Programmer;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers for NCE systems
 *
 * @see         jmri.ProgrammerManager
 * @author	Bob Jacobsen Copyright (C) 2002
 * @version	$Revision: 1.2 $
 */
public class NceProgrammerManager  extends DefaultProgrammerManager {

    private Programmer localProgrammer;

    public NceProgrammerManager(Programmer serviceModeProgrammer) {
        super(serviceModeProgrammer);
        localProgrammer = serviceModeProgrammer;

    }

    /**
     * Works with command station to provide Ops Mode, so say it works
     * @return true
     */
    public boolean isOpsModePossible() {return true;}

    public Programmer getOpsModeProgrammer(boolean pLongAddress, int pAddress) {
        return new NceOpsModeProgrammer(pAddress, pLongAddress);
    }

    public Programmer reserveOpsModeProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    };
    public void releaseOopsModeProgrammer(Programmer p) {}
}


/* @(#)NceProgrammerManager.java */
