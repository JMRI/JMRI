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
        _memo = memo;
    }
    
    TmccSystemConnectionMemo _memo;

    static final ProgrammingMode TMCCMODE1 = new ProgrammingMode("TMCCMODE1", Bundle.getMessage("TMCCMODE1"));
    static final ProgrammingMode TMCCMODE2 = new ProgrammingMode("TMCCMODE2", Bundle.getMessage("TMCCMODE2"));

    /**
     * Does TMCC have the equivalent of Ops Mode?
     *
     * @return always false until Ops Mode implemented
     */
    @Override
    public boolean isAddressedModePossible() {
        return true;
    }

    @Override
    protected AddressedProgrammer getConcreteAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new TmccOpsModeProgrammer(pAddress, pLongAddress, _memo);
    }

    @Override
    protected AddressedProgrammer reserveConcreteAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }


}
