package jmri.jmrix.xpa;

import jmri.LocoAddress;
import jmri.ThrottleManager;
import jmri.jmrix.AbstractThrottleManager;

/**
 * XPA implementation of a ThrottleManager.
 *
 * @author Paul Bender Copyright (C) 2004
 */
public class XpaThrottleManager extends AbstractThrottleManager {

    private XpaTrafficController tc = null;

    /**
     * Create a throttle manager.
     *
     * @param m the memo for the connection the manager is associated with
     */
    public XpaThrottleManager(XpaSystemConnectionMemo m) {
        super(m);
        userName = m.getUserName();
        tc = m.getXpaTrafficController();
    }

    /**
     * Request a new throttle object be created for the address, and let the
     * throttle listeners know about it.
     *
     * {@inheritDoc }
     */
    @Override
    public void requestThrottleSetup(LocoAddress address, boolean control) {
        XpaThrottle throttle = new XpaThrottle(address, tc);
        notifyThrottleKnown(throttle, address);
    }

    /**
     * The XPA does not support the dispatch function.
     *
     * @return false
     */
    @Override
    public boolean hasDispatchFunction() {
        return false;
    }

    /**
     * {@inheritDoc } Address 100 and above is a long address.
     */
    @Override
    public boolean canBeLongAddress(int address) {
        return isLongAddress(address);
    }

    /**
     * {@inheritDoc } Address 99 and below is a short address.
     */
    @Override
    public boolean canBeShortAddress(int address) {
        return !isLongAddress(address);
    }

    /**
     * {@inheritDoc }
     *
     * @return true
     */
    @Override
    public boolean addressTypeUnique() {
        return true;
    }

    /**
     * Local method for deciding short/long address.
     */
    static boolean isLongAddress(int num) {
        return (num >= 100);
    }

}
