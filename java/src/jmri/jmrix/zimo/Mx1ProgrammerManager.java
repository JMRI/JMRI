/* Mx1ProgrammerManager.java */
package jmri.jmrix.zimo;

import jmri.AddressedProgrammer;
import jmri.Programmer;
import jmri.managers.DefaultProgrammerManager;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers for Zimo
 * systems.
 *
 * @see jmri.managers.DefaultProgrammerManager
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Ken Cameron Copyright (C) 2014
 * @author Kevin Dickerson Copyright (C) 2014
 *
 */
public class Mx1ProgrammerManager extends DefaultProgrammerManager {
    
    private Mx1SystemConnectionMemo _memo = null;

    public Mx1ProgrammerManager(Programmer serviceModeProgrammer, Mx1SystemConnectionMemo memo) {
        super(serviceModeProgrammer, memo);
        _memo = memo;
    }

    /**
     * Works with command station to provide Ops Mode, so say it works
     *
     * @return true
     */
    @Override
    public boolean isAddressedModePossible() {
        if (_memo.getConnectionType() == Mx1SystemConnectionMemo.MXULF)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean isGlobalProgrammerAvailable() {
        return true;
    }

    @Override
    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        if (_memo.getConnectionType() == Mx1SystemConnectionMemo.MXULF)
        {
            return new Mx1OpsModeProgrammer(pAddress, pLongAddress, _memo.getMx1TrafficController());
        }
        else
        {
            return null;
        }
    }

    @Override
    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }
}
