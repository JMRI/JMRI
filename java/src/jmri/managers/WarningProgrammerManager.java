package jmri.managers;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import jmri.AddressedProgrammer;
import jmri.LocoAddress;
import jmri.Programmer;
import jmri.ProgrammerManager;
import jmri.ProgrammingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link jmri.ProgrammerManager} that logs a warning when
 * used.
 *
 * @author Randall Wood (C) 2016
 * @deprecated since 4.5.4; use only to flag uses of the deprecated
 * ProgrammerManager class in scripts.
 */
@Deprecated
public class WarningProgrammerManager implements ProgrammerManager {

    private final ProgrammerManager manager;
    private final static Logger log = LoggerFactory.getLogger(WarningProgrammerManager.class);
    private boolean warned = false;

    public WarningProgrammerManager(@Nullable ProgrammerManager manager) {
        this.manager = manager;
    }

    private void warn() {
        if (!warned) {
            String warning = "Using deprecated ProgrammerManager; use AddressedProgrammerManager or GlobalProgrammerManager instead.\n";
            warning = warning + "This is most likely doable by replacing \"programmers\" with \"addressedProgrammers\" or \"globalProgrammers\" in your script.";
            log.warn(warning);
            warned = true;
        }
    }

    @Override
    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        this.warn();
        if (this.manager != null) {
            return this.manager.getAddressedProgrammer(pLongAddress, pAddress);
        }
        return null;
    }

    @Override
    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        this.warn();
        if (this.manager != null) {
            return this.manager.reserveAddressedProgrammer(pLongAddress, pAddress);
        }
        return null;
    }

    @Override
    public void releaseAddressedProgrammer(AddressedProgrammer p) {
        this.warn();
        if (this.manager != null) {
            this.manager.releaseAddressedProgrammer(p);
        }
    }

    @Override
    public boolean isAddressedModePossible() {
        this.warn();
        if (this.manager != null) {
            return this.manager.isAddressedModePossible();
        }
        return false;
    }

    @Override
    public boolean isAddressedModePossible(LocoAddress address) {
        this.warn();
        if (this.manager != null) {
            return this.manager.isAddressedModePossible(address);
        }
        return false;
    }

    @Override
    public List<ProgrammingMode> getDefaultModes() {
        this.warn();
        if (this.manager != null) {
            return this.manager.getDefaultModes();
        }
        return new ArrayList<>();
    }

    @Override
    public String getUserName() {
        this.warn();
        if (this.manager != null) {
            return this.manager.getUserName();
        }
        return ""; // NOI18N returning empty string by contract
    }

    @Override
    public Programmer getGlobalProgrammer() {
        this.warn();
        if (this.manager != null) {
            return this.manager.getGlobalProgrammer();
        }
        return null;
    }

    @Override
    public Programmer reserveGlobalProgrammer() {
        this.warn();
        if (this.manager != null) {
            return this.manager.reserveGlobalProgrammer();
        }
        return null;
    }

    @Override
    public void releaseGlobalProgrammer(Programmer p) {
        this.warn();
        if (this.manager != null) {
            this.manager.releaseGlobalProgrammer(p);
        }
    }

    @Override
    public boolean isGlobalProgrammerAvailable() {
        this.warn();
        if (this.manager != null) {
            return this.manager.isGlobalProgrammerAvailable();
        }
        return false;
    }

}
