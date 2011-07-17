package jmri.jmrix.sprog;

import jmri.LocoAddress;
import jmri.DccLocoAddress;


import jmri.jmrix.AbstractThrottleManager;

/**
 * SPROG implementation of a ThrottleManager.
 * <P>
 * @author	    Bob Jacobsen  Copyright (C) 2001
 * @version         $Revision: 1.8 $
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

    public void requestThrottleSetup(LocoAddress address, boolean control) {
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
            failedThrottleRequest((DccLocoAddress)address, "Only one Throttle can be in use at anyone time with the Sprog.");
            //javax.swing.JOptionPane.showMessageDialog(null,"Only one Throttle can be in use at anyone time with the Sprog.","Sprog Throttle",javax.swing.JOptionPane.WARNING_MESSAGE);
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
     * Address 127 and below is a short address
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
        return (num>=127);
    }

    public boolean disposeThrottle(jmri.DccThrottle t, jmri.ThrottleListener l){
        if(super.disposeThrottle(t, l)){
            throttleInUse = false;
            return true;
        }
        return false;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SprogThrottleManager.class.getName());

}
