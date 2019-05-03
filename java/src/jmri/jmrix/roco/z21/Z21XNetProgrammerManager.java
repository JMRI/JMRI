package jmri.jmrix.roco.z21;

import jmri.AddressedProgrammer;
import jmri.Programmer;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * Extend XNetProgrammerManager for Z21.
 * @see jmri.jmrix.lenz.XNetProgrammerManager
 * @author Paul Bender Copyright (C) 2018
 * @navassoc 1 - 1 jmri.jmrix.roco.z21.Z21XNetProgrammer
 * @navassoc 1 - * jmri.jmrix.roco.z21.Z21XNetOpsModeProgrammer
 */
public class Z21XNetProgrammerManager extends jmri.jmrix.lenz.XNetProgrammerManager {

    private LocoNetSystemConnectionMemo lnMemo = null;

    public Z21XNetProgrammerManager(Programmer pProgrammer, XNetSystemConnectionMemo memo) {
        this(pProgrammer, memo, null);
    }

    public Z21XNetProgrammerManager(Programmer pProgrammer, XNetSystemConnectionMemo xnetMemo,LocoNetSystemConnectionMemo loconetMemo) {
        super(pProgrammer, xnetMemo);
        setLocoNetMemo(loconetMemo);
    }

    /**
     * Z21 Command stations do provide Ops Mode.
     * @return true
     */
    @Override
    public boolean isAddressedModePossible() {
        return true; 
    }

    @Override
    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        if(lnMemo!=null) {
           return new Z21XNetOpsModeProgrammer(pAddress, tc, lnMemo.getLnTrafficController()); 
        } else {
           return new Z21XNetOpsModeProgrammer(pAddress, tc );
        }
    }

    public void setLocoNetMemo(LocoNetSystemConnectionMemo loconetMemo) {
        lnMemo = loconetMemo;
    }

}
