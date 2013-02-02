package jmri.jmrix.sprog;

import org.apache.log4j.Logger;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.DccThrottle;


import jmri.jmrix.AbstractThrottleManager;

/**
 * SPROG implementation of a ThrottleManager.
 * <P> Updated by Andrew Crosland February 2012 to enable 28 step
 * speed packets</P>
 * @author	    Bob Jacobsen  Copyright (C) 2001
 * @version         $Revision$
 */
public class SprogThrottleManager extends AbstractThrottleManager {

    /**
     * Constructor.
     */
    public SprogThrottleManager(SprogSystemConnectionMemo memo) {
        super(memo);
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
            notifyThrottleKnown(new SprogThrottle((SprogSystemConnectionMemo)adapterMemo, address), address);
        } else {
            failedThrottleRequest((DccLocoAddress)address, "Only one Throttle can be in use at anyone time with the Sprog.");
            //javax.swing.JOptionPane.showMessageDialog(null,"Only one Throttle can be in use at anyone time with the Sprog.","Sprog Throttle",javax.swing.JOptionPane.WARNING_MESSAGE);
            log.warn("Single SPROG Throttle already in use");
        }
    }

    /**
     * What speed modes are supported by this system?                       
     * value should be or of possible modes specified by the
     * DccThrottle interface
     */
    public int supportedSpeedModes() {
        return(DccThrottle.SpeedStepMode128 | DccThrottle.SpeedStepMode28);
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

    static Logger log = Logger.getLogger(SprogThrottleManager.class.getName());

}
