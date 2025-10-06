package jmri.jmrix.tmcc;

import jmri.AddressedProgrammer;
import jmri.Programmer;
import jmri.managers.DefaultProgrammerManager;
import jmri.ProgrammingMode;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers for TMCC
 * systems.
 *
 * From EasyDccProgrammerManager
 *
 * @see jmri.managers.DefaultProgrammerManager
 * @author Bob Jacobsen Copyright (C) 2002, 2025
 */
public class TmccProgrammerManager extends DefaultProgrammerManager {

    public TmccProgrammerManager(Programmer serviceModeProgrammer, TmccSystemConnectionMemo memo) {
        super(serviceModeProgrammer, memo);
    }
    
    static final ProgrammingMode TMCCMODE1_ID = new ProgrammingMode("TMCCMODE1_ID", Bundle.getMessage("TMCCMODE1_ID"));
    static final ProgrammingMode TMCCMODE2_ID = new ProgrammingMode("TMCCMODE2_ID", Bundle.getMessage("TMCCMODE2_ID"));


    /**
     * Does TMCC have the equivalent of Ops Mode?
     *
     * @return always false until Ops Mode implemented
     */
    @Override
    public boolean isAddressedModePossible() {
        return false;
    }

    @Override
    protected AddressedProgrammer getConcreteAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }

    @Override
    protected AddressedProgrammer reserveConcreteAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }


}
