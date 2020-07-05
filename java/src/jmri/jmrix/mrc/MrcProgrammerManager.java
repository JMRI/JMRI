/* MrcProgrammerManager.java */
package jmri.jmrix.mrc;

import jmri.AddressedProgrammer;
import jmri.Programmer;
import jmri.managers.DefaultProgrammerManager;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers for MRC
 * systems
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Ken Cameron Copyright (C) 2014
 * @author Kevin Dickerson Copyright (C) 2014
 * 
 */
@API(status = EXPERIMENTAL)
public class MrcProgrammerManager extends DefaultProgrammerManager {

    MrcSystemConnectionMemo memo;

    public MrcProgrammerManager(Programmer serviceModeProgrammer, MrcSystemConnectionMemo memo) {
        super(serviceModeProgrammer, memo);
        this.memo = memo;
    }

    /**
     * Works with command station to provide Ops Mode, so say it works
     *
     * @return true
     */
    @Override
    public boolean isAddressedModePossible() {
        return true;
    }

    /**
     * @return true
     */
    @Override
    public boolean isGlobalProgrammerAvailable() {
        return true;
    }

    @Override
    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new MrcOpsModeProgrammer(memo, pAddress, pLongAddress);
    }

    @Override
    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }
}



