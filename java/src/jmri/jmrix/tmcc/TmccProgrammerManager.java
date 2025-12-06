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
 * with edits/additions by
 * @author Timothy Jump Copyright (C) 2025
 */
public class TmccProgrammerManager extends DefaultProgrammerManager {

    public TmccProgrammerManager(Programmer serviceModeProgrammer, TmccSystemConnectionMemo memo) {
        super(serviceModeProgrammer, memo);
        _memo = memo;
    }

    TmccSystemConnectionMemo _memo;

    static final ProgrammingMode TMCCMODE1_ENGID = new ProgrammingMode("TMCCMODE1_ENGID", Bundle.getMessage("TMCCMODE1_ENGID"));
    static final ProgrammingMode TMCCMODE2_ENGID = new ProgrammingMode("TMCCMODE2_ENGID", Bundle.getMessage("TMCCMODE2_ENGID"));
    
    static final ProgrammingMode TMCCMODE1_TRKID = new ProgrammingMode("TMCCMODE1_TRKID", Bundle.getMessage("TMCCMODE1_TRKID"));
    static final ProgrammingMode TMCCMODE2_TRKID = new ProgrammingMode("TMCCMODE2_TRKID", Bundle.getMessage("TMCCMODE2_TRKID"));

    static final ProgrammingMode TMCCMODE1_SWID = new ProgrammingMode("TMCCMODE1_SWID", Bundle.getMessage("TMCCMODE1_SWID"));
    static final ProgrammingMode TMCCMODE1_ACCID = new ProgrammingMode("TMCCMODE1_ACCID", Bundle.getMessage("TMCCMODE1_ACCID"));

    static final ProgrammingMode TMCCMODE1_ENGFEATURE = new ProgrammingMode("TMCCMODE1_ENGFEATURE", Bundle.getMessage("TMCCMODE1_ENGFEATURE"));
    static final ProgrammingMode TMCCMODE2_ENGFEATURE = new ProgrammingMode("TMCCMODE2_ENGFEATURE", Bundle.getMessage("TMCCMODE2_ENGFEATURE"));

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
