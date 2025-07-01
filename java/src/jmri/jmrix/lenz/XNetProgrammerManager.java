package jmri.jmrix.lenz;

import jmri.AddressedProgrammer;
import jmri.Programmer;
import jmri.managers.DefaultProgrammerManager;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers on XPressNet
 * @see jmri.managers.DefaultProgrammerManager
 * @author Paul Bender Copyright (C) 2003
 * @navassoc 1 - 1 jmri.jmrix.lenz.XNetProgrammer
 * @navassoc 1 - * jmri.jmrix.lenz.XNetOpsModeProgrammer
 */
public class XNetProgrammerManager extends DefaultProgrammerManager {

    protected XNetTrafficController tc;

    public XNetProgrammerManager(Programmer pProgrammer, XNetSystemConnectionMemo memo) {
        super(pProgrammer, memo);
        tc = memo.getXNetTrafficController();
    }

    /**
     * XpressNet command station does provide Ops Mode.
     * @return whether or not the command station supports Ops Mode.
     */
    @Override
    public boolean isAddressedModePossible() {
        return tc.getCommandStation().isOpsModePossible();
    }

    @Override
    protected AddressedProgrammer getConcreteAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new XNetOpsModeProgrammer(pAddress, tc);
    }

    @Override
    protected AddressedProgrammer reserveConcreteAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }

}
