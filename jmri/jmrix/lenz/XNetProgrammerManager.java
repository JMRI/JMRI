/* XNetProgrammerManager.java */

package jmri.jmrix.lenz;

import jmri.DefaultProgrammerManager;
import jmri.Programmer;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers on XPressNet
 *
 * @see         jmri.ProgrammerManager
 * @author	Paul Bender Copyright (C) 2003
 * @version	$Revision: 2.0 $
 */
public class XNetProgrammerManager  extends DefaultProgrammerManager {

    private Programmer mProgrammer;

    public XNetProgrammerManager(Programmer pProgrammer) {
        super(pProgrammer);
	mProgrammer=pProgrammer;
    }

    /**
     * XPressNet command station does provide Ops Mode
     * We should make this return false based on what command station 
     * we're using but for now, we'll return true
     */
    public boolean isOpsModePossible() {
        int csType=XNetTrafficController.instance()
                                .getCommandStation()
                                .getCommandStationType();
        if(csType==0x01 || csType==0x02) return false;
	  else return true;
    }

    public Programmer getOpsModeProgrammer(boolean pLongAddress, int pAddress) {
        return new XNetOpsModeProgrammer(pAddress);
    }

    public Programmer reserveOpsModeProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    };
    public void releaseOopsModeProgrammer(Programmer p) {}
}

/* @(#)DefaultProgrammerManager.java */
