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

    static final ProgrammingMode TMCCMODE1_ID = new ProgrammingMode("TMCCMODE1_ID", Bundle.getMessage("TMCCMODE1_ID"));
    static final ProgrammingMode TMCCMODE2_ID = new ProgrammingMode("TMCCMODE2_ID", Bundle.getMessage("TMCCMODE2_ID"));

    static final ProgrammingMode TMCCMODE1_FEATURE = new ProgrammingMode("TMCCMODE1_FEATURE", Bundle.getMessage("TMCCMODE1_FEATURE"));
    static final ProgrammingMode TMCCMODE2_FEATURE = new ProgrammingMode("TMCCMODE2_FEATURE", Bundle.getMessage("TMCCMODE2_FEATURE"));

    /**
     * For Implementing TMCCOpsModeProgrammer
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
