package jmri.jmrix.ecos;

import jmri.AddressedProgrammer;
import jmri.Programmer;
import jmri.ProgrammingMode;
import jmri.jmrix.ecos.EcosOpsModeProgrammer;
import jmri.managers.DefaultProgrammerManager;

/**
 * Extend DefaultProgrammerManager to provide programmers on ECos
 * Programming track is supported from ECoS firmware version 4.1.
 * Ops mode or POM is supported from ECoS firmware version 4.2.3
 *
 * @see jmri.managers.DefaultProgrammerManager
 * @author Karl Johan Lisby Copyright (C) 2015 and 2018
 */
public class EcosProgrammerManager extends DefaultProgrammerManager {

    //private Programmer mProgrammer;
    public EcosProgrammerManager(Programmer serviceModeProgrammer, EcosSystemConnectionMemo memo) {
        super(serviceModeProgrammer, memo);
        this.tc = memo.getTrafficController();
    }
    EcosTrafficController tc;

    /**
     * ECoS command station provides Ops Mode on the LAN interface.
     *
     * @return true
     */
    @Override
    public boolean isAddressedModePossible() {
        return true;
    }

 //   @Override
 //   public java.util.List<ProgrammingMode> getDefaultModes() {
 //       java.util.ArrayList<ProgrammingMode> retval = new java.util.ArrayList<>();
 //       retval.add(ProgrammingMode.DIRECTBYTEMODE);
 //       return retval;
 //   }

    @Override
    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new EcosOpsModeProgrammer(tc, pAddress, pLongAddress);
    }

    @Override
    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }
    
}
