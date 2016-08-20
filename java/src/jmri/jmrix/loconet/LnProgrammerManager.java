/* LnProgrammerManager.java */
package jmri.jmrix.loconet;

import java.util.ArrayList;
import java.util.List;
import jmri.AddressedProgrammer;
import jmri.ProgrammingMode;
import jmri.managers.DefaultProgrammerManager;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers on LocoNet
 *
 * @see jmri.ProgrammerManager
 * @author	Bob Jacobsen Copyright (C) 2002
 * @version	$Revision$
 */
public class LnProgrammerManager extends DefaultProgrammerManager {

    //private Programmer mProgrammer;
    public LnProgrammerManager(SlotManager pSlotManager, LocoNetSystemConnectionMemo memo) {
        super(pSlotManager, memo);
        mSlotManager = pSlotManager;
        this.memo = memo;
    }

    SlotManager mSlotManager;
    LocoNetSystemConnectionMemo memo;

    /**
     * LocoNet command station does provide Ops Mode
     *
     * @return true
     */
    public boolean isAddressedModePossible() {
        return true;
    }

    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new LnOpsModeProgrammer(mSlotManager, memo, pAddress, pLongAddress);
    }

    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }

    static final ProgrammingMode LOCONETSV1MODE    = new ProgrammingMode("LOCONETSV1MODE", Bundle.getMessage("LOCONETSV1MODE"));
    static final ProgrammingMode LOCONETSV2MODE    = new ProgrammingMode("LOCONETSV2MODE", Bundle.getMessage("LOCONETSV2MODE"));
    static final ProgrammingMode LOCONETBDOPSWMODE = new ProgrammingMode("LOCONETBDOPSWMODE", Bundle.getMessage("LOCONETBDOPSWMODE"));
    static final ProgrammingMode LOCONETCSOPSWMODE = new ProgrammingMode("LOCONETCSOPSWMODE", Bundle.getMessage("LOCONETCSOPSWMODE"));

    /**
     * Types implemented here.
     */
    @Override
    public List<ProgrammingMode> getDefaultModes() {
        List<ProgrammingMode> ret = new ArrayList<ProgrammingMode>();
        ret.add(DefaultProgrammerManager.OPSBYTEMODE);
        ret.add(LOCONETSV2MODE);
        ret.add(LOCONETSV1MODE); // the show in interface in order listed here
        return ret;
    }

}


/* @(#)DefaultProgrammerManager.java */
