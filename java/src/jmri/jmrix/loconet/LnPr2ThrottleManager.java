package jmri.jmrix.loconet;

import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.jmrix.AbstractThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LocoNet implementation of a ThrottleManager for the PR2.
 * <p>
 * Does direct "push" writes to the extended slot in the PR2.
 * <p>
 * The PR2 only allows a single locomotive address to be active, because it
 * implements a single-slot command station.
 *
 * @see AbstractThrottleManager
 * @author Bob Jacobsen Copyright (C) 2001, 2006
 */
public class LnPr2ThrottleManager extends AbstractThrottleManager {

    /**
     * Constructor, works via superclass.
     * @param memo the LocoNetSystemConnectionMemo
     */
    public LnPr2ThrottleManager(LocoNetSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * PR2 allows only one throttle
     * <p>
     * {@inheritDoc}
     */
    @Override
    protected boolean singleUse() {
        return true;
    }

    /**
     * Get a new Throttle object.
     *
     * This immediately invokes the callback with the a new throttle object.
     */
    @Override
    public void requestThrottleSetup(LocoAddress address, boolean control) {
        // The PR2 has only one slot, hence
        // doesn't require an interaction with the command
        // station to allocate slot, so immediately trigger the callback.
        if (address instanceof DccLocoAddress) {
            activeAddress = (DccLocoAddress) address;
        } else {
            log.error("cannot cast the passed address to DccLocoAddress.");
        }
        log.debug("new Pr2Throttle for " + activeAddress);
        notifyThrottleKnown(new Pr2Throttle((LocoNetSystemConnectionMemo) adapterMemo, activeAddress), activeAddress);
    }

    DccLocoAddress activeAddress = null;

    /**
     * PR2 does not have a Dispatch function
     *
     */
    @Override
    public boolean hasDispatchFunction() {
        return false;
    }

    /**
     * Address 128 and above is a long address
     *
     */
    @Override
    public boolean canBeLongAddress(int address) {
        return isLongAddress(address);
    }

    /**
     * Address 127 and below is a short address
     *
     */
    @Override
    public boolean canBeShortAddress(int address) {
        return !isLongAddress(address);
    }

    /**
     * Are there any ambiguous addresses (short vs long) on this system?
     */
    @Override
    public boolean addressTypeUnique() {
        return true;
    }

    /*
     * Local method for deciding short/long address
     */
    static boolean isLongAddress(int num) {
        return (num >= 128);
    }

    /**
     * Make the active address available to the power manager, which needs it to
     * turn on and off "neutral mode" in the locomotive
     * @return a DccLocoAddress
     */
    public DccLocoAddress getActiveAddress() {
        return activeAddress;
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(LnPr2ThrottleManager.class);
}
