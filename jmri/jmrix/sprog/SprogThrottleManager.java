package jmri.jmrix.sprog;

import jmri.LocoAddress;
import jmri.DccLocoAddress;


import jmri.jmrix.AbstractThrottleManager;

/**
 * SPROG implementation of a ThrottleManager.
 * <P>
 * @author	    Bob Jacobsen  Copyright (C) 2001
 * @version         $Revision: 1.3 $
 */
public class SprogThrottleManager extends AbstractThrottleManager {

    /**
     * Constructor.
     */
    public SprogThrottleManager() {
        super();
        if (mInstance!=null) log.warn("Creating too many objects");
        mInstance = this;
    }

    static private SprogThrottleManager mInstance = null;
    static public SprogThrottleManager instance() {
        return mInstance;
    }

    boolean throttleInUse = false;
    void release() {
        throttleInUse = false;
    }

    public void requestThrottleSetup(LocoAddress address) {
        // The SPROG protocol doesn't require an interaction with the command
        // station for this, so set the address and immediately trigger the callback
        // if a throttle is not in use.
        if (!throttleInUse) {
            throttleInUse = true;
            log.debug("new SprogThrottle for "+address);
            String addr = ""+((DccLocoAddress)address).getNumber();
            SprogMessage m = new SprogMessage(2+addr.length());
            int i = 0;
            m.setElement(i++,'A');
            m.setElement(i++, ' ');
            for (int j=0; j<addr.length(); j++)
                m.setElement(i++, addr.charAt(j));
            SprogTrafficController.instance().sendSprogMessage(m, null);
            notifyThrottleKnown(new SprogThrottle(address), address);
        } else {
            log.warn("Single SPROG Throttle already in use");
        }
    }

    /**
     * Address 100 and above is a long address
     **/
    public boolean canBeLongAddress(int address) {
        return isLongAddress(address);
    }
    
    /**
     * Address 99 and below is a short address
     **/
    public boolean canBeShortAddress(int address) {
        return !isLongAddress(address);
    }

    /**
     * Are there any ambiguous addresses (short vs long) on this system?
     */
    public boolean addressTypeUnique() { return true; }

    /*
     * Local method for deciding short/long address
     */
    static boolean isLongAddress(int num) {
        return (num>=100);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SprogThrottleManager.class.getName());

}