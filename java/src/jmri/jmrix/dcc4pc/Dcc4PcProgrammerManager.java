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
 * @author Kevin Dickerson Copyright (C) 2012
 * 
 */
public class Dcc4PcProgrammerManager extends DefaultProgrammerManager {

    private ProgrammerManager defaultManager;

    public Dcc4PcProgrammerManager(ProgrammerManager dpm) {
        super(dpm.getGlobalProgrammer());
        defaultManager = dpm;
    }

    @Override
    public Programmer getGlobalProgrammer() {
        if (defaultManager == null) {
            return null;
        }
        return defaultManager.getGlobalProgrammer();
    }

    @Override
    public String getUserName() {
        return defaultManager.getUserName();
    }

    /**
     * XPressNet command station does provide Ops Mode We should make this
     * return false based on what command station we're using but for now, we'll
     * return true
     */
    @Override
    public boolean isAddressedModePossible() {
        if (defaultManager == null) {
            return false;
        }
        return defaultManager.isAddressedModePossible();
    }

    @Override
    public boolean isGlobalProgrammerAvailable() {
        if (defaultManager == null) {
            return false;
        }
        return defaultManager.isGlobalProgrammerAvailable();
    }

    @Override
    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        if (defaultManager == null) {
            return null;
        }
        return new Dcc4PcOpsModeProgrammer(pLongAddress, pAddress, defaultManager);
    }

    @Override
    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        if (defaultManager == null) {
            return null;
        }
        return defaultManager.reserveAddressedProgrammer(pLongAddress, pAddress);
    }

    @Override
    public void releaseAddressedProgrammer(AddressedProgrammer p) {
        if (defaultManager == null) {
            return;
        }
        defaultManager.releaseAddressedProgrammer(p);
    }

    @Override
    public Programmer reserveGlobalProgrammer() {
        if (defaultManager == null) {
            return null;
        }
        return defaultManager.reserveGlobalProgrammer();
    }

    @Override
    public void releaseGlobalProgrammer(Programmer p) {
        if (defaultManager == null) {
            return;
        }
        defaultManager.releaseGlobalProgrammer(p);
    }
}
