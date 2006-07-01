/* XNetProgrammerManager.java */

package jmri.jmrix.lenz;

import jmri.DefaultProgrammerManager;
import jmri.Programmer;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers on XPressNet
 *
 * @see         jmri.ProgrammerManager
 * @author	Paul Bender Copyright (C) 2003
 * @version	$Revision: 1.1 $
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
    public boolean isOpsModePossible() {return true;}

    public Programmer getOpsModeProgrammer(boolean pLongAddress, int pAddress) {
        return new XNetOpsModeProgrammer(pAddress);
    }

    public Programmer reserveOpsModeProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    };
    public void releaseOopsModeProgrammer(Programmer p) {}
}

/* @(#)DefaultProgrammerManager.java */
