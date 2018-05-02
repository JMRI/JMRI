package jmri.jmrix.srcp;

import jmri.AddressedProgrammer;
import jmri.Programmer;
import jmri.managers.DefaultProgrammerManager;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers for SRCP
 * systems
 *
 * @see jmri.managers.DefaultProgrammerManager
 * @author Bob Jacobsen Copyright (C) 2002, 2008
 */
public class SRCPProgrammerManager extends DefaultProgrammerManager {

    //private Programmer localProgrammer;
    //private SRCPBusConnectionMemo _memo=null;
    public SRCPProgrammerManager(Programmer serviceModeProgrammer, SRCPBusConnectionMemo memo) {
        super(serviceModeProgrammer, memo);
        //localProgrammer = serviceModeProgrammer;
        //_memo=memo;

    }

    /**
     * Works with command station to provide Ops Mode, so say it works
     *
     * @return true
     */
    //public boolean isAddressedModePossible() {return true;}
    //public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
    //    return new SRCPOpsModeProgrammer(pAddress, pLongAddress,_memo);
    //}
    @Override
    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }
}
