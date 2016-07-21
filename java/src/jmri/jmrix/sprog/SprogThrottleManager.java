package jmri.jmrix.sprog;

import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.jmrix.AbstractThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SPROG implementation of a ThrottleManager.
 * <P>
 * Updated by Andrew Crosland February 2012 to enable 28 step speed packets</P>
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 */
public class SprogThrottleManager extends AbstractThrottleManager {

    /**
     * Constructor.
     */
    public SprogThrottleManager(SprogSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static public SprogThrottleManager instance() {
        return null;
    }

    boolean throttleInUse = false;

    void release() {
        throttleInUse = false;
    }

    public void requestThrottleSetup(LocoAddress a, boolean control) {
        // The SPROG protocol doesn't require an interaction with the command
        // station for this, so set the address and immediately trigger the callback
        // if a throttle is not in use.
            DccLocoAddress address = (DccLocoAddress) a;
        if (!throttleInUse) {
            throttleInUse = true;
            log.debug("new SprogThrottle for " + address);
            String addr = "" + address.getNumber() + ( address.isLongAddress() ? " 0" : "");
            SprogMessage m = new SprogMessage(2 + addr.length());
            int i = 0;
            m.setElement(i++, 'A');
            m.setElement(i++, ' ');
            for (int j = 0; j < addr.length(); j++) {
                m.setElement(i++, addr.charAt(j));
            }
            ((SprogSystemConnectionMemo) adapterMemo).getSprogTrafficController().sendSprogMessage(m, null);
            notifyThrottleKnown(new SprogThrottle((SprogSystemConnectionMemo) adapterMemo, address), address);
        } else {
            failedThrottleRequest(address, "Only one Throttle can be in use at anyone time with the Sprog.");
            //javax.swing.JOptionPane.showMessageDialog(null,"Only one Throttle can be in use at anyone time with the Sprog.","Sprog Throttle",javax.swing.JOptionPane.WARNING_MESSAGE);
            log.warn("Single SPROG Throttle already in use");
        }
    }

    /**
     * What speed modes are supported by this system? value should be or of
     * possible modes specified by the DccThrottle interface
     */
    public int supportedSpeedModes() {
        return (DccThrottle.SpeedStepMode128 | DccThrottle.SpeedStepMode28);
    }

    /**
     * Addresses 0-10239 can be long
     *
     */
    public boolean canBeLongAddress(int address) {
        return ((address >= 0) && (address <= 10239));
    }

    /**
     * The short addresses 1-127 are available
     *
     */
    public boolean canBeShortAddress(int address) {
        return ((address >= 1) && (address <= 127));
    }

    /**
     * Are there any ambiguous addresses (short vs long) on this system?
     */
    public boolean addressTypeUnique() {
        return false;
    }

    public boolean disposeThrottle(jmri.DccThrottle t, jmri.ThrottleListener l) {
        if (super.disposeThrottle(t, l)) {
            throttleInUse = false;
            return true;
        }
        return false;
    }

    private final static Logger log = LoggerFactory.getLogger(SprogThrottleManager.class.getName());

}
