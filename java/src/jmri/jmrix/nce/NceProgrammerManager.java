package jmri.jmrix.nce;

import java.util.Objects;
import javax.annotation.Nonnull;
import jmri.AddressedProgrammer;
import jmri.Programmer;
import jmri.managers.DefaultProgrammerManager;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers for NCE
 * systems.
 *
 * @see jmri.GlobalProgrammerManager
 * @author Bob Jacobsen Copyright (C) 2002, 2016
 * @author Ken Cameron Copyright (C) 2013
 */
public class NceProgrammerManager extends DefaultProgrammerManager {

    NceTrafficController tc;
    NceSystemConnectionMemo memo;

    public NceProgrammerManager(@Nonnull NceSystemConnectionMemo memo) {
        super(
                checkGlobalProgrammerAvailable(memo.getNceTrafficController())
                ? new NceProgrammer(memo.getNceTrafficController())
                : null,
                memo);
        this.tc = memo.getNceTrafficController();
        this.memo = memo;
        log.trace("NceProgrammerManager({}) with {}", memo,
                checkGlobalProgrammerAvailable(memo.getNceTrafficController()));
        Objects.requireNonNull(memo, "require NceSystemConnectionMemo");
    }

    /**
     * {@inheritDoc}
     *
     * @return true if selected NCE hardware and connection type support Ops
     *         Mode
     */
    @Override
    public boolean isAddressedModePossible() {
        Objects.requireNonNull(tc, "require NceTrafficController");
        Objects.requireNonNull(memo, "require NceSystemConnectionMemo");

        switch (memo.getNceUsbSystem()) {
            case NceTrafficController.USB_SYSTEM_POWERPRO:
                log.trace("isAddressedModePossible returns false");
                return false;
            default:
                log.trace("isAddressedModePossible returns true");
                return true;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return true if selected NCE hardware and connection type support Service
     *         Mode
     */
    @Override
    public boolean isGlobalProgrammerAvailable() {
        Objects.requireNonNull(tc, "require NceTrafficController");
        return checkGlobalProgrammerAvailable(tc);
    }

    // this centralizes the isGlobalProgrammerAvailable logic.  It
    // has to be static so it can be called during the construction of
    // an object of this class
    static private boolean checkGlobalProgrammerAvailable(@Nonnull NceTrafficController tc) {
        switch (tc.getUsbSystem()) {
            case NceTrafficController.USB_SYSTEM_NONE: // Serial or Simulator
            case NceTrafficController.USB_SYSTEM_POWERCAB:
            case NceTrafficController.USB_SYSTEM_TWIN:
                log.trace("checkGlobalProgrammerAvailable returns true");
                return true;
            default:
                log.trace("checkGlobalProgrammerAvailable returns false");
                return false;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note: The NCE service mode programmer might exist, but not be able to
     * function. Not a great situation, but there it is. We therefore check
     * before returning it.
     */
    @Override
    public Programmer getGlobalProgrammer() {
        if (!isGlobalProgrammerAvailable()) {
            return null;
        }
        return super.getGlobalProgrammer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new NceOpsModeProgrammer(tc, pAddress, pLongAddress);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NceProgrammerManager.class);

}
