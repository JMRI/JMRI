/* XNetProgrammerManager.java */
package jmri.jmrix.lenz;

import jmri.AddressedProgrammer;
import jmri.Programmer;
import jmri.managers.DefaultProgrammerManager;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers on XPressNet
 * @see jmri.ProgrammerManager
 * @author	Paul Bender Copyright (C) 2003
 * @navassoc 1 - 1 jmri.jmrix.lenz.XNetProgrammer
 * @navassoc 1 - * jmri.jmrix.lenz.XNetOpsModeProgrammer
 */
public class XNetProgrammerManager extends DefaultProgrammerManager {

    protected XNetTrafficController tc = null;

    public XNetProgrammerManager(Programmer pProgrammer, XNetSystemConnectionMemo memo) {
        super(pProgrammer, memo);
        tc = memo.getXNetTrafficController();
    }

    /**
     * XPressNet command station does provide Ops Mode We should make this
     * return false based on what command station we're using but for now, we'll
     * return true
     */
    public boolean isAddressedModePossible() {
        return tc.getCommandStation().isOpsModePossible();
    }

    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new XNetOpsModeProgrammer(pAddress, tc);
    }

    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }
}

/* @(#)DefaultProgrammerManager.java */
