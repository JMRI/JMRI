package jmri.jmrix;

import jmri.DccThrottle;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import com.sun.java.util.collections.HashMap;

/**
 * Abstract implementation of a ThrottleManager.
 *
 * <P>
 * Based on Glen Oberhauser's original LnThrottleManager implementation
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version         $Revision: 1.7 $
 */
abstract public class AbstractThrottleManager implements ThrottleManager {
    private HashMap throttleListeners;
    
    /**
     * Request a throttle, given a decoder address. When the decoder address
     * is located, the ThrottleListener gets a callback via the ThrottleListener.notifyThrottleFound
     * method.
     * @param address The decoder address desired.
     * @param l The ThrottleListener awaiting notification of a found throttle.
     * @return True if the request will continue, false if the request will not
     * be made. False may be returned if a the throttle is already in use.
     */
    public boolean requestThrottle(int address, ThrottleListener l) {
        boolean throttleInUse = false;
        if (throttleListeners == null) {
            throttleListeners = new HashMap(5);
        }
        
        Integer addressKey = new Integer(address);
        if (throttleListeners.containsKey(addressKey)) {
            throttleInUse = true;
        } else {
            throttleListeners.put(addressKey, l);
            requestThrottleSetup(address);
        }
        return throttleInUse;
    }
    
    /**
     * Abstract member to actually do the work of configuring a new throttle,
     * perhaps via interaction with the DCC system
     */
    abstract public void requestThrottleSetup(int address);
    
    /**
     * Cancel a request for a throttle
     * @param address The decoder address desired.
     * @param l The ThrottleListener cancelling request for a throttle.
     */
    public void cancelThrottleRequest(int address, ThrottleListener l) {
        if (throttleListeners != null) {
            Integer addressKey = new Integer(address);
            throttleListeners.remove(addressKey);
        }
    }
    
    /**
     * Handle throttle information when it's finally available, e.g. when
     * a new Throttle object has been created.
     * <P>
     * This method creates a throttle for all ThrottleListeners of that address
     * and notifies them via the ThrottleListener.notifyThrottleFound method.
     */
    public void notifyThrottleKnown(DccThrottle throttle, int addr) {
        log.debug("notifyThrottleKnown for "+addr);
        Integer address = new Integer(addr);
        ThrottleListener l = (ThrottleListener)throttleListeners.get(address);
        if (l != null) {
            log.debug("Notify listener");
            l.notifyThrottleFound(throttle);
        }
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractThrottleManager.class.getName());
}
