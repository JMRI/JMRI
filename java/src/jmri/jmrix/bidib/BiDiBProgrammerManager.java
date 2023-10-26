package jmri.jmrix.bidib;

import java.util.Objects;
import javax.annotation.Nonnull;
import jmri.AddressedProgrammer;
import jmri.Programmer;
import jmri.managers.DefaultProgrammerManager;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers for BiDiB
 * systems
 *
 * @see jmri.GlobalProgrammerManager
 * @author Bob Jacobsen Copyright (C) 2002, 2016
 * @author Eckart Meyer Copyright (C) 2019
 */
public class BiDiBProgrammerManager extends DefaultProgrammerManager {

    BiDiBTrafficController tc;
    BiDiBSystemConnectionMemo memo;

    public BiDiBProgrammerManager(@Nonnull BiDiBSystemConnectionMemo memo) {
        super(
                checkGlobalProgrammerAvailable(memo.getBiDiBTrafficController())
                ? new BiDiBProgrammer(memo.getBiDiBTrafficController())
                : null,
                memo);
        this.tc = memo.getBiDiBTrafficController();
        this.memo = memo;
        log.trace("BiDiBProgrammerManager({}) with {}", memo,
                checkGlobalProgrammerAvailable(memo.getBiDiBTrafficController()));
        Objects.requireNonNull(memo, "require BiDiBSystemConnectionMemo");
    }

    /**
     * {@inheritDoc}
     *
     * @return true if selected BiDiB hardware and connection type support Ops
     *         Mode
     */
    @Override
    public boolean isAddressedModePossible() {
//        Objects.requireNonNull(tc, "require BiDiBTrafficController");
        Objects.requireNonNull(memo, "require BiDiBSystemConnectionMemo");

        return (tc.getFirstCommandStationNode() != null);
    }

    /**
     * {@inheritDoc}
     *
     * @return true if selected BiDiB hardware and connection type support Service
     *         Mode
     */
    @Override
    public boolean isGlobalProgrammerAvailable() {
        Objects.requireNonNull(tc, "require BiDiBTrafficController");
        return checkGlobalProgrammerAvailable(tc);
    }

    // this centralizes the isGlobalProgrammerAvailable logic.  It
    // has to be static so it can be called during the construction of
    // an object of this class
    static private boolean checkGlobalProgrammerAvailable(@Nonnull BiDiBTrafficController tc) {
        return (tc.getCurrentGlobalProgrammerNode() != null);
    }

    /**
     * {@inheritDoc}
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
        return new BiDiBOpsModeProgrammer(pAddress, tc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null; //what is this??
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void releaseGlobalProgrammer(@Nonnull Programmer p) {
        log.debug("release global programmer: {}", p);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void releaseAddressedProgrammer(@Nonnull AddressedProgrammer p) {
        log.debug("release addressed programmer: {}", p);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BiDiBProgrammerManager.class);
}
