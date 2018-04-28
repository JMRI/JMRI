package jmri.jmrix.loconet;

import java.util.ArrayList;
import java.util.List;
import jmri.AddressedProgrammer;
import jmri.ProgrammingMode;
import jmri.managers.DefaultProgrammerManager;

/**
 * Extend DefaultProgrammerManager to provide programmers on LocoNet
 *
 * @see jmri.managers.DefaultProgrammerManager
 * @author Bob Jacobsen Copyright (C) 2002
  */
public class LnProgrammerManager extends DefaultProgrammerManager {

    public LnProgrammerManager(SlotManager pSlotManager, LocoNetSystemConnectionMemo memo) {
        super(pSlotManager, memo);
        mSlotManager = pSlotManager;
        this.memo = memo;
    }

    SlotManager mSlotManager;
    LocoNetSystemConnectionMemo memo;

    /**
     * {@inheritDoc}
     * LocoNet command station does provide Ops Mode
     *
     * @return true
     */
    @Override
    public boolean isAddressedModePossible() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new LnOpsModeProgrammer(mSlotManager, memo, pAddress, pLongAddress);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }

    /**
     * Programming in Ops mode via the LocoNet cable.
     */
    static final ProgrammingMode LOCONETOPSBOARD    = new ProgrammingMode("LOCONETOPSBOARD", Bundle.getMessage("LOCONETOPSBOARD"));
    
    /**
     * Programming for LocoNet System Variables using version 1 of the protocol.
     */
    static final ProgrammingMode LOCONETSV1MODE    = new ProgrammingMode("LOCONETSV1MODE", Bundle.getMessage("LOCONETSV1MODE"));
    
    /**
     * Programming for LocoNet System Variables using version 2 of the protocol.
     */
    static final ProgrammingMode LOCONETSV2MODE    = new ProgrammingMode("LOCONETSV2MODE", Bundle.getMessage("LOCONETSV2MODE"));
    
    /**
     * Programming via LocoNet messages for Digitrax DS*, PM*, BDL*, SE* boards
     */
    static final ProgrammingMode LOCONETBDOPSWMODE = new ProgrammingMode("LOCONETBDOPSWMODE", Bundle.getMessage("LOCONETBDOPSWMODE"));
    
    /**
     * Programming via LocoNet messages for Digitrax Command Station op switches
     */
    static final ProgrammingMode LOCONETCSOPSWMODE = new ProgrammingMode("LOCONETCSOPSWMODE", Bundle.getMessage("LOCONETCSOPSWMODE"));

    /**
     * Types implemented here.
     */
    @Override
    public List<ProgrammingMode> getDefaultModes() {
        List<ProgrammingMode> ret = new ArrayList<ProgrammingMode>();
        ret.add(ProgrammingMode.OPSBYTEMODE);
        ret.add(LOCONETOPSBOARD);
        ret.add(LOCONETSV2MODE);
        ret.add(LOCONETSV1MODE); // the show in interface in order listed here
        return ret;
    }

}
