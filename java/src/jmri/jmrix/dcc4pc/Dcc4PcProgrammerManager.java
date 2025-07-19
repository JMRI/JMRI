package jmri.jmrix.dcc4pc;

import javax.annotation.Nonnull;
import jmri.AddressedProgrammer;
import jmri.AddressedProgrammerManager;
import jmri.GlobalProgrammerManager;
import jmri.Programmer;
import jmri.managers.DefaultProgrammerManager;

/**
 * DCC4PC Programmer acts as a proxy for ops mode programming. Extend
 * DefaultProgrammerManager to provide ops mode programmers on XpressNet
 *
 * @see jmri.managers.DefaultProgrammerManager
 * @author Kevin Dickerson Copyright (C) 2012
 *
 */
public class Dcc4PcProgrammerManager extends DefaultProgrammerManager {

    private final GlobalProgrammerManager manager;

    public <T extends AddressedProgrammerManager & GlobalProgrammerManager> Dcc4PcProgrammerManager(@Nonnull T manager) {
        super(manager.getGlobalProgrammer());
        this.manager = manager;
    }

    @Override
    protected Programmer getConcreteGlobalProgrammer() {
        return manager.getGlobalProgrammer();
    }

    @Override
    public String getUserName() {
        return manager.getUserName();
    }

    /**
     * XpressNet command station does provide Ops Mode We should make this
     * return false based on what command station we're using but for now, we'll
     * return true
     */
    @Override
    public boolean isAddressedModePossible() {
        if (manager instanceof AddressedProgrammerManager) {
            return ((AddressedProgrammerManager) manager).isAddressedModePossible();
        }
        return false;
    }

    @Override
    public boolean isGlobalProgrammerAvailable() {
        return manager.isGlobalProgrammerAvailable();
    }

    @Override
    protected AddressedProgrammer getConcreteAddressedProgrammer(boolean pLongAddress, int pAddress) {
        if (manager instanceof AddressedProgrammerManager) {
            return new Dcc4PcOpsModeProgrammer(pLongAddress, pAddress, (AddressedProgrammerManager) manager);
        }
        return null;
    }

    @Override
    protected AddressedProgrammer reserveConcreteAddressedProgrammer(boolean pLongAddress, int pAddress) {
        if (manager instanceof AddressedProgrammerManager) {
            return ((AddressedProgrammerManager) manager).reserveAddressedProgrammer(pLongAddress, pAddress);
        }
        return null;
    }

    @Override
    public void releaseAddressedProgrammer(AddressedProgrammer p) {
        if (manager instanceof AddressedProgrammerManager) {
            ((AddressedProgrammerManager) manager).releaseAddressedProgrammer(p);
        }
    }

    @Override
    protected Programmer reserveConcreteGlobalProgrammer() {
        return manager.reserveGlobalProgrammer();
    }

    @Override
    public void releaseGlobalProgrammer(Programmer p) {
        manager.releaseGlobalProgrammer(p);
    }
}
