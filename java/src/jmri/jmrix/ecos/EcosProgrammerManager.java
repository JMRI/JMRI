package jmri.jmrix.ecos;

import jmri.Programmer;
import jmri.ProgrammingMode;
import jmri.managers.DefaultProgrammerManager;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers on LocoNet
 *
 * @see jmri.managers.DefaultProgrammerManager
 * @author Karl Johan Lisby Copyright (C) 2015
 */
public class EcosProgrammerManager extends DefaultProgrammerManager {

    //private Programmer mProgrammer;
    public EcosProgrammerManager(Programmer serviceModeProgrammer, EcosSystemConnectionMemo memo) {
        super(serviceModeProgrammer, memo);
    }

    /**
     * ECoS command station does not provide Ops Mode on the LAN interface.
     *
     * @return false
     */
    @Override
    public boolean isAddressedModePossible() {
        return false;
    }

    @Override
    public java.util.List<ProgrammingMode> getDefaultModes() {
        java.util.ArrayList<ProgrammingMode> retval = new java.util.ArrayList<>();
        retval.add(ProgrammingMode.DIRECTBYTEMODE);
        return retval;
    }

}
