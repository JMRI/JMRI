/* Dcc4PcProgrammerManager.java */
package jmri.jmrix.dcc4pc;

import jmri.AddressedProgrammer;
import jmri.Programmer;
import jmri.ProgrammerManager;
import jmri.managers.DefaultProgrammerManager;

/**
 * DCC4PC Programmer acts as a proxy for ops mode programming. Extend
 * DefaultProgrammerManager to provide ops mode programmers on XPressNet
 *
 * @see jmri.ProgrammerManager
 * @author	Kevin Dickerson Copyright (C) 2012
 * @version	$Revision: 18841 $
 */
public class Dcc4PcProgrammerManager extends DefaultProgrammerManager {

    private ProgrammerManager defaultManager;

    public Dcc4PcProgrammerManager(ProgrammerManager dpm) {
        super(dpm.getGlobalProgrammer());
        defaultManager = dpm;
    }

    public Programmer getGlobalProgrammer() {
        if (defaultManager == null) {
            return null;
        }
        return defaultManager.getGlobalProgrammer();
    }

    public String getUserName() {
        return defaultManager.getUserName();
    }

    /**
     * XPressNet command station does provide Ops Mode We should make this
     * return false based on what command station we're using but for now, we'll
     * return true
     */
    public boolean isAddressedModePossible() {
        if (defaultManager == null) {
            return false;
        }
        return defaultManager.isAddressedModePossible();
    }

    public boolean isGlobalProgrammerAvailable() {
        if (defaultManager == null) {
            return false;
        }
        return defaultManager.isGlobalProgrammerAvailable();
    }

    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        if (defaultManager == null) {
            return null;
        }
        return new Dcc4PcOpsModeProgrammer(pLongAddress, pAddress, defaultManager);
    }

    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        if (defaultManager == null) {
            return null;
        }
        return defaultManager.reserveAddressedProgrammer(pLongAddress, pAddress);
    }

    public void releaseAddressedProgrammer(AddressedProgrammer p) {
        if (defaultManager == null) {
            return;
        }
        defaultManager.releaseAddressedProgrammer(p);
    }

    public Programmer reserveGlobalProgrammer() {
        if (defaultManager == null) {
            return null;
        }
        return defaultManager.reserveGlobalProgrammer();
    }

    public void releaseGlobalProgrammer(Programmer p) {
        if (defaultManager == null) {
            return;
        }
        defaultManager.releaseGlobalProgrammer(p);
    }
}

/* @(#)DefaultProgrammerManager.java */
