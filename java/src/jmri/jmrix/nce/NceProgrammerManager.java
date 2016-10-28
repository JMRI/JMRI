package jmri.jmrix.nce;

import jmri.AddressedProgrammer;
import jmri.Programmer;
import jmri.managers.DefaultProgrammerManager;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers for NCE systems
 *
 * @see jmri.ProgrammerManager
 * @author	Bob Jacobsen Copyright (C) 2002, 2016
 * @author	Ken Cameron Copyright (C) 2013
 */
public class NceProgrammerManager extends DefaultProgrammerManager {

    NceTrafficController tc;

    public NceProgrammerManager(NceSystemConnectionMemo memo) {
        super(
             checkGlobalProgrammerAvailable(memo.getNceTrafficController()) ?
                new NceProgrammer(memo.getNceTrafficController()) : null, 
             memo);
        this.tc = memo.getNceTrafficController();
    }

    /**
     * Works with command station to provide Ops Mode, so say it works
     *
     * @return true
     */
    public boolean isAddressedModePossible() {
        return true;
    }

    /**
     * Works with PH command station to provide Service Mode and USB connect to
     * PowerCab.
     *
     * @return true if not USB connect to SB3,PowerPro,SB5
     */
    public boolean isGlobalProgrammerAvailable() {
        return checkGlobalProgrammerAvailable(tc);
    }

    static private boolean checkGlobalProgrammerAvailable(NceTrafficController tc) {
        if (tc != null && (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE)) {
            if ((tc.getCmdGroups() & NceTrafficController.CMDS_PROGTRACK) == 0) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
    
    /**
     * Provides a service mode programmer
     *<p>
     * Note: The NCE service mode programmer might exist, but not be able to function.
     * Not a great situation, but there it is.  We therefore check before returning it.
     */
    public Programmer getGlobalProgrammer() {
        if ( ! isGlobalProgrammerAvailable() ) return null;
        return super.getGlobalProgrammer();
    }

    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new NceOpsModeProgrammer(tc, pAddress, pLongAddress);
    }

    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }
}
