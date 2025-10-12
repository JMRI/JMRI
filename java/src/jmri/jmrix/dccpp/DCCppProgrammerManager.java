/* DCCppProgrammerManager.java */
package jmri.jmrix.dccpp;

import jmri.AddressedProgrammer;
import jmri.Programmer;
import jmri.managers.DefaultProgrammerManager;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers on DCC++
 *
 * @see jmri.managers.DefaultProgrammerManager
 * @author Paul Bender Copyright (C) 2003
 * @author Mark Underwood Copyright (C) 2015
  *
 * Based on XNetProgrammerManager by Paul Bender
 */
public class DCCppProgrammerManager extends DefaultProgrammerManager {

    protected DCCppTrafficController tc = null;

    public DCCppProgrammerManager(Programmer pProgrammer, DCCppSystemConnectionMemo memo) {
        super(pProgrammer, memo);
        tc = memo.getDCCppTrafficController();
    }

    @Override
    public boolean isAddressedModePossible() {
        return tc.getCommandStation().isOpsModePossible();
    }

    @Override
    protected AddressedProgrammer getConcreteAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new DCCppOpsModeProgrammer(pAddress, tc);
    }

    @Override
    protected AddressedProgrammer reserveConcreteAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }
}


