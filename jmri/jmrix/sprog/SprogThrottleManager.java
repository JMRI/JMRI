package jmri.jmrix.sprog;

import jmri.jmrix.AbstractThrottleManager;

/**
 * SPROG implementation of a ThrottleManager.
 * <P>
 * @author	    Bob Jacobsen  Copyright (C) 2001
 * @version         $Revision: 1.1 $
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

    public void requestThrottleSetup(int address) {
        // The SPROG protocol doesn't require an interaction with the command
        // station for this, so set the address and immediately trigger the callback
        // if a throttle is not in use.
        if (!throttleInUse) {
            throttleInUse = true;
            log.debug("new SprogThrottle for "+address);
            String addr = ""+address;
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SprogThrottleManager.class.getName());

}