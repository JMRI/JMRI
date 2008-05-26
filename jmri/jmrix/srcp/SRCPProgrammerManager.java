/* SRCPProgrammerManager.java */

package jmri.jmrix.srcp;

import jmri.DefaultProgrammerManager;
import jmri.Programmer;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers for SRCP systems
 *
 * @see         jmri.ProgrammerManager
 * @author	Bob Jacobsen Copyright (C) 2002, 2008
 * @version	$Revision: 1.1 $
 */
public class SRCPProgrammerManager  extends DefaultProgrammerManager {

    private Programmer localProgrammer;

    public SRCPProgrammerManager(Programmer serviceModeProgrammer) {
        super(serviceModeProgrammer);
        localProgrammer = serviceModeProgrammer;

    }

    /**
     * Works with command station to provide Ops Mode, so say it works
     * @return true
     */
    public boolean isOpsModePossible() {return true;}

    public Programmer getOpsModeProgrammer(boolean pLongAddress, int pAddress) {
        return new SRCPOpsModeProgrammer(pAddress, pLongAddress);
    }

    public Programmer reserveOpsModeProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    };
    public void releaseOopsModeProgrammer(Programmer p) {}
}


/* @(#)SRCPProgrammerManager.java */
